package com.cmcglobal.plugins.jira.webwork;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.model.querydsl.ApplicationUserDTO;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.cmcglobal.plugins.service.TeamsMembersService;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Helper;
import com.google.gson.Gson;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TeamsManagementAction extends JiraWebActionSupport {

    @JiraImport
    private TeamsMembersService teamsMembersService;

    private String issueKey;
    private String assignUser;
    private String projectKey;

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }

    public String getAssignUser() {
        return assignUser;
    }

    public void setAssignUser(String assignUser) {
        this.assignUser = assignUser;
    }

    public TeamsManagementAction(final TeamsMembersService teamsMembersService) {
        this.teamsMembersService = teamsMembersService;
    }

    private long teamId;

    public void setTeamId(final long teamId) throws Exception {
        this.teamId = teamId;
    }

    private long userId;

    public void setUserId(final long userId) throws Exception {
        this.userId = userId;
    }

    private long pid;

    public void setPid(final long pid) throws Exception {
        this.pid = pid;
    }

    public void doViewTeamDetail() throws Exception {
        final Map<String, Object> teamDetailData = new HashMap<>();
        teamDetailData.put(Constants.STRING_QCS, convertToApplicationUserDTO(getSelectedProjectQcs()));
        teamDetailData.put(Constants.STRING_MEMBERS, convertToApplicationUserDTO(getTeamMembers()));
        if (hasAnyErrors()) {
            teamDetailData.put(Constants.TEAM_MANAGEMENT_ERRORS_KEY, getErrors());
        }
        final String json = new Gson().toJson(teamDetailData);
        getHttpResponse().setContentType(Constants.CONTENT_TYPE);
        getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
        getHttpResponse().getWriter().write(json);
        getHttpResponse().setStatus(HttpServletResponse.SC_OK);
    }

    private Set<ApplicationUser> getSelectedProjectQcs() {
        final ProjectRoleManager projectRoleManager = ComponentManager.getComponentInstanceOfType(
                ProjectRoleManager.class);
        final ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(
                Helper.getProjectRoleByRoleName(Constants.STRING_QC), Helper.getProjectByPid(pid));
        return projectRoleActors.getUsers();
    }

    private List<ApplicationUserDTO> convertToApplicationUserDTO(final Set<ApplicationUser> theApplicationUsers) {
        final List<ApplicationUserDTO> applicationUserDTOs = new ArrayList<>();
        for (final ApplicationUser applicationUser : theApplicationUsers) {
            final ApplicationUserDTO applicationUserDTO = new ApplicationUserDTO(applicationUser.getId(),
                                                                                 applicationUser.getKey(),
                                                                                 applicationUser.getKey() +
                                                                                 Constants.OPEN_PARENTHESES +
                                                                                 applicationUser.getDisplayName() +
                                                                                 Constants.CLOSE_PARENTHESES);
            applicationUserDTOs.add(applicationUserDTO);
        }
        return applicationUserDTOs;
    }

    private Set<ApplicationUser> getTeamMembers() {
        return teamsMembersService.findTeamMembers(pid, teamId);
    }

    public void doAddMember() throws Exception {
        if (teamsMembersService.isExistedMember(pid, teamId, userId)) {
            addError(Constants.TEAM_MANAGEMENT_ADDING_ERRORS_KEY, Constants.ERROR_SELECTED_USER_EXISTED);
        } else {
            teamsMembersService.addMember(pid, teamId, userId);
        }
        doViewTeamDetail();
    }

    public void doAddOnlyMember() throws Exception {
        // check role of member add is QL leader
        ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager.class);
        ProjectRole projectRoleQcLeader = projectRoleManager.getProjectRole(Constants.ROLE_QC_LEADER);
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        Project currentProject = projectManager.getProjectObj(pid);
        ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(projectRoleQcLeader,
                                                                                      currentProject);
        Set<ApplicationUser> listQCLeads = projectRoleActors.getUsers();
        boolean isQCLead = false;
        // check add member is QClead or not
        ApplicationUser addMember = null;
        for (ApplicationUser user : listQCLeads) {
            if (user.getId() == userId) {
                isQCLead = true;
                addMember = user;
                break;
            }
        }

        if (teamsMembersService.isExistedMember(pid, teamId, userId)) {
            addError(Constants.TEAM_MANAGEMENT_ADDING_ERRORS_KEY, Constants.ERROR_SELECTED_USER_EXISTED);
        } else if (teamsMembersService.isExistedOnlyMember(pid, teamId, userId)) {
            addError(Constants.TEAM_MANAGEMENT_ADDING_ERRORS_KEY, Constants.ERROR_SELECTED_USER_IN_OTHER_TEAM);
        } else if (isQCLead && addMember.getId() == teamId) {
            addError(Constants.TEAM_MANAGEMENT_ADDING_ERRORS_KEY, Constants.ERROR_SELECTED_USER_IS_CURRENT_QC_LEAD);
        } else if (isQCLead) {
            addError(Constants.TEAM_MANAGEMENT_ADDING_ERRORS_KEY, Constants.ERROR_SELECTED_USER_LEADER);
        } else {
            teamsMembersService.addMember(pid, teamId, userId);
        }
        doViewTeamDetail();
    }

    public void doRemoveMember() throws Exception {
        if (teamsMembersService.isExistedMember(pid, teamId, userId)) {
            teamsMembersService.removeMember(pid, teamId, userId);
        }
        doViewTeamDetail();
    }

    public void doValidateAssign() throws Exception {
        ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager.class);
        ProjectRole projectRoleQcLeader = projectRoleManager.getProjectRole(Constants.ROLE_QC_LEADER);
        // get current login user
        ApplicationUser currentUserApp = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        // get project has current issue
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        Project projectHasCurrentIssue = projectManager.getProjectObjByKeyIgnoreCase(projectKey);
        if (projectHasCurrentIssue != null) {
            // get edit assign issue
            IssueManager issueManager = ComponentAccessor.getIssueManager();
            Issue currentIssue = issueManager.getIssueByKeyIgnoreCase(issueKey);
            // get assign user
            UserManager userManager = ComponentAccessor.getUserManager();
            ApplicationUser assignUserApp = !StringUtils.isEmpty(assignUser) ?
                                            userManager.getUserByKey(assignUser) :
                                            null;
            ApplicationUser assigningUserApp = currentIssue.getAssignee();
            // check device project or not
            final String[] issueTypesName = { Constants.ISSUE_TYPE_AUTOMOTIVE_DEVICE,
                                              Constants.ISSUE_TYPE_PERIPHERAL_DEVICE,
                                              Constants.ISSUE_TYPE_EXTERNAL_DEVICE };
            boolean isDeviceProject = true;
            final Collection<IssueType> issueTypes = projectHasCurrentIssue.getIssueTypes();
            for (IssueType issueType : issueTypes) {
                if (!Arrays.asList(issueTypesName).contains(issueType.getName())) {
                    isDeviceProject = false;
                }
            }
            if (projectRoleManager.isUserInProjectRole(currentUserApp, projectRoleQcLeader, projectHasCurrentIssue) &&
                isDeviceProject) {
                if (assigningUserApp == null ||
                    !(teamsMembersService.isExistedMember(projectHasCurrentIssue.getId(), currentUserApp.getId(),
                                                          assigningUserApp.getId()) ||
                      assigningUserApp.getUsername().equalsIgnoreCase(currentUserApp.getUsername()))) {
                    addError(Constants.ERROR_ASSIGN, Constants.ERROR_ASSIGN_CANT_ASSSIGN);
                } else if (assignUserApp == null) {
                    addError(Constants.ERROR_ASSIGN, Constants.ERROR_ASSIGN_CANT_UNASSSIGN);
                } else if (!teamsMembersService.isExistedMember(projectHasCurrentIssue.getId(), currentUserApp.getId(),
                                                                assignUserApp.getId()) &&
                           !(assignUserApp.getUsername().equalsIgnoreCase(currentUserApp.getUsername()))) {
                    addError(Constants.ERROR_ASSIGN, Constants.ERROR_ASSIGN_TO_ANOTHER_TEAM);
                }
            }
            if (hasAnyErrors()) {
                String json = new Gson().toJson(getErrors());
                getHttpResponse().setContentType(Constants.CONTENT_TYPE);
                getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
                getHttpResponse().getWriter().write(json);
                getHttpResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                getHttpResponse().setContentType(Constants.CONTENT_TYPE);
                getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
                getHttpResponse().setStatus(HttpServletResponse.SC_OK);
            }
        }
    }
}
