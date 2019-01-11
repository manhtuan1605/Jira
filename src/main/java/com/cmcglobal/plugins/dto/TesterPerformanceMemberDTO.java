package com.cmcglobal.plugins.dto;

import com.cmcglobal.plugins.utils.Constants;

import java.util.Map;

public class TesterPerformanceMemberDTO {
    private ApplicationUserDTO member;
    private float percent;
    private int resolve;
    private String estimate;
    private double testDuration;
    private double qaTime;
    private double defectTime;

    public ApplicationUserDTO getMember() {
        return member;
    }

    public void setMember(ApplicationUserDTO member) {
        this.member = member;
    }

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

    public int getResolve() {
        return resolve;
    }

    public void setResolve(int resolve) {
        this.resolve = resolve;
    }

    public String getEstimate() {
        return estimate;
    }

    public void setEstimate(String estimate) {
        this.estimate = estimate;
    }

    public double getTestDuration() { return testDuration; }

    public void setTestDuration(double testDuration) { this.testDuration = testDuration; }

    public double getQaTime() { return qaTime; }

    public void setQaTime(double qaTime) { this.qaTime = qaTime; }

    public double getDefectTime() { return defectTime; }

    public void setDefectTime(double defectTime) { this.defectTime = defectTime; }

    public void setCountTimeMonitor(Map<String, Double> map) {
        this.testDuration = map.get(Constants.CUSTOM_FIELD_TEST_DURATION);
        this.qaTime = map.get(Constants.CUSTOM_FIELD_TIME_FOR_QNA_SUBMISSION);
        this.defectTime = map.get(Constants.CUSTOM_FIELD_TIME_FOR_DEFECT_SUBMISSION);
    }
}
