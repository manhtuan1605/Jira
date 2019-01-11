package com.cmcglobal.plugins.jira.validators;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
import com.cmcglobal.plugins.utils.Constants;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

import java.util.Map;

public class FieldValidatorFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginValidatorFactory {
    protected void getVelocityParamsForInput(Map velocityParams) {
        //the default message
        velocityParams.put(Constants.VALIDATOR_DESCRIPTOR_OBJECT_LIST_CUSTOM_FIELD,
                           ComponentAccessor.getCustomFieldManager().getCustomFieldObjects());
    }

    protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor) {
        getVelocityParamsForInput(velocityParams);
        getVelocityParamsForView(velocityParams, descriptor);
    }

    protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor) {
        if (!(descriptor instanceof ValidatorDescriptor)) {
            throw new IllegalArgumentException("Descriptor must be a ValidatorDescriptor.");
        }
        ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor) descriptor;
        velocityParams.put(Constants.VALIDATOR_DESCRIPTOR_ARGS_CUSTOM_FIELD,
                           validatorDescriptor.getArgs().get(Constants.VALIDATOR_DESCRIPTOR_ARGS_CUSTOM_FIELD));
        velocityParams.put(Constants.VALIDATOR_DESCRIPTOR_OBJECT_CUSTOM_FIELD_NAME, "List of Custom field");
    }

    public Map<String, ?> getDescriptorParams(Map validatorParams) {
        // Process The map
        String selectedCustomField = extractSingleParam(validatorParams,
                                                        Constants.VALIDATOR_DESCRIPTOR_ARGS_CUSTOM_FIELD);
        return EasyMap.build(Constants.VALIDATOR_DESCRIPTOR_ARGS_CUSTOM_FIELD, selectedCustomField);
    }
}
