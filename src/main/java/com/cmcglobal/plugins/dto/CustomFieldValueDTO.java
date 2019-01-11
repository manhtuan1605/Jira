package com.cmcglobal.plugins.dto;

public class CustomFieldValueDTO {
    String key;
    String value;

    public CustomFieldValueDTO() {
    }

    public CustomFieldValueDTO(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
