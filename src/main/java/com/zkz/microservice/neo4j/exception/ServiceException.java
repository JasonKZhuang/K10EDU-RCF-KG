package com.zkz.microservice.neo4j.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceException extends RuntimeException {
    private final HttpStatus status;

    /**
     * the constructor with HttpStatus
     * @param status HttpStatus
     */
    public ServiceException(HttpStatus status) {
        super(status.getReasonPhrase());
        this.status = status;
    }

    /**
     * the constructor with HttpStatus and error message
     * @param status HttpStatus
     * @param message String
     */
    public ServiceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static ServiceException genericError(String message) {
        return new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static ServiceException badRequest() {
        return new ServiceException(HttpStatus.BAD_REQUEST);
    }

    public static ServiceException badRequest(String message) {
        return new ServiceException(HttpStatus.BAD_REQUEST, message);
    }

    public static ServiceException notFound() {
        return new ServiceException(HttpStatus.NOT_FOUND);
    }

    public static ServiceException notFound(String message) {
        return new ServiceException(HttpStatus.NOT_FOUND, message);
    }

    public static ServiceException accessDenied() {
        return new ServiceException(HttpStatus.FORBIDDEN);
    }

    public static ServiceException accessDenied(String message) {
        return new ServiceException(HttpStatus.FORBIDDEN, message);
    }

    public static ServiceException unauthorized() {
        return new ServiceException(HttpStatus.UNAUTHORIZED);
    }

    public static ServiceException unauthorized(String message) {
        return new ServiceException(HttpStatus.UNAUTHORIZED, message);
    }
}
