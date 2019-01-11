package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import org.springframework.core.annotation.Order;

@Preload
public interface TestResult extends Entity {

    @NotNull
    long getTestCaseId();

    @NotNull
    void setTestCaseId(long testCaseId);

    @NotNull
    long getIssueId();

    @NotNull
    void setIssueId(long issueId);

    @Order(value = 1)
    String getLatestResult();

    void setLatestResult(String latestResult);

    @Order(value = 2)
    int getExecuteCount();

    void setExecuteCount(int executeCount);

    @Order(value = 3)
    String getLastRecentTest();

    void setLastRecentTest(String lastRecentTest);

    @Order(value = 4)
    String getPendingType();

    void setPendingType(String pendingType);

    @Order(value = 5)
    String getHuIdHuIndex();

    void setHuIdHuIndex(String huIdHuIndex);

    @Order(value = 6)
    String getHuId();

    void setHuId(String huId);

    @Order(value = 7)
    String getHuIndex();

    void setHuIndex(String huIndex);

    @Order(value = 8)
    String getHuMarket();

    void setHuMarket(String huMarket);

    @Order(value = 9)
    String getVehicleParameter();

    void setVehicleParameter(String vehicleParameter);

    @Order(value = 10)
    String getAmpType();

    void setAmpType(String ampType);

    @Order(value = 11)
    String getSiIndex();

    void setSiIndex(String siIndex);

    @Order(value = 12)
    String getExternalDeviceCode();

    void setExternalDeviceCode(String externalDeviceCode);

    @Order(value = 13)
    String getTestDuration();

    void setTestDuration(String testDuration);

    @Order(value = 14)
    String getDateOfImplementation();

    void setDateOfImplementation(String dateOfImplementation);

    @Order(value = 15)
    String getTesterRemark();

    void setTesterRemark(String resultComment);

    @Order(value = 16)
    String getCorrectionRequest();

    void setCorrectionRequest(String dateOfImplementation);

    @Order(value = 17)
    String getResultComment();

    void setResultComment(String resultComment);

    @Order(value = 18)
    String getRtcCode();

    void setRtcCode(String rtcCode);

    @Order(value = 19)
    String getExternalTicketId();

    void setExternalTicketId(String externalTicketId);

    @Order(value = 20)
    String getEvaluationSoftware();

    void setEvaluationSoftware(String evaluationSoftware);

    @Order(value = 21)
    String getEvaluationHard();

    void setEvaluationHard(String evaluationHard);

    @Order(value = 22)
    String getPeripheralDeviceNumber();

    void setPeripheralDeviceNumber(String peripheralDeviceNumber);

    @Order(value = 23)
    String getFreeDescription();

    void setFreeDescription(String freeDescriptionField);

    @Order(value = 24)
    String getEvaluationVariation();

    void setEvaluationVariation(String evaluationVariation);

    @Order(value = 25)
    String getRevisionFlag();

    void setRevisionFlag(String evaluationVariation);

    @Order(value = 26)
    String getIncompleteContent();

    void setIncompleteContent(String evaluationVariation);

    @Order(value = 27)
    String getTesterComments();

    void setTesterComments(String testerComments);


}
