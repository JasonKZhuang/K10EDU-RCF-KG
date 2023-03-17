/**
 * Project: SpringBootNeo4j
 * Date: 30/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.entity.education;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("SubTopic")
@Data
@NoArgsConstructor
@Jacksonized
public class SubTopicNode {

    @Id
    @GeneratedValue
    private Long id;

    @Property("nodeId")
    private String nodeId;

    @Property("title")
    private String title;

    @Property("description")
    private String description;

    public SubTopicNode(Long id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public SubTopicNode(String title, String description) {
        this(null, title, description);
    }

    public SubTopicNode withId(Long id) {
        if (this.id.equals(id)) {
            return this;
        } else {
            return new SubTopicNode(id, this.title, this.description);
        }
    }

}
