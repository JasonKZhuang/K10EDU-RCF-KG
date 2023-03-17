/**
 * Project: SpringBootNeo4j
 * Date: 22/11/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.entity.education;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.neo4j.core.schema.*;

import java.util.Set;

@Node("Resource")
@Data
@NoArgsConstructor
@Jacksonized
public class ResourceNode {

    @Id
    @GeneratedValue
    private Long id;

    @Property("nodeId")
    private String nodeId;

    @Property("title")
    private String title;

    /**
     * this type could be formula or figure
     */
    @Property("resourceType")
    private String resourceType;

    @Property("description")
    private String description;

    @Property("sourceURI")
    private String sourceURI;
    @Relationship(type = "APPLIED_TO", direction = Relationship.Direction.OUTGOING)
    private Set<SubTopicNode> subtopics;
    @Relationship(type = "APPLIED_TO", direction = Relationship.Direction.OUTGOING)
    private Set<TopicNode> topics;
    @Relationship(type = "APPLIED_TO", direction = Relationship.Direction.OUTGOING)
    private Set<UnitNode> units;

    public ResourceNode(Long id, String title, String resourceType, String description, String sourceURI) {
        this.id = id;
        this.title = title;
        this.resourceType = resourceType;
        this.description = description;
        this.sourceURI = sourceURI;
    }

    public ResourceNode(String title, String resourceType, String description, String sourceURI) {
        this(null, title, resourceType, description, sourceURI);
    }

    public ResourceNode withId(Long id) {
        if (this.id.equals(id)) {
            return this;
        } else {
            return new ResourceNode(id, this.title, this.resourceType, this.description, this.sourceURI);
        }
    }
}
