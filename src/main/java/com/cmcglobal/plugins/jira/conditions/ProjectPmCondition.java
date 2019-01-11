package com.cmcglobal.plugins.jira.conditions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Helper;

import java.util.Collection;

public class ProjectPmCondition extends AbstractWebCondition {
    @Override
    public boolean shouldDisplay(final ApplicationUser applicationUser, final JiraHelper jiraHelper) {
        final ProjectRoleManager prm = ComponentAccessor.getComponent(ProjectRoleManager.class);
        final Collection<ProjectRole> projectRoles = prm.getProjectRoles(applicationUser, Helper.getCurrentProject(jiraHelper));
        if (projectRoles.size() > 0) {
            for (final ProjectRole projectRole : projectRoles) {
                if (Constants.ROLE_PM.equals(projectRole.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
