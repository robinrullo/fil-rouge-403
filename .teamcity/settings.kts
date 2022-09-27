import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.buildFeatures.sshAgent
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.sshExec
import jetbrains.buildServer.configs.kotlin.projectFeatures.dockerRegistry
import jetbrains.buildServer.configs.kotlin.triggers.finishBuildTrigger
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2022.04"

project {

    vcsRoot(HttpsGithubComRobinrulloFilRouge403refsHeadsMain)

    buildType(Test)
    buildType(Publish)
    buildType(Deploy)
    buildType(Build_2)

    params {
        text("DEV_SERVER_SSH_PORT", "2222", display = ParameterDisplay.HIDDEN, allowEmpty = false)
        text("DOCKER_REGISTRY_URL", "rullo.zapto.org:5000", display = ParameterDisplay.HIDDEN, allowEmpty = false)
        text("DEV_SERVER_SSH_HOST", "rullo.zapto.org", display = ParameterDisplay.HIDDEN, allowEmpty = false)
        text("DEV_SERVER_SSH_USER", "rrullo", display = ParameterDisplay.HIDDEN, allowEmpty = false)
        text("DOCKER_REGISTRY_USER", "registry-user", display = ParameterDisplay.HIDDEN, allowEmpty = false)
        text("DOCKER_COMPOSE_DIR", "/home/rrullo/fil-rouge-403", display = ParameterDisplay.HIDDEN, allowEmpty = false)
        password("DOCKER_REGISTRY_PASSWORD", "zxxf18e61201172dc46ef7f4fb7a41c7cad", display = ParameterDisplay.HIDDEN)
        text("DOCKER_SERVICE_NAME", "spring-api", allowEmpty = false)
        text("DOCKER_REGISTRY_PROTOCOL", "http", display = ParameterDisplay.HIDDEN, allowEmpty = false)
    }

    features {
        dockerRegistry {
            id = "PROJECT_EXT_3"
            name = "Docker Registry"
            url = "http://rullo.zapto.org:5000"
            userName = "registry-user"
            password = "zxxf18e61201172dc46ef7f4fb7a41c7cad"
        }
    }
    buildTypesOrder = arrayListOf(Test, Build_2, Publish, Deploy)
}

object Build_2 : BuildType({
    id("Build")
    name = "Build"

    artifactRules = "+:build/libs/* => target"
    publishArtifacts = PublishMode.SUCCESSFUL

    vcs {
        root(HttpsGithubComRobinrulloFilRouge403refsHeadsMain)
    }

    steps {
        gradle {
            name = "build jar"
            tasks = "clean build -x test -x checkstyleMain -x checkstyleTest"
            buildFile = "build.gradle"
        }
    }

    triggers {
        finishBuildTrigger {
            buildType = "${Test.id}"
            branchFilter = "+:*"
        }
    }

    dependencies {
        snapshot(Test) {
        }
    }
})

object Deploy : BuildType({
    name = "Deploy"

    enablePersonalBuilds = false
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1

    vcs {
        root(HttpsGithubComRobinrulloFilRouge403refsHeadsMain)

        showDependenciesChanges = true
    }

    steps {
        sshExec {
            name = "Deploy"
            commands = """
                cd %DOCKER_COMPOSE_DIR%
                docker login -u "%DOCKER_REGISTRY_USER%" -p "%DOCKER_REGISTRY_PASSWORD%" "%DOCKER_REGISTRY_PROTOCOL%://%DOCKER_REGISTRY_URL%"
                docker compose pull %DOCKER_SERVICE_NAME%
                docker compose up -d %DOCKER_SERVICE_NAME%
            """.trimIndent()
            targetUrl = "%DEV_SERVER_SSH_HOST%"
            authMethod = sshAgent {
                username = "%DEV_SERVER_SSH_USER%"
            }
            param("jetbrains.buildServer.sshexec.port", "%DEV_SERVER_SSH_PORT%")
        }
    }

    features {
        sshAgent {
            teamcitySshKey = "DEV server"
        }
    }

    dependencies {
        snapshot(Publish) {
            onDependencyFailure = FailureAction.CANCEL
        }
    }
})

object Publish : BuildType({
    name = "Publish"

    vcs {
        root(HttpsGithubComRobinrulloFilRouge403refsHeadsMain)
    }

    steps {
        dockerCommand {
            name = "build image"
            commandType = build {
                source = file {
                    path = "Dockerfile"
                }
                namesAndTags = """
                    %DOCKER_REGISTRY_URL%/transport-routing-server:latest
                    %DOCKER_REGISTRY_URL%/transport-routing-server:dev-%build.number%
                """.trimIndent()
                commandArgs = "--pull"
            }
        }
        dockerCommand {
            name = "push image"
            commandType = push {
                namesAndTags = """
                    %DOCKER_REGISTRY_URL%/transport-routing-server:latest
                    %DOCKER_REGISTRY_URL%/transport-routing-server:dev-%build.number%
                """.trimIndent()
            }
        }
    }

    features {
        dockerSupport {
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_3"
            }
        }
    }

    dependencies {
        dependency(Build_2) {
            snapshot {
                onDependencyFailure = FailureAction.CANCEL
            }

            artifacts {
                artifactRules = "target"
            }
        }
    }
})

object Test : BuildType({
    name = "Test"

    vcs {
        root(HttpsGithubComRobinrulloFilRouge403refsHeadsMain)
    }

    triggers {
        vcs {
        }
    }
})

object HttpsGithubComRobinrulloFilRouge403refsHeadsMain : GitVcsRoot({
    name = "https://github.com/robinrullo/fil-rouge-403#refs/heads/main"
    url = "https://github.com/robinrullo/fil-rouge-403"
    branch = "refs/heads/main"
    branchSpec = "refs/heads/*"
    authMethod = password {
        userName = "robinrullo"
        password = "zxx434556ee5d482dfdf98e4e2d45ebf59a181456892b6b82a151c4691ea26d154018024f73aadc4e38775d03cbe80d301b"
    }
    param("oauthProviderId", "PROJECT_EXT_2")
})
