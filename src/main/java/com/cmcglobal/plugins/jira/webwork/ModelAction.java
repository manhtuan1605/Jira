package com.cmcglobal.plugins.jira.webwork;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.cmcglobal.plugins.dto.ModelDTO;
import com.cmcglobal.plugins.entity.Model;
import com.cmcglobal.plugins.service.ModelService;
import com.cmcglobal.plugins.utils.Constants;
import com.google.gson.Gson;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ModelAction extends JiraWebActionSupport {
    @JiraImport
    private ModelService modelService;
    private List<Model>  models;
    private String       projectId;
    private String       modelName;
    private String       modelId;

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public List<Model> getModels() {
        return models;
    }

    public void setModels(List<Model> models) {
        this.models = models;
    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public String doDefault() throws Exception {
        final Collection<IssueType> issueTypes = getSelectedProject().getIssueTypes();
        for (IssueType issueType : issueTypes) {
            if (issueTypes.size() == Constants.NUMBER_ISSUE_TYPE &&
                Constants.SET_OF_DEVICE.equals(issueType.getName())) {
                models = modelService.findByProjectId(getSelectedProject().getId());
                return Constants.PAGE_VIEW_LIST;
            }
        }
        return Constants.PAGE_NOT_EXIST;
    }

    //    create Model
    public void doCreate() throws IOException {
        doValidateNewModel();
        if (!hasAnyErrors()) {
            modelService.create(
                    new ModelDTO(0, Long.parseLong(projectId), modelName, new Date(), getLoggedInUser().getUsername(),
                                 null, null));
            updateListOption(Constants.ACTION_CREATE, modelName, null);
            models = modelService.findByProjectId(Long.parseLong(projectId));
            String json = new Gson().toJson(convertToModelDto(models));
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_OK);
        }
    }

    public void doValidateNewModel() throws IOException {
        if (!StringUtils.isEmpty(modelName)) {
            modelName = modelName.trim();
        }
        if (StringUtils.isEmpty(modelName)) {
            addError(Constants.MODEL_NAME, Constants.MODEL_IS_REQUIRED);
        } else if (modelService.isExistedModelName(modelName, Long.parseLong(projectId))) {
            addError(Constants.MODEL_NAME, Constants.MODEL_IS_EXISTED);
        } else if (modelName.length() > Constants.MAX_CHARACTER) {
            addError(Constants.MODEL_NAME, Constants.MODEL_MORE_THAN_100);
        }

        if (hasAnyErrors()) {
            String json = new Gson().toJson(getErrors());
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // get current Model to load form edit
    public void doGetCurrentModel() throws IOException {
        if (modelService.findByModelId(Long.parseLong(modelId)) != null) {
            String json = new Gson().toJson(modelService.findByModelId(Long.parseLong(modelId)));
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_OK);
        } else {
            getHttpResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // edit
    public void doEdit() throws IOException {
        doValidateEditModel();
        // update
        if (!hasAnyErrors()) {
            ModelDTO modelDTO = modelService.findByModelId(Long.parseLong(modelId));
            String oldModelName = modelDTO.getModelName();
            modelDTO.setUpdateDate(new Date());
            modelDTO.setUpdateUser(getLoggedInUser().getUsername());
            modelDTO.setModelName(modelName);
            modelService.update(modelDTO);
            updateListOption(Constants.ACTION_EDIT, modelName, oldModelName);
            String json = new Gson().toJson(modelDTO);
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_OK);
        } else {
            getHttpResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // validate edit
    private void doValidateEditModel() throws IOException {
        if (StringUtils.isEmpty(modelId) || !modelService.findById(Long.parseLong(modelId))) {
            addError(Constants.MODEL_ID, Constants.MODEL_IS_NOT_EXISTED);
        }
        if (!StringUtils.isEmpty(modelName)) {
            modelName = modelName.trim();
        }
        if (StringUtils.isEmpty(modelName)) {
            addError(Constants.MODEL_NAME, Constants.MODEL_IS_REQUIRED);
        } else if (modelService.isExistedModelNameSecond(modelName, Long.parseLong(modelId),
                                                         Long.parseLong(projectId))) {
            addError(Constants.MODEL_NAME, Constants.MODEL_IS_EXISTED);
        } else if (modelName.length() > Constants.MAX_CHARACTER) {
            addError(Constants.MODEL_NAME, Constants.MODEL_MORE_THAN_100);
        }
        if (hasAnyErrors()) {
            String json = new Gson().toJson(getErrors());
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // convert to Model DTO
    private List<ModelDTO> convertToModelDto(List<Model> models) {
        if (models.isEmpty()) {
            return Collections.<ModelDTO>emptyList();
        }
        List<ModelDTO> modelDTOS = new ArrayList<>();
        for (Model model : models) {
            ModelDTO modelDTO = new ModelDTO();
            modelDTO.setId(model.getID());
            modelDTO.setModelName(model.getModelName());
            modelDTO.setProjectId(model.getProjectId());
            modelDTO.setCreateUser(model.getCreateUser());
            modelDTO.setCreateDate(model.getCreateDate());
            modelDTO.setUpdateUser(model.getUpdateUser());
            modelDTO.setUpdateDate(model.getUpdateDate());
            modelDTOS.add(modelDTO);
        }
        return modelDTOS;
    }

    //ADD MODEL OPTION IN CREATE SET OF DEVICE SCREEN
    private void updateListOption(String action, String newOption, String oldOption) {
        CustomField customField = ComponentAccessor.getCustomFieldManager()
                                                   .getCustomFieldObjectByName(Constants.CUSTOM_FIELD_MODEL);
        List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();

        FieldConfigScheme fieldConfigScheme = null;
        for (FieldConfigScheme scheme : schemes) {
            if (scheme.getAssociatedProjectIds().contains(Long.parseLong(projectId))) {
                fieldConfigScheme = scheme;
                break;
            }
        }
        //add or update value list option of customfield model
        if (fieldConfigScheme != null) {
            Map configs = fieldConfigScheme.getConfigsByConfig();
            if (configs != null && !configs.isEmpty()) {
                FieldConfig config = (FieldConfig) configs.keySet().iterator().next();
                OptionsManager optionsManager = ComponentManager.getComponentInstanceOfType(OptionsManager.class);
                switch (action) {
                    case Constants.ACTION_CREATE:
                        long numberAdded = 100;
                        optionsManager.createOption(config, null, numberAdded, newOption);
                        break;
                    case Constants.ACTION_EDIT:
                        List<Option> options = optionsManager.getOptions(config);
                        for (Option option : options) {
                            if (option.getValue().equalsIgnoreCase(oldOption)) {
                                optionsManager.setValue(option, newOption);
                                break;
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
