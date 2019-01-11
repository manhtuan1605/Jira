package com.cmcglobal.plugins.jira.webpanel;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DeviceTeamsContextProvider extends AbstractJiraContextProvider {
    public Map<String, Object> getContextMap(final ApplicationUser applicationUser, final JiraHelper jiraHelper) {
        final Map<String, Object> contextMap = new HashMap<>();
        final Project currentProject = Helper.getCurrentProject(jiraHelper);
        contextMap.put(Constants.STRING_BASER_URL,
                       ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL));
        contextMap.put(Constants.STRING_PID, currentProject.getId());
        contextMap.put(Constants.STRING_QC_LEADERS, getSelectedProjectQcLeaders(currentProject));
        return contextMap;
    }

    private Set<ApplicationUser> getSelectedProjectQcLeaders(final Project currentProject) {
        final ProjectRoleManager projectRoleManager = ComponentManager.getComponentInstanceOfType(
                ProjectRoleManager.class);
        final ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(
                Helper.getProjectRoleByRoleName(Constants.STRING_QC_LEADER), currentProject);
        return projectRoleActors.getUsers();
    }
}
