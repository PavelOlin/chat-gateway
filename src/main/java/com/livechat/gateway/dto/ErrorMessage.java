package com.livechat.gateway.dto;

import java.util.HashMap;
import java.util.Map;

public class ErrorMessage {
    private String description;
    private Map<String, String> fieldDetails = new HashMap<>();

    public ErrorMessage() {}

    public ErrorMessage(String description, Map<String, String> fieldDetails) {
        this.description = description;
        this.fieldDetails = fieldDetails;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getFieldDetails() {
        return fieldDetails;
    }

    public void setFieldDetails(Map<String, String> fieldDetails) {
        this.fieldDetails = fieldDetails;
    }
}
