package com.cmcglobal.plugins.jira.postfunctions;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.option.Option;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is the post-function class that gets executed at the end of the transition.
 * Any parameters that were saved in your factory class will be available in the transientVars Map.
 */
public class EnvironmentPostFunction extends AbstractJiraFunctionProvider {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentPostFunction.class);

    public void execute(Map transientVars, Map args, PropertySet ps) {
        MutableIssue issue = getIssue(transientVars);
        if (issue.getIssueType() != null && issue.getIssueType().getName().equalsIgnoreCase(Constants.SET_OF_DEVICE)) {
            CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
            CustomField customFieldHuId = customFieldManager.getCustomFieldObjectByName(Constants.HU_ID_ATTRIBUTE);
            CustomField customFieldHuDevice = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_HU_DEVICE);
            CustomField customFieldAmpType = null;
            Collection<CustomField> customFieldAmpTypes = customFieldManager.getCustomFieldObjectsByName(
                    Constants.AMP_TYPE_ATTRIBUTE);
            for (CustomField customField : customFieldAmpTypes) {
                if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_SELECT)) {
                    customFieldAmpType = customField;
                }
            }
            CustomField customFieldDeviceType = customFieldManager.getCustomFieldObjectByName(
                    Constants.DEVICE_TYPE_ATTRIBUTE);
            CustomField customFieldDeviceSubType = customFieldManager.getCustomFieldObjectByName(
                    Constants.DEVICE_SUB_TYPE_ATTRIBUTE);
            CustomField customFieldDeviceId = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_DEVICE_ID);
            CustomField customFieldScreenDevice = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_SCREEN_DEVICE);
            CustomField customFieldScreenDeviceType = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_SCREEN_DEVICE_TYPE);
            CustomField customFieldDeviceName = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_DEVICE_NAME);
            CustomField customFieldBuildNumber = customFieldManager.getCustomFieldObjectByName(
                    Constants.BUILD_NUMBER_ATTRIBUTE);
            CustomField customFieldAmigoPgVersion = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_AMIGO_PG_VERSION);
            CustomField customFieldAmigoDataVersion = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_AMIGO_DATA_VERSION);
            CustomField customFieldScreenType = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_SCREEN_TYPE);
            CustomField customFieldSysSetIdVersion = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_SYS_SET_ID_VERSION);
            CustomField customFieldSysDataVersion = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_SYS_DATA_VERSION);
            CustomField customFieldSysSoft = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_SYS_SOFT);
            CustomField customFieldVehicleParameter = null;
            Collection<CustomField> customFieldVehicles = customFieldManager.getCustomFieldObjectsByName(
                    Constants.VEHICLE_ATTRIBUTE);
            for (CustomField customField : customFieldVehicles) {
                if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_SELECT)) {
                    customFieldVehicleParameter = customField;
                }
            }
            CustomField customFieldSysDataPrefix = customFieldManager.getCustomFieldObjectByName(
                    Constants.CUSTOM_FIELD_SYS_DATA_PREFIX);
            CustomField customFieldTestHuMarket = customFieldManager.getCustomFieldObjectByName(
                    Constants.TEST_HU_MARKET_ATTRIBUTE);
            CustomField customFieldSiIndexValue = customFieldManager.getCustomFieldObjectByName(
                    Constants.SI_INDEX_VALUE);

            CustomField customFieldSiIndex = customFieldManager.getCustomFieldObjectByName(Constants.SI_INDEX);
            CustomField customFieldHuIndex = customFieldManager.getCustomFieldObjectByName(Constants.HU_INDEX);
            Object valueHuDevice = issue.getCustomFieldValue(customFieldHuDevice);
            Object valueAmpType = issue.getCustomFieldValue(customFieldAmpType);
            Object valueDeviceType = issue.getCustomFieldValue(customFieldDeviceType);
            Object valueDeviceSubType = issue.getCustomFieldValue(customFieldDeviceSubType);
            Object valueScreenDevice = issue.getCustomFieldValue(customFieldScreenDevice);
            Object valueScreenDeviceType = issue.getCustomFieldValue(customFieldScreenDeviceType);
            Object valueDeviceName = issue.getCustomFieldValue(customFieldDeviceName);
            Object valueBuildNumber = issue.getCustomFieldValue(customFieldBuildNumber);
            Object valueAmigoPgVersion = issue.getCustomFieldValue(customFieldAmigoPgVersion);
            Object valueAmigoDataVersion = issue.getCustomFieldValue(customFieldAmigoDataVersion);
            Object valueSysSetIdVersion = issue.getCustomFieldValue(customFieldSysSetIdVersion);
            Object valueSysDataVersion = issue.getCustomFieldValue(customFieldSysDataVersion);
            Object valueSysSoft = issue.getCustomFieldValue(customFieldSysSoft);
            Object valueVehicleParameter = issue.getCustomFieldValue(customFieldVehicleParameter);
            Object valueSysDataPrefix = issue.getCustomFieldValue(customFieldSysDataPrefix);
            Object valueTestHuMarket = issue.getCustomFieldValue(customFieldTestHuMarket);
            ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
            PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
            JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
            SearchService searchService = ComponentAccessor.getComponentOfType(SearchService.class);
            SearchResults searchResults;
            List<Issue> tempIssues;
            Long projectId = ((Issue) transientVars.get("issue")).getProjectId();
            String newValueAmpType = null;
            String newValueDeviceType = null;
            String newValueScreenDeviceType = null;
            String newValueSysDataPrefix = null;
            StringBuilder valueSiIndexValueFromSetOfDevice = new StringBuilder();

            try {
                //FILL DATA HU DEVICE
                Query query = jqlClauseBuilder.project(projectId).and().issueType(Constants.HU_DEVICE).buildQuery();
                searchResults = searchService.search(user, query, pagerFilter);
                tempIssues = searchResults.getIssues();
                for (Issue i : tempIssues) {
                    if (i.getCustomFieldValue(customFieldHuId).toString().equals(valueHuDevice.toString())) {
                        customFieldAmpType.updateValue(null, issue, new ModifiedValue(valueAmpType,
                                                                                      ((Option) i.getCustomFieldValue(
                                                                                              customFieldAmpType))),
                                                       new DefaultIssueChangeHolder());
                        newValueAmpType = ((Option) i.getCustomFieldValue(customFieldAmpType)).getValue();
                        customFieldDeviceType.updateValue(null, issue, new ModifiedValue(valueDeviceType,
                                                                                         i.getCustomFieldValue(
                                                                                                 customFieldDeviceType)
                                                                                          .toString()),
                                                          new DefaultIssueChangeHolder());
                        newValueDeviceType = i.getCustomFieldValue(customFieldDeviceType).toString();
                        customFieldDeviceSubType.updateValue(null, issue, new ModifiedValue(valueDeviceSubType,
                                                                                            i.getCustomFieldValue(
                                                                                                    customFieldDeviceSubType)
                                                                                             .toString()),
                                                             new DefaultIssueChangeHolder());
                    }
                }
                //FILL DATA SCREEN DEVICE
                jqlClauseBuilder.clear();
                query = jqlClauseBuilder.project(projectId).and().issueType(Constants.SCREEN_DEVICE).buildQuery();
                searchResults = searchService.search(user, query, pagerFilter);
                tempIssues = searchResults.getIssues();
                for (Issue i : tempIssues) {
                    if (i.getCustomFieldValue(customFieldDeviceId).toString().equals(valueScreenDevice.toString())) {
                        customFieldScreenDeviceType.updateValue(null, issue, new ModifiedValue(valueScreenDeviceType,
                                                                                               i.getCustomFieldValue(
                                                                                                       customFieldScreenDeviceType)
                                                                                                .toString()),
                                                                new DefaultIssueChangeHolder());
                        newValueScreenDeviceType = i.getCustomFieldValue(customFieldScreenDeviceType).toString();
                        customFieldDeviceName.updateValue(null, issue, new ModifiedValue(valueDeviceName,
                                                                                         i.getCustomFieldValue(
                                                                                                 customFieldDeviceName)
                                                                                          .toString()),
                                                          new DefaultIssueChangeHolder());
                    }
                }
                //FILL DATA SI AMIGO
                jqlClauseBuilder.clear();
                query = jqlClauseBuilder.project(projectId).and().issueType(Constants.SI_AMIGO_INFO).buildQuery();
                searchResults = searchService.search(user, query, pagerFilter);
                tempIssues = searchResults.getIssues();
                for (Issue i : tempIssues) {
                    if (i.getCustomFieldValue(customFieldBuildNumber).toString().equals(valueBuildNumber.toString()) &&
                        i.getCustomFieldValue(customFieldDeviceType).toString().equals(newValueDeviceType) &&
                        ((Option) i.getCustomFieldValue(customFieldAmpType)).getValue().equals(newValueAmpType)) {
                        customFieldAmigoPgVersion.updateValue(null, issue, new ModifiedValue(valueAmigoPgVersion,
                                                                                             i.getCustomFieldValue(
                                                                                                     customFieldAmigoPgVersion)
                                                                                              .toString()),
                                                              new DefaultIssueChangeHolder());
                        customFieldAmigoDataVersion.updateValue(null, issue, new ModifiedValue(valueAmigoDataVersion,
                                                                                               i.getCustomFieldValue(
                                                                                                       customFieldAmigoDataVersion)
                                                                                                .toString()),
                                                                new DefaultIssueChangeHolder());
                        valueSiIndexValueFromSetOfDevice.append(
                                i.getCustomFieldValue(customFieldAmigoPgVersion).toString().trim());
                        valueSiIndexValueFromSetOfDevice.append(
                                i.getCustomFieldValue(customFieldAmigoDataVersion).toString().trim());
                    }
                }
                //FILL DATA HU TYPE VEHICLE
                jqlClauseBuilder.clear();
                query = jqlClauseBuilder.project(projectId).and().issueType(Constants.HU_TYPE_VEHICLE).buildQuery();
                searchResults = searchService.search(user, query, pagerFilter);
                tempIssues = searchResults.getIssues();
                for (Issue i : tempIssues) {

                    if (i.getCustomFieldValue(customFieldVehicleParameter)
                         .toString()
                         .equals(valueVehicleParameter.toString()) &&
                        ((Option) i.getCustomFieldValue(customFieldAmpType)).getValue().equals(newValueAmpType) &&
                        i.getCustomFieldValue(customFieldDeviceType).toString().equals(newValueDeviceType)) {
                        customFieldSysDataPrefix.updateValue(null, issue, new ModifiedValue(valueSysDataPrefix,
                                                                                            i.getCustomFieldValue(
                                                                                                    customFieldSysDataPrefix)
                                                                                             .toString()),
                                                             new DefaultIssueChangeHolder());
                        newValueSysDataPrefix = i.getCustomFieldValue(customFieldSysDataPrefix).toString();
                        customFieldTestHuMarket.updateValue(null, issue, new ModifiedValue(valueTestHuMarket,
                                                                                           i.getCustomFieldValue(
                                                                                                   customFieldTestHuMarket)),
                                                            new DefaultIssueChangeHolder());
                        valueSiIndexValueFromSetOfDevice.append(
                                i.getCustomFieldValue(customFieldTestHuMarket).toString());
                    }
                }
                //FILL DATA SYS AMIGO
                jqlClauseBuilder.clear();
                query = jqlClauseBuilder.project(projectId).and().issueType(Constants.SI_SYS_INFO).buildQuery();
                searchResults = searchService.search(user, query, pagerFilter);
                tempIssues = searchResults.getIssues();
                for (Issue i : tempIssues) {
                    if (i.getCustomFieldValue(customFieldBuildNumber).toString().equals(valueBuildNumber.toString()) &&
                        i.getCustomFieldValue(customFieldScreenType).toString().equals(newValueScreenDeviceType)) {
                        customFieldSysSetIdVersion.updateValue(null, issue, new ModifiedValue(valueSysSetIdVersion,
                                                                                              i.getCustomFieldValue(
                                                                                                      customFieldSysSetIdVersion)
                                                                                               .toString()),
                                                               new DefaultIssueChangeHolder());
                        customFieldSysDataVersion.updateValue(null, issue, new ModifiedValue(valueSysDataVersion,
                                                                                             newValueSysDataPrefix +
                                                                                             i.getCustomFieldValue(
                                                                                                     customFieldSysDataVersion)
                                                                                              .toString()),
                                                              new DefaultIssueChangeHolder());
                        customFieldSysSoft.updateValue(null, issue, new ModifiedValue(valueSysSoft,
                                                                                      i.getCustomFieldValue(
                                                                                              customFieldSysSoft)
                                                                                       .toString()),
                                                       new DefaultIssueChangeHolder());
                        valueSiIndexValueFromSetOfDevice.append(
                                i.getCustomFieldValue(customFieldSysSetIdVersion).toString().trim());
                        valueSiIndexValueFromSetOfDevice.append(
                                i.getCustomFieldValue(customFieldSysDataVersion).toString().trim());
                        valueSiIndexValueFromSetOfDevice.append(
                                i.getCustomFieldValue(customFieldSysSoft).toString().trim());
                    }
                }
                //COUNT HU INDEX
                jqlClauseBuilder.clear();
                query = jqlClauseBuilder.project(projectId).and().issueType(Constants.SET_OF_DEVICE).buildQuery();
                searchResults = searchService.search(user, query, pagerFilter);
                tempIssues = searchResults.getIssues();
                customFieldHuIndex.updateValue(null, issue,
                                               new ModifiedValue(issue.getCustomFieldValue(customFieldHuIndex),
                                                                 String.valueOf(tempIssues.size())),
                                               new DefaultIssueChangeHolder());
                int maxSiIndex = 1;
                for (Issue i : tempIssues) {
                    if ((i.getCustomFieldValue(customFieldSiIndex) != null) &&
                        (Integer.parseInt((String) i.getCustomFieldValue(customFieldSiIndex)) > maxSiIndex)) {
                        maxSiIndex = Integer.parseInt((String) i.getCustomFieldValue(customFieldSiIndex));
                    }
                }
                boolean isSame = false;
                //CREATE SI INDEX
                for (Issue i : tempIssues) {
                    if (tempIssues.size() == 1) {
                        customFieldSiIndex.updateValue(null, issue,
                                                       new ModifiedValue(issue.getCustomFieldValue(customFieldSiIndex),
                                                                         String.valueOf(tempIssues.size())),
                                                       new DefaultIssueChangeHolder());
                        customFieldSiIndexValue.updateValue(null, issue, new ModifiedValue(
                                issue.getCustomFieldValue(customFieldSiIndexValue),
                                valueSiIndexValueFromSetOfDevice.toString()), new DefaultIssueChangeHolder());
                        isSame = true;
                        break;
                    }
                    if ((!i.equals(issue)) && i.getCustomFieldValue(customFieldSiIndexValue)
                                               .toString()
                                               .equalsIgnoreCase(valueSiIndexValueFromSetOfDevice.toString())) {
                        isSame = true;
                        customFieldSiIndex.updateValue(null, issue,
                                                       new ModifiedValue(issue.getCustomFieldValue(customFieldSiIndex),
                                                                         i.getCustomFieldValue(customFieldSiIndex)
                                                                          .toString()), new DefaultIssueChangeHolder());
                        customFieldSiIndexValue.updateValue(null, issue, new ModifiedValue(
                                issue.getCustomFieldValue(customFieldSiIndexValue),
                                valueSiIndexValueFromSetOfDevice.toString()), new DefaultIssueChangeHolder());
                        break;
                    }
                }
                if (!isSame) {
                    customFieldSiIndex.updateValue(null, issue,
                                                   new ModifiedValue(issue.getCustomFieldValue(customFieldSiIndex),
                                                                     String.valueOf(maxSiIndex + 1)),
                                                   new DefaultIssueChangeHolder());
                    customFieldSiIndexValue.updateValue(null, issue, new ModifiedValue(
                            issue.getCustomFieldValue(customFieldSiIndexValue),
                            valueSiIndexValueFromSetOfDevice.toString()), new DefaultIssueChangeHolder());
                }
                //RE-INDEX ISSUE
                Helper.reIndexIssue(issue);

            } catch (SearchException e) {
                logger.error(e.getMessage());
            }
        }
    }

}