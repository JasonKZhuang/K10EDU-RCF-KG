/**
 * Project: SpringBootNeo4j
 * Date: 23/10/2022
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
public class ChapterBean {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("nodeId")
    private String nodeId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("sequence")
    private String sequence;

    @JsonProperty("focusArea")
    private String focusArea;
}
