package com.cmcglobal.plugins.service;

import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.dto.TestExecuteChartDTO;

import java.time.LocalDate;
import java.util.List;

public interface TestExecuteChartService {
    List<TestExecuteChartDTO> getTestExecuteChartByProjectIdAndAssigneeAndDate(Long projectId,
                                                                               List<ApplicationUser> assignees,
                                                                               LocalDate startDate, LocalDate endDate);
}