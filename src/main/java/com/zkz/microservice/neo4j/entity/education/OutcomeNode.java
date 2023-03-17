/**
 * Project: SpringBootNeo4j
 * Date: 23/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.entity.education;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.neo4j.core.schema.*;

//Immutable MovieEntity with internal Neo4j id
@Node("Outcome")
@Data
@NoArgsConstructor
@Jacksonized
public class OutcomeNode {
    @Id
    @GeneratedValue
    private Long id;

    @Property("nodeId")
    private String nodeId;

    @Property("title")
    private String title;
    @Property("level")
    private String level;
    @Property("description")
    private String description;

    @Relationship(type="KEY_OUTCOME", direction = Relationship.Direction.INCOMING)
    private ChapterNode chapter;

    public OutcomeNode(Long id, String title, String level, String description) {
        this.id = id;
        this.title = title;
        this.level = level;
        this.description = description;
    }

    public OutcomeNode(String title, String level, String description) {
        this(null, title, level, description);
    }

    public OutcomeNode withId(Long id) {
        if (this.id.equals(id)) {
            return this;
        } else {
            return new OutcomeNode(id, this.title, this.level, this.description);
        }
    }
}
