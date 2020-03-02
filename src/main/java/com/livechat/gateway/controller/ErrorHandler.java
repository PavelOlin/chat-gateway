package com.livechat.gateway.controller;

import com.google.gson.Gson;
import com.livechat.gateway.dto.ErrorMessage;
import com.livechat.gateway.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

@RestControllerAdvice
public class ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleValidationError(ValidationException e) {
        LOGGER.info("Got validation exception: message[" + e.getMessage() + "], validation[" + new Gson().toJson(e.getValidation()) + "]");
        return new ErrorMessage(e.getMessage(), e.getValidation());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleInternalError(Throwable throwable) {
        LOGGER.error("Unhandled exception: " + throwable.getMessage(), throwable);
        return new ErrorMessage(throwable.getClass().getCanonicalName() + ": " + throwable.getMessage(), Collections.emptyMap());
    }
}
