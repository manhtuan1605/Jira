package com.cmcglobal.plugins.dto;

import java.util.Date;

public class TestCaseTypeDTO {
    private int ID;
    private long projectId;
    private String testCaseTypeName;
    private Long performance;
    private Date createDate;
    private String createUser;
    private Date updateDate;
    private String updateUser;
    private Boolean isActive;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getTestCaseTypeName() {
        return testCaseTypeName;
    }

    public void setTestCaseTypeName(String testCaseTypeName) {
        this.testCaseTypeName = testCaseTypeName;
    }

    public Long getPerformance() {
        return performance;
    }

    public void setPerformance(Long performance) {
        this.performance = performance;
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

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
    public TestCaseTypeDTO() {

    }
    public TestCaseTypeDTO(int ID, long projectId, String testCaseTypeName, Long performance, Date createDate, String createUser, Date updateDate, String updateUser, Boolean isActive) {
        this.ID = ID;
        this.projectId = projectId;
        this.testCaseTypeName = testCaseTypeName;
        this.performance = performance;
        this.createDate = createDate;
        this.createUser = createUser;
        this.updateDate = updateDate;
        this.updateUser = updateUser;
        this.isActive = isActive;
    }
}
