/**
 * Project: MicroserviceNeo4j
 * Date: 7/3/2023
 * Author: Jason
 */
package com.zkz.microservice.neo4j.controller;

import com.zkz.microservice.neo4j.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/query")
@AllArgsConstructor
public class QueryController {

    @GetMapping(value = "/chapter")
    @Operation(summary = "Get chapters and related nodes by its id or title parameter")
    public List<ChapterBean> getChaptersByIdOrTitle(
            @RequestParam("id") String argId,
            @RequestParam("title") String argTitle) {
        // MATCH (c:Chapter) -- (m)  WHERE ID(c)=1225    return c,m
        return null;
    }

    @GetMapping(value = "/outcome")
    @Operation(summary = "Get learning outcomes and related nodes by its id or title parameter")
    public List<OutcomeBean> getOutcomesByIdOrTitle(
            @RequestParam("id") String argId,
            @RequestParam("title") String argTitle) {
        // MATCH (c:Chapter) -- (m)  WHERE ID(c)=1225    return c,m
        return null;
    }

    @GetMapping(value = "/unit")
    @Operation(summary = "Get units and related nodes by its id or title parameter")
    public List<UnitBean> getUnitsByIdOrTitle(
            @RequestParam("id") String argId,
            @RequestParam("title") String argTitle) {
        // MATCH (c:Chapter) -- (m)  WHERE ID(c)=1225    return c,m
        return null;
    }

    @GetMapping(value = "/topic")
    @Operation(summary = "Get topics and related nodes by its id or title parameter")
    public List<TopicBean> getTopicsByIdOrTitle(
            @RequestParam("id") String argId,
            @RequestParam("title") String argTitle) {
        // MATCH (c:Chapter) -- (m)  WHERE ID(c)=1225    return c,m
        return null;
    }

    @GetMapping(value = "/entity")
    @Operation(summary = "Get entity and related nodes by its name parameter")
    public List<ElementBean> getEntityByName(@RequestParam("name") String argName) {
        //MATCH p= (e:Entity) -- (m)  WHERE ID(e)=1749    return p;
        return null;
    }

    @GetMapping(value = "/elaboration")
    @Operation(summary = "Get elaboration and related nodes by its name parameter")
    public List<ElaborationBean> getElaborationByName(@RequestParam("name") String argName) {
        //MATCH p= (e:Entity) -- (m)  WHERE ID(e)=1749    return p;
        return null;
    }


}
