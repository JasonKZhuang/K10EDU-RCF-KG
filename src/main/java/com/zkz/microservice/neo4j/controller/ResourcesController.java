/**
 * Project: SpringBootNeo4j
 * Date: 29/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.controller;

import com.zkz.microservice.neo4j.configuration.NodeLabel;
import com.zkz.microservice.neo4j.dto.ResourceBean;
import com.zkz.microservice.neo4j.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourcesController {
    private final ResourceService resourceService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ResourceBean> getAllTopics() {
        return resourceService.getAll();
    }

    @PostMapping()
    public ResourceBean create(@RequestBody ResourceBean requestObject) {
        return resourceService.createSimpleResource(requestObject);
    }

    @PutMapping("/{id}")
    public ResourceBean update(@RequestBody ResourceBean requestObject, @PathVariable Long id) {
        requestObject.setId(id);
        return resourceService.updateSimpleResource(requestObject);
    }

    @PostMapping("/r/{resourceId}/subTopic/{subTopicId}")
    public String createRelationWithSubTopic(@PathVariable Long resourceId, @PathVariable Long subTopicId) {
        resourceService.createRelationBtResourceAndOtherNodeByIds(resourceId,subTopicId,NodeLabel.SUBTOPIC);
        return "OK";
    }

    @PostMapping("/r/{resourceId}/topic/{topicId}")
    public String createRelationWithTopic(@PathVariable Long resourceId, @PathVariable Long topicId) {
        resourceService.createRelationBtResourceAndOtherNodeByIds(resourceId,topicId,NodeLabel.TOPIC);
        return "OK";
    }

    @PostMapping("/r/{resourceId}/unit/{unitId}")
    public String createRelationWithUnit(@PathVariable Long resourceId, @PathVariable Long unitId) {
        resourceService.createRelationBtResourceAndOtherNodeByIds(resourceId,unitId,NodeLabel.UNIT);
        return "OK";
    }

    @PostMapping("/subTopic/{id}")
    public String createWithMountSubTopic(@RequestBody ResourceBean requestObject, @PathVariable Long id) {
        resourceService.createResourceWithRelation(requestObject, id, NodeLabel.SUBTOPIC);
        return "OK";
    }

    @PostMapping("/topic/{id}")
    public String createWithMountTopic(@RequestBody ResourceBean requestObject, @PathVariable Long id) {
        resourceService.createResourceWithRelation(requestObject, id, NodeLabel.TOPIC);
        return "OK";
    }

    @PostMapping("/unit/{id}")
    public String createWithMountUnit(@RequestBody ResourceBean requestObject, @PathVariable Long id) {
        resourceService.createResourceWithRelation(requestObject, id, NodeLabel.UNIT);
        return "OK";
    }

    @DeleteMapping("/id/{id}")
    public String deleteResourceById(@PathVariable Long id) {
        resourceService.deleteResourceAndRelationByNodeId(id);
        return "OK";
    }

    @DeleteMapping("/title/{title}")
    public String deleteResourceByTitle(@PathVariable String title) {
        resourceService.deleteResourceAndRelationByNodeTitle(title);
        return "OK";
    }

}
