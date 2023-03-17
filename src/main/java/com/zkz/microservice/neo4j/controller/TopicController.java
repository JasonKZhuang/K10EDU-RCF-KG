/**
 * Project: SpringBootNeo4j
 * Date: 29/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.controller;

import com.zkz.microservice.neo4j.dto.HierarchyRelation;
import com.zkz.microservice.neo4j.dto.TopicBean;
import com.zkz.microservice.neo4j.exception.ServiceException;
import com.zkz.microservice.neo4j.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {
    private final TopicService topicService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TopicBean> getAllTopics() {
        return topicService.getAllTopicNodes(false);
    }

    @PostMapping()
    public TopicBean createTopic(@RequestBody TopicBean requestObject) {
        return topicService.createSimpleTopic(requestObject, false);
    }

    @PutMapping("/id/{topicId}")
    public TopicBean updateTopic(@PathVariable String topicId, @RequestBody TopicBean requestObject) {
        try {
            requestObject.setId(Long.parseLong(topicId));
            return topicService.updateSimpleTopic(requestObject, false);
        } catch (Exception exp) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, exp.getMessage());
        }
    }

    @PostMapping("/u")
    public TopicBean createTopicWithUnit(@RequestBody TopicBean requestObject) {
        return topicService.createTopicWithUnitRelation(requestObject);
    }

    @PostMapping("/r")
    public String createRelation(@RequestBody HierarchyRelation argBean) {
        topicService.createRelationBtTopicAndUnitByNodeIds(argBean.getChildId(), argBean.getParentId());
        return "OK";
    }

    @PutMapping("/r")
    public String updateRelation(@RequestBody HierarchyRelation argBean) {
        topicService.updateRelationBtTopicAndUnitByNodeIds(
                argBean.getChildId(),
                argBean.getOldParentId(),
                argBean.getParentId());
        return "OK";
    }

    @DeleteMapping("/id/{id}")
    public String deleteTopicById(@PathVariable Long id) {
        topicService.deleteTopicAndRelationByNodeId(id, false);
        return "OK";
    }

    @DeleteMapping("/title/{title}")
    public String deleteTopicByTitle(@PathVariable String title) {
        topicService.deleteNodeByTitle(title);
        return "OK";
    }

    @DeleteMapping("/r")
    public String deleteRelationshipBtTopicAndUnit(@RequestParam("topicId") String argTopicId,
                                                   @RequestParam("unitId") String argUnitId) {
        try {
            long topicId = Long.parseLong(argTopicId);
            long unitId = Long.parseLong(argUnitId);
            topicService.deleteRelationBtTopicAndUnitByNodeIds(topicId, unitId);
        } catch (Exception exp) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, exp.getMessage());
        }
        return "OK";
    }
}
