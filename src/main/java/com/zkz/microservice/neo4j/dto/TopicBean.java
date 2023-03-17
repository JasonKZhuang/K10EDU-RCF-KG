/**
 * Project: SpringBootNeo4j
 * Date: 22/11/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@NoArgsConstructor
@Jacksonized
public class TopicBean {
    @Id
    private Long id;

    @JsonProperty("nodeId")
    private String nodeId;

    @Property("title")
    private String title;

    @Property("description")
    private String description;

    @Property("unitId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long unitId;

    @Property("parentId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long parentId;

    public TopicBean(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public TopicBean(Long id, String nodeId, String title, String description) {
        this.id = id;
        this.nodeId = nodeId;
        this.title = title;
        this.description = description;
    }

}
