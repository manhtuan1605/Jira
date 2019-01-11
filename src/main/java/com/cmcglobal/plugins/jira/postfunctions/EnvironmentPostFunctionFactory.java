package com.cmcglobal.plugins.jira.postfunctions;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the factory class responsible for dealing with the UI for the post-function.
 * This is typically where you put default values into the velocity context and where you store user input.
 */

public class EnvironmentPostFunctionFactory extends AbstractWorkflowPluginFactory
        implements WorkflowPluginFunctionFactory {

    private static final String FIELD_MESSAGE = "messageField";

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams) {
        //
    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        getVelocityParamsForInput(velocityParams);
        getVelocityParamsForView(velocityParams, descriptor);
    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        if (!(descriptor instanceof FunctionDescriptor)) {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        }

        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
        String message = (String) functionDescriptor.getArgs().get(FIELD_MESSAGE);

        if (message == null) {
            message = "No Message";
        }

        velocityParams.put(FIELD_MESSAGE, message);
    }

    public Map<String, ?> getDescriptorParams(Map<String, Object> formParams) {
        Map<String, String> params = new HashMap<>();

        // Process The map
        String message = extractSingleParam(formParams, FIELD_MESSAGE);
        params.put(FIELD_MESSAGE, message);

        return params;
    }

}