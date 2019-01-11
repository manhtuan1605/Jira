package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.*;
import com.atlassian.jira.issue.customfields.option.LazyLoadedOption;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.cmcglobal.plugins.dto.CustomFieldValueDTO;
import com.cmcglobal.plugins.dto.IssueMessageDTO;
import com.cmcglobal.plugins.entity.TestCase;
import com.cmcglobal.plugins.entity.TestResultHistory;
import com.cmcglobal.plugins.service.IssueHelperService;
import com.cmcglobal.plugins.service.TestCaseService;
import com.cmcglobal.plugins.service.TestMonitorService;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Utilities;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cmcglobal.plugins.utils.Constants.CUSTOM_FIELD_PHASE;
import static com.cmcglobal.plugins.utils.Constants.CUSTOM_FIELD_TESTCASE_TYPE;
import static com.cmcglobal.plugins.utils.Constants.CUSTOM_FIELD_TYPE_TEXTAREA;
import static com.cmcglobal.plugins.utils.Constants.CUSTOM_FIELD_UPDATE_DATE;
import static com.cmcglobal.plugins.utils.Constants.ERROR_UPDATE_DATE;
import static com.cmcglobal.plugins.utils.Constants.FORMAT_UPDATE_DATE;

@Scanned
@Named
public class IssueHelperServiceImpl implements IssueHelperService {
    @ComponentImport
    private final ProjectService   projectService;
    @ComponentImport
    private ActiveObjects ao;
    @ComponentImport
    private final ConstantsManager constantsManager;

    @JiraImport
    private final CustomFieldManager customFieldManager;

    private TestCaseService testCaseService;

    private TestMonitorService testMonitorService;

    @Autowired
    public void setTestCaseService(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    //    @ComponentImport
    //    private final StatusManager statusManager;

    private static final Logger log = LoggerFactory.getLogger(IssueHelperServiceImpl.class);

    @Inject
    public IssueHelperServiceImpl(final ProjectService projectService, final ConstantsManager constantsManager,
                                  final CustomFieldManager customFieldManager, final TestMonitorService testMonitorService
                                  //            , StatusManager statusManager
    ) {
        this.projectService = projectService;
        this.constantsManager = constantsManager;
        this.customFieldManager = customFieldManager;
        this.testMonitorService = testMonitorService;
        //        this.statusManager = statusManager;
    }

    @Override
    public IssueMessageDTO createIssue(final ApplicationUser applicationUser, final Long projectId,
                                       final String issueTypeName, final String summary,
                                       final List<CustomFieldValueDTO> customFieldValueDTOMap) {

        final IssueService issueService = ComponentAccessor.getIssueService();
        final IssueType issueType = constantsManager.getAllIssueTypeObjects()
                                                    .stream()
                                                    .filter(issueTypeElement -> issueTypeElement.getName()
                                                                                                .equalsIgnoreCase(
                                                                                                        issueTypeName))
                                                    .findFirst()
                                                    .orElse(null);

        if (issueType == null) {
            log.error(Constants.ERR_ISSUE_TYPE_NOT_EXIST);
            return null;
        }

        final IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
        issueInputParameters.setProjectId(projectId)
                            .setSummary(summary)
                            .setReporterId(applicationUser.getName())
                            .setAssigneeId(applicationUser.getName())
                            .setIssueTypeId(issueType.getId());
        addCustomfield(customFieldValueDTOMap, issueInputParameters);
        final IssueService.CreateValidationResult createValidationResult = issueService.validateCreate(applicationUser,
                                                                                                       issueInputParameters);
        if (createValidationResult.isValid()) {
            createValidationResult.getIssue().setReporter(applicationUser);
            createValidationResult.getIssue().setAssignee(applicationUser);
            final IssueService.IssueResult createResult = issueService.create(applicationUser, createValidationResult);
            if (createResult.isValid()) {
                return new IssueMessageDTO(createResult.getIssue().getId(), "");
            }
            log.error(Constants.ERR_CREATE_ISSUE + createResult.getErrorCollection().toString());
            return new IssueMessageDTO(null, getErrorMessage(createResult.getErrorCollection()));
        }
        log.error(Constants.ERR_VALIDATION_CREATE_ISSUE + createValidationResult.getErrorCollection().toString());
        return new IssueMessageDTO(null, getErrorMessage(createValidationResult.getErrorCollection()));
    }

    public void addCustomfield(final List<CustomFieldValueDTO> customFieldValueDTOMap,
                               final IssueInputParameters issueInputParameters) {
        for (final CustomFieldValueDTO attribute : customFieldValueDTOMap) {
            final Collection<CustomField> customFields = customFieldManager.getCustomFieldObjectsByName(
                    attribute.getKey());
            if (!customFields.isEmpty()) {
                for (final CustomField cf : customFields) {
                    if (!cf.getFieldName().equals(Constants.CUSTOM_FIELD_VEHICLE_PARAMETER) ||
                        (cf.getFieldName().equals(Constants.CUSTOM_FIELD_VEHICLE_PARAMETER) &&
                         cf.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_TEXT))) {
                        issueInputParameters.addCustomFieldValue(cf.getIdAsLong(), attribute.getValue());
                    }
                }
            }
        }

    }

    @Override
    public boolean updateIssue(final ApplicationUser applicationUser, long issueId,
                               final List<CustomFieldValueDTO> customFieldValueDTOList) {
        final IssueService issueService = ComponentAccessor.getIssueService();
        final IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
        addCustomfield(customFieldValueDTOList, issueInputParameters);

        final IssueService.UpdateValidationResult updateValidationResult = issueService.validateUpdate(applicationUser,
                                                                                                       issueId,
                                                                                                       issueInputParameters);

        if (updateValidationResult.isValid()) {
            final IssueService.IssueResult updateResult = issueService.update(applicationUser, updateValidationResult);
            if (updateResult.isValid()) {
                return true;
            }
            log.error(Constants.ERR_UPDATE_ISSUE + updateResult.getErrorCollection().toString() +
                      Constants.ERR_INPUT_PARAM + issueInputParameters.toString());
            return false;
        }
        log.error(Constants.ERR_VALIDATION_UPDATE_ISSUE + updateValidationResult.getErrorCollection().toString() +
                  Constants.ERR_INPUT_PARAM + issueInputParameters.toString());
        return false;
    }

    @Override
    public IssueMessageDTO updateIssue(final ApplicationUser applicationUser, final TestCase testCase,
                                       final List<CustomFieldValueDTO> customFieldValueDTOList) {
        final IssueService issueService = ComponentAccessor.getIssueService();
        final IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
        addCustomfield(customFieldValueDTOList, issueInputParameters);

        final IssueService.UpdateValidationResult updateValidationResult = issueService.validateUpdate(applicationUser,
                                                                                                       testCase.getIssueId(),
                                                                                                       issueInputParameters);

        if (updateValidationResult.isValid()) {
            final IssueService.IssueResult updateResult = issueService.update(applicationUser, updateValidationResult);
            if (updateResult.isValid()) {
                return new IssueMessageDTO(updateResult.getIssue().getId(), "");
            }
            log.error(Constants.ERR_UPDATE_ISSUE + updateResult.getErrorCollection().toString());
            return new IssueMessageDTO(null, getErrorMessage(updateResult.getErrorCollection()));
        }
        log.error(Constants.ERR_VALIDATION_UPDATE_ISSUE + updateValidationResult.getErrorCollection().toString());
        return new IssueMessageDTO(null, getErrorMessage(updateValidationResult.getErrorCollection()));
    }

    @Override
    public boolean updateIssue(final String action, final ApplicationUser applicationUser, final TestCase testCase,
                               final List<CustomFieldValueDTO> customFieldValueDTOList) {
        final IssueService issueService = ComponentAccessor.getIssueService();
        final IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();

        addCustomfield(customFieldValueDTOList, issueInputParameters);

        final IssueService.UpdateValidationResult updateValidationResult = issueService.validateUpdate(applicationUser,
                                                                                                       testCase.getIssueId(),
                                                                                                       issueInputParameters);

        if (updateValidationResult.isValid()) {
            final IssueService.IssueResult updateResult = issueService.update(applicationUser, updateValidationResult);
            if (updateResult.isValid() && !StringUtils.isNoneEmpty(action)) {
                return true;
            } else if (updateResult.isValid() && StringUtils.isNoneEmpty(action)) {
                final String err = updateIssueStatusByAction(applicationUser, testCase.getIssueId(), action,
                                                             issueInputParameters);
                if ("".equals(err)) {
                    return true;
                }
            }
            log.error(Constants.ERR_UPDATE_ISSUE + updateResult.getErrorCollection().toString() +
                      Constants.ERR_INPUT_PARAM + issueInputParameters.toString());
            return false;
        }
        log.error(Constants.ERR_VALIDATION_UPDATE_ISSUE + updateValidationResult.getErrorCollection().toString() +
                  Constants.ERR_INPUT_PARAM + issueInputParameters.toString());
        return false;
    }

    @Override
    public boolean updateIssue(final ApplicationUser applicationUser, final TestCase testCase,
                               final List<CustomFieldValueDTO> customFieldValueDTOList, final String assignee) {
        final IssueService issueService = ComponentAccessor.getIssueService();
        final IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
        issueInputParameters.setAssigneeId(assignee);
        final Status status = constantsManager.getStatusByName(Constants.STATUS_WAITING_TO_TEST);
        issueInputParameters.setStatusId(status.getId());
        addCustomfield(customFieldValueDTOList, issueInputParameters);
        final IssueService.UpdateValidationResult updateValidationResult = issueService.validateUpdate(applicationUser,
                                                                                                       testCase.getIssueId(),
                                                                                                       issueInputParameters);

        if (updateValidationResult.isValid()) {
            final IssueService.IssueResult updateResult = issueService.update(applicationUser, updateValidationResult);
            if (updateResult.isValid()) {
                String err = updateStatusIssue(applicationUser, testCase.getIssueId(), issueService,
                                               issueInputParameters);
                if (err.equals("") || err.contains(Constants.ISSUE_ACTION_NOT_PLAN)) {
                    return true;
                }
            }
            log.error(Constants.ERR_UPDATE_ISSUE + updateResult.getErrorCollection().toString() +
                      Constants.ERR_INPUT_PARAM + issueInputParameters.toString());
            return false;
        }
        log.error(Constants.ERR_VALIDATION_UPDATE_ISSUE + updateValidationResult.getErrorCollection().toString() +
                  Constants.ERR_INPUT_PARAM + issueInputParameters.toString());
        return false;
    }

    public String updateStatusIssue(final ApplicationUser applicationUser, final Long issueId,
                                    final IssueService issueService, final IssueInputParameters issueInputParameters) {
        final MutableIssue issue = ComponentAccessor.getIssueManager().getIssueObject(issueId);
        final JiraWorkflow workFlow = ComponentAccessor.getWorkflowManager().getWorkflow(issue);
        final Status statusCur = issue.getStatusObject();
        final com.opensymphony.workflow.loader.StepDescriptor currentStep = workFlow.getLinkedStep(statusCur);
        final List<ActionDescriptor> actions = currentStep.getActions();
        int actionId = 0;
        for (final ActionDescriptor actionDescriptor : actions) {
            if (actionDescriptor.getName().equals(Constants.ISSUE_PLAN)) {
                actionId = actionDescriptor.getId();
            }
        }
        final IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(
                applicationUser, issue.getId(), actionId, issueInputParameters);
        if (transitionValidationResult.isValid()) {
            final IssueService.IssueResult transitionResult = issueService.transition(applicationUser,
                                                                                      transitionValidationResult);
            if (!transitionResult.isValid()) {
                return transitionResult.getErrorCollection().toString();
            }
        } else {
            return Constants.ISSUE_ACTION_NOT_PLAN + transitionValidationResult.getErrorCollection().toString();
        }
        return "";
    }

    @Override
    public Long create(final ApplicationUser applicationUser,
                       final IssueService.CreateValidationResult createValidationResult) {
        final IssueService issueService = ComponentAccessor.getIssueService();
        final IssueService.IssueResult createResult = issueService.create(applicationUser, createValidationResult);
        if (createResult.isValid()) {
            return createResult.getIssue().getId();
        }
        final IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
        log.error(
                Constants.ERR_CREATE_ISSUE + createResult.getErrorCollection().toString() + Constants.ERR_INPUT_PARAM +
                issueInputParameters.toString());
        return null;
    }

    @Override
    public boolean createOrUpdateIssue(final MutableIssue issue, final boolean isCreate) {
        final DateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_UPDATE_DATE);
        simpleDateFormat.setLenient(false);
        final List<String> listTestcaseCustomFields = Utilities.listTestcaseAttribute();
        final List<CustomField> listCustomFields = ComponentAccessor.getCustomFieldManager()
                                                                    .getCustomFieldObjects(issue);
        final Map<String, String> testcaseValueMap = new HashMap<>();
        Date updateDate = null;
        for (final CustomField customField : listCustomFields) {
            final String customFieldName = customField.getName();
            if (listTestcaseCustomFields.contains(customFieldName)) {
                final Object objectCustomField = issue.getCustomFieldValue(customField);
                if (customField.getCustomFieldType().getKey().equals(CUSTOM_FIELD_TYPE_TEXTAREA) &&
                    issue.getCustomFieldValue(customField) == null) {
                    customField.updateValue(null, issue, new ModifiedValue(objectCustomField, " "),
                                            new DefaultIssueChangeHolder());
                }
                if (customFieldName.equalsIgnoreCase(CUSTOM_FIELD_UPDATE_DATE)) {
                    try {
                        if (objectCustomField != null) {
                            updateDate = simpleDateFormat.parse(objectCustomField.toString());
                        }
                        continue;
                    } catch (ParseException e) {
                        log.error(ERROR_UPDATE_DATE);
                        return false;
                    }
                }
                if (customFieldName.equalsIgnoreCase(CUSTOM_FIELD_PHASE) ||
                    customFieldName.equalsIgnoreCase(CUSTOM_FIELD_TESTCASE_TYPE)) {
                    testcaseValueMap.put(customFieldName, objectCustomField == null ?
                                                          null :
                                                          "" + ((LazyLoadedOption) objectCustomField).getOptionId());
                } else {
                    testcaseValueMap.put(customFieldName,
                                         objectCustomField == null ? null : objectCustomField.toString());
                }
            }
        }
        return testCaseService.synchronizeTestCase(issue.getId(), testcaseValueMap, updateDate, isCreate);
    }

    @Override
    public boolean updateIssueOnEventMove(final ApplicationUser applicationUser, final Long issueId, final String phase,
                                          final String testcaseType) {
        final IssueService issueService = ComponentAccessor.getIssueService();
        final IssueInputParameters issueInputParameters = ComponentAccessor.getIssueService().newIssueInputParameters();
        final Collection<CustomField> customFieldsPhase = customFieldManager.getCustomFieldObjectsByName(
                CUSTOM_FIELD_PHASE);
        if (!customFieldsPhase.isEmpty()) {
            final CustomField customField = (CustomField) customFieldsPhase.toArray()[0];
            issueInputParameters.addCustomFieldValue(customField.getIdAsLong(), phase);
        }
        final Collection<CustomField> customFieldsTestcaseType = customFieldManager.getCustomFieldObjectsByName(
                CUSTOM_FIELD_TESTCASE_TYPE);
        if (!customFieldsTestcaseType.isEmpty()) {
            final CustomField customField = (CustomField) customFieldsTestcaseType.toArray()[0];
            issueInputParameters.addCustomFieldValue(customField.getIdAsLong(), testcaseType);
        }

        final IssueService.UpdateValidationResult updateValidationResult = issueService.validateUpdate(applicationUser,
                                                                                                       issueId,
                                                                                                       issueInputParameters);

        if (updateValidationResult.isValid()) {
            final IssueService.IssueResult updateResult = issueService.update(applicationUser, updateValidationResult);
            if (updateResult.isValid()) {
                return true;
            }
            log.error(Constants.ERR_UPDATE_ISSUE + updateResult.getErrorCollection().toString() +
                      Constants.ERR_INPUT_PARAM + issueInputParameters.toString());
            return false;
        }
        log.error(Constants.ERR_VALIDATION_UPDATE_ISSUE + updateValidationResult.getErrorCollection().toString() +
                  Constants.ERR_INPUT_PARAM + issueInputParameters.toString());
        return false;
    }

    @Override
    public String updateIssueStatusByAction(final ApplicationUser applicationUser, final Long issueId,
                                            final String action, final IssueInputParameters issueInputParameters) {
        final IssueService issueService = ComponentAccessor.getIssueService();
        final MutableIssue issue = ComponentAccessor.getIssueManager().getIssueObject(issueId);
        final JiraWorkflow workFlow = ComponentAccessor.getWorkflowManager().getWorkflow(issue);
        final Status statusCur = issue.getStatusObject();
        issueInputParameters.setStatusId(statusCur.getId());
        final com.opensymphony.workflow.loader.StepDescriptor currentStep = workFlow.getLinkedStep(statusCur);
        final List<ActionDescriptor> actions = currentStep.getActions();
        int actionId = 0;
        for (final ActionDescriptor actionDescriptor : actions) {
            if (actionDescriptor.getName().equals(action)) {
                actionId = actionDescriptor.getId();
            }
        }
        final IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(
                applicationUser, issue.getId(), actionId, issueInputParameters);
        if (transitionValidationResult.isValid()) {
            final IssueService.IssueResult transitionResult = issueService.transition(applicationUser,
                                                                                      transitionValidationResult);
            if (!transitionResult.isValid()) {
                return transitionResult.getErrorCollection().toString();
            }
        } else {
            return transitionValidationResult.getErrorCollection().toString();
        }
        return "";
    }

    private String getErrorMessage(final ErrorCollection errorCollection){
        if (errorCollection == null) {
            return "";
        }
        final StringBuilder stringBuilder = new StringBuilder();
        if (!MapUtils.isEmpty(errorCollection.getErrors())) {
            stringBuilder.append(errorCollection.getErrors().toString() + ". ");
        }
        if (!CollectionUtils.isEmpty(errorCollection.getErrorMessages())) {
            stringBuilder.append(errorCollection.getErrorMessages().toString() + ". ");
        }
        return stringBuilder.toString();
    }
}
