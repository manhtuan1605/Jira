package com.cmcglobal.plugins.jira.imports;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.cmcglobal.plugins.service.TestingPhasesService;
import com.cmcglobal.plugins.service.UploadFileService;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportProvider extends AbstractJiraContextProvider {

    private static final Logger logger = LoggerFactory.getLogger(ImportProvider.class);

    @JiraImport
    private TestingPhasesService testingPhasesService;

    @JiraImport
    private UploadFileService uploadFileService;

    public ImportProvider(TestingPhasesService testingPhasesService, UploadFileService uploadFileService) {
        this.testingPhasesService = testingPhasesService;
        this.uploadFileService = uploadFileService;
    }

    private List<Option> getOptions(CustomField customField) {
        List<Option> options = new ArrayList<>();
        List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();
        if (!schemes.isEmpty()) {
            FieldConfigScheme scheme = schemes.get(0);
            Map configs = scheme.getConfigsByConfig();
            if (configs != null && !configs.isEmpty()) {
                FieldConfig config = (FieldConfig) configs.keySet().iterator().next();
                OptionsManager optionsManager = ComponentManager.getComponentInstanceOfType(OptionsManager.class);
                options = optionsManager.getOptions(config).getRootOptions();
            }
        }
        return options;
    }

    private List<Option> getOptions(CustomField customField, final Long projectId) {
        List<Option> options = new ArrayList<>();
        final List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();
        if (!schemes.isEmpty()) {
            FieldConfigScheme fieldConfigScheme = null;
//            final Long projectId = Helper.getCurrentProject(jiraHelper).getId();
            for (FieldConfigScheme scheme : schemes) {
                if (scheme.getAssociatedProjectIds().contains(projectId)) {
                    fieldConfigScheme = scheme;
                    break;
                }
            }
            if (fieldConfigScheme != null) {
                Map configs = fieldConfigScheme.getConfigsByConfig();
                if (!CollectionUtils.isEmpty(configs)) {
                    FieldConfig config = (FieldConfig) configs.keySet().iterator().next();
                    OptionsManager optionsManager = ComponentManager.getComponentInstanceOfType(OptionsManager.class);
                    options = optionsManager.getOptions(config).getRootOptions();
                }
            }
        }
        return options;
    }

    @Override
    public Map<String, Object> getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        Map<String, Object> contextMap = new HashMap<>();
        CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
        CustomField customField = customFieldManager.getCustomFieldObjectByName(Constants.CUSTOM_FIELD_TESTCASE_TYPE);
        contextMap.put(Constants.TEST_CASE_TYPES, getOptions(customField, Helper.getCurrentProject(jiraHelper).getId()));
        customField = customFieldManager.getCustomFieldObjectByName(Constants.CUSTOM_FIELD_PHASE);
        contextMap.put(Constants.PHASES, getOptions(customField, Helper.getCurrentProject(jiraHelper).getId()));
        contextMap.put(Constants.LIST_FILE_UP_LOAD,
                       this.uploadFileService.findAllByProjectId(Helper.getCurrentProject(jiraHelper).getId(), null));
        contextMap.put(Constants.PROJECT_ID, Helper.getCurrentProject(jiraHelper).getId());
        contextMap.put(Constants.URI, ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL));
        return contextMap;
    }
}