/**
 * Project: SpringBootNeo4j
 * Date: 23/10/2022
 * Author: Jason
 */
package com.zkz.microservice.neo4j.controller;

import com.zkz.microservice.neo4j.dto.ChapterBean;
import com.zkz.microservice.neo4j.entity.education.ChapterNode;
import com.zkz.microservice.neo4j.exception.ServiceException;
import com.zkz.microservice.neo4j.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chapters")
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterService chapterService;

    @PostMapping
    public ChapterBean create(@RequestBody ChapterBean chapter) {
        if (chapter.getId() != null){
            throw new ServiceException(HttpStatus.NOT_ACCEPTABLE,"Id must be null.");
        }
        return chapterService.createSimpleChapter(chapter);
    }

    @PutMapping
    public ChapterBean update(@RequestBody ChapterBean chapter) {
        if (chapter.getId() == null){
            throw new ServiceException(HttpStatus.NOT_ACCEPTABLE,"Id must not be null.");
        }
        try {
            Long.valueOf(chapter.getId());
        }catch (Exception e){
            throw new ServiceException(HttpStatus.NOT_ACCEPTABLE,"Id must be a digital number.");
        }
        return chapterService.updateSimpleChapter(chapter);
    }

    @DeleteMapping(value = "/id/{id}")
    public ResponseEntity<String> deleteChapterById(@PathVariable String id) {
        return new ResponseEntity<>(chapterService.deleteChapterById(id), HttpStatus.OK);
    }

    @DeleteMapping(value = "/title/{title}")
    public ResponseEntity<String> deleteChapterByTitle(@PathVariable String title) {
        return new ResponseEntity<>(chapterService.deleteChapterByTitle(title), HttpStatus.OK);
    }

    @GetMapping(value = "/id/{id}")
    public ChapterBean getChapterById(@PathVariable String id) {
        return chapterService.getChapterById(id);
    }

    @GetMapping(value = "/title/{title}")
    public List<ChapterBean> getChapterByTitle(@PathVariable(name = "title") String title) {
        return chapterService.getChapterByTitle(title);
    }

    @GetMapping()
    public List<ChapterBean> getAllChapters() {
        return chapterService.getAllChapters();
    }

}