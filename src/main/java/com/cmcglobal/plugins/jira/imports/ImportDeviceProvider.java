package com.cmcglobal.plugins.jira.imports;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.cmcglobal.plugins.service.UploadFileService;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ImportDeviceProvider extends AbstractJiraContextProvider {

    private static final Logger logger = LoggerFactory.getLogger(ImportProvider.class);

    @JiraImport
    private UploadFileService uploadFileService;

    public ImportDeviceProvider(UploadFileService uploadFileService) {
        this.uploadFileService = uploadFileService;
    }

    @Override
    public Map<String, Object> getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager.class);
        Project project = Helper.getCurrentProject(jiraHelper);
        Collection<ProjectRole> roles = projectRoleManager.getProjectRoles(applicationUser, project);
        Map<String, Object> contextMap = new HashMap<>();
        if(roles.contains(projectRoleManager.getProjectRole(Constants.PROJECT_ROLE_PM))) {
            contextMap.put("role", Constants.PROJECT_ROLE_PM);
        } else {
            contextMap.put("role", Constants.PROJECT_ROLE_QC_LEAD);
        }
        contextMap.put(Constants.LIST_FILE_UP_LOAD,
                this.uploadFileService.findAllByProjectId(Helper.getCurrentProject(jiraHelper).getId(), null));
        contextMap.put(Constants.PROJECT_ID, project.getId());
        contextMap.put(Constants.URI, ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL));
        return contextMap;
    }
}