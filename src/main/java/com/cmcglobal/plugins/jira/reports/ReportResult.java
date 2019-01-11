package com.cmcglobal.plugins.jira.reports;

import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.cmcglobal.plugins.service.TestMonitorService;
import com.cmcglobal.plugins.service.TestResultHistoryService;
import com.cmcglobal.plugins.utils.Constants;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class ReportResult extends AbstractReport {

    private final TestMonitorService testMonitorService;

    private TestResultHistoryService testResultHistoryService;

    @Inject
    public ReportResult(final TestMonitorService testMonitorService, TestResultHistoryService testResultHistoryService) {
        this.testMonitorService = testMonitorService;
        this.testResultHistoryService = testResultHistoryService;
    }
    public Map<String, Object> data = new HashMap<>();
    @Override
    public String generateReportHtml(final ProjectActionSupport projectActionSupport, final Map map) throws Exception {
        final long projectId = Long.parseLong(map.get("selectedProjectId").toString());
        try {
            data.put("projectId", projectId);
            return descriptor.getHtml("view", data);
        } catch (Exception e) {
            data.put("error", e.getMessage());
            data.put("message", Constants.FUNCTION_NOT_FOUND_MESSAGE);
            return descriptor.getHtml("error", data);
        }

    }
    @Override
    public boolean isExcelViewSupported() {
        return true;
    }
    @Override
    public String generateReportExcel(ProjectActionSupport action,
                                      Map reqParams) throws Exception {

        return descriptor.getHtml("excel", data);
    }
}


