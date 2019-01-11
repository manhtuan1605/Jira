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
import com.cmcglobal.plugins.dto.TestingPhaseDto;
import com.cmcglobal.plugins.entity.TestingPhase;
import com.cmcglobal.plugins.service.TestingPhasesService;
import com.cmcglobal.plugins.utils.Constants;
import com.google.gson.Gson;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TestingPhasesAction extends JiraWebActionSupport {

    private final DateFormat formatter = new SimpleDateFormat(Constants.DATE_FORMAT_DD_MM_YYYY);
    private       String     pid;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    @JiraImport
    private final TestingPhasesService testingPhasesService;

    public TestingPhasesAction(TestingPhasesService testingPhasesService) {
        this.testingPhasesService = testingPhasesService;
    }

    private List<TestingPhase> phases;

    public List<TestingPhase> getPhases() {
        return phases;
    }

    private String phaseId;

    public void setPhaseId(String phaseId) throws Exception {
        this.phaseId = phaseId;
    }

    private String phaseName;

    public void setPhaseName(String phaseName) throws Exception {
        this.phaseName = phaseName;
    }

    private String startDate;

    public void setStartDate(String startDate) throws Exception {
        this.startDate = startDate;
    }

    private String endDate;

    public void setEndDate(String endDate) throws Exception {
        this.endDate = endDate;
    }

    private String phaseNameFormatted;
    private Date   startDateFormatted;
    private Date   endDateFormatted;

    private void doValidateNewPhase() throws IOException {
        if (!StringUtils.isEmpty(phaseName)) {
            phaseNameFormatted = phaseName.trim();
        }
        if (StringUtils.isEmpty(phaseNameFormatted)) {
            addError(Constants.PHASE_NAME, Constants.ERROR_MESSAGE_REQUIRED_PHASE_NAME);
        } else if (testingPhasesService.isExistedPhaseName(phaseNameFormatted, Long.parseLong(pid))) {
            addError(Constants.PHASE_NAME, Constants.ERROR_MESSAGE_EXISTED_PROJECT_ID);
        } else if (phaseNameFormatted.length() > 100) {
            addError(Constants.PHASE_NAME, Constants.ERROR_MESSAGE_PHASE_NAME_MAX_LENGTH);
        }

        if (!StringUtils.isEmpty(startDate)) {
            try {
                startDateFormatted = formatter.parse(startDate);
            } catch (Exception e) {
                addError(Constants.PHASE_START_DATE, Constants.ERROR_MESSAGE_INVALID_START_DATE);
            }
        }
        if (!StringUtils.isEmpty(endDate)) {
            try {
                endDateFormatted = formatter.parse(endDate);
            } catch (Exception e) {
                addError(Constants.PHASE_END_DATE, Constants.ERROR_MESSAGE_INVALID_END_DATE);
            }
        }

        if ((!StringUtils.isEmpty(startDateFormatted) && !StringUtils.isEmpty(endDateFormatted)) &&
            (startDateFormatted.after(endDateFormatted))) {
            addError(Constants.PHASE_START_DATE, Constants.ERROR_MESSAGE_START_DATE_AFTER_END_DATE);
            addError(Constants.PHASE_END_DATE, Constants.ERROR_MESSAGE_END_DATE_BEFORE_START_DATE);
        }

        if (hasAnyErrors()) {
            String json = new Gson().toJson(getErrors());
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    public void doCreatePhase() throws Exception {
        doValidateNewPhase();
        if (!hasAnyErrors()) {
            try {
                testingPhasesService.create(
                        new TestingPhaseDto(-1, Long.parseLong(pid), phaseNameFormatted, startDateFormatted,
                                            endDateFormatted, new Date(), getLoggedInUser().getUsername(), null, null,
                                            true));
                updateListOption(Constants.ACTION_CREATE, phaseNameFormatted, null);
                phases = testingPhasesService.findByProjectId(Long.parseLong(pid));
                String json = new Gson().toJson(convertToTestingPhaseDto(phases));
                getHttpResponse().setContentType(Constants.CONTENT_TYPE);
                getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
                getHttpResponse().getWriter().write(json);
                getHttpResponse().setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                getHttpResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    public String doDefault() throws Exception {
        final Collection<IssueType> issueTypes = getSelectedProject().getIssueTypes();
        for (IssueType issueType : issueTypes) {
            if (Constants.ISSUE_TYPE_TESTCASE_NAME.equals(issueType.getName())) {
                phases = testingPhasesService.findByProjectId(getSelectedProject().getId());
                return Constants.VIEW_TESTING_PHASES;
            }
        }
        return "page-not-exist";
    }

    private List<TestingPhaseDto> convertToTestingPhaseDto(List<TestingPhase> phases) {
        if (phases.isEmpty()) {
            return Collections.<TestingPhaseDto>emptyList();
        }
        List<TestingPhaseDto> phaseDtos = new ArrayList<>();
        for (TestingPhase phase : phases) {
            TestingPhaseDto phaseDto = new TestingPhaseDto();
            phaseDto.setID(phase.getID());
            phaseDto.setPhaseName(phase.getPhaseName());
            phaseDto.setProjectId(phase.getProjectId());
            phaseDto.setStartDate(phase.getStartDate());
            phaseDto.setEndDate(phase.getEndDate());
            phaseDto.setIsActive(phase.getIsActive());
            phaseDto.setCreateUser(phase.getCreateUser());
            phaseDto.setCreateDate(phase.getCreateDate());
            phaseDto.setUpdateUser(phase.getUpdateUser());
            phaseDto.setUpdateDate(phase.getUpdateDate());
            phaseDtos.add(phaseDto);
        }
        return phaseDtos;
    }

    public void doCheckPhase() throws Exception {
        if (testingPhasesService.findById(Long.parseLong(phaseId))) {
            getHttpResponse().setStatus(HttpServletResponse.SC_OK);
        } else {
            getHttpResponse().setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }

    // get curren phase lo load form edit
    public void doGetCurrentPhase() throws Exception {
        String json = new Gson().toJson(testingPhasesService.findByPhaseId(Long.parseLong(phaseId)));
        getHttpResponse().setContentType(Constants.CONTENT_TYPE);
        getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
        getHttpResponse().getWriter().write(json);
        getHttpResponse().setStatus(HttpServletResponse.SC_OK);
    }

    // validate for edit return json errors

    private void doValidateEdit() throws IOException {
        if (StringUtils.isEmpty(phaseId) || !testingPhasesService.findById(Long.parseLong(phaseId))) {
            addError(Constants.PHASE_ID, Constants.ERROR_MESSAGE_PHASE_ID_NOT_EXIST);
        }
        if (!StringUtils.isEmpty(phaseName)) {
            phaseNameFormatted = phaseName.trim();
        }
        if (StringUtils.isEmpty(phaseNameFormatted)) {
            addError(Constants.PHASE_NAME, Constants.ERROR_MESSAGE_REQUIRED_PHASE_NAME);
        } else if (phaseNameFormatted.length() > 100) {
            addError(Constants.PHASE_NAME, Constants.ERROR_MESSAGE_PHASE_NAME_MAX_LENGTH);
        } else if (testingPhasesService.isExistedPhaseNameSecond(phaseNameFormatted, Long.parseLong(phaseId),
                                                                 Long.parseLong(pid))) {
            addError(Constants.PHASE_NAME, Constants.ERROR_MESSAGE_EXISTED_PROJECT_ID);
        }

        if (!StringUtils.isEmpty(startDate)) {
            try {
                startDateFormatted = formatter.parse(startDate);
            } catch (Exception e) {
                addError(Constants.PHASE_START_DATE, Constants.ERROR_MESSAGE_INVALID_START_DATE);
            }
        }
        if (!StringUtils.isEmpty(endDate)) {
            try {
                endDateFormatted = formatter.parse(endDate);
            } catch (Exception e) {
                addError(Constants.PHASE_END_DATE, Constants.ERROR_MESSAGE_INVALID_END_DATE);
            }
        }

        if ((!StringUtils.isEmpty(startDateFormatted) && !StringUtils.isEmpty(endDateFormatted)) &&
            (startDateFormatted.after(endDateFormatted))) {
            addError(Constants.PHASE_START_DATE, Constants.ERROR_MESSAGE_START_DATE_AFTER_END_DATE);
            addError(Constants.PHASE_END_DATE, Constants.ERROR_MESSAGE_END_DATE_BEFORE_START_DATE);
        }

        if (hasAnyErrors()) {
            Map<String, String> errors = getErrors();
            String json = new Gson().toJson(errors);
            getHttpResponse().setContentType(Constants.CONTENT_TYPE);
            getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
            getHttpResponse().getWriter().write(json);
            getHttpResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    public void doEdit() throws Exception {
        doValidateEdit();
        // update
        if (!hasAnyErrors()) {
            try {
                TestingPhaseDto testPhase = testingPhasesService.findByPhaseId(Long.parseLong(phaseId));
                String oldPhaseName = testPhase.getPhaseName();
                testPhase.setUpdateDate(new Date());
                testPhase.setUpdateUser(getLoggedInUser().getUsername());
                testPhase.setPhaseName(phaseNameFormatted);
                testPhase.setStartDate(startDateFormatted);
                testPhase.setEndDate(endDateFormatted);
                testingPhasesService.update(testPhase);
                updateListOption(Constants.ACTION_EDIT, phaseNameFormatted, oldPhaseName);
                String json = new Gson().toJson(testPhase);
                getHttpResponse().setContentType(Constants.CONTENT_TYPE);
                getHttpResponse().setCharacterEncoding(Constants.CHARACTER_ENCODING);
                getHttpResponse().getWriter().write(json);
                getHttpResponse().setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                getHttpResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

    }

    //ADD PHASE NAME TO PHASE OPTION IN CREATE TESTCASE SCREEN
    private void updateListOption(String action, String newOption, String oldOption) {
        CustomField customField = ComponentAccessor.getCustomFieldManager()
                                                   .getCustomFieldObjectByName(Constants.CUSTOM_FIELD_PHASE);
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
