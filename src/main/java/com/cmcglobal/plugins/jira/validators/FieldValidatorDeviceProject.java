package com.cmcglobal.plugins.jira.validators;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.operator.Operator;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Utilities;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FieldValidatorDeviceProject implements Validator {

    private static final Logger logger = LoggerFactory.getLogger(FieldValidatorDeviceProject.class);

    public void validate(Map transientVars, Map args, PropertySet ps) throws InvalidInputException {
        Issue issue = (Issue) transientVars.get("issue");
        IssueType issueType = issue.getIssueType();
        if (issueType != null && issueType.getName().equalsIgnoreCase(Constants.ISSUE_TYPE_EXTERNAL_DEVICE)) {
            final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
            JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
            SearchService searchService = ComponentAccessor.getComponentOfType(SearchService.class);
            ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
            PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();

            //CHECK DEVICE EXISTED
            checkDeviceExisted(issue, jqlClauseBuilder, searchService, pagerFilter, user, customFieldManager);

            //CHECK DUPLICATE BLUETOOTH
            isDuplicated(issue, args.get(Constants.VALIDATOR_DESCRIPTOR_ARGS_LIST_BLUETOOTH), "Bluetooth",
                         customFieldManager);

            //CHECK DUPLICATE USB
            isDuplicated(issue, args.get(Constants.VALIDATOR_DESCRIPTOR_ARGS_LIST_USB), "USB", customFieldManager);

            //CHECK EXTERNAL DEVICE ID EXISTED
            jqlClauseBuilder.clear();
            checkExternalDeviceIdExisted(issue, jqlClauseBuilder, searchService, pagerFilter, user, customFieldManager);
        }
    }

    private void isDuplicated(Issue issue, Object argsListCustomFields, String device,
                              CustomFieldManager customFieldManager) throws InvalidInputException {
        List<String> listValues = new ArrayList<>();
        CustomField tempCustomField;
        final String[] customFields = ((String) argsListCustomFields).split(Constants.COMMA);
        for (final String customField : customFields) {
            tempCustomField = customFieldManager.getCustomFieldObject(customField);
            if (tempCustomField != null && issue.getCustomFieldValue(tempCustomField) != null) {
                listValues.add(issue.getCustomFieldValue(tempCustomField).toString());
            }
        }
        Set<String> setValues = new HashSet<>(listValues);

        if (setValues.size() < listValues.size()) {
            throw new InvalidInputException(device + " device duplicated!");
        }
    }

    private void checkDeviceExisted(Issue issue, JqlClauseBuilder jqlClauseBuilder, SearchService searchService,
                                    PagerFilter pagerFilter, ApplicationUser user,
                                    CustomFieldManager customFieldManager) throws InvalidInputException {
        List<String> listPeripherals = new ArrayList<>();
        Long projectId = issue.getProjectId();
        Query query = jqlClauseBuilder.project(projectId)
                                      .and()
                                      .issueType(Constants.ISSUE_TYPE_PERIPHERAL_DEVICE)
                                      .buildQuery();
        try {
            SearchResults searchResults = searchService.search(user, query, pagerFilter);
            List<Issue> tempIssues = searchResults.getIssues();
            CustomField customFieldCmcCode = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_CMC_CODE);
            for (Issue tempIssue : tempIssues) {
                if (tempIssue.getCustomFieldValue(customFieldCmcCode) != null) {
                    listPeripherals.add(tempIssue.getCustomFieldValue(customFieldCmcCode).toString());
                }
            }
        } catch (SearchException e) {
            logger.error(e.getMessage());
            throw new InvalidInputException(Constants.ERR_SEARCH_QUERY);
        }
        CustomField customField;
        final ErrorCollection error = new SimpleErrorCollection();
        for (String attribute : Utilities.listExternalDevice()) {
            if (!attribute.equalsIgnoreCase(Constants.EXTERNAL_DEVICE)) {
                customField = customFieldManager.getCustomFieldObjectByName(attribute);
                if (customField != null && issue.getCustomFieldValue(customField) != null &&
                    !listPeripherals.contains(issue.getCustomFieldValue(customField).toString())) {
                    error.addError(customField.getId(), "Device on " + customField.getName() + " not existed.",
                                   ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
        }
        if (error.getErrors().size() != 0) {
            throw new InvalidInputException(error.getErrors());
        }
    }

    private void checkExternalDeviceIdExisted(Issue issue, JqlClauseBuilder jqlClauseBuilder,
                                              SearchService searchService, PagerFilter pagerFilter,
                                              ApplicationUser user, CustomFieldManager customFieldManager)
            throws InvalidInputException {
        Map<String, String> listValues = new HashMap<>();
        Object object;
        for (String attribute : Utilities.listExternalDevice()) {
            if (!attribute.equalsIgnoreCase(Constants.EXTERNAL_DEVICE)) {
                object = issue.getCustomFieldValue(customFieldManager.getCustomFieldObjectByName(attribute));
                listValues.put(attribute, object != null ? object.toString() : null);
            }
        }
        Long projectId = issue.getProjectId();
        jqlClauseBuilder.project(projectId).and().issueType(Constants.ISSUE_TYPE_EXTERNAL_DEVICE);
        listValues.forEach((attribute, value) -> {
            if (value == null) {
                jqlClauseBuilder.and().addEmptyCondition(attribute);
            } else {
                jqlClauseBuilder.and().addStringCondition(attribute, Operator.LIKE, value);
            }
        });
        Query query = jqlClauseBuilder.buildQuery();
        try {
            SearchResults searchResults = searchService.search(user, query, pagerFilter);
            List<Issue> tempIssues = searchResults.getIssues();
            if (!tempIssues.isEmpty()) {
                throw new InvalidInputException("External Device ID: " + tempIssues.iterator()
                                                                                   .next()
                                                                                   .getCustomFieldValue(
                                                                                           customFieldManager.getCustomFieldObjectByName(
                                                                                                   Constants.EXTERNAL_DEVICE))
                                                                                   .toString());
            }
        } catch (SearchException e) {
            logger.error(e.getMessage());
            throw new InvalidInputException(Constants.ERR_SEARCH_QUERY);
        }
    }

}
