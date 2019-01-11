package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.StringLength;
import org.springframework.core.annotation.Order;

import java.util.Date;

import static net.java.ao.schema.StringLength.UNLIMITED;

@Preload
public interface CSVTestResult extends Entity {

    @Order(value = 1)
    long getProjectId();
    void setProjectId(long projectId);

    @Order(value = 2)
    @StringLength(value = StringLength.UNLIMITED)
    String getUploadFileName();
    void setUploadFileName(String uploadFileName);

    @Order(value = 3)
    Date getCreateDate();
    void setCreateDate(Date createDate);

    @Order(value = 4)
    Date getUpdateDate();
    void setUpdateDate(Date updateDate);

    @Order(value = 5)
    String getCreateUser();
    void setCreateUser(String createUser);
}
