package com.cmcglobal.plugins.jira.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.cmcglobal.plugins.utils.Constants;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;

import java.util.Map;

public class TestCasePlanValidatorFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginValidatorFactory
{

    protected void getVelocityParamsForInput(Map velocityParams)
    {

        velocityParams.put(Constants.VALIDATOR_DESCRIPTOR_OBJECT_LIST_CUSTOM_FIELD, ComponentAccessor.getCustomFieldManager().getCustomFieldObjects());
    }

    protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor)
    {
        getVelocityParamsForInput(velocityParams);
        getVelocityParamsForView(velocityParams, descriptor);
    }

    protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor)
    {
        if (!(descriptor instanceof ValidatorDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a ValidatorDescriptor.");
        }
        ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor) descriptor;
        velocityParams.put(Constants.VALIDATOR_DESCRIPTOR_CF_START_DATE, Constants.CUSTOM_FIELD_START_DATE);
        velocityParams.put(Constants.VALIDATOR_DESCRIPTOR_CF_END_DATE, Constants.CUSTOM_FIELD_END_DATE);
    }

    public Map getDescriptorParams(Map validatorParams)
    {
        String startDate = extractSingleParam(validatorParams, Constants.VALIDATOR_DESCRIPTOR_CF_START_DATE);
        String endDate = extractSingleParam(validatorParams, Constants.VALIDATOR_DESCRIPTOR_CF_END_DATE);
        return EasyMap.build(Constants.VALIDATOR_DESCRIPTOR_CF_START_DATE, startDate,Constants.VALIDATOR_DESCRIPTOR_CF_END_DATE,endDate);
    }

}
