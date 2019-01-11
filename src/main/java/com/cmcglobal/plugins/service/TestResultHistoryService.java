package com.cmcglobal.plugins.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.entity.TestResultHistory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TestResultHistoryService {

      List<TestResultHistory> getTestResultHistoryByIssueAndEndDate(long issueId, Date end);

      List<TestResultHistory> getTestResultHistoryByIssue(long issueId);

      List<TestResultHistory> getHistoryByIssueTheDayBeforeEndDate(long issueId, Date end);

      List<Long> getListIdIssuesWithImpletedDateIsEndate(Date end);

      Map<String, Integer> countTotalTestResult(long projectId, ProjectComponent projectComponent, Date start, Date end);

      public List<TestResultHistory> getTestResultHistoryByIssueAndImplementedDateByEndDate(final long issueId, Date end);
}