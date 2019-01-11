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
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.cmcglobal.plugins.jira.postfunctions.DevicePostFunction;
import com.cmcglobal.plugins.jira.validators.FieldValidatorDeviceProject;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Helper;
import com.cmcglobal.plugins.utils.Utilities;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

import java.util.Collection;
import java.util.Map;

public class AddDevicesProjectHook implements AddProjectHook {

    @JiraImport
    private final CustomFieldManager customFieldManager;

    public AddDevicesProjectHook(CustomFieldManager customFieldManager) {
        this.customFieldManager = customFieldManager;
    }

    @Override
    public ValidateResponse validate(final ValidateData validateData) {
        ValidateResponse validateResponse = ValidateResponse.create();
        if (validateData.projectKey().equals(Constants.TEST)) {
            validateResponse.addErrorMessage(Constants.INVALID_PROJECT_KEY);
        }

        return validateResponse;
    }

    @Override
    public ConfigureResponse configure(final ConfigureData configureData) {
        Map<String, FieldScreen> createdScreens = configureData.createdScreens();
        Map<String, JiraWorkflow> createdWorkflows = configureData.createdWorkflows();
        Project project = configureData.project();

        //ADD CUSTOM FIELD TO AUTOMOTIVE DEVICE SCREEN
        automotiveDeviceScreen(createdScreens);

        //ADD CUSTOM FIELD TO PERIPHERAL DEVICE SCREEN
        peripheralDeviceScreen(createdScreens, project);

        //ADD CUSTOM FIELD TO EXTERNAL DEVICE ID SCREEN
        StringBuilder listCustomFieldBluetooth = new StringBuilder();
        StringBuilder listCustomFieldUsb = new StringBuilder();
        externalDeviceIdScreen(createdScreens, listCustomFieldBluetooth, listCustomFieldUsb);

        //ADD SCREEN TO WORKFLOW'S TRANSITION
        editWorkflow(createdWorkflows, listCustomFieldBluetooth, listCustomFieldUsb);

        //ADD DEVICE PERMISSION SCHEME
        Helper.addPermissionScheme(configureData.project(), Constants.PERMISSION_SCHEME_NAME_DEVICE);

        return ConfigureResponse.create();
    }

    private void addValidator(ActionDescriptor action, String listCustomFieldBluetooth, String listCustomFieldUsb) {
        ValidatorDescriptor validatorDescriptor = DescriptorFactory.getFactory().createValidatorDescriptor();
        validatorDescriptor.setType(Constants.VALIDATOR_DESCRIPTOR_TYPE);
        final Map validatorArgs = validatorDescriptor.getArgs();
        validatorArgs.put(Constants.VALIDATOR_DESCRIPTOR_ARGS_CLASS_NAME, FieldValidatorDeviceProject.class.getName());
        validatorArgs.put(Constants.VALIDATOR_DESCRIPTOR_ARGS_LIST_BLUETOOTH, listCustomFieldBluetooth);
        validatorArgs.put(Constants.VALIDATOR_DESCRIPTOR_ARGS_LIST_USB, listCustomFieldUsb);
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
        functionDescriptorArgs.put(Constants.VALIDATOR_DESCRIPTOR_ARGS_CLASS_NAME, DevicePostFunction.class.getName());
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

    private void automotiveDeviceScreen(Map<String, FieldScreen> createdScreens) {
        FieldScreen defaultAutomotiveDeviceScreen = createdScreens.get(Constants.AUTOMOTIVE_DEVICE_DEFAULT_SCREEN);
        FieldScreenTab automotiveDeviceTab = defaultAutomotiveDeviceScreen.getTab(0);
        CustomField customField;
        for (String attribute : Utilities.listAutomotiveDevice()) {
            customField = this.customFieldManager.getCustomFieldObjectByName(attribute);
            if (customField != null) {
                automotiveDeviceTab.addFieldScreenLayoutItem(customField.getId());
            }
        }
    }

    private void peripheralDeviceScreen(Map<String, FieldScreen> createdScreens, Project project) {
        FieldScreen defaultPeripheralDeviceScreen = createdScreens.get(Constants.PERIPHERAL_DEVICE_DEFAULT_SCREEN);
        FieldScreenTab peripheralDeviceTab = defaultPeripheralDeviceScreen.getTab(0);
        CustomField customField;
        for (String attribute : Utilities.listPeripheralDevice()) {
            customField = this.customFieldManager.getCustomFieldObjectByName(attribute);
            if (customField != null) {
                peripheralDeviceTab.addFieldScreenLayoutItem(customField.getId());
                if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_EXTERNAL_DEVICE_TYPE)) {
                    //CREATE CONFIGURATION SCHEME
                    Helper.addConfigurationScheme(project, customField);
                }
            }
        }
    }

    private void externalDeviceIdScreen(Map<String, FieldScreen> createdScreens, StringBuilder listCustomFieldBluetooth,
                                        StringBuilder listCustomFieldUsb) {
        FieldScreen defaultExternalDeviceScreen = createdScreens.get(Constants.EXTERNAL_DEVICE_DEFAULT_SCREEN);
        FieldScreenTab externalDeviceTab = defaultExternalDeviceScreen.getTab(0);
        FieldScreen createExternalDeviceScreen = createdScreens.get(Constants.EXTERNAL_DEVICE_CREATE_SCREEN);
        FieldScreenTab createExternalDeviceTab = createExternalDeviceScreen.getTab(0);
        int order = 0;
        CustomField customField;
        for (String attribute : Utilities.listExternalDevice()) {
            customField = this.customFieldManager.getCustomFieldObjectByName(attribute);
            if (customField != null) {
                externalDeviceTab.addFieldScreenLayoutItem(customField.getId());
                if (order > 0) {
                    createExternalDeviceTab.addFieldScreenLayoutItem(customField.getId());
                    if (order < 7) {
                        listCustomFieldBluetooth.append(customField.getId());
                        listCustomFieldBluetooth.append(Constants.COMMA);
                    } else if (order < 9) {
                        listCustomFieldUsb.append(customField.getId());
                        listCustomFieldUsb.append(Constants.COMMA);
                    }
                }
            }
            order++;
        }
    }

    private void editWorkflow(Map<String, JiraWorkflow> createdWorkflows, StringBuilder listCustomFieldBluetooth,
                              StringBuilder listCustomFieldUsb) {
        JiraWorkflow jiraWorkflow = createdWorkflows.get(Constants.KEY_WORKFLOW_DEVICE);
        Collection<ActionDescriptor> allActions = jiraWorkflow.getAllActions();
        for (ActionDescriptor action : allActions) {
            if (action.getName().equalsIgnoreCase(Constants.WORKFLOW_ENVIRONMENT_ACTION_CREATE)) {
                addValidator(action, listCustomFieldBluetooth.toString(), listCustomFieldUsb.toString());
                addPostFunction(action);
                break;
            }
        }
        ComponentAccessor.getWorkflowManager().saveWorkflowWithoutAudit(jiraWorkflow);
    }

}
