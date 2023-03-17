/**
 * Project: SpringBootNeo4j
 * Date: 30/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.entity.education;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.neo4j.core.schema.*;

import java.util.HashSet;
import java.util.Set;

@Node("Topic")
@Data
@NoArgsConstructor
@Jacksonized
public class TopicNode {

    @Id
    @GeneratedValue
    private Long id;

    @Property("nodeId")
    private String nodeId;

    @Property("title")
    private String title;

    @Property("description")
    private String description;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.INCOMING)
    private Set<SubTopicNode> subTopics = new HashSet<>();

    public TopicNode(Long id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public TopicNode(String title, String description) {
        this(null, title, description);
    }

    public TopicNode withId(Long id) {
        if (this.id.equals(id)) {
            return this;
        } else {
            return new TopicNode(id, this.title, this.description);
        }
    }

}
