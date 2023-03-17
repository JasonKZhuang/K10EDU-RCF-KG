/**
 * Project: SpringBootNeo4j
 * Date: 27/11/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@NoArgsConstructor
@Jacksonized
public class ResourceBean {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("resourceType")
    private String resourceType;

    @JsonProperty("description")
    private String description;

    @JsonProperty("sourceURI")
    private String sourceURI;

    public ResourceBean(Long id, String title, String resourceType, String description, String sourceURI) {
        this.id = id;
        this.title = title;
        this.resourceType = resourceType;
        this.description = description;
        this.sourceURI = sourceURI;
    }

    public ResourceBean(String title, String resourceType, String description, String sourceURI) {
        this.title = title;
        this.resourceType = resourceType;
        this.description = description;
        this.sourceURI = sourceURI;
    }
}
