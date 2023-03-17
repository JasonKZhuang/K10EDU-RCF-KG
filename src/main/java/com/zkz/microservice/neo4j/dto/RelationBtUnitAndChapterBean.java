/**
 * Project: SpringBootNeo4j
 * Date: 7/11/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.dto;

import lombok.Data;

@Data
public class RelationBtUnitAndChapterBean {
    private Long unitId;
    private String unitName;
    private Long chapterId;
    private String chapterName;
    private Long oldChapterId;
    private String oldChapterName;

}
