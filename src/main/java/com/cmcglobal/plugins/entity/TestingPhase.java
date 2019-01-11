package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Preload
public interface TestingPhase extends Entity {
    @NotNull
    long getProjectId();

    @NotNull
    void setProjectId(long projectId);

    String getPhaseName();

    void setPhaseName(String phaseName);

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date getStartDate();

    void setStartDate(Date startDate);

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date getEndDate();

    void setEndDate(Date endDate);

    Date getCreateDate();

    void setCreateDate(Date createDate);

    String getCreateUser();

    void setCreateUser(String createUser);

    Date getUpdateDate();

    void setUpdateDate(Date updateDate);

    String getUpdateUser();

    void setUpdateUser(String updateUser);

    Boolean getIsActive();

    void setIsActive(Boolean isActive);
}
