/**
 * Project: SpringBootNeo4j
 * Date: 23/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.controller;

import com.zkz.microservice.neo4j.dto.OutcomeBean;
import com.zkz.microservice.neo4j.entity.education.OutcomeNode;
import com.zkz.microservice.neo4j.service.OutcomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/outcomes")
@RequiredArgsConstructor
public class OutcomeController {
    private final OutcomeService outcomeService;

    @PostMapping()
    OutcomeNode createOutcome(
            @RequestParam(value = "chapterId", required = false) String chapterId,
            @RequestParam(value = "chapterTitle", required = false) String chapterTitle,
            @RequestBody OutcomeBean outcome) {
        OutcomeNode result;
        if (chapterId != null) {
            result = outcomeService.createOutcome(Long.parseLong(chapterId), outcome);
        } else if (chapterTitle != null) {
            result = outcomeService.createOutcome(chapterTitle, outcome);
        } else {
            result = outcomeService.createOutcome(outcome);
        }
        return result;
    }

    @DeleteMapping(value = "/title/{title}")
    ResponseEntity<String> deleteOutcomeByTitle(@PathVariable String title) {
        return new ResponseEntity<>(outcomeService.deleteOutcomeByTitle(title), HttpStatus.OK);
    }

    @DeleteMapping(value = "/id/{id}")
    ResponseEntity<String> deleteOutcomeById(@PathVariable String id) {
        outcomeService.deleteOutcomeById(id);
        return ResponseEntity.status(HttpStatus.OK).body("delete successfully");
    }

    @GetMapping(value = "/id/{id}")
    OutcomeBean getOutcomeById(@PathVariable(name = "id", required = true) String id) {
        return outcomeService.getOutcomeById(id);
    }
    @GetMapping(value = "/title/{title}")
    OutcomeBean getOutcomeByTitle(@PathVariable(name = "title", required = true) String title) {
        return outcomeService.getOutcomesByTitle(title);
    }

}
