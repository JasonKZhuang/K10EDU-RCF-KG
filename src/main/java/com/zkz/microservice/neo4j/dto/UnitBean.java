/**
 * Project: SpringBootNeo4j
 * Date: 1/11/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@NoArgsConstructor
@Jacksonized
public class UnitBean {

    @Id
    private Long id;

    @JsonProperty("nodeId")
    private String nodeId;

    @Property("title")
    private String title;

    @Property("context")
    private String context;

    @Property("chapterId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long chapterId;

    public UnitBean(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public UnitBean(Long id, String title, String context) {
        this.id = id;
        this.title = title;
        this.context = context;
    }
}
