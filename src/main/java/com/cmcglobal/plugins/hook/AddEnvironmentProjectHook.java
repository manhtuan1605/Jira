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
import com.cmcglobal.plugins.jira.postfunctions.EnvironmentPostFunction;
import com.cmcglobal.plugins.jira.validators.FieldValidatorEnviromentProject;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Helper;
import com.cmcglobal.plugins.utils.Utilities;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

import java.util.Collection;
import java.util.Map;

public class AddEnvironmentProjectHook implements AddProjectHook {

    @JiraImport
    private final CustomFieldManager customFieldManager;

    public AddEnvironmentProjectHook(CustomFieldManager customFieldManager) {
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

        //ADD CUSTOM FIELD TO HU DEVICE SCREEN
        huDeviceScreen(createdScreens);

        //ADD CUSTOM FIELD TO SCREEN DEVICE SCREEN
        screenDeviceScreen(createdScreens);

        //ADD CUSTOM FIELD TO HU TYPE VEHICLE SCREEN
        huTypeVehicleScreen(createdScreens);

        //ADD CUSTOM FIELD TO SI SYS INFO SCREEN
        siSysInfoScreen(createdScreens);

        //ADD CUSTOM FIELD TO SI AMIGO INFO SCREEN
        siAmigoInfoScreen(createdScreens);

        //ADD CUSTOM FIELD TO SET OF DEVICE SCREEN
        setOfDeviceScreen(createdScreens, configureData.project());

        //ADD SCREEN TO WORKFLOW'S TRANSITION
        editWorkflow(createdWorkflows);

        return ConfigureResponse.create();
    }

    // validate here
    private void addValidator(ActionDescriptor action) {
        ValidatorDescriptor validatorDescriptor = DescriptorFactory.getFactory().createValidatorDescriptor();
        validatorDescriptor.setType(Constants.VALIDATOR_DESCRIPTOR_TYPE);
        validatorDescriptor.setName(Constants.VALIDATOR_DESCRIPTOR_NAME);
        final Map validatorArgs = validatorDescriptor.getArgs();
        validatorArgs.put(Constants.VALIDATOR_DESCRIPTOR_ARGS_CLASS_NAME,
                          FieldValidatorEnviromentProject.class.getName());
        boolean flag = true;
        for (Object validatorObject : action.getValidators()) {
            if (validatorObject instanceof ValidatorDescriptor) {
                ValidatorDescriptor thisValidator = (ValidatorDescriptor) validatorObject;
                if (thisValidator.getName().equals(validatorDescriptor.getName())) {
                    flag = false;
                    break;
                }
            }
        }
        if (flag) {
            action.getValidators().add(validatorDescriptor);
        }
    }

    // after validate
    private void addPostFunction(ActionDescriptor action) {
        FunctionDescriptor functionDescriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
        functionDescriptor.setType(Constants.VALIDATOR_DESCRIPTOR_TYPE);
        final Map functionDescriptorArgs = functionDescriptor.getArgs();
        functionDescriptorArgs.put(Constants.VALIDATOR_DESCRIPTOR_ARGS_CLASS_NAME,
                                   EnvironmentPostFunction.class.getName());
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

    private CustomField getCustomFieldEnvironment(String attribute) {
        CustomField customFieldEnvironment = null;
        Collection<CustomField> customFields = this.customFieldManager.getCustomFieldObjectsByName(attribute);
        for (CustomField customField : customFields) {
            if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_SELECT)) {
                customFieldEnvironment = customField;
            }
        }

        return customFieldEnvironment;
    }

    private void huDeviceScreen(Map<String, FieldScreen> createdScreens) {
        FieldScreen defaultHuDeviceScreen = createdScreens.get(Constants.HU_DEVICE_DEFAULT_SCREEN);
        FieldScreenTab huDeviceTab = defaultHuDeviceScreen.getTab(0);
        //GET AMP TYPE ENVIRONMENT CUSTOM FIELD
        CustomField customFieldAmpTypeEnvironment = getCustomFieldEnvironment(Constants.AMP_TYPE_ATTRIBUTE);
        CustomField customField;
        for (String attribute : Utilities.listHuDeviceAttribute()) {
            customField = attribute.equals(Constants.AMP_TYPE_ATTRIBUTE) ?
                          customFieldAmpTypeEnvironment :
                          this.customFieldManager.getCustomFieldObjectByName(attribute);
            if (customField != null) {
                huDeviceTab.addFieldScreenLayoutItem(customField.getId());
            }
        }
    }

    private void screenDeviceScreen(Map<String, FieldScreen> createdScreens) {
        FieldScreen defaultScreenDeviceScreen = createdScreens.get(Constants.SCREEN_DEVICE_DEFAULT_SCREEN);
        FieldScreenTab screenDeviceTab = defaultScreenDeviceScreen.getTab(0);
        CustomField customField;
        for (String attribute : Utilities.listScreenDeviceAttribute()) {
            customField = this.customFieldManager.getCustomFieldObjectByName(attribute);
            if (customField != null) {
                screenDeviceTab.addFieldScreenLayoutItem(customField.getId());
            }
        }
    }

    private void huTypeVehicleScreen(Map<String, FieldScreen> createdScreens) {
        FieldScreen defaultHuTypeVehicleScreen = createdScreens.get(Constants.HU_TYPE_VEHICLE_DEFAULT_SCREEN);
        FieldScreenTab huTypeVehicleTab = defaultHuTypeVehicleScreen.getTab(0);
        //GET AMP TYPE ENVIRONMENT CUSTOM FIELD
        CustomField customFieldAmpTypeEnvironment = getCustomFieldEnvironment(Constants.AMP_TYPE_ATTRIBUTE);
        //GET VEHICLE ENVIRONMENT CUSTOM FIELD
        CustomField customFieldVehicleEnvironment = getCustomFieldEnvironment(Constants.VEHICLE_ATTRIBUTE);
        CustomField customField;
        for (String attribute : Utilities.listHuTypeVehicleAttribute()) {
            switch (attribute) {
                case Constants.VEHICLE_ATTRIBUTE:
                    customField = customFieldVehicleEnvironment;
                    break;
                case Constants.AMP_TYPE_ATTRIBUTE:
                    customField = customFieldAmpTypeEnvironment;
                    break;
                default:
                    customField = this.customFieldManager.getCustomFieldObjectByName(attribute);
                    break;
            }
            if (customField != null) {
                huTypeVehicleTab.addFieldScreenLayoutItem(customField.getId());
            }
        }
    }

    private void siSysInfoScreen(Map<String, FieldScreen> createdScreens) {
        FieldScreen defaultSiSysInfoScreen = createdScreens.get(Constants.SI_SYS_INFO_DEFAULT_SCREEN);
        FieldScreenTab siSysInfoTab = defaultSiSysInfoScreen.getTab(0);
        CustomField customField;
        for (String attribute : Utilities.listSiSysInfoAttribute()) {
            customField = this.customFieldManager.getCustomFieldObjectByName(attribute);
            if (customField != null) {
                siSysInfoTab.addFieldScreenLayoutItem(customField.getId());
            }
        }
    }

    private void siAmigoInfoScreen(Map<String, FieldScreen> createdScreens) {
        FieldScreen defaultSiAmigoInfoScreen = createdScreens.get(Constants.SI_AMIGO_INFO_DEFAULT_SCREEN);
        FieldScreenTab siAmigoInfoTab = defaultSiAmigoInfoScreen.getTab(0);
        //GET AMP TYPE ENVIRONMENT CUSTOM FIELD
        CustomField customFieldAmpTypeEnvironment = getCustomFieldEnvironment(Constants.AMP_TYPE_ATTRIBUTE);
        CustomField customField;
        for (String attribute : Utilities.listSiAmigoInfoAttribute()) {
            customField = attribute.equals(Constants.AMP_TYPE_ATTRIBUTE) ?
                          customFieldAmpTypeEnvironment :
                          this.customFieldManager.getCustomFieldObjectByName(attribute);
            if (customField != null) {
                siAmigoInfoTab.addFieldScreenLayoutItem(customField.getId());
            }
        }
    }

    private void setOfDeviceScreen(Map<String, FieldScreen> createdScreens, Project project) {
        FieldScreen defaultSetOfDeviceScreen = createdScreens.get(Constants.SET_OF_DEVICE_DEFAULT_SCREEN);
        FieldScreenTab setOfDeviceTab = defaultSetOfDeviceScreen.getTab(0);
        FieldScreen createSetOfDeviceScreen = createdScreens.get(Constants.SET_OF_DEVICE_CREATE_SCREEN);
        FieldScreenTab setOfDeviceCreateTab = createSetOfDeviceScreen.getTab(0);
        FieldScreen editSetOfDeviceScreen = createdScreens.get(Constants.SET_OF_DEVICE_EDIT_SCREEN);
        FieldScreenTab setOfDeviceEditTab = editSetOfDeviceScreen.getTab(0);
        //GET AMP TYPE ENVIRONMENT CUSTOM FIELD
        CustomField customFieldAmpTypeEnvironment = getCustomFieldEnvironment(Constants.AMP_TYPE_ATTRIBUTE);
        //GET VEHICLE ENVIRONMENT CUSTOM FIELD
        CustomField customFieldVehicleEnvironment = getCustomFieldEnvironment(Constants.VEHICLE_ATTRIBUTE);
        CustomField customField;
        for (String attribute : Utilities.listSetOfDeviceAttribute()) {
            switch (attribute) {
                case Constants.VEHICLE_ATTRIBUTE:
                    customField = customFieldVehicleEnvironment;
                    break;
                case Constants.AMP_TYPE_ATTRIBUTE:
                    customField = customFieldAmpTypeEnvironment;
                    break;
                case Constants.SI_INDEX_VALUE:
                    customField = null;
                    break;
                default:
                    customField = this.customFieldManager.getCustomFieldObjectByName(attribute);
                    break;
            }
            if (customField != null) {
                setOfDeviceTab.addFieldScreenLayoutItem(customField.getId());
                if (attribute.equalsIgnoreCase(Constants.BUILD_NUMBER_ATTRIBUTE) ||
                    attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_HU_DEVICE) ||
                    attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_SCREEN_DEVICE) ||
                    attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_VEHICLE_PARAMETER) ||
                    attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_EXTERNAL_TICKET_ID) ||
                    attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_VR_TSS_VERSION) ||
                    attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_DATA_MAP) ||
                    attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_MODEL)) {
                    setOfDeviceCreateTab.addFieldScreenLayoutItem(customField.getId());
                }
                if (!attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_HU_INDEX) &&
                    !attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_SI_INDEX)) {
                    setOfDeviceEditTab.addFieldScreenLayoutItem(customField.getId());
                }
                if (attribute.equalsIgnoreCase(Constants.VEHICLE_ATTRIBUTE) ||
                    attribute.equalsIgnoreCase(Constants.TEST_HU_MARKET) ||
                    attribute.equalsIgnoreCase(Constants.MODEL)) {
                    //CREATE CONFIGURATION SCHEME
                    Helper.addConfigurationScheme(project, customField);
                }
            }
        }
    }

    private void editWorkflow(Map<String, JiraWorkflow> createdWorkflows) {
        JiraWorkflow jiraWorkflow = createdWorkflows.get(Constants.KEY_WORKFLOW_ENVIRONMENT);
        Collection<ActionDescriptor> allActions = jiraWorkflow.getAllActions();
        for (ActionDescriptor action : allActions) {
            if (action.getName().equalsIgnoreCase(Constants.WORKFLOW_ENVIRONMENT_ACTION_CREATE)) {
                addValidator(action);
                addPostFunction(action);
                break;
            }
        }
        ComponentAccessor.getWorkflowManager().saveWorkflowWithoutAudit(jiraWorkflow);
    }

}
