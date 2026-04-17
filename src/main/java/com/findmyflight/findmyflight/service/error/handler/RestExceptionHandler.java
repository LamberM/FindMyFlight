package com.findmyflight.findmyflight.service.error.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(value = {MethodArgumentNotValidException.class, InvalidTokenException.class})
    protected ResponseEntity<RestErrorResponse> handleMethodArgumentNotValidException(
            Throwable e) {
        return ResponseEntity.badRequest()
                .body(new RestErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    @ExceptionHandler(EmailSendingException.class)
    protected ResponseEntity<RestErrorResponse> handleEmailSendingException(EmailSendingException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new RestErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), e.getMessage()));
    }
}
