package com.cmcglobal.plugins.servlet;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.cmcglobal.plugins.dto.ResultMessage;
import com.cmcglobal.plugins.service.DeviceService;
import com.cmcglobal.plugins.service.TestCaseService;
import com.cmcglobal.plugins.service.TesterPerformanceService;
import com.cmcglobal.plugins.service.UploadFileService;
import com.cmcglobal.plugins.utils.Constants;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.cmcglobal.plugins.utils.Constants.*;


@Scanned
public class ImportDeviceServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ImportTestCaseServlet.class);

    @Autowired
    TestCaseService testCaseService;
    @Autowired
    DeviceService deviceService;
    @Autowired
    UploadFileService uploadFileService;
    @Autowired
    ProjectService projectService;
    @Autowired
    private TesterPerformanceService testerPerformanceService;
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String deviceType = req.getParameter(DEVICE_TYPE);
        final String importType = req.getParameter(IMPORT_DEVICE_TYPE);
        resp.setContentType(CONTENT_TYPE);
        resp.setCharacterEncoding(CHARACTER_ENCODING);
        final Map<String, String> errors = new HashMap<>();
        final Map<String, Object> data = new HashMap<>();
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        long projectId = Long.parseLong(req.getParameter(PARAMETER_PROJECT_ID));
        String role = testerPerformanceService.getRole(user, projectId);

        final ProjectManager projectManager = ComponentAccessor.getProjectManager();
        final Project project = projectManager.getProjectObj(Long.parseLong(req.getParameter(PARAMETER_PROJECT_ID)));
        if (Objects.equals(role, PROJECT_ROLE_QC_LEAD) && Objects.equals(importType, IMPORT_TYPE_IMPORT_DEVICE)) {
            errors.put(MESSAGE_ERROR_TEST_CASE_TYPE, DEVICE_PERMISSION);
        }
        if (StringUtils.equalsIgnoreCase(importType, IMPORT_TESTCASE_TYPE) && StringUtils.isBlank(deviceType)) {
            errors.put(MESSAGE_ERROR_TEST_CASE_TYPE, TEST_CASE_TYPE_REQUIRED);
        }
        if (errors.size() > 0) {
            resp.getWriter().write(new Gson().toJson(errors));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            final ResultMessage resultMessage = deviceService.imports(req, resp);
            if (resultMessage.getCode() == HttpServletResponse.SC_OK) {
                data.put(Constants.LIST_FILE_UP_LOAD, uploadFileService.findAllByProjectId(project.getId(), null));
                data.put(Constants.TOTAL_ROWS_IMPORT_SUCCESS, resultMessage.getTotalRowSuccess());
                data.put(Constants.TOTAL_ROWS_IMPORT_ERROR, resultMessage.getTotalRowError());
                data.put(Constants.TOTAL_ROWS, resultMessage.getTotalRows());
                resp.getWriter().write(new Gson().toJson(data));
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                errors.put(MESSAGE_ERROR_SERVICE, resultMessage.getMessage());
                resp.getWriter().write(new Gson().toJson(errors));
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
}
