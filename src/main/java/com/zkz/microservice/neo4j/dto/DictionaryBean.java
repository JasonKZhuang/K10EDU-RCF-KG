/**
 * Project: EducationMicroservice
 * Date: 9/3/2023
 * Author: Jason
 */
package com.zkz.microservice.neo4j.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DictionaryBean {
    private Integer id;

    private String word;

    private String description;

    private String subject;
}
