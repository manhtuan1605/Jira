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

public class PlanTestcaseCondition extends AbstractJiraCondition {
    private static final Logger log = LoggerFactory.getLogger(PlanTestcaseCondition.class);

    public boolean passesCondition(Map transientVars, Map args, PropertySet ps) {
        Long projectId = ((Issue) transientVars.get("issue")).getProjectId();
        Project project = ComponentAccessor.getProjectManager().getProjectObj(projectId);
        final ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager.class);
        final ProjectRole projectRolePM = projectRoleManager.getProjectRole(Constants.PROJECT_ROLE_PM);
        final ProjectRole projectRoleQCLeader = projectRoleManager.getProjectRole(Constants.PROJECT_ROLE_QC_LEAD);
        if (projectRolePM != null && projectRoleQCLeader != null) {
            return projectRoleManager.isUserInProjectRole(
                    ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), projectRolePM, project) ||
                   projectRoleManager.isUserInProjectRole(
                           ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), projectRoleQCLeader,
                           project);
        }

        return false;
    }
}
