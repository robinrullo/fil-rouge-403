FROM openjdk:17-jdk

ADD transport-routing-*.jar spring-boot-application.jar

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-Djava.security.egd=file:/dev/./urandom","-jar","./spring-boot-application.jar"]
