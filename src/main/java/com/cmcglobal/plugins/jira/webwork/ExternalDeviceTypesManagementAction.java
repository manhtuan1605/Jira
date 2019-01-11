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
import com.cmcglobal.plugins.dto.ExternalDeviceTypeDTO;
import com.cmcglobal.plugins.entity.ExternalDeviceType;
import com.cmcglobal.plugins.service.ExternalDeviceTypeService;
import com.cmcglobal.plugins.utils.Constants;
import com.google.gson.Gson;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ExternalDeviceTypesManagementAction extends JiraWebActionSupport {
    @JiraImport
    private ExternalDeviceTypeService externalDeviceService;
    private List<ExternalDeviceType>  deviceTypes;
    private String                    deviceId;
    private String                    deviceType;
    private String                    pid;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public List<ExternalDeviceType> getDeviceTypes() {
        return deviceTypes;
    }

    public ExternalDeviceTypesManagementAction(ExternalDeviceTypeService externalDeviceService) {
        this.externalDeviceService = externalDeviceService;
    }

    public String doDefault() {
        final String[] issueTypesName = { Constants.ISSUE_TYPE_AUTOMOTIVE_DEVICE,
                                          Constants.ISSUE_TYPE_PERIPHERAL_DEVICE,
                                          Constants.ISSUE_TYPE_EXTERNAL_DEVICE };
        final Collection<IssueType> issueTypes = getSelectedProject().getIssueTypes();
        for (IssueType issueType : issueTypes) {
            if (!Arrays.asList(issueTypesName).contains(issueType.getName())) {
                return "page-not-exist";
            }
        }
        deviceTypes = this.externalDeviceService.findByProjectId(getSelectedProject().getId());
        return Constants.EXTERNAL_DEVICE_TYPE_VIEW;
    }

    // validate create type
    private void doValidateNewDeviceType() throws IOException {
        if (!StringUtils.isEmpty(deviceType)) {
            deviceType = deviceType.trim();
        }
        if (StringUtils.isEmpty(deviceType)) {
            addError(Constants.EXTERNAL_DEVICE_TYPE, Constants.EXTERNAL_DEVICE_IS_REQUIRED);
        } else if (externalDeviceService.isExistedDeviceType(deviceType, Long.parseLong(pid))) {
            addError(Constants.EXTERNAL_DEVICE_TYPE, Constants.EXTERNAL_DEVICE_ID_EXISTED);
        } else if (deviceType.length() > 100) {
            addError(Constants.EXTERNAL_DEVICE_TYPE, Constants.EXTERNAL_DEVICE_RANGE);
        }
        if (hasAnyErrors()) {
            String json = new Gson().toJson(getErrors());
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // create type
    public void doCreateDeviceType() throws IOException {
        doValidateNewDeviceType();
        if (!hasAnyErrors()) {
            externalDeviceService.create(new ExternalDeviceTypeDTO(-1, Long.parseLong(pid), deviceType, new Date(),
                                                                   getLoggedInUser().getUsername(), null, null, true));
            updateListOption(Constants.ACTION_CREATE, deviceType, null);
            deviceTypes = externalDeviceService.findByProjectId(Long.parseLong(pid));
            String json = new Gson().toJson(convertToDeviceTypeDto(deviceTypes));
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_OK);

        }
    }

    // check exist type
    public void doCheckDeviceType() {
        if (externalDeviceService.findById(Long.parseLong(deviceId))) {
            getHttpResponse().setStatus(HttpServletResponse.SC_OK);
        } else {
            getHttpResponse().setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }

    // validate edit
    private void doValidateEditDeviceType() throws IOException {
        if (StringUtils.isEmpty(deviceId) || !externalDeviceService.findById(Long.parseLong(deviceId))) {
            addError(Constants.EXTERNAL_DEVICE_ID, Constants.EXTERNAL_DEVICE_ID_NOT_EXIST);
        }
        if (!StringUtils.isEmpty(deviceType)) {
            deviceType = deviceType.trim();
        }
        if (StringUtils.isEmpty(deviceType)) {
            addError(Constants.EXTERNAL_DEVICE_TYPE, Constants.EXTERNAL_DEVICE_IS_REQUIRED);
        } else if (deviceType.length() > 100) {
            addError(Constants.EXTERNAL_DEVICE_TYPE, Constants.EXTERNAL_DEVICE_RANGE);
        } else if (externalDeviceService.isExistedTypeNameSecond(deviceType, Long.parseLong(deviceId),
                                                                 Long.parseLong(pid))) {
            addError(Constants.EXTERNAL_DEVICE_TYPE, Constants.EXTERNAL_DEVICE_ID_EXISTED);
        }
        if (hasAnyErrors()) {
            String json = new Gson().toJson(getErrors());
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // get curren phase lo load form edit
    public void doGetCurrentDeviceType() throws IOException {
        if (externalDeviceService.findByTypeId(Long.parseLong(deviceId)) != null) {
            String json = new Gson().toJson(externalDeviceService.findByTypeId(Long.parseLong(deviceId)));
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
        doValidateEditDeviceType();
        // update
        if (!hasAnyErrors()) {
            ExternalDeviceTypeDTO deviceTypeDTO = externalDeviceService.findByTypeId(Long.parseLong(deviceId));
            if (deviceTypeDTO != null) {
                String oldDeviceTypeName = deviceTypeDTO.getDeviceType();
                deviceTypeDTO.setDeviceType(deviceType);
                deviceTypeDTO.setUpdateDate(new Date());
                deviceTypeDTO.setUpdateUser(getLoggedInUser().getUsername());
                externalDeviceService.update(deviceTypeDTO);
                updateListOption(Constants.ACTION_EDIT, deviceType, oldDeviceTypeName);
                String json = new Gson().toJson(deviceTypeDTO);
                getHttpResponse().setContentType(Constants.CONTENT_TYPE);
                getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
                getHttpResponse().getWriter().write(json);
                getHttpResponse().setStatus(HttpServletResponse.SC_OK);
            } else {
                getHttpResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            }
        }

    }

    // convert to type DTO
    private List<ExternalDeviceTypeDTO> convertToDeviceTypeDto(List<ExternalDeviceType> deviceTypes) {
        if (deviceTypes.isEmpty()) {
            return Collections.<ExternalDeviceTypeDTO>emptyList();
        }
        List<ExternalDeviceTypeDTO> deviceTypeDTOs = new ArrayList<>();
        for (ExternalDeviceType type : deviceTypes) {
            ExternalDeviceTypeDTO deviceTypeDTO = new ExternalDeviceTypeDTO();
            deviceTypeDTO.setID(type.getID());
            deviceTypeDTO.setDeviceType(type.getDeviceType());
            deviceTypeDTO.setProjectId(type.getProjectId());
            deviceTypeDTOs.add(deviceTypeDTO);
        }
        return deviceTypeDTOs;
    }

    //ADD PHASE NAME TO TEST CASE TYPE OPTION IN CREATE TESTCASE SCREEN
    private void updateListOption(String action, String newOption, String oldOption) {
        CustomField customField = ComponentAccessor.getCustomFieldManager()
                                                   .getCustomFieldObjectByName(
                                                           Constants.CUSTOM_FIELD_EXTERNAL_DEVICE_TYPE);
        List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();
        FieldConfigScheme fieldConfigScheme = null;
        for (FieldConfigScheme scheme : schemes) {
            if (scheme.getAssociatedProjectIds().contains(Long.parseLong(pid))) {
                fieldConfigScheme = scheme;
                break;
            }
        }
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
