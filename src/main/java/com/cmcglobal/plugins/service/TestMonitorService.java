package com.cmcglobal.plugins.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.dto.TestMonitorMember;
import com.cmcglobal.plugins.entity.TestCaseType;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Transactional
public interface TestMonitorService {
   /**
    * Get user's role in project
    *
    * @param user
    * @param projectId
    * @return
    */
   public ProjectRole getRole(ApplicationUser user, final long projectId);

   public TestCaseType getTestCaseTypeIsFunction(final long projectId);

   public Set<ApplicationUser> getLeaderObjectsByProject(Long projectId);

   public List<TestMonitorMember> getMonitorAllMembers(final long projectId, LocalDate start, LocalDate end);

   public List<TestMonitorMember> getMonitorTeamMembers(long leaderId, long projectId, LocalDate start, LocalDate end);

   public List<TestMonitorMember> getUserMonitor(ApplicationUser user, long projectId, LocalDate start, LocalDate end);

   public List<Issue> getIssuesByProject(Long projectId);

   public List<Issue> getIssuesByProjectandComponent(Long projectId, ProjectComponent projectComponent);

   public List<Issue> getIssuesByProjectandComponentAndEndDate(Long projectId, ProjectComponent projectComponent, Date end);

   public Map<String ,Integer> countTotalIssueByTestResult(Long projectId, ProjectComponent projectComponent);

   public Map<String ,Integer> countTotalIssueByTestResult(Long projectId, ProjectComponent projectComponent, Date start, Date end);
}

