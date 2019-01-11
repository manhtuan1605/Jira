package com.cmcglobal.plugins.jira.postfunctions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.cmcglobal.plugins.service.IssueHelperService;
import com.opensymphony.module.propertyset.PropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * This is the post-function class that gets executed at the end of the transition.
 * Any parameters that were saved in your factory class will be available in the transientVars Map.
 */
public class TestcasePostFunction extends AbstractJiraFunctionProvider {

    @Autowired
    private              IssueHelperService issueHelperService;
    private static final Logger             log = LoggerFactory.getLogger(TestcasePostFunction.class);

    @Override
    public void execute(final Map transientVars, final Map args, final PropertySet ps) {
        final boolean isSuccess = issueHelperService.createOrUpdateIssue(getIssue(transientVars), true);
        if (!isSuccess) {
            try {
                ComponentAccessor.getIssueManager().deleteIssueNoEvent((Issue)getIssue(transientVars));
            } catch (RemoveException e) {
                log.error("Delete issue error: {}", e.getMessage());
            }
        }
    }
}