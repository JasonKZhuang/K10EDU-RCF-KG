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

@Node("Unit")
@Data
@NoArgsConstructor
@Jacksonized
public class UnitNode {

    @Id
    @GeneratedValue
    private Long id;

    @Property("nodeId")
    private String nodeId;

    @Property("title")
    private String title;

    @Property("context")
    private String context;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.INCOMING)
    private Set<TopicNode> topics = new HashSet<>();

    public UnitNode(Long id, String title, String context) {
        this.id = id;
        this.title = title;
        this.context = context;
    }

    public UnitNode(String title, String context) {
        this(null, title, context);
    }

    public UnitNode withId(Long id) {
        if (this.id.equals(id)) {
            return this;
        } else {
            return new UnitNode(id, this.title, this.context);
        }
    }

}
