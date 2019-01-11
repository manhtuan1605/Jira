package com.cmcglobal.plugins.hook;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.template.hook.AddProjectHook;
import com.atlassian.jira.project.template.hook.ConfigureData;
import com.atlassian.jira.project.template.hook.ConfigureResponse;
import com.atlassian.jira.project.template.hook.ValidateData;
import com.atlassian.jira.project.template.hook.ValidateResponse;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.cmcglobal.plugins.jira.conditions.PlanTestcaseCondition;
import com.cmcglobal.plugins.jira.conditions.TestResultCondition;
import com.cmcglobal.plugins.jira.postfunctions.TestResultPostFunction;
import com.cmcglobal.plugins.jira.postfunctions.TestcasePostFunction;
import com.cmcglobal.plugins.jira.validators.FieldValidator;
import com.cmcglobal.plugins.jira.validators.FieldValidatorTask;
import com.cmcglobal.plugins.jira.workflow.TestCasePlanValidator;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Helper;
import com.cmcglobal.plugins.utils.Utilities;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.RestrictionDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by User on 7/17/2018.
 */
@Scanned
public class AddTestProjectHook implements AddProjectHook {

    @JiraImport
    private final CustomFieldManager customFieldManager;

    public AddTestProjectHook(CustomFieldManager customFieldManager) {
        this.customFieldManager = customFieldManager;
    }

    @Override
    public ValidateResponse validate(final ValidateData validateData) {
        ValidateResponse validateResponse = ValidateResponse.create();
        if (validateData.projectKey().equals("TEST")) {
            validateResponse.addErrorMessage("Invalid Project Key");
        }

        return validateResponse;
    }

    @Override
    public ConfigureResponse configure(final ConfigureData configureData) {
        Map<String, FieldScreen> createdScreens = configureData.createdScreens();
        Project project = configureData.project();

        //ADD CUSTOM FIELD TO TESTCASE SCREEN
        StringBuilder listCustomFieldCreateScreen = new StringBuilder();
        testcaseScreen(createdScreens, listCustomFieldCreateScreen, project);

        //ADD CUSTOM FIELD TO TEST RESULT SCREEN
        StringBuilder listCustomFieldOkScreen = new StringBuilder();
        StringBuilder listCustomFieldNgScreen = new StringBuilder();
        StringBuilder listCustomFieldPendingScreen = new StringBuilder();
        StringBuilder listCustomFieldOutOfScopeScreen = new StringBuilder();
        testResultScreen(createdScreens, listCustomFieldOkScreen, listCustomFieldNgScreen, listCustomFieldPendingScreen,
                         listCustomFieldOutOfScopeScreen);

        //ADD CUSTOM FIELD TO TASK SCREEN
        StringBuilder listCustomFieldTaskScreen = new StringBuilder();
        taskScreen(createdScreens, listCustomFieldTaskScreen);

        //ADD SCREEN TO WORKFLOW'S TRANSITION
        editWorkflow(configureData, listCustomFieldCreateScreen, listCustomFieldOkScreen, listCustomFieldNgScreen,
                     listCustomFieldPendingScreen, listCustomFieldOutOfScopeScreen, listCustomFieldTaskScreen);

        //ADD TESTCASE PERMISSION SCHEME
        Helper.addPermissionScheme(project, Constants.PERMISSION_SCHEME_NAME_TESTCASE);

        return ConfigureResponse.create();
    }

    private void addValidator(ActionDescriptor action, String listCustomFields, Class clazz) {
        ValidatorDescriptor validatorDescriptor = DescriptorFactory.getFactory().createValidatorDescriptor();
        validatorDescriptor.setType(Constants.VALIDATOR_DESCRIPTOR_TYPE);
        final Map validatorArgs = validatorDescriptor.getArgs();
        validatorArgs.put(Constants.VALIDATOR_DESCRIPTOR_ARGS_CLASS_NAME, clazz.getName());
        validatorArgs.put(Constants.VALIDATOR_DESCRIPTOR_ARGS_CUSTOM_FIELD, listCustomFields);
        boolean flag = true;
        for (Object validatorObject : action.getValidators()) {
            ValidatorDescriptor thisValidator = (ValidatorDescriptor) validatorObject;
            if (thisValidator.getName().equals(validatorDescriptor.getName())) {
                flag = false;
                break;
            }
        }
        if (flag) {
            action.getValidators().add(validatorDescriptor);
        }
    }

    private void addPlanValidator(ActionDescriptor action, String startDate, String endDate) {
        ValidatorDescriptor validatorDescriptor = DescriptorFactory.getFactory().createValidatorDescriptor();
        validatorDescriptor.setType(Constants.VALIDATOR_DESCRIPTOR_TYPE);
        final Map validatorArgs = validatorDescriptor.getArgs();
        validatorArgs.put(Constants.VALIDATOR_DESCRIPTOR_ARGS_CLASS_NAME, TestCasePlanValidator.class.getName());
        validatorArgs.put(Constants.VALIDATOR_DESCRIPTOR_CF_START_DATE, startDate);
        validatorArgs.put(Constants.VALIDATOR_DESCRIPTOR_CF_END_DATE, endDate);
        boolean flag = true;
        for (Object validatorObject : action.getValidators()) {
            ValidatorDescriptor thisValidator = (ValidatorDescriptor) validatorObject;
            if (thisValidator.getName().equals(validatorDescriptor.getName())) {
                flag = false;
                break;
            }
        }
        if (flag) {
            action.getValidators().add(validatorDescriptor);
        }
    }

    private void addPostFunction(ActionDescriptor action) {
        FunctionDescriptor functionDescriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
        functionDescriptor.setType(Constants.VALIDATOR_DESCRIPTOR_TYPE);
        final Map functionDescriptorArgs = functionDescriptor.getArgs();
        functionDescriptorArgs.put(Constants.VALIDATOR_DESCRIPTOR_ARGS_CLASS_NAME,
                                   TestcasePostFunction.class.getName());
        boolean flag = true;
        for (Object validatorObject : action.getPostFunctions()) {
            FunctionDescriptor thisValidator = (FunctionDescriptor) validatorObject;
            if (thisValidator.getName().equals(functionDescriptor.getName())) {
                flag = false;
                break;
            }
        }
        if (flag) {
            action.getPostFunctions().add(functionDescriptor);
        }
    }

    private void addPostFunctionForTestResultHistory(ActionDescriptor action) {
        FunctionDescriptor functionDescriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
        functionDescriptor.setType(Constants.VALIDATOR_DESCRIPTOR_TYPE);
        final Map functionDescriptorArgs = functionDescriptor.getArgs();
        functionDescriptorArgs.put(Constants.VALIDATOR_DESCRIPTOR_ARGS_CLASS_NAME,
                TestResultPostFunction.class.getName());
        boolean flag = true;
        for (Object validatorObject : action.getPostFunctions()) {
            FunctionDescriptor thisValidator = (FunctionDescriptor) validatorObject;
            if (thisValidator.getName().equals(functionDescriptor.getName())) {
                flag = false;
                break;
            }
        }
        if (flag) {
            action.getPostFunctions().add(functionDescriptor);
        }
    }

    private void addCondition(ActionDescriptor action, Class clazz) {
        ConditionsDescriptor conditionsDescriptor = DescriptorFactory.getFactory().createConditionsDescriptor();
        ConditionDescriptor conditionDescriptor = DescriptorFactory.getFactory().createConditionDescriptor();
        conditionDescriptor.setType(Constants.VALIDATOR_DESCRIPTOR_TYPE);
        final Map conditionDescriptorArgs = conditionDescriptor.getArgs();
        conditionDescriptorArgs.put(Constants.VALIDATOR_DESCRIPTOR_ARGS_CLASS_NAME, clazz.getName());
        conditionsDescriptor.setConditions(Collections.singletonList(conditionDescriptor));
        RestrictionDescriptor restrictionDescriptor = new RestrictionDescriptor();
        restrictionDescriptor.setConditionsDescriptor(conditionsDescriptor);
        action.setRestriction(restrictionDescriptor);
    }

    private CustomField getCustomFieldTestcase(String attribute) {
        CustomField customFieldTestcase = null;
        Collection<CustomField> customFields = this.customFieldManager.getCustomFieldObjectsByName(attribute);
        for (CustomField customField : customFields) {
            if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_TEXT)) {
                customFieldTestcase = customField;
            }
        }

        return customFieldTestcase;
    }

    private void testcaseScreen(Map<String, FieldScreen> createdScreens, StringBuilder listCustomFieldCreateScreen,
                                Project project) {
        FieldScreen defaultScreen = createdScreens.get(Constants.KEY_DEFAULT_SCREEN);
        FieldScreenTab viewTestCaseTab = defaultScreen.getTab(0);
        FieldScreen createScreen = createdScreens.get(Constants.KEY_CREATE_SCREEN);
        FieldScreenTab createTab = createScreen.getTab(0);
        FieldScreen editScreen = createdScreens.get(Constants.KEY_EDIT_SCREEN);
        FieldScreenTab editTab = editScreen.getTab(0);
        FieldScreen planScreen = createdScreens.get(Constants.KEY_PLAN_SCREEN);
        FieldScreenTab planTab = planScreen.getTab(0);
        int position = Constants.CUSTOM_FIELD_BEGIN_POSITION;
        CustomField customField;
        for (String attribute : Utilities.listTestcaseAttribute()) {
            customField = this.customFieldManager.getCustomFieldObjectByName(attribute);
            if (customField != null) {
                if (!attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_START_DATE) &&
                    !attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_END_DATE)) {
                    createTab.addFieldScreenLayoutItem(customField.getId(), position);
                    editTab.addFieldScreenLayoutItem(customField.getId());
                    position++;
                } else {
                    planTab.addFieldScreenLayoutItem(customField.getId());
                }
                viewTestCaseTab.addFieldScreenLayoutItem(customField.getId());
                if (attribute.equals(Constants.CUSTOM_FIELD_PHASE) ||
                    attribute.equals(Constants.CUSTOM_FIELD_TESTCASE_TYPE)) {
                    listCustomFieldCreateScreen.append(customField.getId());
                    listCustomFieldCreateScreen.append(Constants.COMMA);
                    //CREATE CONFIGURATION SCHEME
                    Helper.addConfigurationScheme(project, customField);
                } else if (attribute.equals(Constants.CUSTOM_FIELD_TESTCASE_NO)) {
                    listCustomFieldCreateScreen.append(customField.getId());
                    listCustomFieldCreateScreen.append(Constants.COMMA);
                }
            }
        }

        //REMOVE FIRST 3 DEFAULT SYSTEM FIELDS
        for (int i = 0; i < 3; i++) {
            planTab.removeFieldScreenLayoutItem(0);
        }
    }

    private void testResultScreen(Map<String, FieldScreen> createdScreens, StringBuilder listCustomFieldOkScreen,
                                  StringBuilder listCustomFieldNgScreen, StringBuilder listCustomFieldPendingScreen,
                                  StringBuilder listCustomFieldOutOfScopeScreen) {
        FieldScreen defaultScreen = createdScreens.get(Constants.KEY_DEFAULT_SCREEN);
        FieldScreenTab viewTestResultTab = defaultScreen.getTab(1);
        FieldScreen okScreen = createdScreens.get(Constants.KEY_OK_SCREEN);
        FieldScreenTab okTab = okScreen.getTab(0);
        FieldScreen pendingScreen = createdScreens.get(Constants.KEY_PENDING_SCREEN);
        FieldScreenTab pendingTab = pendingScreen.getTab(0);
        FieldScreen ngScreen = createdScreens.get(Constants.KEY_NG_SCREEN);
        FieldScreenTab ngTab = ngScreen.getTab(0);
        FieldScreen outOfScopeScreen = createdScreens.get(Constants.KEY_OUT_OF_SCOPE_SCREEN);
        FieldScreenTab outOfScopeTab = outOfScopeScreen.getTab(0);
        CustomField customFieldTimeForQnaSubmission = this.customFieldManager.getCustomFieldObjectByName(
                Constants.CUSTOM_FIELD_TIME_FOR_QNA_SUBMISSION);
        //GET AMP TYPE TESTCASE CUSTOM FIELD
        CustomField customFieldAmpTypeTestcase = getCustomFieldTestcase(Constants.AMP_TYPE_ATTRIBUTE);
        //GET VEHICLE TESTCASE CUSTOM FIELD
        CustomField customFieldVehicleTestcase = getCustomFieldTestcase(Constants.VEHICLE_ATTRIBUTE);
        CustomField customField;
        int order = 0;
        for (String attribute : Utilities.listTestResultAttribute()) {
            switch (attribute) {
                case Constants.VEHICLE_ATTRIBUTE:
                    customField = customFieldVehicleTestcase;
                    break;
                case Constants.AMP_TYPE_ATTRIBUTE:
                    customField = customFieldAmpTypeTestcase;
                    break;
                case Constants.CUSTOM_FIELD_TIME_FOR_QA_SUBMISSION:
                    customField = customFieldTimeForQnaSubmission;
                    break;
                default:
                    customField = this.customFieldManager.getCustomFieldObjectByName(attribute);
                    break;
            }

            if (customField != null && !attribute.equals(Constants.CUSTOM_FIELD_RESULT_COMMENT)) {
                if (!attribute.equals(Constants.CUSTOM_FIELD_PENDING_TYPE)) {
                    viewTestResultTab.addFieldScreenLayoutItem(customField.getId());
                }
                if (attribute.equals(Constants.ERR_KEY_EVALUATION_SOFTWARE) || attribute.equals(Constants.ERR_KEY_EVALUATION_HARD) || attribute.equals(Constants.ERR_KEY_PERIPHERAL_DEVICE_NUMBER)){
                    pendingTab.addFieldScreenLayoutItem(customField.getId());
                    listCustomFieldPendingScreen.append(customField.getId());
                }

                if (order > 2) {
                    if (attribute.equals(Constants.CUSTOM_FIELD_PENDING_TYPE)) {
                        pendingTab.addFieldScreenLayoutItem(customField.getId());
                        outOfScopeTab.addFieldScreenLayoutItem(customField.getId());
                        listCustomFieldPendingScreen.append(customField.getId()).append(Constants.COMMA);
                        listCustomFieldOutOfScopeScreen.append(customField.getId());
                        listCustomFieldOutOfScopeScreen.append(Constants.COMMA);
                    }
                    if (order > 3) {
                        ngTab.addFieldScreenLayoutItem(customField.getId());
                        if (isNotNeedValidation(attribute) && order < 22) {
                            listCustomFieldNgScreen.append(customField.getId());
                            listCustomFieldNgScreen.append(Constants.COMMA);
                        }
                        if (!attribute.equals(Constants.CUSTOM_FIELD_RTC_CODE) && !attribute.equals(Constants.CUSTOM_FIELD_EXTERNAL_TICKET_ID)) {
                            okTab.addFieldScreenLayoutItem(customField.getId());
                            if (isNotNeedValidation(attribute)  && order < 22) {
                                listCustomFieldOkScreen.append(customField.getId());
                                listCustomFieldOkScreen.append(Constants.COMMA);
                            }
                        }
                        if (order >= 12 && order < 19) {
                            pendingTab.addFieldScreenLayoutItem(customField.getId());
                            outOfScopeTab.addFieldScreenLayoutItem(customField.getId());
                            if (isNotNeedValidation(attribute) && !attribute.equals(Constants.CUSTOM_FIELD_TEST_DURATION)) {
                                listCustomFieldPendingScreen.append(customField.getId());
                                listCustomFieldPendingScreen.append(Constants.COMMA);
                                listCustomFieldOutOfScopeScreen.append(customField.getId());
                                listCustomFieldOutOfScopeScreen.append(Constants.COMMA);
                            }

                        }


                            else if(order >= 22){
                            pendingTab.addFieldScreenLayoutItem(customField.getId());
                            outOfScopeTab.addFieldScreenLayoutItem(customField.getId());
                        }
                    }
                }
            }
            order++;
        }

        //REMOVE FIRST 3 DEFAULT SYSTEM FIELDS
        for (int i = 0; i < 3; i++) {
            okTab.removeFieldScreenLayoutItem(0);
            pendingTab.removeFieldScreenLayoutItem(0);
            ngTab.removeFieldScreenLayoutItem(0);
            outOfScopeTab.removeFieldScreenLayoutItem(0);
        }
    }

    private boolean isNotNeedValidation(String attribute){
        return !attribute.equals(Constants.CUSTOM_FIELD_RESULT_COMMENT) && !attribute.equals(Constants.CUSTOM_FIELD_TESTER_REMARK) && !attribute.equals(Constants.CUSTOM_FIELD_CORRECTION_REQUESTS);
    }


    private void taskScreen(Map<String, FieldScreen> createdScreens, StringBuilder listCustomFieldTaskScreen) {
        FieldScreen taskScreen = createdScreens.get(Constants.KEY_TASK_SCREEN);
        FieldScreenTab taskTab = taskScreen.getTab(0);
        for (String attribute : Utilities.listTaskAttribute()) {
            CustomField customField = this.customFieldManager.getCustomFieldObjectByName(attribute);
            if (customField != null) {
                taskTab.addFieldScreenLayoutItem(customField.getId(), Constants.CUSTOM_FIELD_BEGIN_POSITION);
                listCustomFieldTaskScreen.append(customField.getId());
                listCustomFieldTaskScreen.append(Constants.COMMA);
            }
        }
    }

    private void editWorkflow(ConfigureData configureData, StringBuilder listCustomFieldCreateScreen,
                              StringBuilder listCustomFieldOkScreen, StringBuilder listCustomFieldNgScreen,
                              StringBuilder listCustomFieldPendingScreen, StringBuilder listCustomFieldOutOfScopeScreen,
                              StringBuilder listCustomFieldTaskScreen) {
        Map<String, FieldScreen> createdScreens = configureData.createdScreens();
        Map<String, JiraWorkflow> createdWorkflows = configureData.createdWorkflows();
        JiraWorkflow testcaseWorkflow = createdWorkflows.get(Constants.KEY_WORKFLOW_TESTCASE);
        Collection<ActionDescriptor> allTestcaseActions = testcaseWorkflow.getAllActions();
        Class fieldValidatorClass = FieldValidator.class;
        Class testResultConditionClass = TestResultCondition.class;
        Map<String, String> metaAttributes;
        FieldScreen fieldScreen;
        for (ActionDescriptor action : allTestcaseActions) {
            metaAttributes = new HashMap<>();
            switch (action.getName()) {
                case Constants.WORKFLOW_TESTCASE_ACTION_OK:
                    fieldScreen = createdScreens.get(Constants.KEY_OK_SCREEN);
                    metaAttributes.put(Constants.WORKFLOW_ATTRIBUTE, String.valueOf(fieldScreen.getId()));
                    action.setView(Constants.WORKFLOW_TESTCASE_ACTION_OK);
                    action.setMetaAttributes(metaAttributes);
                    addValidator(action, listCustomFieldOkScreen.toString(), fieldValidatorClass);
                    addCondition(action, testResultConditionClass);
                    addPostFunctionForTestResultHistory(action);
                    break;
                case Constants.WORKFLOW_TESTCASE_ACTION_PENDING:
                    fieldScreen = createdScreens.get(Constants.KEY_PENDING_SCREEN);
                    metaAttributes.put(Constants.WORKFLOW_ATTRIBUTE, String.valueOf(fieldScreen.getId()));
                    action.setView(Constants.WORKFLOW_TESTCASE_ACTION_PENDING);
                    action.setMetaAttributes(metaAttributes);
                    addValidator(action, listCustomFieldPendingScreen.toString(), fieldValidatorClass);
                    addCondition(action, testResultConditionClass);
                    addPostFunctionForTestResultHistory(action);
                    break;
                case Constants.WORKFLOW_TESTCASE_ACTION_NG:
                    fieldScreen = createdScreens.get(Constants.KEY_NG_SCREEN);
                    metaAttributes.put(Constants.WORKFLOW_ATTRIBUTE, String.valueOf(fieldScreen.getId()));
                    action.setView(Constants.WORKFLOW_TESTCASE_ACTION_NG);
                    action.setMetaAttributes(metaAttributes);
                    addValidator(action, listCustomFieldNgScreen.toString(), fieldValidatorClass);
                    addCondition(action, testResultConditionClass);
                    addPostFunctionForTestResultHistory(action);
                    break;
                case Constants.WORKFLOW_TESTCASE_ACTION_OUT_OF_SCOPE:
                    fieldScreen = createdScreens.get(Constants.KEY_OUT_OF_SCOPE_SCREEN);
                    metaAttributes.put(Constants.WORKFLOW_ATTRIBUTE, String.valueOf(fieldScreen.getId()));
                    action.setView(Constants.WORKFLOW_TESTCASE_ACTION_OUT_OF_SCOPE);
                    action.setMetaAttributes(metaAttributes);
                    addValidator(action, listCustomFieldOutOfScopeScreen.toString(), fieldValidatorClass);
                    addCondition(action, testResultConditionClass);
                    addPostFunctionForTestResultHistory(action);
                    break;
                case Constants.WORKFLOW_TESTCASE_ACTION_CREATE:
                    addValidator(action, listCustomFieldCreateScreen.toString(), fieldValidatorClass);
                    addPostFunction(action);
                    break;
                case Constants.WORKFLOW_TESTCASE_ACTION_PLAN:
                    fieldScreen = createdScreens.get(Constants.KEY_PLAN_SCREEN);
                    metaAttributes.put(Constants.WORKFLOW_ATTRIBUTE, String.valueOf(fieldScreen.getId()));
                    action.setView(Constants.WORKFLOW_TESTCASE_ACTION_PLAN);
                    action.setMetaAttributes(metaAttributes);
                    addCondition(action, PlanTestcaseCondition.class);
                    addPlanValidator(action, this.customFieldManager.getCustomFieldObjectByName(
                            Constants.CUSTOM_FIELD_START_DATE).getId(),
                                     this.customFieldManager.getCustomFieldObjectByName(Constants.CUSTOM_FIELD_END_DATE)
                                                            .getId());
                    break;
                default:
                    break;
            }
        }

        JiraWorkflow taskWorkflow = createdWorkflows.get(Constants.KEY_WORKFLOW_TASK);
        Collection<ActionDescriptor> allTaskActions = taskWorkflow.getAllActions();
        for (ActionDescriptor action : allTaskActions) {
            if (action.getName().equalsIgnoreCase(Constants.WORKFLOW_ENVIRONMENT_ACTION_CREATE)) {
                fieldScreen = createdScreens.get(Constants.KEY_TASK_SCREEN);
                metaAttributes = new HashMap<>();
                metaAttributes.put(Constants.WORKFLOW_ATTRIBUTE, String.valueOf(fieldScreen.getId()));
                action.setView(Constants.WORKFLOW_ENVIRONMENT_ACTION_CREATE);
                action.setMetaAttributes(metaAttributes);
                addValidator(action, listCustomFieldTaskScreen.toString(), FieldValidatorTask.class);
                break;
            }
        }

        WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();
        workflowManager.saveWorkflowWithoutAudit(testcaseWorkflow);
        workflowManager.saveWorkflowWithoutAudit(taskWorkflow);
    }

}
