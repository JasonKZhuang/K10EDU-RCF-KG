/**
 * Project: SpringBootNeo4j
 * Date: 29/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.controller;

import com.zkz.microservice.neo4j.dto.RelationBtUnitAndChapterBean;
import com.zkz.microservice.neo4j.dto.UnitBean;
import com.zkz.microservice.neo4j.exception.ServiceException;
import com.zkz.microservice.neo4j.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/units")
@RequiredArgsConstructor
public class UnitController {
    private final UnitService unitService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UnitBean> getAllUnits() {
        return unitService.getAllUnitNodes();
    }

    @GetMapping(value = "/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UnitBean getUnitById(@PathVariable String id) {
        UnitBean retValue = null;
        try {
            Long lId = Long.valueOf(id);
            retValue = unitService.getSingleUnitByNodeId(lId);
            if (retValue == null) {
                throw new ServiceException(HttpStatus.NOT_FOUND, "No node found.");
            }
        } catch (Exception exp) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, exp.getMessage());
        }
        return retValue;
    }

    @GetMapping(value = "/title/{title}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UnitBean getUnitByTitle(@PathVariable String title) {
        UnitBean retValue = unitService.getSingleUnitByTitle(title);
        if (retValue == null) {
            throw new ServiceException(HttpStatus.NOT_FOUND, "No node found.");
        }
        return retValue;
    }

    @PostMapping()
    public UnitBean createUnit(@RequestBody UnitBean requestObject) {
        return unitService.createSimpleUnit(requestObject);
    }

    @PutMapping()
    public UnitBean updateUnit(@RequestBody UnitBean requestObject) {
        return unitService.updateSimpleUnit(requestObject);
    }

    @PostMapping("/c")
    public UnitBean createUnitWithChapter(@RequestBody UnitBean requestObject) {
        return unitService.createUnitWithChapter(requestObject);
    }

    @PostMapping("/r")
    public String createRelation(@RequestBody RelationBtUnitAndChapterBean argBean) {
        unitService.createRelationshipByNodeId(argBean.getUnitId(), argBean.getChapterId());
        return "OK";
    }

    @PutMapping("/r")
    public String updateRelation(@RequestBody RelationBtUnitAndChapterBean argBean) {
        unitService.updateRelationByNodeId(argBean.getUnitId(), argBean.getOldChapterId(), argBean.getChapterId());
        return "OK";
    }

    @DeleteMapping("/id/{unitId}")
    public String deleteUnitById(@PathVariable Long unitId) {
        unitService.deleteUnitWithRelationById(unitId);
        return "OK";
    }

    @DeleteMapping("/title/{unitTitle}")
    public String deleteUnitByTitle(@PathVariable String unitTitle) {
        unitService.deleteUnitWithRelationByTitle(unitTitle);
        return "OK";
    }

    @DeleteMapping("/r")
    public String deleteRelationshipBtUnitAndChapter(@RequestParam("unitId") String argUnitId,
                                                     @RequestParam("chapterId") String argChapterId) {
        unitService.deleteRelationshipByNodeId(Long.parseLong(argUnitId), Long.parseLong(argChapterId));
        return "OK";
    }

}
