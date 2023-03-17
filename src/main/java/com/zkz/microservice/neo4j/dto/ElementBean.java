/**
 * Project: MicroserviceNeo4j
 * Date: 7/3/2023
 * Author: Jason
 */
package com.zkz.microservice.neo4j.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class ElementBean {
    private Long id;
    @JsonProperty("nodeId")
    private String nodeId;
    private String name;
    private String type;
}
