package com.cmcglobal.plugins.jira.validators;

import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;

import java.util.HashMap;
import java.util.Map;

public class FieldValidatorDeviceProjectFactory extends AbstractWorkflowPluginFactory
        implements WorkflowPluginValidatorFactory {
    public static final String FIELD_WORD = "word";

    protected void getVelocityParamsForInput(Map velocityParams) {
        //the default message
        velocityParams.put(FIELD_WORD, "test");
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

        velocityParams.put(FIELD_WORD, validatorDescriptor.getArgs().get(FIELD_WORD));
    }

    public Map getDescriptorParams(Map validatorParams) {
        // Process The map
        Map params = new HashMap();
        params.put(FIELD_WORD, extractSingleParam(validatorParams, FIELD_WORD));
        return params;
    }
}
