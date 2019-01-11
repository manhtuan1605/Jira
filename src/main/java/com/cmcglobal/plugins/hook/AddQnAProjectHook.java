package com.cmcglobal.plugins.hook;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.project.template.hook.AddProjectHook;
import com.atlassian.jira.project.template.hook.ConfigureData;
import com.atlassian.jira.project.template.hook.ConfigureResponse;
import com.atlassian.jira.project.template.hook.ValidateData;
import com.atlassian.jira.project.template.hook.ValidateResponse;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Utilities;

import java.util.Collection;
import java.util.Map;

/**
 * Created by User on 7/17/2018.
 */
public class AddQnAProjectHook implements AddProjectHook {

    @JiraImport
    private final CustomFieldManager customFieldManager;

    public AddQnAProjectHook(CustomFieldManager customFieldManager) {
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
        ConfigureResponse response = ConfigureResponse.create();
        Collection<CustomField> customFields;
        Map<String, FieldScreen> createdScreens = configureData.createdScreens();
        FieldScreen defaultQnAScreen = createdScreens.get(Constants.KEY_DEFAULT_QNA_SCREEN);
        FieldScreenTab qnATab = defaultQnAScreen.getTab(0);
        for (String attribute : Utilities.listQnAAttribute()) {
            customFields = this.customFieldManager.getCustomFieldObjectsByName(attribute);
            if (!customFields.isEmpty()) {
                qnATab.addFieldScreenLayoutItem(customFields.iterator().next().getId());
            }
        }
        FieldScreen defaultDefectScreen = createdScreens.get(Constants.KEY_DEFAULT_DEFECT_SCREEN);
        FieldScreenTab defectTab = defaultDefectScreen.getTab(0);
        for (String attribute : Utilities.listDefectAttribute()) {
            customFields = this.customFieldManager.getCustomFieldObjectsByName(attribute);
            if (!customFields.isEmpty()) {
                defectTab.addFieldScreenLayoutItem(customFields.iterator().next().getId());
            }
        }

        return response;
    }
}
