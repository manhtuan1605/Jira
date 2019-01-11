package com.cmcglobal.plugins.jira.reports;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.dto.TestMonitorMember;
import com.cmcglobal.plugins.service.TestMonitorService;
import com.cmcglobal.plugins.utils.Constants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.cmcglobal.plugins.utils.Constants.CHARACTER_ENCODING;
import static com.cmcglobal.plugins.utils.Constants.CONTENT_TYPE;

public class TesterPerformanceServlet extends HttpServlet {
    private static final Logger             log = LoggerFactory.getLogger(TesterPerformanceServlet.class);

    private TestMonitorService testMonitorService;

    @Autowired
    private CustomFieldManager customFieldManager;

    public TesterPerformanceServlet(TestMonitorService testMonitorService) {
        this.testMonitorService = testMonitorService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_DD_MM_YYYY);
        try {
            log.info("Get tester performance");
            long userId = Long.parseLong(req.getParameter("userId"));
            Long projectId = Long.valueOf(req.getParameter("projectId"));
            LocalDate start = LocalDate.parse(req.getParameter("startDate"), formatter);
            LocalDate end = LocalDate.parse(req.getParameter("endDate"), formatter);
            resp.setContentType("application/json");
            if (userId != 0) {
                Optional<ApplicationUser> user = ComponentAccessor.getUserManager().getUserById(userId);
                if (user.isPresent()) {
                    List<TestMonitorMember> monitorMembers = testMonitorService.getMonitorTeamMembers(userId, projectId,
                                                                                                      start, end);
                    String gson = new Gson().toJson(monitorMembers);
                    resp.getWriter().write(gson);
                }
            } else {
                List<TestMonitorMember> monitorMembers = testMonitorService.getMonitorAllMembers(projectId, start, end);
                resp.getWriter().write(new Gson().toJson(monitorMembers));
            }

        } catch (IllegalArgumentException e) {
            log.debug(String.format(Constants.TESTER_PERFORMANCE_AJAX_ERROR, req.getParameter("userId"),
                                    req.getParameter("projectId")));
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String customfieldName = req.getParameter("customfieldName");
        resp.setContentType(CONTENT_TYPE);
        resp.setCharacterEncoding(CHARACTER_ENCODING);
        resp.getWriter().write(new Gson().toJson(this.customFieldManager.getCustomFieldObjectByName(customfieldName).getId()));
        resp.setStatus(HttpServletResponse.SC_OK);
    }

}