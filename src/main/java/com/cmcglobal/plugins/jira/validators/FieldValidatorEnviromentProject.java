package com.cmcglobal.plugins.jira.validators;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
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
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FieldValidatorEnviromentProject implements Validator {

    private static final Logger logger = LoggerFactory.getLogger(FieldValidatorEnviromentProject.class);

    public void validate(Map transientVars, Map args, PropertySet ps) throws InvalidInputException {
        Issue matchHuDevice = null;
        Issue matchScreenDevice = null;
        CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
        ErrorCollection error = new SimpleErrorCollection();
        ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        SearchService searchService = ComponentAccessor.getComponentOfType(SearchService.class);
        Issue createIssue = (Issue) transientVars.get("issue");
        List<CustomField> listCustomFieldNotNull = new ArrayList<>();
        CustomField customFieldAmpType = null;
        Collection<CustomField> customFieldAmpTypes = customFieldManager.getCustomFieldObjectsByName(
                Constants.AMP_TYPE_ATTRIBUTE);
        for (CustomField customField : customFieldAmpTypes) {
            if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_SELECT)) {
                customFieldAmpType = customField;
            }
        }
        switch (createIssue.getIssueType().getName()) {
            case Constants.HU_DEVICE:
                CustomField customFieldDeviceType = customFieldManager.getCustomFieldObjectByName(
                        Constants.DEVICE_TYPE_ATTRIBUTE);
                CustomField customFieldDeviceSubType = customFieldManager.getCustomFieldObjectByName(
                        Constants.DEVICE_SUB_TYPE_ATTRIBUTE);
                listCustomFieldNotNull.add(customFieldAmpType);
                listCustomFieldNotNull.add(customFieldDeviceType);
                listCustomFieldNotNull.add(customFieldDeviceSubType);
                break;
            case Constants.SCREEN_DEVICE:
                CustomField customFieldScreenId = customFieldManager.getCustomFieldObjectByName(
                        Constants.DEVICE_ID_ATTRIBUTE);
                CustomField customFieldScreenDeviceType = customFieldManager.getCustomFieldObjectByName(
                        Constants.CUSTOM_FIELD_SCREEN_DEVICE_TYPE);
                CustomField customFieldScreenName = customFieldManager.getCustomFieldObjectByName(
                        Constants.DEVICE_NAME_ATTRIBUTE);
                listCustomFieldNotNull.add(customFieldScreenId);
                listCustomFieldNotNull.add(customFieldScreenDeviceType);
                listCustomFieldNotNull.add(customFieldScreenName);
                break;
            case Constants.SI_AMIGO_INFO:
                CustomField customFieldAmigoPgVersion = customFieldManager.getCustomFieldObjectByName(
                        Constants.AMIGO_PG_VERSION_ATTRIBUTE);
                CustomField customFieldAmigoDataVersion = customFieldManager.getCustomFieldObjectByName(
                        Constants.AMIGO_DATA_VERSION_ATTRIBUTE);
                listCustomFieldNotNull.add(customFieldAmigoPgVersion);
                listCustomFieldNotNull.add(customFieldAmigoDataVersion);
                listCustomFieldNotNull.add(customFieldAmpType);
                break;
            case Constants.SI_SYS_INFO:
                CustomField customFieldSysSetIdVersion = customFieldManager.getCustomFieldObjectByName(
                        Constants.SYS_SET_ID_VERSION_ATTRIBUTE);
                CustomField customFieldSysDataVersion = customFieldManager.getCustomFieldObjectByName(
                        Constants.SYS_DATA_VERSION_ATTRIBUTE);
                CustomField customFieldSysSoft = customFieldManager.getCustomFieldObjectByName(
                        Constants.SYS_SOFT_ATTRIBUTE);
                listCustomFieldNotNull.add(customFieldSysSetIdVersion);
                listCustomFieldNotNull.add(customFieldSysDataVersion);
                listCustomFieldNotNull.add(customFieldSysSoft);
                break;
            case Constants.HU_TYPE_VEHICLE:
                CustomField customFieldSysDataPrefix = customFieldManager.getCustomFieldObjectByName(
                        Constants.SYS_DATA_PREFIX_ATTRIBUTE);
                CustomField customFieldTestHuMarket = customFieldManager.getCustomFieldObjectByName(
                        Constants.TEST_HU_MARKET_ATTRIBUTE);
                listCustomFieldNotNull.add(customFieldSysDataPrefix);
                listCustomFieldNotNull.add(customFieldTestHuMarket);
                break;
            default:
                break;
        }
        if (!listCustomFieldNotNull.isEmpty()) {
            for (CustomField customField : listCustomFieldNotNull) {
                if (createIssue.getCustomFieldValue(customField) == null) {
                    error.addError(customField.getId(),
                                   Constants.YOU_MUST_SPECIFY_A + customField.getName() + Constants.OF_THE_ISSUE,
                                   ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
        }
        Query query = jqlClauseBuilder.project(createIssue.getProjectId()).buildQuery();
        try {
            // if create issue is set of device
            if (createIssue.getIssueType().getName().equalsIgnoreCase(Constants.SET_OF_DEVICE)) {
                List<Issue> tempIssues;
                SearchResults searchResults = searchService.search(currentUser, query, pagerFilter);
                tempIssues = searchResults.getIssues();

                CustomField customFieldModel = customFieldManager.getCustomFieldObjectByName(
                        Constants.MODEL);
                if (createIssue.getCustomFieldValue(customFieldModel) == null) {
                    error.addError(customFieldModel.getId(),
                                   Constants.YOU_MUST_SPECIFY_A + customFieldModel.getName() +
                                   Constants.OF_THE_ISSUE, ErrorCollection.Reason.VALIDATION_FAILED);
                }
                // validate Hu device

                CustomField customFieldHuDevice = customFieldManager.getCustomFieldObjectByName(
                        Constants.HU_DEVICE_ATTRIBUTE);
                if (createIssue.getCustomFieldValue(customFieldHuDevice) == null) {
                    error.addError(customFieldHuDevice.getId(),
                                   Constants.YOU_MUST_SPECIFY_A + customFieldHuDevice.getName() +
                                   Constants.OF_THE_ISSUE, ErrorCollection.Reason.VALIDATION_FAILED);
                } else {
                    // run loop find Hu Device(set of device) = Hu Id (Hu Device)
                    CustomField customFieldHuId = customFieldManager.getCustomFieldObjectByName(
                            Constants.HU_ID_ATTRIBUTE);
                    boolean existHuid = false;
                    for (Issue issue : tempIssues) {
                        if ((issue.getIssueType().getName().equalsIgnoreCase(Constants.HU_DEVICE)) &&
                            (((String) createIssue.getCustomFieldValue(customFieldHuDevice)).equalsIgnoreCase(
                                    (String) issue.getCustomFieldValue(customFieldHuId)))) {
                            existHuid = true;
                            matchHuDevice = issue;
                            break;
                        }

                    }
                    if (!existHuid) {
                        error.addError(customFieldHuDevice.getId(), Constants.ERR_HU_DEVICE_NOT_EXIST,
                                       ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                }
                // validate Screen Device

                CustomField customFieldScreenDevice = customFieldManager.getCustomFieldObjectByName(
                        Constants.SCREEN_DEVICE_ATTRIBUTE);
                if (createIssue.getCustomFieldValue(customFieldScreenDevice) == null) {
                    error.addError(customFieldScreenDevice.getId(),
                                   Constants.YOU_MUST_SPECIFY_A + customFieldScreenDevice.getName() +
                                   Constants.OF_THE_ISSUE, ErrorCollection.Reason.VALIDATION_FAILED);

                } else {
                    // run loop for check screen device(set of device) == device id(Screen device)
                    boolean existDeviceId = false;
                    CustomField customFieldDeviceId = customFieldManager.getCustomFieldObjectByName(
                            Constants.DEVICE_ID_ATTRIBUTE);
                    for (Issue issue : tempIssues) {
                        if ((issue.getIssueType().getName().equalsIgnoreCase(Constants.SCREEN_DEVICE)) &&
                            (((String) createIssue.getCustomFieldValue(customFieldScreenDevice)).equalsIgnoreCase(
                                    (String) issue.getCustomFieldValue(customFieldDeviceId)))) {
                            existDeviceId = true;
                            matchScreenDevice = issue;
                            break;
                        }
                    }
                    if (!existDeviceId) {
                        error.addError(customFieldScreenDevice.getId(), Constants.ERR_SCREEN_DEVICE_NOT_EXIST,
                                       ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                }

                // Build Number
                // check build number and sy sys info

                CustomField customFieldBuildNumber = customFieldManager.getCustomFieldObjectByName(
                        Constants.BUILD_NUMBER_ATTRIBUTE);
                if (createIssue.getCustomFieldValue(customFieldBuildNumber) == null) {
                    error.addError(customFieldBuildNumber.getId(),
                                   Constants.YOU_MUST_SPECIFY_A + customFieldBuildNumber.getName() +
                                   Constants.OF_THE_ISSUE, ErrorCollection.Reason.VALIDATION_FAILED);

                } else {
                    // run loop for check screen device(set of device) == device id(Screen device)
                    boolean existSysAmigo = false;
                    CustomField customFieldDeviceType = customFieldManager.getCustomFieldObjectByName(
                            Constants.DEVICE_TYPE_ATTRIBUTE);
                    CustomField customFieldScreenDeviceType = customFieldManager.getCustomFieldObjectByName(
                            Constants.SCREEN_DEVICE_TYPE);
                    for (Issue issue : tempIssues) {
                        if ((issue.getIssueType().getName().equalsIgnoreCase(Constants.SI_AMIGO_INFO)) &&
                            (((String) createIssue.getCustomFieldValue(customFieldBuildNumber)).equalsIgnoreCase(
                                    (String) issue.getCustomFieldValue(customFieldBuildNumber))) &&
                            (matchHuDevice != null) &&
                            (((String) matchHuDevice.getCustomFieldValue(customFieldDeviceType)).equalsIgnoreCase(
                                    (String) issue.getCustomFieldValue(customFieldDeviceType))) &&
                            (matchHuDevice.getCustomFieldValue(customFieldAmpType) != null) &&
                            (((Option) matchHuDevice.getCustomFieldValue(customFieldAmpType)).getValue()
                                                                                             .equalsIgnoreCase(
                                                                                                     ((Option) issue.getCustomFieldValue(
                                                                                                             customFieldAmpType))
                                                                                                             .getValue()))) {
                            existSysAmigo = true;
                            break;
                        }
                    }

                    // check for build number voi si sys info
                    boolean existSiSys = false;
                    CustomField customFieldScreenType = customFieldManager.getCustomFieldObjectByName(
                            Constants.SCREEN_TYPE);
                    for (Issue issue : tempIssues) {
                        if ((issue.getIssueType().getName().equalsIgnoreCase(Constants.SI_SYS_INFO)) &&
                            (((String) createIssue.getCustomFieldValue(customFieldBuildNumber)).equalsIgnoreCase(
                                    (String) issue.getCustomFieldValue(customFieldBuildNumber))) &&
                            (matchScreenDevice != null) &&
                            (matchScreenDevice.getCustomFieldValue(customFieldScreenDeviceType) != null) &&
                            (((String) matchScreenDevice.getCustomFieldValue(
                                    customFieldScreenDeviceType)).equalsIgnoreCase(
                                    (String) issue.getCustomFieldValue(customFieldScreenType)))) {
                            existSiSys = true;
                            break;
                        }
                    }
                    if (!existSiSys || !existSysAmigo) {
                        String errorMessage = (!existSysAmigo) ? Constants.ERR_SI_AMIGO_NOT_EXIST : "";
                        errorMessage = errorMessage + ((!existSiSys) ? Constants.ERR_SI_SYS_NOT_EXIST : "");
                        error.addError(customFieldBuildNumber.getId(), errorMessage,
                                       ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                }

                // vehicle parameter
                CustomField customFieldVehicleParameter = null;
                Collection<CustomField> customFieldVehicles = customFieldManager.getCustomFieldObjectsByName(
                        Constants.VEHICLE_ATTRIBUTE);
                for (CustomField customField : customFieldVehicles) {
                    if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_SELECT)) {
                        customFieldVehicleParameter = customField;
                    }
                }
                CustomField customFieldDeviceType = customFieldManager.getCustomFieldObjectByName(
                        Constants.DEVICE_TYPE_ATTRIBUTE);
                if (createIssue.getCustomFieldValue(customFieldVehicleParameter) == null) {
                    error.addError(customFieldVehicleParameter.getId(),
                                   Constants.YOU_MUST_SPECIFY_A + customFieldVehicleParameter.getName() +
                                   Constants.OF_THE_ISSUE, ErrorCollection.Reason.VALIDATION_FAILED);

                } else {
                    // run loop for check screen device(set of device) == device id(Screen device)
                    boolean existHuTypeVehicle = false;
                    for (Issue issue : tempIssues) {
                        if ((issue.getIssueType().getName().equalsIgnoreCase(Constants.HU_TYPE_VEHICLE)) &&
                            ((createIssue.getCustomFieldValue(customFieldVehicleParameter)).equals(
                                    issue.getCustomFieldValue(customFieldVehicleParameter))) &&
                            (matchHuDevice != null) && (((Option) matchHuDevice.getCustomFieldValue(customFieldAmpType))
                                .getValue()
                                .equalsIgnoreCase(
                                        ((Option) issue.getCustomFieldValue(customFieldAmpType)).getValue())) &&
                            (((String) matchHuDevice.getCustomFieldValue(customFieldDeviceType)).equalsIgnoreCase(
                                    (String) issue.getCustomFieldValue(customFieldDeviceType)))) {
                            existHuTypeVehicle = true;
                            break;
                        }
                    }
                    if (!existHuTypeVehicle) {
                        error.addError(customFieldVehicleParameter.getId(), Constants.ERR_HU_TYPE_VEHICLE_EXIST,
                                       ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                }
            }

        } catch (SearchException e) {
            logger.error(e.getMessage());
            throw new InvalidInputException(Constants.ERR_SEARCH_QUERY);
        }

        if (error.getErrors().size() != 0) {
            throw new InvalidInputException(error.getErrors());
        } else if (createIssue.getIssueType().getName().equalsIgnoreCase(Constants.SET_OF_DEVICE)) {
            try {
                JqlClauseBuilder setOfDeviceJcb = JqlQueryBuilder.newClauseBuilder();
                setOfDeviceJcb.project(createIssue.getProjectId()).and().issueType(Constants.SET_OF_DEVICE);
                for (String attribute : Constants.SET_OF_DEVICE_ATTRIBUTES) {
                    Object object = createIssue.getCustomFieldValue(customFieldManager.getCustomFieldObjectByName(attribute));
                    if (object != null && Constants.VEHICLE_ATTRIBUTE.equals(attribute)) {
                        setOfDeviceJcb.and().addStringCondition(Constants.QUERY_CUSTOM_FIELD_PREFIX
                                                                + customFieldManager.getCustomFieldObjectByName(attribute).getIdAsLong().toString()
                                                                + Constants.QUERY_CUSTOM_FIELD_SUFFIX,
                                                                Operator.EQUALS, object.toString());
                    } else if (object != null && !Constants.VEHICLE_ATTRIBUTE.equals(attribute)) {
                        setOfDeviceJcb.and().addStringCondition(attribute, Operator.LIKE, object.toString());
                    }
                }
                Query setOfDeviceQuery = setOfDeviceJcb.buildQuery();
                SearchResults searchResults = searchService.search(currentUser, setOfDeviceQuery, pagerFilter);
                List<Issue> setOfDevices = searchResults.getIssues();
                if (!setOfDevices.isEmpty()) {
                    throw new InvalidInputException(Constants.ERR_SET_OF_DEVICE_ALREADY_EXISTS_PREFIX
                                                    + ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL)
                                                    + Constants.ERR_SET_OF_DEVICE_ALREADY_EXISTS_SUFFIX
                                                    + setOfDevices.get(0).getKey());
                }
            } catch (SearchException e) {
                logger.error(e.getMessage());
                throw new InvalidInputException(Constants.ERR_SEARCH_QUERY);
            }
        }
    }
}
