package com.zkz.microservice.neo4j.exception;

import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionsHandler extends ResponseEntityExceptionHandler {
    public static final String DEFAULT_ERROR_VIEW = "error";

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ApiExceptionMessages> handleDefaultErrorHandler(Exception e) {
        logger.error(e);
        ApiExceptionMessages error = new ApiExceptionMessages("Errors",HttpStatus.INTERNAL_SERVER_ERROR,Arrays.asList(e.getMessage()));
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiExceptionMessages> handleServiceException(ServiceException e) {
        logger.warn("Service Exception", e);
        ApiExceptionMessages error = new ApiExceptionMessages(
                String.format("Service Exception"),
                e.getStatus(),
                Arrays.asList(e.getMessage())
        );
        return ResponseEntity.status(e.getStatus()).body(error);
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        return new ResponseEntity<>("Wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
