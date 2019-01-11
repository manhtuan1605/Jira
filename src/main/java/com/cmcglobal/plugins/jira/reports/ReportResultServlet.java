package com.cmcglobal.plugins.jira.reports;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.cmcglobal.plugins.dto.ReportResultDTO;
import com.cmcglobal.plugins.entity.TestResultHistory;
import com.cmcglobal.plugins.service.TestMonitorService;
import com.cmcglobal.plugins.service.TestResultHistoryService;
import com.cmcglobal.plugins.utils.Constants;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.cmcglobal.plugins.utils.Constants.CHARACTER_ENCODING;
import static com.cmcglobal.plugins.utils.Constants.CONTENT_TYPE;

public class ReportResultServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ReportResultServlet.class);

    private TestMonitorService testMonitorService;

    @Autowired
    private CustomFieldManager customFieldManager;

    private TestResultHistoryService testResultHistoryService;

    public ReportResultServlet(TestMonitorService testMonitorService, TestResultHistoryService testResultHistoryService) {

        this.testMonitorService = testMonitorService;
        this.testResultHistoryService = testResultHistoryService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String startDate = req.getParameter("startDate");
        String endDate = req.getParameter("endDate");
        final long projectId = Long.parseLong(req.getParameter("projectId"));

        Map<String, Object> data = new HashMap<>();
        final ProjectManager projectManager = ComponentAccessor.getProjectManager();
        final Project project = projectManager.getProjectObj(projectId);
        List<ProjectComponent> componentList = (List<ProjectComponent>) project.getComponents();
        ArrayList<ReportResultDTO> reportResultDTOS = new ArrayList<>();
        try {
            //Mặc định ngày kết thúc là ngày hiện tại
            Calendar c = new GregorianCalendar();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            Date end = c.getTime();
            Date start = null;
            //Kiểm tra xem ngày nhận từ view về có null ko
            if(!startDate.isEmpty()) {
                start = new SimpleDateFormat(Constants.PATTERM_DATE).parse(startDate);
            }
            if(!endDate.isEmpty()) {
                end = new SimpleDateFormat(Constants.PATTERM_DATE).parse(endDate);
            }
            //duyệt các component trong project
            for (ProjectComponent projectComponent : componentList) {
                HashMap<String, Integer> map = (HashMap<String, Integer>)testResultHistoryService.countTotalTestResult(projectId, projectComponent, start, end);
                int totalOK = map.get("totalOK");
                int oKPlus = 0;
                int oKMinus = 0;
                int nGMunis = 0;
                int nGPlus = 0;
                int totalNG = map.get("totalNG");
                int pNMinus = 0;
                int pNPlus = 0;
                int totalPN = map.get("totalPN");
                //Lấy ra các issue ở component đang duyệt. Theo ngày implemented trong quá khứ.
                List<Issue> issues = testMonitorService.getIssuesByProjectandComponentAndEndDate(projectId, projectComponent, end);
                //Duyệt từng issue
                for (Issue issue : issues) {
                    //Lấy lịch sử cuối cùng của issue tính trước hoặc bằng ngày kết thúc để tính toán.
                    List<TestResultHistory> testResultHistories = testResultHistoryService.getTestResultHistoryByIssueAndImplementedDateByEndDate(issue.getId(), end);
                    if(!testResultHistories.isEmpty()){
                        if(testResultHistories.get(0).getLatestResult().contains(Constants.PN)){
                            pNPlus++;
                            continue;
                        }
                        switch (testResultHistories.get(0).getLatestResult()) {
                            case Constants.OK:
                                oKPlus++;
                                break;
                            case Constants.NG:
                                nGPlus++;
                                break;
                            default:
                                break;
                        }
                    }
                    //Lấy lịch sử cuối cùng trước ngày kết thúc 1 ngày của issue để tính toán.
                    List<TestResultHistory> testResultHistoryOneDayBefore = testResultHistoryService.getHistoryByIssueTheDayBeforeEndDate(issue.getId(), end);
                    //Kiểm tra xem nó có lịch sử trước 1 ngày không?
                    if (!testResultHistoryOneDayBefore.isEmpty()) {
                        if(testResultHistoryOneDayBefore.get(0).getLatestResult().contains(Constants.PN)){
                            pNMinus++;
                            continue;
                        }
                        switch (testResultHistoryOneDayBefore.get(0).getLatestResult()) {
                            case Constants.OK:
                                oKMinus++;
                                break;
                            case Constants.NG:
                                if (testResultHistories.get(0).getLatestResult().equals(Constants.NG)) {
                                    nGPlus--;
                                    break;
                                }
                                nGMunis++;
                                break;
                            default:
                                break;
                        }
                    }
                }
                ReportResultDTO reportResult = new ReportResultDTO();
                reportResult.setComponent(projectComponent.getName());
                reportResult.setTotalTestCase(map.get("totalTestcase"));
                reportResult.setOkPlus(oKPlus);
                reportResult.setOkMinus(oKMinus);
                reportResult.setTotalOK(totalOK);
                reportResult.setTodayNG(nGPlus - nGMunis);
                reportResult.setnGMinus(nGMunis);
                reportResult.setTotalNG(totalNG);
                reportResult.setnGPlus(nGPlus);
                reportResult.setTodayPN(pNPlus - pNMinus);
                reportResult.setpNMinus(pNMinus);
                reportResult.setTotalPN(totalPN);
                reportResult.setpNPlus(pNPlus);
                reportResultDTOS.add(reportResult);
            }
            data.put("reportResultDTOS", reportResultDTOS);
            resp.setContentType(CONTENT_TYPE);
            resp.setCharacterEncoding(CHARACTER_ENCODING);
            resp.getWriter().write(new Gson().toJson(data));
            resp.setStatus(HttpServletResponse.SC_OK);

        }catch (Exception e){
            data.put("error", e.getMessage());
            data.put("message", e.getStackTrace().toString() + e.getMessage() + e.toString() );
            e.printStackTrace();
            resp.setContentType(CONTENT_TYPE);
            resp.setCharacterEncoding(CHARACTER_ENCODING);
            resp.getWriter().write(new Gson().toJson(data));
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }

}