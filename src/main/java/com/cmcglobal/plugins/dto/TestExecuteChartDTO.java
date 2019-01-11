package com.cmcglobal.plugins.dto;

import java.time.LocalDateTime;

public class TestExecuteChartDTO {
    private ApplicationUserDTO assignee;
    private String executeTime;
    private Double productivity;

    public ApplicationUserDTO getAssignee() {
        return assignee;
    }

    public void setAssignee(final ApplicationUserDTO assignee) {
        this.assignee = assignee;
    }

    public String getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(final String executeTime) {
        this.executeTime = executeTime;
    }

    public Double getProductivity() {
        return productivity;
    }

    public void setProductivity(final Double productivity) {
        this.productivity = productivity;
    }
}