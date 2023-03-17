package com.zkz.microservice.neo4j.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
public class ApiExceptionMessages {
    private String timestamp;
    private String message;
    private List<String> details;
    private HttpStatus httpStatus;

    public ApiExceptionMessages(String message) {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        this.message = message;
    }
    public ApiExceptionMessages(String message, HttpStatus httpStatus) {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        this.message = message;
        this.httpStatus = httpStatus;
    }
    public ApiExceptionMessages(String message, HttpStatus httpStatus, List<String> details) {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        this.message = message;
        this.httpStatus = httpStatus;
        this.details = details;
    }


}
