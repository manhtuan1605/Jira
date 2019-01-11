package com.cmcglobal.plugins.dto;

import java.util.Date;

public class VehicleParameterDTO {
    private int id;
    private long projectId;
    private String vehicleParameterName;
    private Date createDate;
    private String createUser;
    private Date updateDate;
    private String updateUser;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getVehicleParameterName() {
        return vehicleParameterName;
    }

    public void setVehicleParameterName(String vehicleParameterName) {
        this.vehicleParameterName = vehicleParameterName;
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

    public VehicleParameterDTO() {}

    public VehicleParameterDTO(int id, long projectId, String vehicleParameterName, Date createDate, String createUser,
                               Date updateDate, String updateUser) {
        this.id = id;
        this.projectId = projectId;
        this.vehicleParameterName = vehicleParameterName;
        this.createDate = createDate;
        this.createUser = createUser;
        this.updateDate = updateDate;
        this.updateUser = updateUser;
    }
}
