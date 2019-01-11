package com.cmcglobal.plugins.jira.conditions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.workflow.condition.AbstractJiraCondition;
import com.cmcglobal.plugins.utils.Constants;
import com.opensymphony.module.propertyset.PropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TestResultCondition extends AbstractJiraCondition {
    private static final Logger log = LoggerFactory.getLogger(TestResultCondition.class);

    public boolean passesCondition(Map transientVars, Map args, PropertySet ps) {
        Issue issue = (Issue) transientVars.get("issue");
        Long projectId = issue.getProjectId();
        Project project = ComponentAccessor.getProjectManager().getProjectObj(projectId);
        final ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager.class);
        final ProjectRole projectRolePM = projectRoleManager.getProjectRole(Constants.PROJECT_ROLE_PM);
        final ProjectRole projectRoleQCLeader = projectRoleManager.getProjectRole(Constants.PROJECT_ROLE_QC_LEAD);
        final ProjectRole projectRoleQC = projectRoleManager.getProjectRole(Constants.PROJECT_ROLE_QC);
        if (projectRolePM != null && projectRoleQCLeader != null && projectRoleQC != null) {
            return projectRoleManager.isUserInProjectRole(
                    ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), projectRolePM, project) ||
                   projectRoleManager.isUserInProjectRole(
                           ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), projectRoleQCLeader,
                           project) || (projectRoleManager.isUserInProjectRole(
                    ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), projectRoleQC, project) &&
                                        issue.getAssignee() != null && issue.getAssignee()
                                                                            .equals(ComponentAccessor.getJiraAuthenticationContext()
                                                                                                     .getLoggedInUser()));
        }

        return false;
    }
}
