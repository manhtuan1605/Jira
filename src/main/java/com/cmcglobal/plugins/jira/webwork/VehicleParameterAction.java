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
import com.cmcglobal.plugins.dto.VehicleParameterDTO;
import com.cmcglobal.plugins.entity.VehicleParameter;
import com.cmcglobal.plugins.service.VehicleParameterService;
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

public class VehicleParameterAction extends JiraWebActionSupport {
    @JiraImport
    private VehicleParameterService vehicleParameterService;
    private String                  vehicleParameterName;
    private List<VehicleParameter>  vehicles;
    private String                  projectId;
    private String                  vehicleParameterId;

    public String getVehicleParameterId() {
        return vehicleParameterId;
    }

    public void setVehicleParameterId(String vehicleParameterId) {
        this.vehicleParameterId = vehicleParameterId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public List<VehicleParameter> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<VehicleParameter> vehicles) {
        this.vehicles = vehicles;

    }

    public String getVehicleParameterName() {
        return vehicleParameterName;
    }

    public void setVehicleParameterName(String vehicleParameterName) throws Exception {
        this.vehicleParameterName = vehicleParameterName;
    }

    public VehicleParameterAction(VehicleParameterService vehicleParameterService) {
        this.vehicleParameterService = vehicleParameterService;
    }

    public String doDefault() throws Exception {
        final Collection<IssueType> issueTypes = getSelectedProject().getIssueTypes();
        for (IssueType issueType : issueTypes) {
            if (issueTypes.size() == 6 && Constants.SET_OF_DEVICE.equals(issueType.getName())) {
                vehicles = vehicleParameterService.findByProjectId(getSelectedProject().getId());
                return "vehicle-parameter";
            }
        }
        return "page-not-exist";
    }

    public void doValidateNewVehicleParameter() throws IOException {
        if (!StringUtils.isEmpty(vehicleParameterName)) {
            vehicleParameterName = vehicleParameterName.trim();
        }
        if (StringUtils.isEmpty(vehicleParameterName)) {
            addError(Constants.VEHICLE_PARAMETER_NAME, Constants.VEHICLE_PARAMETER_IS_REQUIRED);
        } else if (vehicleParameterService.isExistedVehicleParameterName(vehicleParameterName,
                                                                         Long.parseLong(projectId))) {
            addError(Constants.VEHICLE_PARAMETER_NAME, Constants.VEHICLE_PARAMETER_IS_EXISTED);
        } else if (vehicleParameterName.length() > 100) {
            addError(Constants.VEHICLE_PARAMETER_NAME, Constants.VEHICLE_PARAMETER_MORE_THAN_100);
        }

        if (hasAnyErrors()) {
            String json = new Gson().toJson(getErrors());
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    //    create vehicle parameter
    public void doCreate() throws IOException {
        doValidateNewVehicleParameter();
        if (!hasAnyErrors()) {
            vehicleParameterService.create(
                    new VehicleParameterDTO(0, Long.parseLong(projectId), vehicleParameterName, new Date(),
                                            getLoggedInUser().getUsername(), null, null));
            updateListOption(Constants.ACTION_CREATE, vehicleParameterName, null);
            vehicles = vehicleParameterService.findByProjectId(Long.parseLong(projectId));
            String json = new Gson().toJson(convertToVehicleParameterDto(vehicles));
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_OK);
        }
    }

    // check exist vehicle parameter
    public void doCheckVehicleParameter() {
        if (vehicleParameterService.findById(Long.parseLong(vehicleParameterId))) {
            getHttpResponse().setStatus(HttpServletResponse.SC_OK);
        } else {
            getHttpResponse().setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }

    // validate edit
    private void doValidateEditVehicleParameter() throws IOException {
        if (StringUtils.isEmpty(vehicleParameterId) ||
            !vehicleParameterService.findById(Long.parseLong(vehicleParameterId))) {
            addError(Constants.VEHICLE_PARAMETER_ID, Constants.VEHICLE_PARAMETER_IS_NOT_EXISTED);
        }
        if (!StringUtils.isEmpty(vehicleParameterName)) {
            vehicleParameterName = vehicleParameterName.trim();
        }
        if (StringUtils.isEmpty(vehicleParameterName)) {
            addError(Constants.VEHICLE_PARAMETER_NAME, Constants.VEHICLE_PARAMETER_IS_REQUIRED);
        } else if (vehicleParameterService.isExistedVehicleParameterNameSecond(vehicleParameterName,
                                                                   Long.parseLong(vehicleParameterId), Long.parseLong(projectId))) {
            addError(Constants.VEHICLE_PARAMETER_NAME, Constants.VEHICLE_PARAMETER_IS_EXISTED);
        } else if (vehicleParameterName.length() > 100) {
            addError(Constants.VEHICLE_PARAMETER_NAME, Constants.VEHICLE_PARAMETER_MORE_THAN_100);
        }

        if (hasAnyErrors()) {
            String json = new Gson().toJson(getErrors());
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // get curren vehicle to load form edit
    public void doGetCurrentVehicleParameter() throws IOException {
        if (vehicleParameterService.findByVehicleParameterId(Long.parseLong(vehicleParameterId)) != null) {
            String json = new Gson().toJson(
                    vehicleParameterService.findByVehicleParameterId(Long.parseLong(vehicleParameterId)));
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
        doValidateEditVehicleParameter();
        // update
        if (!hasAnyErrors()) {
            VehicleParameterDTO vehicleParameterDTO = vehicleParameterService.findByVehicleParameterId(
                    Long.parseLong(vehicleParameterId));
            String oldVehicleParameterName = vehicleParameterDTO.getVehicleParameterName();
            vehicleParameterDTO.setUpdateDate(new Date());
            vehicleParameterDTO.setUpdateUser(getLoggedInUser().getUsername());
            vehicleParameterDTO.setVehicleParameterName(vehicleParameterName);
            vehicleParameterService.update(vehicleParameterDTO);
            updateListOption(Constants.ACTION_EDIT, vehicleParameterName, oldVehicleParameterName);
            String json = new Gson().toJson(vehicleParameterDTO);
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_OK);
        } else {
            getHttpResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // convert to vehicle parameter DTO
    private List<VehicleParameterDTO> convertToVehicleParameterDto(List<VehicleParameter> vehicles) {
        if (vehicles.isEmpty()) {
            return Collections.<VehicleParameterDTO>emptyList();
        }
        List<VehicleParameterDTO> vehicleParameterDTOS = new ArrayList<>();
        for (VehicleParameter vehicleParameter : vehicles) {
            VehicleParameterDTO vehicleParameterDTO = new VehicleParameterDTO();
            vehicleParameterDTO.setId(vehicleParameter.getID());
            vehicleParameterDTO.setVehicleParameterName(vehicleParameter.getVehicleParameterName());
            vehicleParameterDTO.setProjectId(vehicleParameter.getProjectId());
            vehicleParameterDTO.setCreateUser(vehicleParameter.getCreateUser());
            vehicleParameterDTO.setCreateDate(vehicleParameter.getCreateDate());
            vehicleParameterDTO.setUpdateUser(vehicleParameter.getUpdateUser());
            vehicleParameterDTO.setUpdateDate(vehicleParameter.getUpdateDate());
            vehicleParameterDTOS.add(vehicleParameterDTO);
        }
        return vehicleParameterDTOS;
    }

    //ADD VEHICLE PARAMETER OPTION IN CREATE SET OF DEVICE SCREEN
    private void updateListOption(String action, String newOption, String oldOption) {
        CustomField customField = null;
        Collection<CustomField> customFieldVehicles = ComponentAccessor.getCustomFieldManager()
                                                                       .getCustomFieldObjectsByName(
                                                                               Constants.CUSTOM_FIELD_VEHICLE_PARAMETER);
        for (CustomField c : customFieldVehicles) {
            if (c.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_SELECT)) {
                customField = c;
            }
        }
        List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();

        FieldConfigScheme fieldConfigScheme = null;
        for (FieldConfigScheme scheme : schemes) {
            if (scheme.getAssociatedProjectIds().contains(Long.parseLong(projectId))) {
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
