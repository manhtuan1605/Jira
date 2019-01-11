package com.cmcglobal.plugins.jira.reports;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.NotFoundException;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.cmcglobal.plugins.dto.ApplicationUserDTO;
import com.cmcglobal.plugins.dto.TestMonitorMember;
import com.cmcglobal.plugins.service.TestMonitorService;
import com.cmcglobal.plugins.utils.Constants;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.*;

public class TesterPerformance extends AbstractReport {

    private final TestMonitorService testMonitorService;

    @Inject
    public TesterPerformance(final TestMonitorService testMonitorService) {
        this.testMonitorService = testMonitorService;
    }

    @Override
    public String generateReportHtml(final ProjectActionSupport projectActionSupport, final Map map) throws Exception {
        final ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        final long projectId = Long.parseLong(map.get("selectedProjectId").toString());
        final ProjectRole role = testMonitorService.getRole(user, projectId);
        final ProjectManager projectManager = ComponentAccessor.getProjectManager();
        final Project project = projectManager.getProjectObj(projectId);
        final Map<String, Object> data = new HashMap<>();
        try {
            List<TestMonitorMember> monitorMembers = new ArrayList<>();
            Set<ApplicationUser> leaders = new HashSet<>();
            Set<ApplicationUserDTO> assignees = new HashSet<>();
            if (role != null && Objects.equals(role.getName(), Constants.PROJECT_ROLE_PM)) {
                leaders = testMonitorService.getLeaderObjectsByProject(projectId);
                monitorMembers = testMonitorService.getMonitorAllMembers(projectId, LocalDate.now(), LocalDate.now());
                assignees = getAssigneesFromMonitorMembers(monitorMembers);
            } else if (role != null && Objects.equals(role.getName(), Constants.PROJECT_ROLE_QC_LEAD)) {
                monitorMembers = testMonitorService.getMonitorTeamMembers(user.getId(), projectId, LocalDate.now(),
                                                                          LocalDate.now());
                leaders.add(user);
                assignees = getAssigneesFromMonitorMembers(monitorMembers);
            } else if (role != null && Objects.equals(role.getName(), Constants.PROJECT_ROLE_QC)) {
                monitorMembers = testMonitorService.getUserMonitor(user, projectId, LocalDate.now(), LocalDate.now());
                leaders.add(user);
                assignees = getAssigneesFromMonitorMembers(monitorMembers);
            } else {
                data.put("error", Constants.ROLE_ERROR_MESSAGE);
                data.put("message", Constants.ROLE_REPORT_MESSAGE);
                return descriptor.getHtml("error", data);
            }
            data.put("role", role.getName());
            data.put("projectName", project.getName());
            data.put("project", projectId);
            data.put("leaders", leaders);
            data.put("monitors", monitorMembers);
            data.put("assignees", assignees);
            return descriptor.getHtml("view", data);
        } catch (final NotFoundException e) {
            data.put("error", e.getMessage());
            data.put("message", Constants.FUNCTION_NOT_FOUND_MESSAGE);
            return descriptor.getHtml("error", data);
        }
    }

    private Set<ApplicationUserDTO> getAssigneesFromMonitorMembers(final List<TestMonitorMember> monitorMembers) {
        final Set<ApplicationUserDTO> assignees = new HashSet<>();
        for (final TestMonitorMember monitorMember : monitorMembers) {
            assignees.add(monitorMember.getMember());
        }
        return assignees;
    }
}
