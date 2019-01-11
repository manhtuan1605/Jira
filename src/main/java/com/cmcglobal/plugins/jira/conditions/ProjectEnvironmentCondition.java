package com.cmcglobal.plugins.jira.conditions;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.cmcglobal.plugins.utils.Constants;

import java.util.Collection;

public class ProjectEnvironmentCondition extends AbstractWebCondition {
    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        final Collection<IssueType> issueTypes = getProject(applicationUser).getIssueTypes();
        for (IssueType issueType : issueTypes) {
            if (issueTypes.size() == 6 && Constants.SET_OF_DEVICE.equals(issueType.getName())) {
                return true;
            }

        }
        return false;
    }

    Project getProject(ApplicationUser applicationUser) {
        UserProjectHistoryManager userProjectHistoryManager = ComponentManager.getComponentInstanceOfType(
                UserProjectHistoryManager.class);
        Project selectedProject = userProjectHistoryManager.getCurrentProject(
                com.atlassian.jira.security.Permissions.PROJECT_ADMIN, applicationUser);

        return selectedProject;
    }
}
