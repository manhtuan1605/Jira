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
import com.cmcglobal.plugins.dto.TestCaseTypeDTO;
import com.cmcglobal.plugins.entity.TestCaseType;
import com.cmcglobal.plugins.service.TestCaseTypeService;
import com.cmcglobal.plugins.utils.Constants;
import com.google.gson.Gson;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class TestCaseTypesAction extends JiraWebActionSupport {
    @JiraImport
    private TestCaseTypeService testCaseTypeService;
    private List<TestCaseType>  types;
    private String              typeId;
    private String              typeName;
    private String              typePerformance;
    private String              pid;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypePerformance() {
        return typePerformance;
    }

    public void setTypePerformance(String typePerformance) {
        this.typePerformance = typePerformance;
    }

    public List<TestCaseType> getTypes() {
        return types;
    }

    public TestCaseTypesAction(TestCaseTypeService testCaseTypeService) {
        this.testCaseTypeService = testCaseTypeService;
    }

    public String doDefault() throws Exception {
        final Collection<IssueType> issueTypes = getSelectedProject().getIssueTypes();
        for (IssueType issueType : issueTypes) {
            if (Constants.ISSUE_TYPE_TESTCASE_NAME.equals(issueType.getName())) {
                types = this.testCaseTypeService.findByProjectId(getSelectedProject().getId());
                return Constants.VIEW_TEST_CASE_TYPE;
            }
        }
        return "page-not-exist";
    }

    // validate create type
    private void doValidateNewType() throws IOException {
        if (!StringUtils.isEmpty(typeName)) {
            typeName = typeName.trim();
        }
        if (StringUtils.isEmpty(typeName)) {
            addError(Constants.TYPE_NAME, Constants.ERROR_TYPE_NAME_REQUIRE);
        } else if (testCaseTypeService.isExistedTypeName(typeName, Long.parseLong(pid))) {
            addError(Constants.TYPE_NAME, Constants.ERROR_TYPE_NAME_EXISTED);
        } else if (typeName.length() > 100) {
            addError(Constants.TYPE_NAME, Constants.ERROR_TYPE_NAME_RANGE);
        }

        if (StringUtils.isEmpty(typePerformance) || StringUtils.isEmpty(typePerformance.trim())) {
            addError(Constants.TYPE_PERFORMANCE, Constants.ERROR_TYPE_PERFORMANCE_REQUIRE);
        } else {
            try {
                Long formatPerformance = Long.parseLong(typePerformance.trim());
                if ((formatPerformance <= 0) || (formatPerformance > 1000000)) {
                    addError(Constants.TYPE_PERFORMANCE, Constants.ERROR_TYPE_PERFORMANCE_RANGE);
                }
            } catch (Exception e) {
                addError(Constants.TYPE_PERFORMANCE, Constants.ERROR_TYPE_PERFORMANCE_NOT_VALID);
            }
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
    public void doCreateType() throws Exception {
        doValidateNewType();
        if (!hasAnyErrors()) {
            try {
                testCaseTypeService.create(
                        new TestCaseTypeDTO(-1, Long.parseLong(pid), typeName, Long.parseLong(typePerformance),
                                            new Date(), getLoggedInUser().getUsername(), null, null, true));
                updateListOption(Constants.ACTION_CREATE, typeName, null);
                types = testCaseTypeService.findByProjectId(Long.parseLong(pid));
                String json = new Gson().toJson(convertToTypeDto(types));
                getHttpResponse().setContentType(Constants.CONTENT_TYPE);
                getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
                getHttpResponse().getWriter().write(json);
                getHttpResponse().setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                getHttpResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    // check exist type
    public void doCheckType() throws Exception {
        if (testCaseTypeService.findById(Long.parseLong(typeId))) {
            getHttpResponse().setStatus(HttpServletResponse.SC_OK);
        } else {
            getHttpResponse().setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }

    // validate edit
    private void doValidateEditType() throws IOException {
        if (StringUtils.isEmpty(typeId) || !testCaseTypeService.findById(Long.parseLong(typeId))) {
            addError(Constants.TYPE_ID, Constants.ERROR_ID_NOT_EXIST);
        }
        if (!StringUtils.isEmpty(typeName)) {
            typeName = typeName.trim();
        }
        if (StringUtils.isEmpty(typeName)) {
            addError(Constants.TYPE_NAME, Constants.ERROR_TYPE_NAME_REQUIRE);
        } else if (typeName.length() > 100) {
            addError(Constants.TYPE_NAME, Constants.ERROR_TYPE_NAME_RANGE);
        } else if (testCaseTypeService.isExistedTypeNameSecond(typeName, Long.parseLong(typeId), Long.parseLong(pid))) {
            addError(Constants.TYPE_NAME, Constants.ERROR_TYPE_NAME_EXISTED);
        }

        if (StringUtils.isEmpty(typePerformance) || StringUtils.isEmpty(typePerformance.trim())) {
            addError(Constants.TYPE_PERFORMANCE, Constants.ERROR_TYPE_PERFORMANCE_REQUIRE);
        } else {
            try {
                Long formatPerformance = Long.parseLong(typePerformance.trim());
                if ((formatPerformance <= 0) || (formatPerformance > 1000000)) {
                    addError(Constants.TYPE_PERFORMANCE, Constants.ERROR_TYPE_PERFORMANCE_RANGE);
                }
            } catch (Exception e) {
                addError(Constants.TYPE_PERFORMANCE, Constants.ERROR_TYPE_PERFORMANCE_NOT_VALID);
            }
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
    public void doGetCurrentType() throws Exception {
        String json = new Gson().toJson(testCaseTypeService.findByTypeId(Long.parseLong(typeId)));
        getHttpResponse().setContentType(Constants.CONTENT_TYPE);
        getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
        getHttpResponse().getWriter().write(json);
        getHttpResponse().setStatus(HttpServletResponse.SC_OK);
    }

    // edit
    public void doEdit() throws Exception {
        doValidateEditType();
        // update
        if (!hasAnyErrors()) {
            try {
                TestCaseTypeDTO testType = testCaseTypeService.findByTypeId(Long.parseLong(typeId));
                String oldTypeName = testType.getTestCaseTypeName();
                testType.setUpdateDate(new Date());
                testType.setUpdateUser(getLoggedInUser().getUsername());
                testType.setTestCaseTypeName(typeName);
                testType.setPerformance(Long.parseLong(typePerformance));
                testCaseTypeService.update(testType);
                updateListOption(Constants.ACTION_EDIT, typeName, oldTypeName);
                String json = new Gson().toJson(testType);
                getHttpResponse().setContentType(Constants.CONTENT_TYPE);
                getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
                getHttpResponse().getWriter().write(json);
                getHttpResponse().setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                getHttpResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

    }

    // convert to type DTO
    private List<TestCaseTypeDTO> convertToTypeDto(List<TestCaseType> types) {
        if (types.isEmpty()) {
            return Collections.<TestCaseTypeDTO>emptyList();
        }
        List<TestCaseTypeDTO> typeDtos = new ArrayList<>();
        for (TestCaseType type : types) {
            TestCaseTypeDTO typeDto = new TestCaseTypeDTO();
            typeDto.setID(type.getID());
            typeDto.setTestCaseTypeName(type.getTestCaseTypeName());
            typeDto.setProjectId(type.getProjectId());
            typeDto.setPerformance(type.getPerformance());
            typeDto.setCreateUser(type.getCreateUser());
            typeDto.setCreateDate(type.getCreateDate());
            typeDto.setUpdateUser(type.getUpdateUser());
            typeDto.setUpdateDate(type.getUpdateDate());
            typeDtos.add(typeDto);
        }
        return typeDtos;
    }

    //ADD PHASE NAME TO TEST CASE TYPE OPTION IN CREATE TESTCASE SCREEN
    private void updateListOption(String action, String newOption, String oldOption) {
        CustomField customField = ComponentAccessor.getCustomFieldManager()
                                                   .getCustomFieldObjectByName(Constants.CUSTOM_FIELD_TESTCASE_TYPE);
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
