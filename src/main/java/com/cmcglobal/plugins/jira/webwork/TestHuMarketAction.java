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
import com.cmcglobal.plugins.dto.TestHuMarketDTO;
import com.cmcglobal.plugins.entity.TestHuMarket;
import com.cmcglobal.plugins.service.TestHuMarketService;
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

public class TestHuMarketAction extends JiraWebActionSupport {
    @JiraImport
    private TestHuMarketService testHuMarketService;

    private String             testHuMarketName;
    private List<TestHuMarket> testHuMarkets;
    private String             projectId;
    private String             testHuMarketId;

    public TestHuMarketService getTestHuMarketService() {
        return testHuMarketService;
    }

    public void setTestHuMarketService(TestHuMarketService testHuMarketService) {
        this.testHuMarketService = testHuMarketService;
    }

    public String getTestHuMarketName() {
        return testHuMarketName;
    }

    public void setTestHuMarketName(String testHuMarketName) {
        this.testHuMarketName = testHuMarketName;
    }

    public List<TestHuMarket> getTestHuMarkets() {
        return testHuMarkets;
    }

    public void setTestHuMarkets(List<TestHuMarket> testHuMarkets) {
        this.testHuMarkets = testHuMarkets;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTestHuMarketId() {
        return testHuMarketId;
    }

    public void setTestHuMarketId(String testHuMarketId) {
        this.testHuMarketId = testHuMarketId;
    }

    public TestHuMarketAction(TestHuMarketService testHuMarketService) {
        this.testHuMarketService = testHuMarketService;
    }

    public String doDefault() throws Exception {
        final Collection<IssueType> issueTypes = getSelectedProject().getIssueTypes();
        for (IssueType issueType : issueTypes) {
            if (issueTypes.size() == 6 && Constants.SET_OF_DEVICE.equals(issueType.getName())) {
                testHuMarkets = testHuMarketService.findByProjectId(getSelectedProject().getId());
                return "success";
            }
        }
        return "page-not-exist";

    }

    public void doValidateNewTestHuMarket() throws IOException {
        if (!StringUtils.isEmpty(testHuMarketName)) {
            testHuMarketName = testHuMarketName.trim();
        }
        if (StringUtils.isEmpty(testHuMarketName)) {
            addError(Constants.TEST_HU_MARKET_NAME, Constants.TEST_HU_MARKET_IS_REQUIRED);
        } else if (testHuMarketService.isExistedTestHuMarketName(testHuMarketName, Long.parseLong(projectId))) {
            addError(Constants.TEST_HU_MARKET_NAME, Constants.TEST_HU_MARKET_IS_EXISTED);
        } else if (testHuMarketName.length() > 100) {
            addError(Constants.TEST_HU_MARKET_NAME, Constants.TEST_HU_MARKET_MORE_THAN_100);
        }

        if (hasAnyErrors()) {
            String json = new Gson().toJson(getErrors());
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    //    create Test Hu Market
    public void doCreate() throws IOException {
        doValidateNewTestHuMarket();
        if (!hasAnyErrors()) {
            testHuMarketService.create(new TestHuMarketDTO(0, Long.parseLong(projectId), testHuMarketName, new Date(),
                                                           getLoggedInUser().getUsername(), null, null));
            updateListOption(Constants.ACTION_CREATE, testHuMarketName, null);
            testHuMarkets = testHuMarketService.findByProjectId(Long.parseLong(projectId));
            String json = new Gson().toJson(convertToTestHuMarketDto(testHuMarkets));
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_OK);
        }
    }

    // check exist test Hu Market
    public void doCheckTestHuMarket() {
        if (testHuMarketService.findById(Long.parseLong(testHuMarketId))) {
            getHttpResponse().setStatus(HttpServletResponse.SC_OK);
        } else {
            getHttpResponse().setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }

    // validate edit
    private void doValidateEditTestHuMarket() throws IOException {
        if (StringUtils.isEmpty(testHuMarketId) || !testHuMarketService.findById(Long.parseLong(testHuMarketId))) {
            addError(Constants.TEST_HU_MARKET_ID, Constants.TEST_HU_MARKET_IS_NOT_EXISTED);
        }
        if (!StringUtils.isEmpty(testHuMarketName)) {
            testHuMarketName = testHuMarketName.trim();
        }
        if (StringUtils.isEmpty(testHuMarketName)) {
            addError(Constants.TEST_HU_MARKET_NAME, Constants.TEST_HU_MARKET_IS_REQUIRED);
        } else if (testHuMarketService.isExistedTestHuMarketNameSecond(testHuMarketName, Long.parseLong(testHuMarketId), Long.parseLong(projectId))) {
            addError(Constants.TEST_HU_MARKET_NAME, Constants.TEST_HU_MARKET_IS_EXISTED);
        } else if (testHuMarketName.length() > 100) {
            addError(Constants.TEST_HU_MARKET_NAME, Constants.TEST_HU_MARKET_MORE_THAN_100);
        }
        if (hasAnyErrors()) {
            String json = new Gson().toJson(getErrors());
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // get current test hu market to load form edit
    public void doGetCurrentTestHuMarket() throws IOException {
        if (testHuMarketService.findByTestHuMarketId(Long.parseLong(testHuMarketId)) != null) {
            String json = new Gson().toJson(testHuMarketService.findByTestHuMarketId(Long.parseLong(testHuMarketId)));
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
        doValidateEditTestHuMarket();
        // update
        if (!hasAnyErrors()) {
            TestHuMarketDTO testHuMarketDTO = testHuMarketService.findByTestHuMarketId(Long.parseLong(testHuMarketId));
            String oldTestHuMarketName = testHuMarketDTO.getTestHuMarketName();
            testHuMarketDTO.setUpdateDate(new Date());
            testHuMarketDTO.setUpdateUser(getLoggedInUser().getUsername());
            testHuMarketDTO.setTestHuMarketName(testHuMarketName);
            testHuMarketService.update(testHuMarketDTO);
            updateListOption(Constants.ACTION_EDIT, testHuMarketName, oldTestHuMarketName);
            String json = new Gson().toJson(testHuMarketDTO);
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_OK);
        } else {
            getHttpResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // convert to test Hu market DTO
    private List<TestHuMarketDTO> convertToTestHuMarketDto(List<TestHuMarket> testHuMarkets) {
        if (testHuMarkets.isEmpty()) {
            return Collections.<TestHuMarketDTO>emptyList();
        }
        List<TestHuMarketDTO> testHuMarketDTOS = new ArrayList<>();
        for (TestHuMarket testHuMarket : testHuMarkets) {
            TestHuMarketDTO testHuMarketDTO = new TestHuMarketDTO();
            testHuMarketDTO.setId(testHuMarket.getID());
            testHuMarketDTO.setTestHuMarketName(testHuMarket.getTestHuMarketName());
            testHuMarketDTO.setProjectId(testHuMarket.getProjectId());
            testHuMarketDTO.setCreateUser(testHuMarket.getCreateUser());
            testHuMarketDTO.setCreateDate(testHuMarket.getCreateDate());
            testHuMarketDTO.setUpdateUser(testHuMarket.getUpdateUser());
            testHuMarketDTO.setUpdateDate(testHuMarket.getUpdateDate());
            testHuMarketDTOS.add(testHuMarketDTO);
        }
        return testHuMarketDTOS;
    }

    //ADD TEST HU MARKET OPTION IN CREATE SET OF DEVICE SCREEN
    private void updateListOption(String action, String newOption, String oldOption) {
        CustomField customField = ComponentAccessor.getCustomFieldManager()
                                                   .getCustomFieldObjectByName(Constants.TEST_HU_MARKET_ATTRIBUTE);
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
