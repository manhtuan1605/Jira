package com.cmcglobal.plugins.utils;

public enum ImportStatus {
    IMPORTING("Importing"),
    IMPORTED("Imported"),
    WAITING("Waiting"),
    BIG_FILE_WAITING("Big File Waiting"),
    ERROR("Error");

    ImportStatus(String status) {
        this.status = status;
    }

    private String status;

    public String getValue() {
        return status;
    }

}
