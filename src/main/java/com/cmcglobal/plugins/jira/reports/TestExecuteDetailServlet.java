package com.cmcglobal.plugins.jira.reports;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.NotFoundException;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.dto.TestExecuteDetailDTO;
import com.cmcglobal.plugins.service.TestExecuteDetailService;
import com.cmcglobal.plugins.utils.Constants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class TestExecuteDetailServlet extends HttpServlet {
    private static final Logger                   log = LoggerFactory.getLogger(TestExecuteDetailServlet.class);
    private final        TestExecuteDetailService testExecuteDetailService;

    public TestExecuteDetailServlet(final TestExecuteDetailService testExecuteDetailService) {
        this.testExecuteDetailService = testExecuteDetailService;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        final String sProjectId = req.getParameter(Constants.REQUEST_PARAMETER_PROJECT_ID);
        final String sAssigneeId = req.getParameter(Constants.REQUEST_PARAMETER_ASSIGNEE_ID);
        final String sStartDate = req.getParameter(Constants.REQUEST_PARAMETER_START_DATE);
        final String sEndDate = req.getParameter(Constants.REQUEST_PARAMETER_END_DATE);
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMATTER_D_MM_YYYY);
        final ApplicationUser assignee;
        final long projectId;
        final LocalDate startDate;
        final LocalDate endDate;

        try {
            projectId = Long.parseLong(sProjectId);
            final long assigneeId = Long.parseLong(sAssigneeId);
            if (sStartDate.isEmpty() || sEndDate.isEmpty()) {
                startDate = LocalDate.now();
                endDate = LocalDate.now();
            } else {
                startDate = LocalDate.parse(sStartDate, formatter);
                endDate = LocalDate.parse(sEndDate, formatter);
            }
            final Optional<ApplicationUser> applicationUser = ComponentAccessor.getUserManager()
                                                                               .getUserById(assigneeId);
            if (applicationUser.isPresent()) {
                assignee = applicationUser.get();
            } else {
                throw new NotFoundException(Constants.ERR_MESSAGE_ASSIGNEE_NOT_EXIST);
            }
        } catch (final NumberFormatException | DateTimeParseException | NotFoundException e) {
            log.error(e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            final List<TestExecuteDetailDTO> testExecuteDetailDTOs
                    = testExecuteDetailService.getTestExecuteDetailByProjectIdAndAssigneeAndDate(projectId, assignee,
                                                                                                 startDate, endDate);
            resp.getWriter().write(new Gson().toJson(testExecuteDetailDTOs));
        } catch (final IOException e) {
            log.error(e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
