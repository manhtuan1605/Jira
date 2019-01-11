package com.cmcglobal.plugins.dto;

public class ReportResultDTO {
    private String component;
    private Integer totalTestCase;

    private Integer okPlus;
    private Integer okMinus;
    private Integer totalOK;

    private Integer nGMinus;
    private Integer nGPlus;
    private Integer todayNG;
    private Integer totalNG;

    private Integer pNMinus;
    private Integer pNPlus;
    private Integer todayPN;
    private Integer totalPN;

    public Integer getTotalOK() {
        return totalOK;
    }

    public void setTotalOK(Integer totalOK) {
        this.totalOK = totalOK;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Integer getTotalTestCase() {
        return totalTestCase;
    }

    public void setTotalTestCase(Integer totalTestCase) {
        this.totalTestCase = totalTestCase;
    }

    public Integer getOkPlus() {
        return okPlus;
    }

    public void setOkPlus(Integer okPlus) {
        this.okPlus = okPlus;
    }

    public Integer getOkMinus() {
        return okMinus;
    }

    public void setOkMinus(Integer okMinus) {
        this.okMinus = okMinus;
    }

    public Integer getTodayNG() {
        return todayNG;
    }

    public void setTodayNG(Integer todayNG) {
        this.todayNG = todayNG;
    }

    public Integer getnGMinus() {
        return nGMinus;
    }

    public void setnGMinus(Integer nGMinus) {
        this.nGMinus = nGMinus;
    }

    public Integer getnGPlus() {
        return nGPlus;
    }

    public void setnGPlus(Integer nGPlus) {
        this.nGPlus = nGPlus;
    }

    public Integer getTotalNG() {
        return totalNG;
    }

    public void setTotalNG(Integer totalNG) {
        this.totalNG = totalNG;
    }

    public Integer getTodayPN() {
        return todayPN;
    }

    public void setTodayPN(Integer todayPN) {
        this.todayPN = todayPN;
    }

    public Integer getpNMinus() {
        return pNMinus;
    }

    public void setpNMinus(Integer pNMinus) {
        this.pNMinus = pNMinus;
    }

    public Integer getpNPlus() {
        return pNPlus;
    }

    public void setpNPlus(Integer pNPlus) {
        this.pNPlus = pNPlus;
    }

    public Integer getTotalPN() {
        return totalPN;
    }

    public void setTotalPN(Integer totalPN) {
        this.totalPN = totalPN;
    }
}
