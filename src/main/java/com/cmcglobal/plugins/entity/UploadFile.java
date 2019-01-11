package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.StringLength;
import org.springframework.core.annotation.Order;

import java.util.Date;

import static net.java.ao.schema.StringLength.UNLIMITED;

@Preload
public interface UploadFile extends Entity {

    @Order(value = 1)
    long getProjectId();
    void setProjectId(long projectId);

    @Order(value = 2)
    @StringLength(value = StringLength.UNLIMITED)
    String getUploadFileName();
    void setUploadFileName(String uploadFileName);

    @Order(value = 3)
    @StringLength(value = StringLength.UNLIMITED)
    String getUploadFileNameInvalid();
    void setUploadFileNameInvalid(String uploadFileNameInvalid);

    @Order(value = 4)
    String getType();
    void setType(String type);

    @Order(value = 5)
    String getPhase();
    void setPhase(String phase);

    @Order(value = 6)
    String getTestCaseType();
    void setTestCaseType(String testCaseType);

    @Order(value = 7)
    Date getCreateDate();
    void setCreateDate(Date createDate);

    @Order(value = 8)
    String getCreateUser();
    void setCreateUser(String createUser);

    @Order(value = 9)
    String getPathFileValid();
    void setPathFileValid(String pathFileValid);

    @Order(value = 10)
    String getPathFileInvalid();
    void setPathFileInvalid(String pathFileinValid);

    @Order(value = 11)
    String getStatus();
    void setStatus(String status);

    @Order(value = 12)
    @StringLength(value = UNLIMITED)
    String getLastMessage();
    void setLastMessage(String LastMessage);

    /*@Order(value = 12)
    String getProgress();
    void setProgress(String progress);*/
}
