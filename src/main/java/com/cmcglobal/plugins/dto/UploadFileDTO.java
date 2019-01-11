package com.cmcglobal.plugins.dto;

import java.util.Date;

public class UploadFileDTO {

    private long   id;
    private long   projectId;
    private String uploadFileName;
    private String uploadFileNameInvalid;
    private String type;
    private String phase;
    private String testCaseType;
    private Date   createDate;
    private String createUser;
    private String pathFileValid;
    private String pathFileInvalid;
    private String status;
    private String progress;
    private String importType;
    private String lastMessage;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public void setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }

    public String getUploadFileNameInvalid() {
        return uploadFileNameInvalid;
    }

    public void setUploadFileNameInvalid(String uploadFileNameInvalid) {
        this.uploadFileNameInvalid = uploadFileNameInvalid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getTestCaseType() {
        return testCaseType;
    }

    public void setTestCaseType(String testCaseType) {
        this.testCaseType = testCaseType;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getPathFileValid() {
        return pathFileValid;
    }

    public void setPathFileValid(String pathFileValid) {
        this.pathFileValid = pathFileValid;
    }

    public String getPathFileInvalid() {
        return pathFileInvalid;
    }

    public void setPathFileInvalid(String pathFileInvalid) {
        this.pathFileInvalid = pathFileInvalid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getImportType() {
        return importType;
    }

    public void setImportType(String importType) {
        this.importType = importType;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
