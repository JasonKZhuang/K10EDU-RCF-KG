/**
 * Project: MicroserviceNeo4j
 * Date: 7/3/2023
 * Author: Jason
 */
package com.zkz.microservice.neo4j.controller;

import com.zkz.microservice.neo4j.dto.*;
import com.zkz.microservice.neo4j.service.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/query")
@AllArgsConstructor
public class    QueryController {
    private final ChapterService chapterService;
    private final OutcomeService outcomeService;
    private final UnitService unitService;
    private final TopicService topicService;
    private final ElementService elementService;

    private final ElaborationService elaborationService;


    @GetMapping(value = "/chapter")
    @Operation(summary = "Get chapters and related nodes by its id or title parameter")
    public List<ChapterBean> getChaptersByIdOrTitle(
            @RequestParam("id") String argId,
            @RequestParam("title") String argTitle) {
        List<ChapterBean> beans = new ArrayList<>();
        if (!StringUtils.isEmpty(argId)){
            ChapterBean chapterBean = chapterService.getChapterById(argId);
            beans.add(chapterBean);
        }
        if (!StringUtils.isEmpty(argTitle)){
            List<ChapterBean>  chapterBeans = chapterService.getChapterByTitle(argTitle);
            beans.addAll(chapterBeans);
        }
        return beans;
    }

    @GetMapping(value = "/outcome")
    @Operation(summary = "Get learning outcomes and related nodes by its id or title parameter")
    public List<OutcomeBean> getOutcomesByIdOrTitle(
            @RequestParam("id") String argId,
            @RequestParam("title") String argTitle) {
        List<OutcomeBean> beans = new ArrayList<>();
        if (!StringUtils.isEmpty(argId)){
            OutcomeBean tempBean = outcomeService.getOutcomeById(argId);
            beans.add(tempBean);
        }
        if (!StringUtils.isEmpty(argTitle)){
            List<OutcomeBean>  tempBeans = outcomeService.getOutcomesByTitle(argTitle);
            beans.addAll(tempBeans);
        }
        return beans;
    }

    @GetMapping(value = "/unit")
    @Operation(summary = "Get units and related nodes by its id or title parameter")
    public List<UnitBean> getUnitsByIdOrTitle(
            @RequestParam("id") String argId,
            @RequestParam("title") String argTitle) {
        List<UnitBean> beans = new ArrayList<>();
        if (!StringUtils.isEmpty(argId)){
            UnitBean tempBean = unitService.getSingleUnitByNodeId(Long.valueOf(argId));
            beans.add(tempBean);
        }
        if (!StringUtils.isEmpty(argTitle)){
            UnitBean  tempBean = unitService.getSingleUnitByTitle(argTitle);
            beans.add(tempBean);
        }
        return beans;
    }

    @GetMapping(value = "/topic")
    @Operation(summary = "Get topics and related nodes by its id or title parameter")
    public List<TopicBean> getTopicsByIdOrTitle(
            @RequestParam("id") String argId,
            @RequestParam("title") String argTitle) {
        List<TopicBean> beans = new ArrayList<>();
        if (!StringUtils.isEmpty(argId)){
            TopicBean tempBean = topicService.getSingleTopicById(Long.valueOf(argId));
            beans.add(tempBean);
        }
        if (!StringUtils.isEmpty(argTitle)){
            TopicBean  tempBean = topicService.getSingleTopicByTitle(argTitle);
            beans.add(tempBean);
            tempBean = topicService.getSingleSubTopicByTitle(argTitle);
            beans.add(tempBean);
        }

        return beans;
    }

    @GetMapping(value = "/entity")
    @Operation(summary = "Get entity and related nodes by its name parameter")
    public List<ElementBean> getEntityByName(@RequestParam("name") String argName) {
        List<ElementBean> beans = new ArrayList<>();
        if (!StringUtils.isEmpty(argName)){
            beans = elementService.getElementNodesByName(argName);
        }
        return beans;
    }

    @GetMapping(value = "/elaboration")
    @Operation(summary = "Get elaboration and related nodes by its name parameter")
    public List<ElaborationBean> getElaborationByName(@RequestParam("name") String argName) {
        List<ElaborationBean> beans = new ArrayList<>();
        if (!StringUtils.isEmpty(argName)){
            beans = elaborationService.getElaborationNodesByName(argName);
        }
        return beans;
    }


}
