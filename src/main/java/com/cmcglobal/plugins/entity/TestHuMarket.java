package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;

import java.util.Date;

@Preload
public interface TestHuMarket extends Entity {
    @NotNull
    long getProjectId();
    @NotNull
    void setProjectId(long projectId);

    String getTestHuMarketName();
    void setTestHuMarketName(String testHuMarketName);

    Date getCreateDate();
    void setCreateDate(Date createDate);

    String getCreateUser();
    void setCreateUser(String createUser);

    Date getUpdateDate();
    void setUpdateDate(Date updateDate);

    String getUpdateUser();
    void setUpdateUser(String updateUser);

}
