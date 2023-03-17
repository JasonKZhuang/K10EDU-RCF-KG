package com.zkz.microservice.neo4j.configuration;

public enum NodeLabel {
    CHAPTER("Chapter"),
    OUTCOME("Outcome"),
    UNIT("Unit"),
    TOPIC("Topic"),
    SUBTOPIC("SubTopic"),
    RESOURCE("Resource");

    private String nodeType;

    NodeLabel(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeType() {
        return nodeType;
    }
}
