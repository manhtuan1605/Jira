package com.cmcglobal.plugins.dto;

public class TestPlanDTO {
    private String testCaseNo;
    private String assign;
    private String startDate;
    private String endDate;
    private String phase;
    private String errTestPlan;

    public String getTestCaseNo() {
        return testCaseNo;
    }

    public void setTestCaseNo(String testCaseNo) {
        this.testCaseNo = testCaseNo;
    }

    public String getAssign() {
        return assign;
    }

    public void setAssign(String assign) {
        this.assign = assign;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getErrTestPlan() {
        return errTestPlan;
    }

    public void setErrTestPlan(String errTestPlan) {
        this.errTestPlan = errTestPlan;
    }
}
