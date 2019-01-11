package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;

import java.util.Date;

@Preload
public interface Model extends Entity {
    @NotNull
    long getProjectId();
    @NotNull
    void setProjectId(long projectId);

    String getModelName();
    void setModelName(String modelName);

    Date getCreateDate();
    void setCreateDate(Date createDate);

    String getCreateUser();
    void setCreateUser(String createUser);

    Date getUpdateDate();
    void setUpdateDate(Date updateDate);

    String getUpdateUser();
    void setUpdateUser(String updateUser);

}
