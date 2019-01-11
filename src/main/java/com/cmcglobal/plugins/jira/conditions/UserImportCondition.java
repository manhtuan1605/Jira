package com.cmcglobal.plugins.jira.conditions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.utils.Constants;

public class UserImportCondition extends AbstractWebCondition {
    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        return applicationUser != null &&
               ComponentAccessor.getGroupManager().isUserInGroup(applicationUser, Constants.GROUP_LEADER);
    }
}
