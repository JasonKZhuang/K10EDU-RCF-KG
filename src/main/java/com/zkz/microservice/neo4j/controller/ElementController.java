/**
 * Project: EducationMicroservice
 * Date: 9/3/2023
 * Author: Jason
 */
package com.zkz.microservice.neo4j.controller;

import com.zkz.microservice.neo4j.service.ElementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/elements")
@RequiredArgsConstructor
public class ElementController {
    private final ElementService elementService;

    @PostMapping("/filter")
    public String filterByDictionary() {
        int num = elementService.filterByDictionary();
        return "successfully filtered " + num + " Elements";
    }
}
