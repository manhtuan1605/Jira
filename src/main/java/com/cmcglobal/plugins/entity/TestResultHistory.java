package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import org.springframework.core.annotation.Order;

import java.util.Date;

@Preload
public interface TestResultHistory extends Entity {

    String getTestCaseName();

    void setTestCaseName(String testCaseName);

    long getIssueId();

    void setIssueId(long issueId);

    @Order(value = 1)
    String getLatestResult();

    void setLatestResult(String latestResult);

    @Order(value = 2)
    String getLastRecentTest();

    void setLastRecentTest(String lastRecentTest);

    @Order(value = 3)
    Date getDateOfImplementation();

    void setDateOfImplementation(Date dateOfImplementation);

    @Order(value = 4)
    String getCreatedDate();

    void setCreatedDate(String createdDate);
}
