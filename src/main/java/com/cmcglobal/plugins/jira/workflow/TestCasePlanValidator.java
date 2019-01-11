package com.cmcglobal.plugins.jira.workflow;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.cmcglobal.plugins.utils.Constants;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Map;

public class TestCasePlanValidator implements Validator {
    private static final Logger logger = LoggerFactory.getLogger(TestCasePlanValidator.class);

    public void validate(Map transientVars, Map args, PropertySet ps) throws InvalidInputException {
        logger.info("Validate start date and end date");

        Issue issue = (Issue) transientVars.get(Constants.VALIDATOR_DESCRIPTOR_ARGS_ISSUE);
        final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
        CustomField startDateCF = customFieldManager.getCustomFieldObject(
                (String) args.get(Constants.VALIDATOR_DESCRIPTOR_CF_START_DATE));
        CustomField endDateCF = customFieldManager.getCustomFieldObject(
                (String) args.get(Constants.VALIDATOR_DESCRIPTOR_CF_END_DATE));

        Object timeStampStartDate = issue.getCustomFieldValue(startDateCF);
        Object timeStampEndDate = issue.getCustomFieldValue(endDateCF);

        /*if (timeStampStartDate == null || timeStampEndDate == null) {
            throw new InvalidInputException(Constants.VALIDATOR_START_DATE_AND_END_DATE_REQUIRED);
        }*/

        if (timeStampStartDate != null && timeStampEndDate != null) {
            LocalDate startDateValue = ((Timestamp) timeStampStartDate).toLocalDateTime().toLocalDate();
            LocalDate endDateValue = ((Timestamp) timeStampEndDate).toLocalDateTime().toLocalDate();
            if (startDateValue.isAfter(endDateValue)) {
                throw new InvalidInputException(Constants.VALIDATOR_START_DATE_AND_END_DATE_COMPARE);
            }
        }
    }
}
