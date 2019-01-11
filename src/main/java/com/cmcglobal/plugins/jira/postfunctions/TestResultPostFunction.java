package com.cmcglobal.plugins.jira.postfunctions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.external.ActiveObjectsModuleMetaData;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.cmcglobal.plugins.entity.TestResultHistory;
import com.cmcglobal.plugins.utils.Constants;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

import net.java.ao.DBParam;
import net.java.ao.EntityStreamCallback;
import net.java.ao.Query;
import net.java.ao.RawEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * This is the post-function class that gets executed at the end of the transition.
 * Any parameters that were saved in your factory class will be available in the transientVars Map.
 */
public class TestResultPostFunction extends AbstractJiraFunctionProvider
{
    @ComponentImport
    private ActiveObjects ao;

    private static final Logger log = LoggerFactory.getLogger(TestResultPostFunction.class);

    @Inject
    public TestResultPostFunction(final ActiveObjects ao) {
        this.ao = ao;
    }

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException
    {
        MutableIssue issue = getIssue(transientVars);
        CustomField customFieldLatestResult = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(Constants.CUSTOM_FIELD_LATEST_RESULT);
        CustomField customFieldLastRecentTest = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(Constants.CUSTOM_FIELD_LAST_RECENT_TEST);
        CustomField customFieldImplementDate = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(Constants.CUSTOM_FIELD_DATE_OF_IMPLEMENTATION);
        TestResultHistory testResultHistory = ao.create(TestResultHistory.class);
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat(Constants.FORMATTER_YYYY_MM_DD_HH_MM_SS);
        DateFormat dateOfImplementationFormat = new SimpleDateFormat(Constants.PATTERM_DATE);
        String strDate = dateFormat.format(date);
        testResultHistory.setCreatedDate(strDate);
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        if (customFieldLastRecentTest.getValue(issue) != null) {
            testResultHistory.setLastRecentTest(customFieldLastRecentTest.getValue(issue).toString());
        }
        if (customFieldImplementDate.getValue(issue) != null) {
            try {
                testResultHistory.setDateOfImplementation(dateOfImplementationFormat.parse(customFieldImplementDate.getValue(issue).toString()));
            } catch (ParseException e) {
            }
        }
        if (customFieldLatestResult.getValue(issue) != null) {
            testResultHistory.setLatestResult(customFieldLatestResult.getValue(issue).toString());
        }
        testResultHistory.setTestCaseName(issue.getSummary());
        testResultHistory.setIssueId(issue.getId());
        testResultHistory.save();
    }
}