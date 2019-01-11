package com.cmcglobal.plugins.jira.validators;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.cmcglobal.plugins.utils.Constants;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class FieldValidatorTask implements Validator {

    private static final Logger logger = LoggerFactory.getLogger(FieldValidatorTask.class);

    public void validate(Map transientVars, Map args, PropertySet ps) throws InvalidInputException {
        final String[] selectedCustomFields = ((String) args.get(
                Constants.VALIDATOR_DESCRIPTOR_ARGS_CUSTOM_FIELD)).split(Constants.COMMA);
        final Issue issue = (Issue) transientVars.get("issue");
        final ErrorCollection error = new SimpleErrorCollection();
        CustomField customField;
        Object value;
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
            }
        }
        if (error.getErrors().size() != 0) {
            throw new InvalidInputException(error.getErrors());
        }
    }
}
