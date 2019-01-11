package com.cmcglobal.plugins.jira.conditions;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Helper;

import java.util.Arrays;
import java.util.Collection;

public class ProjectDeviceCondition extends AbstractWebCondition {

    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        final String[] issueTypesName = { Constants.ISSUE_TYPE_AUTOMOTIVE_DEVICE,
                                          Constants.ISSUE_TYPE_PERIPHERAL_DEVICE,
                                          Constants.ISSUE_TYPE_EXTERNAL_DEVICE };
        final Collection<IssueType> issueTypes = Helper.getCurrentProject(jiraHelper).getIssueTypes();
        for (IssueType issueType : issueTypes) {
            if (!Arrays.asList(issueTypesName).contains(issueType.getName())) {
                return false;
            }
        }
        return true;
    }
}
