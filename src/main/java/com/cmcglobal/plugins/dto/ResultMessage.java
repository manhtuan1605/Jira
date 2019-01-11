package com.cmcglobal.plugins.dto;

public class ResultMessage {
    private int code;
    private String message;
    private int totalRowSuccess;
    private int totalRowError;
    private int totalRows;
    private String fileName;

    public ResultMessage() {
    }

    public ResultMessage(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResultMessage(int code, String message, int totalRowSuccess, int totalRowError, int totalRows) {
        this.code = code;
        this.message = message;
        this.totalRowSuccess = totalRowSuccess;
        this.totalRowError = totalRowError;
        this.totalRows = totalRows;
    }

    public ResultMessage(int code, String message, int totalRowSuccess, int totalRowError, int totalRows,
                         String fileName) {
        this.code = code;
        this.message = message;
        this.totalRowSuccess = totalRowSuccess;
        this.totalRowError = totalRowError;
        this.totalRows = totalRows;
        this.fileName = fileName;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTotalRowSuccess() {
        return totalRowSuccess;
    }

    public void setTotalRowSuccess(int totalRowSuccess) {
        this.totalRowSuccess = totalRowSuccess;
    }

    public int getTotalRowError() {
        return totalRowError;
    }

    public void setTotalRowError(int totalRowError) {
        this.totalRowError = totalRowError;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
