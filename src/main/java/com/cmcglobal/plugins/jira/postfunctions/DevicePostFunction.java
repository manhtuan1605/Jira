package com.cmcglobal.plugins.jira.postfunctions;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.atlassian.query.Query;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Helper;
import com.opensymphony.module.propertyset.PropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * This is the post-function class that gets executed at the end of the transition.
 * Any parameters that were saved in your factory class will be available in the transientVars Map.
 */
public class DevicePostFunction extends AbstractJiraFunctionProvider {
    private static final Logger logger = LoggerFactory.getLogger(DevicePostFunction.class);

    public void execute(Map transientVars, Map args, PropertySet ps) {
        MutableIssue issue = getIssue(transientVars);
        final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
        final JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        SearchService searchService = ComponentAccessor.getComponentOfType(SearchService.class);
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
        Long projectId = issue.getProjectId();
        Query query = jqlClauseBuilder.project(projectId)
                                      .and()
                                      .issueType(Constants.ISSUE_TYPE_EXTERNAL_DEVICE)
                                      .buildQuery();
        try {
            SearchResults searchResults = searchService.search(user, query, pagerFilter);
            List<Issue> tempIssues = searchResults.getIssues();
            CustomField customFieldExternalDeviceId = customFieldManager.getCustomFieldObjectByName(
                    Constants.EXTERNAL_DEVICE);
            customFieldExternalDeviceId.updateValue(null, issue, new ModifiedValue(
                                                            issue.getCustomFieldValue(customFieldExternalDeviceId), String.valueOf(tempIssues.size())),
                                                    new DefaultIssueChangeHolder());
            //RE-INDEX ISSUE
            Helper.reIndexIssue(issue);
        } catch (SearchException e) {
            logger.error(e.getMessage());
        }
    }
}