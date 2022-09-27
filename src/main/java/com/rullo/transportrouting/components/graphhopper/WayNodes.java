package com.rullo.transportrouting.components.graphhopper;

/**
 * Class to store way nodes.

 * @param fromNode start node.
 * @param toNode end node.
 */
public record WayNodes(int fromNode, int toNode) {}
