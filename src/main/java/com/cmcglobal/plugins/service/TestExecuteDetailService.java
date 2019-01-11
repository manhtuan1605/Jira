package com.cmcglobal.plugins.service;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.dto.TestExecuteDetailDTO;
import com.cmcglobal.plugins.entity.TestCaseType;

import java.time.LocalDate;
import java.util.List;

public interface TestExecuteDetailService {
    List<TestExecuteDetailDTO> getTestExecuteDetailByProjectIdAndAssigneeAndDate(Long projectId,
                                                                                 ApplicationUser assignee,
                                                                                 LocalDate startDate,
                                                                                 LocalDate endDate);

    List<TestCaseType> getTestcaseTypesByProjectId(Long projectId);

    List<Issue> getExecutedTestCasesByProjectIdAndAssigneeAndDate(Long projectId, ApplicationUser assignee,
                                                                  LocalDate startDate, LocalDate endDate);

    TestCaseType getFunctionTestcaseType(List<TestCaseType> testcaseTypes);

    List<Issue> filterExecutedTestCasesByTestCaseType(List<Issue> executedTestCases, String testCaseType);

    double getSumOfIssueTestDuration(List<Issue> testCases);
}