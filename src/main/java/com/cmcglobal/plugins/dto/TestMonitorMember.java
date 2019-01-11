package com.cmcglobal.plugins.dto;

public class TestMonitorMember {
    private ApplicationUserDTO member;
    private TestMonitor testExecute;
    private TestMonitor defectSubmit;
    private TestMonitor qaSubmit;
    private TestMonitor defectFix;
    private TestMonitor dfAndQaFollow;
    private TestMonitor other;

    public ApplicationUserDTO getMember() {
        return member;
    }

    public void setMember(ApplicationUserDTO member) {
        this.member = member;
    }

    public TestMonitor getTestExecute() {
        return testExecute;
    }

    public void setTestExecute(TestMonitor testExecute) {
        this.testExecute = testExecute;
    }

    public TestMonitor getDefectSubmit() {
        return defectSubmit;
    }

    public void setDefectSubmit(TestMonitor defectSubmit) {
        this.defectSubmit = defectSubmit;
    }

    public TestMonitor getQaSubmit() {
        return qaSubmit;
    }

    public void setQaSubmit(TestMonitor qaSubmit) {
        this.qaSubmit = qaSubmit;
    }

    public TestMonitor getDefectFix() {
        return defectFix;
    }

    public void setDefectFix(TestMonitor defectFix) {
        this.defectFix = defectFix;
    }

    public TestMonitor getDfAndQaFollow() {
        return dfAndQaFollow;
    }

    public void setDfAndQaFollow(TestMonitor dfAndQaFollow) {
        this.dfAndQaFollow = dfAndQaFollow;
    }

    public TestMonitor getOther() {
        return other;
    }

    public void setOther(TestMonitor other) {
        this.other = other;
    }
}
