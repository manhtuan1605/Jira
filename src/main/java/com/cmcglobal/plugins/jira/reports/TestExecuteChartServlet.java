package com.cmcglobal.plugins.jira.reports;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.NotFoundException;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.dto.TestExecuteChartDTO;
import com.cmcglobal.plugins.service.TestExecuteChartService;
import com.cmcglobal.plugins.utils.Constants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class TestExecuteChartServlet extends HttpServlet {
    private static final Logger                  log = LoggerFactory.getLogger(TestExecuteChartServlet.class);
    private final        TestExecuteChartService testExecuteChartService;

    public TestExecuteChartServlet(final TestExecuteChartService testExecuteChartService) {
        this.testExecuteChartService = testExecuteChartService;
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) {
        final String sProjectId = req.getParameter(Constants.REQUEST_PARAMETER_PROJECT_ID);
        final String sUserName = req.getParameter(Constants.REQUEST_PARAMETER_USERNAMES);
        final String sStartDate = req.getParameter(Constants.REQUEST_PARAMETER_START_DATE);
        final String sEndDate = req.getParameter(Constants.REQUEST_PARAMETER_END_DATE);
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMATTER_D_MM_YYYY);
        List<ApplicationUser> assignees = new ArrayList<>();
        final long projectId;
        final LocalDate startDate;
        final LocalDate endDate;

        try {
            projectId = Long.parseLong(sProjectId);
            final List<String> sUserNames = newArrayList(sUserName.replaceAll(Constants.APOSTROPHE,Constants.QUOTATION_MARKS).split(Constants.COMMA));
            for (final String username : sUserNames) {
                final ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByName(username);
                if (null == applicationUser) {
                    throw new NotFoundException(Constants.ERR_MESSAGE_ASSIGNEE_NOT_EXIST);
                }
                assignees.add(applicationUser);
            }
            if (sStartDate.isEmpty() || sEndDate.isEmpty()) {
                startDate = LocalDate.now();
                endDate = LocalDate.now();
            } else {
                startDate = LocalDate.parse(sStartDate, formatter);
                endDate = LocalDate.parse(sEndDate, formatter);
            }
        } catch (final NumberFormatException | DateTimeParseException | NotFoundException e) {
            log.error(e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            final List<TestExecuteChartDTO> testExecuteChartDTOs
                    = testExecuteChartService.getTestExecuteChartByProjectIdAndAssigneeAndDate(projectId, assignees,
                                                                                               startDate, endDate);
            resp.getWriter().write(new Gson().toJson(testExecuteChartDTOs));
        } catch (final IOException e) {
            log.error(e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
