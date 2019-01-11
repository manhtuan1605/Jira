package com.cmcglobal.plugins.servlet;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.cmcglobal.plugins.dto.ResultMessage;
import com.cmcglobal.plugins.entity.UploadFile;
import com.cmcglobal.plugins.service.TestCaseService;
import com.cmcglobal.plugins.service.UploadFileService;
import com.cmcglobal.plugins.utils.ImportStatus;
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

import static com.cmcglobal.plugins.utils.Constants.CHARACTER_ENCODING;
import static com.cmcglobal.plugins.utils.Constants.CONTENT_TYPE;
import static com.cmcglobal.plugins.utils.Constants.FILE_NAME;
import static com.cmcglobal.plugins.utils.Constants.IMPORT_TESTCASE_TYPE;
import static com.cmcglobal.plugins.utils.Constants.IMPORT_TYPE;
import static com.cmcglobal.plugins.utils.Constants.LIST_FILE_UP_LOAD;
import static com.cmcglobal.plugins.utils.Constants.MESSAGE_ERROR_SERVICE;
import static com.cmcglobal.plugins.utils.Constants.MESSAGE_ERROR_TEST_CASE_TYPE;
import static com.cmcglobal.plugins.utils.Constants.PARAMETER_PROJECT_ID;
import static com.cmcglobal.plugins.utils.Constants.PARAM_PHASE_ID;
import static com.cmcglobal.plugins.utils.Constants.PHASE_ID;
import static com.cmcglobal.plugins.utils.Constants.PHASE_ID_REQUIRED;
import static com.cmcglobal.plugins.utils.Constants.TEST_CASE_TYPE;
import static com.cmcglobal.plugins.utils.Constants.TEST_CASE_TYPE_REQUIRED;
import static com.cmcglobal.plugins.utils.Constants.TOTAL_ROWS;
import static com.cmcglobal.plugins.utils.Constants.TOTAL_ROWS_IMPORT_ERROR;
import static com.cmcglobal.plugins.utils.Constants.TOTAL_ROWS_IMPORT_SUCCESS;

@Scanned
public class ImportTestCaseServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ImportTestCaseServlet.class);

    @Autowired
    TestCaseService   testCaseService;
    @Autowired
    UploadFileService uploadFileService;
    @Autowired
    ProjectService    projectService;

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        final String phaseId = req.getParameter(PHASE_ID);
        final String testCaseType = req.getParameter(TEST_CASE_TYPE);
        final String importType = req.getParameter(IMPORT_TYPE);
        resp.setContentType(CONTENT_TYPE);
        resp.setCharacterEncoding(CHARACTER_ENCODING);
        final Map<String, String> errors = new HashMap<>();
        final Map<String, Object> map = new HashMap<>();
        final ProjectManager projectManager = ComponentAccessor.getProjectManager();
        final Project project = projectManager.getProjectObj(Long.parseLong(req.getParameter(PARAMETER_PROJECT_ID)));
        if (StringUtils.isBlank(phaseId)) {
            errors.put(PARAM_PHASE_ID, PHASE_ID_REQUIRED);
        }
        if (StringUtils.equalsIgnoreCase(importType, IMPORT_TESTCASE_TYPE) && StringUtils.isBlank(testCaseType)) {
            errors.put(MESSAGE_ERROR_TEST_CASE_TYPE, TEST_CASE_TYPE_REQUIRED);
        }
        if (errors.size() > 0) {
            resp.getWriter().write(new Gson().toJson(errors));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            final ResultMessage resultMessage = testCaseService.imports(req, resp);
            if (resultMessage.getCode() == HttpServletResponse.SC_OK) {
                map.put(LIST_FILE_UP_LOAD, uploadFileService.findAllByProjectId(project.getId(), null));
                map.put(TOTAL_ROWS_IMPORT_SUCCESS, resultMessage.getTotalRowSuccess());
                map.put(TOTAL_ROWS_IMPORT_ERROR, resultMessage.getTotalRowError());
                map.put(TOTAL_ROWS, resultMessage.getTotalRows());
                map.put(FILE_NAME, resultMessage.getFileName());
                resp.getWriter().write(new Gson().toJson(map));
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                errors.put(MESSAGE_ERROR_SERVICE, resultMessage.getMessage());
                resp.getWriter().write(new Gson().toJson(errors));
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException{
        final String projectId = req.getParameter(PARAMETER_PROJECT_ID);
        final String fileName = req.getParameter(FILE_NAME);
        resp.setContentType(CONTENT_TYPE);
        resp.setCharacterEncoding(CHARACTER_ENCODING);
        final Map<String, String> errors = new HashMap<>();
        if (!StringUtils.isBlank(projectId) && !StringUtils.isBlank(fileName)) {
            final ProjectManager projectManager = ComponentAccessor.getProjectManager();
            final Project project = projectManager.getProjectObj(Long.parseLong(projectId));
            UploadFile uploadFile = uploadFileService.findByProjectIdAndFileNameStatus(Long.parseLong(projectId), fileName,
                                                                                       ImportStatus.WAITING.getValue());
            try {
                testCaseService.initThread(uploadFile.getType(),
                                           uploadFile.getPathFileValid() + "/" + uploadFile.getUploadFileName(),
                                           uploadFile.getUploadFileName(), project, uploadFile.getPhase(),
                                           uploadFile.getTestCaseType(),
                                           ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(),
                                           ImportStatus.WAITING);
            } catch (Exception e) {
                log.error(e.getMessage());
                errors.put(MESSAGE_ERROR_SERVICE, e.getMessage());
                resp.getWriter().write(new Gson().toJson(errors));
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
}