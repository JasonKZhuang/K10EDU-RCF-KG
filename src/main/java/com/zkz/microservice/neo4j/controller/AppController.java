package com.zkz.microservice.neo4j.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Jason Zhuang 17/11/2021
 */
@Slf4j
//RestController indicates that the data returned by each method will be written straight into the response body
//instead of rendering a template.
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class AppController {
    @GetMapping("/status")
    public String heartBreak(){
        return "OK";
    }
}
