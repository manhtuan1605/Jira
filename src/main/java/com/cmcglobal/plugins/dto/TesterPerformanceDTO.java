package com.cmcglobal.plugins.dto;

import com.atlassian.jira.user.ApplicationUser;

import java.util.List;

public class TesterPerformanceDTO {
    private ApplicationUserDTO leader;
    private List<TesterPerformanceMemberDTO> members;
    private float percent;
    private int resolve;
    private String estimate;

    public ApplicationUserDTO getLeader() {
        return leader;
    }

    public void setLeader(ApplicationUserDTO leader) {
        this.leader = leader;
    }

    public List<TesterPerformanceMemberDTO> getMembers() {
        return members;
    }

    public void setMembers(List<TesterPerformanceMemberDTO> members) {
        this.members = members;
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
}
