package com.cmcglobal.plugins.servlet;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.cmcglobal.plugins.service.TesterPerformanceService;
import com.cmcglobal.plugins.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.cmcglobal.plugins.utils.Constants.CHARACTER_ENCODING;
import static com.cmcglobal.plugins.utils.Constants.CONTENT_TYPE;
import static com.cmcglobal.plugins.utils.Constants.PROJECT_ID;

public class TesterPerformanceChartServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ImportTestCaseServlet.class);

    @Autowired
    private TesterPerformanceService testerPerformanceService;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        Long projectId = Long.parseLong(req.getParameter(PROJECT_ID));
        resp.setContentType(CONTENT_TYPE);
        resp.setCharacterEncoding(CHARACTER_ENCODING);
        List<String> applicationUsers = new ArrayList<>();
        List<String> applicationUsersQC = new ArrayList<>();
        Set<ApplicationUser> userPM = testerPerformanceService.getMemberProjectWithRole(projectId,
                                                                                        Constants.PROJECT_ROLE_PM);
        Set<ApplicationUser> userQCLead = testerPerformanceService.getMemberProjectWithRole(projectId,
                                                                                            Constants.PROJECT_ROLE_QC_LEAD);
        Set<ApplicationUser> userQC = testerPerformanceService.getMemberProjectWithRole(projectId,
                                                                                        Constants.PROJECT_ROLE_QC);
        for (ApplicationUser member : userPM) {
            applicationUsers.add(member.getUsername());
        }
        for (ApplicationUser member : userQCLead) {
            applicationUsers.add(member.getUsername());
        }
        for (ApplicationUser member : userQC) {
            applicationUsers.add(member.getUsername());
            applicationUsersQC.add(member.getUsername());
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("allMember", applicationUsers);
            jsonObject.put("QC", applicationUsersQC);
        } catch (JSONException e) {
            log.error(e.getMessage());
        }
        resp.getWriter().write(jsonObject.toString());
        resp.setStatus(HttpServletResponse.SC_OK);

    }

}
