package com.cmcglobal.plugins.jira.conditions;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Helper;

import java.util.Collection;

public class IsProjectTest extends AbstractWebCondition {
    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        final Collection<IssueType> issueTypes = Helper.getCurrentProject(jiraHelper).getIssueTypes();
        for (IssueType issueType : issueTypes) {
            if (Constants.ISSUE_TYPE_TESTCASE_NAME.equals(issueType.getName())) {
                return true;
            }
        }
        return false;
    }
}
