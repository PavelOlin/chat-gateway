package com.livechat.gateway.exception;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends RuntimeException {
    private final Map<String, String> validation = new HashMap<>();

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, String field, String validationMessage) {
        this(message);
        this.validation.put(field, validationMessage);
    }

    public ValidationException(String message, Map<String, String> validation) {
        this(message);
        this.validation.putAll(validation);
    }

    public Map<String, String> getValidation() {
        return validation;
    }
}
