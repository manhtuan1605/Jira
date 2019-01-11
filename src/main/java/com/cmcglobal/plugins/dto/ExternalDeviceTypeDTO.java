package com.cmcglobal.plugins.dto;

import java.util.Date;

public class ExternalDeviceTypeDTO {
    private  int     ID;
    private  long    projectId;
    private  String  deviceType;
    private Date    createDate;
    private  String  createUser;
    private  Date    updateDate;
    private  String  updateUser;
    private  Boolean isActive;

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

    public ExternalDeviceTypeDTO(int ID, long projectId, String deviceType) {
        this.ID = ID;
        this.projectId = projectId;
        this.deviceType = deviceType;
    }

    public ExternalDeviceTypeDTO() {
    }

    public ExternalDeviceTypeDTO(int ID, long projectId, String deviceType, Date createDate, String createUser,
                                 Date updateDate, String updateUser, Boolean isActive) {
        this.ID = ID;
        this.projectId = projectId;
        this.deviceType = deviceType;
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

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
