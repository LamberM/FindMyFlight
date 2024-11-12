package com.findmyflight.findmyflight.service.error.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    protected ResponseEntity<RestErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest()
                .body(new RestErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }
}
