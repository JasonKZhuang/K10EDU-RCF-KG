/**
 * Project: SpringBootNeo4j
 * Date: 26/11/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.dto;

import lombok.Data;

@Data
public class HierarchyRelation {
    private Long parentId;
    private String parentName;
    private Long childId;
    private String childName;
    private Long oldParentId;
    private String oldParentName;
}
