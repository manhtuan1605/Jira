package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;

import java.util.Date;

@Preload
public interface VehicleParameter extends Entity {

    @NotNull
    long getProjectId();
    @NotNull
    void setProjectId(long projectId);

    String getVehicleParameterName();
    void setVehicleParameterName(String vehicleParameterName);

    Date getCreateDate();
    void setCreateDate(Date createDate);

    String getCreateUser();
    void setCreateUser(String createUser);

    Date getUpdateDate();
    void setUpdateDate(Date updateDate);

    String getUpdateUser();
    void setUpdateUser(String updateUser);

}
