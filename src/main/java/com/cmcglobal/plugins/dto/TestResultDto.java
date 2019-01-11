package com.cmcglobal.plugins.dto;

import java.util.Date;

public class TestResultDto {
    private long   IssueId;
    private String Phase;
    private String TestcaseNo;
    private String result;
    private String LatestResult;
    private String PendingType;
    private String HuIdHuIndex;
    private String HuId;
    private String HuIndex;
    private String ExternalTestDeviceId;
    private String  ExternalDeviceCode;
    private String TestDuration;
    private String DateOfImplementation;
    private String TimeForQnASubmission;
    private String TimeForDefectSubmission;
    private String RTCCode;
    private String ExternalTicketId;
    private String BlockingTicketId;
    private String ResultComment;
    private String ErrorTestResult;
    private String HuMarket;
    private String VehicleParameter;
    private String AmpType;
    private String SiIndex;
    private String TesterRemark;
    private String CorrectionRequest;
    private String EvaluationSoftware;
    private String EvaluationHard;
    private String PeripheralDeviceNumber;
    private String FreeDescription;
    private String EvaluationVariation;
    private String RevisionFlag;
    private String IncompleteContent;
    private String TesterComments;
    private int    CurrentIndex;

    public long getIssueId() {
        return IssueId;
    }

    public void setIssueId(final long issueId) {
        IssueId = issueId;
    }

    public String getPhase() {
        return Phase;
    }

    public void setPhase(final String phase) {
        Phase = phase;
    }

    public String getTestcaseNo() {
        return TestcaseNo;
    }

    public void setTestcaseNo(final String testcaseNo) {
        TestcaseNo = testcaseNo;
    }

    public String getResult() {
        return result;
    }

    public void setResult(final String result) {
        this.result = result;
    }

    public String getPendingType() {
        return PendingType;
    }

    public void setPendingType(final String pendingType) {
        PendingType = pendingType;
    }

    public String getLatestResult() {
        return LatestResult;
    }

    public void setLatestResult(final String latestResult) {
        LatestResult = latestResult;
    }

    public String getHuIdHuIndex() {
        return HuIdHuIndex;
    }

    public void setHuIdHuIndex(final String huIdHuIndex) {
        HuIdHuIndex = huIdHuIndex;
    }

    public String getHuId() {
        return HuId;
    }

    public void setHuId(final String huId) {
        HuId = huId;
    }

    public String getHuIndex() {
        return HuIndex;
    }

    public void setHuIndex(final String huIndex) {
        HuIndex = huIndex;
    }

    public String getExternalTestDeviceId() {
        return ExternalTestDeviceId;
    }

    public void setExternalTestDeviceId(final String externalTestDeviceId) {
        ExternalTestDeviceId = externalTestDeviceId;
    }

    public void setExternalDeviceCode(String externalDeviceCode) {
        ExternalDeviceCode = externalDeviceCode;
    }

    public String getExternalDeviceCode() {
        return ExternalDeviceCode;
    }

    public String getTestDuration() {
        return TestDuration;
    }

    public void setTestDuration(final String testDuration) {
        TestDuration = testDuration;
    }

    public void setDateOfImplementation(String dateOfImplementation) {
        DateOfImplementation = dateOfImplementation;
    }

    public String getDateOfImplementation() {
        return DateOfImplementation;
    }

    public String getTimeForQnASubmission() {
        return TimeForQnASubmission;
    }

    public void setTimeForQnASubmission(final String timeForQnASubmission) {
        TimeForQnASubmission = timeForQnASubmission;
    }

    public String getTimeForDefectSubmission() {
        return TimeForDefectSubmission;
    }

    public void setTimeForDefectSubmission(final String timeForDefectSubmission) {
        TimeForDefectSubmission = timeForDefectSubmission;
    }

    public void setRTCCode(String RTCCode) {
        this.RTCCode = RTCCode;
    }

    public String getRTCCode() {
        return RTCCode;
    }

    public void setExternalTicketId(String externalTicketId) {
        ExternalTicketId = externalTicketId;
    }

    public String getExternalTicketId() {
        return ExternalTicketId;
    }

    public String getBlockingTicketId() {
        return BlockingTicketId;
    }

    public void setBlockingTicketId(final String blockingTicketId) {
        BlockingTicketId = blockingTicketId;
    }

    public String getResultComment() {
        return ResultComment;
    }

    public void setResultComment(final String resultComment) {
        ResultComment = resultComment;
    }

    public String getErrorTestResult() {
        return ErrorTestResult;
    }

    public void setErrorTestResult(final String errorTestResult) {
        ErrorTestResult = errorTestResult;
    }

    public String getHuMarket() {
        return HuMarket;
    }

    public void setHuMarket(final String huMarket) {
        HuMarket = huMarket;
    }

    public String getVehicleParameter() {
        return VehicleParameter;
    }

    public void setVehicleParameter(final String vehicleParameter) {
        VehicleParameter = vehicleParameter;
    }

    public String getAmpType() {
        return AmpType;
    }

    public void setAmpType(final String ampType) {
        AmpType = ampType;
    }

    public String getSiIndex() {
        return SiIndex;
    }

    public void setSiIndex(final String siIndex) {
        SiIndex = siIndex;
    }

    public int getCurrentIndex() {
        return CurrentIndex;
    }

    public void setCurrentIndex(final int currentIndex) {
        CurrentIndex = currentIndex;
    }

    public String getTesterRemark() {
        return TesterRemark;
    }

    public void setTesterRemark(String testerRemark) {
        TesterRemark = testerRemark;
    }

    public String getCorrectionRequest() {
        return CorrectionRequest;
    }

    public void setCorrectionRequest(String correctionRequest) {
        CorrectionRequest = correctionRequest;
    }

    public void setEvaluationHard(String evaluationHard) {
        EvaluationHard = evaluationHard;
    }

    public String getEvaluationHard() {
        return EvaluationHard;
    }

    public void setEvaluationSoftware(String evaluationSoftware) {
        EvaluationSoftware = evaluationSoftware;
    }

    public String getEvaluationSoftware() {
        return EvaluationSoftware;
    }

    public void setPeripheralDeviceNumber(String peripheralDeviceNumber) {
        PeripheralDeviceNumber = peripheralDeviceNumber;
    }

    public String getPeripheralDeviceNumber() {
        return PeripheralDeviceNumber;
    }

    public String getFreeDescription() {
        return FreeDescription;
    }

    public void setFreeDescription(String freeDescription) {
        FreeDescription = freeDescription;
    }

    public String getEvaluationVariation() {
        return EvaluationVariation;
    }

    public void setEvaluationVariation(String evaluationVariation) {
        EvaluationVariation = evaluationVariation;
    }

    public String getRevisionFlag() {
        return RevisionFlag;
    }

    public void setRevisionFlag(String revisionFlag) {
        RevisionFlag = revisionFlag;
    }

    public String getIncompleteContent() {
        return IncompleteContent;
    }

    public void setIncompleteContent(String incompleteContent) {
        IncompleteContent = incompleteContent;
    }

    public String getTesterComments() {
        return TesterComments;
    }

    public void setTesterComments(String testerComments) {
        TesterComments = testerComments;
    }

}
