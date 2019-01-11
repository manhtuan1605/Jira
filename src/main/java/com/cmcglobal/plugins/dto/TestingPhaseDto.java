package com.cmcglobal.plugins.dto;

import java.util.Date;

public class TestingPhaseDto {
    private int ID;
    private long projectId;
    private String phaseName;
    private Date startDate;
    private Date endDate;
    private Date createDate;
    private String createUser;
    private Date updateDate;
    private String updateUser;
    private Boolean isActive;

    public TestingPhaseDto() {
    }

    public TestingPhaseDto(int ID, long projectId, String phaseName, Date startDate, Date endDate, Date createDate,
                           String createUser, Date updateDate, String updateUser, Boolean isActive) {
        this.ID = ID;
        this.projectId = projectId;
        this.phaseName = phaseName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createDate = createDate;
        this.createUser = createUser;
        this.updateDate = updateDate;
        this.updateUser = updateUser;
        this.isActive = isActive;
    }

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

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
