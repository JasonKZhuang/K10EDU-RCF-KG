package com.zkz.microservice.neo4j.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Entity")
@Data
@NoArgsConstructor
@Jacksonized
public class Entity {
    @Id
    @GeneratedValue
    private Long id;

    @Property("title")
    private String title;

    public Entity(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Entity(String title) {
        this(null, title);
    }

    public Entity withId(Long id) {
        if (this.id.equals(id)) {
            return this;
        } else {
            return new Entity(id, this.title);
        }
    }
}