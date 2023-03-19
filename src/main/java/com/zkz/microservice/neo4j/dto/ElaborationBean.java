/**
 * Project: MicroserviceNeo4j
 * Date: 7/3/2023
 * Author: Jason
 */
package com.zkz.microservice.neo4j.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElaborationBean {
    private String nodeId;
    private String title;
    private String description;
    private String notation;
    private String subSubject;
    private String yearLevel;
}
