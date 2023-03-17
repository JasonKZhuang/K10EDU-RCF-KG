package com.zkz.microservice.neo4j.entity.education;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.neo4j.core.schema.*;

import java.util.HashSet;
import java.util.Set;

//Immutable MovieEntity with internal Neo4j id
@Node("Chapter")
@Data
@NoArgsConstructor
@Jacksonized
public class ChapterNode {
    @Id
    @GeneratedValue
    private Long id;

    @Property("nodeId")
    private String nodeId;

    @Property("title")
    private String title;

    @Property("sequence")
    private String sequence;
    @Property("focusArea")
    private String focusArea;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.INCOMING)
    private Set<UnitNode> units = new HashSet<>();

    public ChapterNode(Long id, String title, String sequence, String focusArea) {
        this.id = id;
        this.title = title;
        this.sequence = sequence;
        this.focusArea = focusArea;
    }

    public ChapterNode withId(Long id) {
        if (this.id.equals(id)) {
            return this;
        } else {
            return new ChapterNode(id, this.title,this.sequence, this.focusArea);
        }
    }

}