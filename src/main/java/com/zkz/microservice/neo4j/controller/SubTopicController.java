/**
 * Project: SpringBootNeo4j
 * Date: 29/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.controller;

import com.zkz.microservice.neo4j.dto.ElementBean;
import com.zkz.microservice.neo4j.dto.HierarchyRelation;
import com.zkz.microservice.neo4j.dto.TopicBean;
import com.zkz.microservice.neo4j.exception.ServiceException;
import com.zkz.microservice.neo4j.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/subTopics")
@RequiredArgsConstructor
public class SubTopicController {
    private final TopicService topicService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TopicBean> getAllTopics() {
        return topicService.getAllTopicNodes(true);
    }

    @PostMapping()
    public TopicBean createSubTopic(@RequestBody TopicBean requestObject) {
        return topicService.createSimpleTopic(requestObject, true);
    }

    @PutMapping("/id/{topicId}")
    public TopicBean updateSubTopic(@PathVariable String topicId, @RequestBody TopicBean requestObject) {
        try {
            requestObject.setId(Long.parseLong(topicId));
            return topicService.updateSimpleTopic(requestObject, true);
        } catch (Exception exp) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, exp.getMessage());
        }
    }

    @PostMapping("/p")
    public TopicBean createSubTopicWithParent(@RequestBody TopicBean requestObject) {
        return topicService.createSubTopicWithParentTopicRelation(requestObject);
    }

    @PostMapping("/r")
    public String createRelation(@RequestBody HierarchyRelation argBean) {
        topicService.createRelationBtSubtopicAndTopicByNodeIds(argBean.getChildId(), argBean.getParentId());
        return "OK";
    }

    @PutMapping("/r")
    public String updateRelation(@RequestBody HierarchyRelation argBean) {
        topicService.updateRelationBtSubtopicAndParentTopicByNodeIds(
                argBean.getChildId(),
                argBean.getOldParentId(),
                argBean.getParentId());
        return "OK";
    }

    @DeleteMapping("/id/{id}")
    public String deleteSubTopicById(@PathVariable Long id) {
        topicService.deleteTopicAndRelationByNodeId(id, true);
        return "OK";
    }

    @DeleteMapping("/title/{title}")
    public String deleteSubTopicByTitle(@PathVariable String title) {
        topicService.deleteNodeByTitle(title);
        return "OK";
    }

    @DeleteMapping("/r")
    public String deleteRelationshipBtSubtopicAndTopic(
            @RequestParam("subId") String argSubTopicId,
            @RequestParam("parId") String argParentId) {
        try {
            long subTopicId = Long.parseLong(argSubTopicId);
            long parTopicId = Long.parseLong(argParentId);
            topicService.deleteRelationBtTopicAndUnitByNodeIds(subTopicId, parTopicId);
        } catch (Exception exp) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, exp.getMessage());
        }
        return "OK";
    }
    @PostMapping("/extract")
    public List<ElementBean> extractEntities(@RequestParam("subId") String argSubTopicId,
                                             @RequestParam("subName") String argSubTopicName){
        TopicBean topicBean = new TopicBean();
        if (StringUtils.isEmpty(argSubTopicId)){
            if (StringUtils.isEmpty(argSubTopicName)){
                throw new ServiceException(HttpStatus.BAD_REQUEST,"either id or name should has value.");
            }else{
                topicBean =  topicService.getSingleSubTopicByTitle(argSubTopicName);
            }
        }else{
            topicBean =  topicService.getSingleSubTopicById(Long.valueOf(argSubTopicId));
        }

        List<ElementBean> retList = new ArrayList<>();

        retList = topicService.extractElementsFromProperty( topicBean.getId(),"description");

        return retList;
    }

}
