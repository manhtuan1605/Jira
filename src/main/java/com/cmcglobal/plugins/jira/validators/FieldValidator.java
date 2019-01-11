package com.cmcglobal.plugins.jira.validators;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.workflow.ImmutableWorkflowDescriptor;
import com.atlassian.query.Query;
import com.atlassian.query.operator.Operator;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Utilities;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FieldValidator implements Validator {

    private static final Logger logger = LoggerFactory.getLogger(FieldValidator.class);

    @Override
    public void validate(final Map transientVars, final Map args, final PropertySet ps) throws InvalidInputException {
        final String[] selectedCustomFields = ((String) args.get(
                Constants.VALIDATOR_DESCRIPTOR_ARGS_CUSTOM_FIELD)).split(Constants.COMMA);
        final Issue issue = (Issue) transientVars.get("issue");
        final ErrorCollection error = new SimpleErrorCollection();
        CustomField customField;
        Object value, value2, value1, value3;

        final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
        for (final String selectedCustomField : selectedCustomFields) {
            customField = customFieldManager.getCustomFieldObject(selectedCustomField);
            if (customField != null) {
                // thuc hien validate
                value = issue.getCustomFieldValue(customField);
                if (value == null) {
                    error.addError(customField.getId(),
                            "You must specify a " + customField.getName() + " of the issue.",
                            ErrorCollection.Reason.VALIDATION_FAILED);
                }
                if(customField.equals(Constants.PENDING_TYPE_CF)){
                    value = issue.getCustomFieldValue(customField);
                    value1 = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(Constants.ERR_KEY_EVALUATION_SOFTWARE));
                    value2 = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(Constants.ERR_KEY_EVALUATION_HARD));
                    value3 = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(Constants.ERR_KEY_PERIPHERAL_DEVICE_NUMBER));

                    if(value == "PN(Bug)"){
                        if(value1 == null || value2 ==null || value3 ==null){
                            error.addError(customField.getId(),
                                    "You must specify a " + customField.getName() + " of the issue.",
                                    ErrorCollection.Reason.VALIDATION_FAILED);
                        }
                    }
                }

            }

        }
        if (error.getErrors().size() != 0) {
            throw new InvalidInputException(error.getErrors());
        } else {
            postFunction(transientVars, issue, customFieldManager);
        }
    }

    private void postFunction(final Map transientVars, final Issue issue, final CustomFieldManager customFieldManager)
            throws InvalidInputException {
        final int actionId = (int) transientVars.get("actionId");
        final ImmutableWorkflowDescriptor immutableWorkflowDescriptor = (ImmutableWorkflowDescriptor) transientVars.get(
                "descriptor");
        final ActionDescriptor action = immutableWorkflowDescriptor.getAction(actionId);
        final String actionName = action.getName();
        switch (actionName) {
            case Constants.WORKFLOW_TESTCASE_ACTION_CREATE:
                createTestcase(issue, customFieldManager);
                break;
            case Constants.WORKFLOW_TESTCASE_ACTION_OK:
                okTestcase(issue, customFieldManager);
                break;
            case Constants.WORKFLOW_TESTCASE_ACTION_PENDING:
                nokTestcase(issue, Constants.TEST_RESULT_PN, true, customFieldManager);
                break;
            case Constants.WORKFLOW_TESTCASE_ACTION_NG:
                nokTestcase(issue, Constants.TEST_RESULT_NG, false, customFieldManager);
                break;
            case Constants.WORKFLOW_TESTCASE_ACTION_OUT_OF_SCOPE:
                nokTestcase(issue, null, true, customFieldManager);
                break;
            default:
                break;
        }
    }

    private void createTestcase(final Issue issue, final CustomFieldManager customFieldManager)
            throws InvalidInputException {
        final PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
        final JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        final SearchService searchService = ComponentAccessor.getComponentOfType(SearchService.class);
        final Query query = jqlClauseBuilder.project(issue.getProjectId())
                .and()
                .issueType(Constants.ISSUE_TYPE_TESTCASE_NAME)
                .buildQuery();
        final SearchResults searchResults;
        try {
            //VALIDATE Testcase No
            final String phase = issue.getCustomFieldValue(
                    customFieldManager.getCustomFieldObjectByName(Constants.CUSTOM_FIELD_PHASE)).toString();
            final String testcaseNo = issue.getCustomFieldValue(
                    customFieldManager.getCustomFieldObjectByName(Constants.CUSTOM_FIELD_TESTCASE_NO)).toString();
            searchResults = searchService.search(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(),
                    query, pagerFilter);
            final List<Issue> tempIssues = searchResults.getIssues();
            final CustomField customFieldPhase = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_PHASE);
            final CustomField customFieldTestcaseNo = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_TESTCASE_NO);
            for (final Issue i : tempIssues) {
                if (i.getCustomFieldValue(customFieldTestcaseNo) != null &&
                        i.getCustomFieldValue(customFieldTestcaseNo).toString().equals(testcaseNo) &&
                        i.getCustomFieldValue(customFieldPhase).toString().equals(phase)) {
                    throw new InvalidInputException("Testcase No is invalid. It already existed in phase: " + phase);
                }
            }
        } catch (final SearchException e) {
            logger.error("Validate Testcase No error: {}", e.getMessage());
            throw new InvalidInputException(Constants.ERR_SEARCH_QUERY);
        }
    }

    private void okTestcase(final Issue issue, final CustomFieldManager customFieldManager)
            throws InvalidInputException {
        CustomField customField;
        final List<String> listTestResultCustomFields = Utilities.listTestResultAttribute();
        ErrorCollection error = new SimpleErrorCollection();
        for (String attribute : listTestResultCustomFields) {
            customField = customFieldManager.getCustomFieldObjectByName(attribute);
            if (customField != null) {
                final Object objValue = issue.getCustomFieldValue(customField);
                if (attribute.equals(Constants.CUSTOM_FIELD_EXECUTE_COUNT)) {
                    //ADD EXECUTE COUNT BY 1
                    calculateExecuteCount(issue, customField);
                } else if (attribute.equals(Constants.CUSTOM_FIELD_LATEST_RESULT)) {
                    //UPDATE LAST RECENT TEST AND LATEST RESULT
                    updateLatestResult(issue, customField, Constants.TEST_RESULT_OK, false, customFieldManager);
                } else if (attribute.equals(Constants.CUSTOM_FIELD_TEST_DURATION)) {
                    //VALIDATE NUMBER FIELD GREATER THAN -1
                    validateNumberField(issue, customField, error);
                } else if (attribute.equals(Constants.CUSTOM_FIELD_EXTERNAL_TEST_DEVICE_ID)) {
                    //VALIDATE NUMBER FIELD GREATER THAN -1
                    // validateNumberField(issue, customField, error);
                    //VALIDATE EXTERNAL DEVICE ID EXISTED
                    // validateExternalDeviceIdExisted(issue, customField, error);
                } else if ((customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_TEXT) &&
                        !attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_LAST_RECENT_TEST) && !attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_RTC_CODE)) ||
                        (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_NUMBER) && !attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_RTC_CODE)) ) {
                    //CLEAR INPUT WHEN CHANGING STATUS
                    customField.updateValue(null, issue, new ModifiedValue(objValue, null),
                            new DefaultIssueChangeHolder());
                } else if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_SELECT)) {
                    //CLEAR INPUT WHEN CHANGING STATUS
                    customField.updateValue(null, issue, new ModifiedValue(objValue,
                                    ComponentAccessor.getOptionsManager()
                                            .findByOptionId(-1L)),
                            new DefaultIssueChangeHolder());
                }
            }
        }
        if (error.getErrors().size() != 0) {
            throw new InvalidInputException(error.getErrors());
        }
    }

    private void nokTestcase(final Issue issue, final String pendingType, final boolean isOutOfScope,
                             final CustomFieldManager customFieldManager) throws InvalidInputException {
        CustomField customField;
        final List<String> listTestResultCustomFields = Utilities.listTestResultAttribute();
        ErrorCollection error = new SimpleErrorCollection();
        for (String attribute : listTestResultCustomFields) {
            if (attribute.equals(Constants.CUSTOM_FIELD_TIME_FOR_QA_SUBMISSION)) {
                attribute = Constants.CUSTOM_FIELD_TIME_FOR_QNA_SUBMISSION;
            }
            customField = attribute.equals(Constants.VEHICLE_ATTRIBUTE) ?
                    getCustomFieldTestcase(Constants.VEHICLE_ATTRIBUTE) :
                    customFieldManager.getCustomFieldObjectByName(attribute);
            if (customField != null) {
                final Object objValue = issue.getCustomFieldValue(customField);
                if (attribute.equals(Constants.CUSTOM_FIELD_EXECUTE_COUNT)) {
                    //ADD EXECUTE COUNT BY 1
                    calculateExecuteCount(issue, customField);
                } else if (attribute.equals(Constants.CUSTOM_FIELD_LATEST_RESULT)) {
                    //UPDATE LAST RECENT TEST AND LATEST RESULT
                    updateLatestResult(issue, customField, pendingType, isOutOfScope, customFieldManager);
                } else if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_NUMBER)) {
                    //VALIDATE NUMBER FIELD GREATER THAN -1
                    validateNumberField(issue, customField, error);
                    //VALIDATE EXTERNAL DEVICE ID EXISTED
                    if (attribute.equals(Constants.CUSTOM_FIELD_EXTERNAL_TEST_DEVICE_ID)) {
                        // validateExternalDeviceIdExisted(issue, customField, error);
                    }
                    //CLEAR INPUT WHEN CHANGING STATUS
                    customField.updateValue(null, issue, new ModifiedValue(objValue, null),
                            new DefaultIssueChangeHolder());
                } else if ((customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_TEXT) &&
                        !attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_LAST_RECENT_TEST))) {
                    //CLEAR INPUT WHEN CHANGING STATUS
                    customField.updateValue(null, issue, new ModifiedValue(objValue, null),
                            new DefaultIssueChangeHolder());
                } else if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_SELECT)) {
                    //CLEAR INPUT WHEN CHANGING STATUS
                    customField.updateValue(null, issue, new ModifiedValue(objValue,
                                    ComponentAccessor.getOptionsManager()
                                            .findByOptionId(-1L)),
                            new DefaultIssueChangeHolder());
                }
            }
        }
        if (error.getErrors().size() != 0) {
            throw new InvalidInputException(error.getErrors());
        }
    }

    private void calculateExecuteCount(Issue issue, CustomField customField) {
        final Object objCount = issue.getCustomFieldValue(customField);
        final String strCount = objCount == null ? "1" : String.valueOf(Integer.valueOf(objCount.toString()) + 1);
        customField.updateValue(null, issue, new ModifiedValue(objCount, strCount), new DefaultIssueChangeHolder());
    }

    private void updateLatestResult(Issue issue, CustomField customField, final String pendingType,
                                    final boolean isOutOfScope, CustomFieldManager customFieldManager) {
        final Object objLatestResult = issue.getCustomFieldValue(customField);
        if (objLatestResult != null) {
            final CustomField customFieldLastRecentTest = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_LAST_RECENT_TEST);
            final Object objLastRecentTest = issue.getCustomFieldValue(customFieldLastRecentTest);
            customFieldLastRecentTest.updateValue(null, issue,
                    new ModifiedValue(objLastRecentTest, objLatestResult.toString()),
                    new DefaultIssueChangeHolder());
        }
        final String strResult = isOutOfScope ?
                issue.getCustomFieldValue(customFieldManager.getCustomFieldObjectByName(
                        Constants.CUSTOM_FIELD_PENDING_TYPE)).toString() :
                pendingType;

        List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();
        if (!schemes.isEmpty()) {
            FieldConfigScheme scheme = schemes.get(0);
            Map configs = scheme.getConfigsByConfig();
            if (configs != null && !configs.isEmpty()) {
                FieldConfig config = (FieldConfig) configs.keySet().iterator().next();
                List<Option> options = ComponentManager.getComponentInstanceOfType(OptionsManager.class)
                        .getOptions(config);
                for (Option option : options) {
                    if (option.getValue().equalsIgnoreCase(strResult)) {
                        customField.updateValue(null, issue, new ModifiedValue(objLatestResult, option),
                                new DefaultIssueChangeHolder());
                        break;
                    }
                }
            }
        }
    }

    private void validateNumberField(Issue issue, CustomField customField, ErrorCollection error) {
        final Object customFieldValue = issue.getCustomFieldValue(customField);
        if (customFieldValue != null) {
            final Float number = Float.valueOf(issue.getCustomFieldValue(customField).toString());
            if (number < 0) {
                error.addError(customField.getId(), "'" + number + "' is too small. Minimum value allowed is 0",
                        ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }
    }

    private void validateExternalDeviceIdExisted(Issue issue, CustomField customField, ErrorCollection error) {
        final Object customFieldValue = issue.getCustomFieldValue(customField);
        if (customFieldValue != null) {
            final PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
            final JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
            final SearchService searchService = ComponentAccessor.getComponentOfType(SearchService.class);
            final Query query = jqlClauseBuilder.issueType(Constants.ISSUE_TYPE_EXTERNAL_DEVICE)
                    .and()
                    .addStringCondition(Constants.EXTERNAL_DEVICE, Operator.LIKE,
                            String.valueOf(Math.round(Double.valueOf(
                                    customFieldValue.toString()))))
                    .buildQuery();
            final SearchResults searchResults;
            try {
                searchResults = searchService.search(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(),
                        query, pagerFilter);
                final List<Issue> tempIssues = searchResults.getIssues();
                if (tempIssues.isEmpty()) {
                    error.addError(customField.getId(), "External Device ID is not existed.",
                            ErrorCollection.Reason.VALIDATION_FAILED);
                }
            } catch (SearchException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private CustomField getCustomFieldTestcase(String attribute) {
        CustomField customFieldTestcase = null;
        Collection<CustomField> customFields = ComponentAccessor.getCustomFieldManager()
                .getCustomFieldObjectsByName(attribute);
        for (CustomField customField : customFields) {
            if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_TEXT)) {
                customFieldTestcase = customField;
            }
        }

        return customFieldTestcase;
    }

}
