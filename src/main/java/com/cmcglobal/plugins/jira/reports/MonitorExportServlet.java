package com.cmcglobal.plugins.jira.reports;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.dto.TestMonitorMember;
import com.cmcglobal.plugins.service.TestMonitorService;
import com.cmcglobal.plugins.utils.Constants;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MonitorExportServlet extends HttpServlet{
    private static final Logger log = LoggerFactory.getLogger(MonitorExportServlet.class);
    private static final String MONITOR_QUANTITY = "Qty";
    private static final String MONITOR_TIME = "Time (h)";
    private static final String MONITOR_PRODUCTIVITY = "Productivity";
    private static final String MONITOR_COMMENTS = "Comments";

    private final TestMonitorService testMonitorService;

    public MonitorExportServlet(TestMonitorService testMonitorService) {
        this.testMonitorService = testMonitorService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_DD_MM_YYYY);
        log.info("Export monitoring report");
        long userId = Long.parseLong(req.getParameter("userId"));
        long projectId = Long.parseLong(req.getParameter("projectId"));
        LocalDate start = LocalDate.parse(req.getParameter("start"), formatter);
        LocalDate end = LocalDate.parse(req.getParameter("end"), formatter);
        String fileName = "monitor_report_".concat(Long.toString(System.currentTimeMillis())).concat(".xlsx");
        resp.setContentType(Constants.EXCEL_XLSX_CONTENT_TYPE);
        resp.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        List<TestMonitorMember> monitorMembers = new ArrayList<>();
        if (userId != 0) {
            Optional<ApplicationUser> user = ComponentAccessor.getUserManager().getUserById(userId);
            if (user.isPresent()) {
                monitorMembers = testMonitorService.getMonitorTeamMembers(userId, projectId,start, end);

            }
        } else {
            monitorMembers = testMonitorService.getMonitorAllMembers(projectId, start, end);
        }
        Project project = ComponentAccessor.getProjectManager().getProjectObj(projectId);
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        ProjectRole role = testMonitorService.getRole(user, projectId);
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reports");

            CellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);

            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Project :");
            row.createCell(1).setCellValue(project.getName());
            row.createCell(4).setCellValue("Start Date :");
            row.createCell(5).setCellValue(req.getParameter("start"));

            row = sheet.createRow(1);
            row.createCell(0).setCellValue("User:");
            row.createCell(1).setCellValue(user.getDisplayName());
            row.createCell(4).setCellValue("End Date :");
            row.createCell(5).setCellValue(req.getParameter("end"));

            row = sheet.createRow(2);
            row.createCell(0).setCellValue("Role:");
            row.createCell(1).setCellValue(role.getName());

            int rowCount = 4;
            row = sheet.createRow(rowCount);
            row.createCell(0).setCellValue("Project Team");
            row.getCell(0).setCellStyle(style);
            sheet.addMergedRegion(new CellRangeAddress(rowCount,rowCount+1,0,0));

            row.createCell(1).setCellValue(Constants.TYPE_OF_TEST_EXECUTE_STATUS);
            row.getCell(1).setCellStyle(style);
            row.createCell(2);
            row.getCell(2).setCellStyle(style);
            row.createCell(3);
            row.getCell(3).setCellStyle(style);
            sheet.addMergedRegion(new CellRangeAddress(rowCount,rowCount,1,3));

            row.createCell(4).setCellValue(Constants.TYPE_OF_WORK_DEFECT_SUBMIT);
            row.getCell(4).setCellStyle(style);
            row.createCell(5);
            row.getCell(5).setCellStyle(style);
            row.createCell(6);
            row.getCell(6).setCellStyle(style);
            sheet.addMergedRegion(new CellRangeAddress(rowCount,rowCount,4,6));

            row.createCell(7).setCellValue(Constants.TYPE_OF_WORK_QA_SUBMIT);
            row.getCell(7).setCellStyle(style);
            row.createCell(8);
            row.getCell(8).setCellStyle(style);
            row.createCell(9);
            row.getCell(9).setCellStyle(style);
            sheet.addMergedRegion(new CellRangeAddress(rowCount,rowCount,7,9));

            row.createCell(10).setCellValue(Constants.TYPE_OF_WORK_DEFECT_FIX);
            row.getCell(10).setCellStyle(style);
            row.createCell(11);
            row.getCell(11).setCellStyle(style);
            row.createCell(12);
            row.getCell(12).setCellStyle(style);
            sheet.addMergedRegion(new CellRangeAddress(rowCount,rowCount,10,12));

            row.createCell(13).setCellValue(Constants.TYPE_OF_WORK_DF_QA_FOLLOW_UP);
            row.getCell(13).setCellStyle(style);
            row.createCell(14);
            row.getCell(14).setCellStyle(style);
            row.createCell(15);
            row.getCell(15).setCellStyle(style);
            sheet.addMergedRegion(new CellRangeAddress(rowCount,rowCount,13,15));

            row.createCell(16).setCellValue(Constants.TYPE_OF_WORK_OTHER);
            row.getCell(16).setCellStyle(style);
            row.createCell(17);
            row.getCell(17).setCellStyle(style);
            row.createCell(18);
            row.getCell(18).setCellStyle(style);
            sheet.addMergedRegion(new CellRangeAddress(rowCount,rowCount,16,18));
            rowCount++;

            int columnCount = 0;

            row = sheet.createRow(rowCount++);
            row.createCell(columnCount);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_QUANTITY);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_TIME);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_PRODUCTIVITY);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_QUANTITY);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_TIME);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_PRODUCTIVITY);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_QUANTITY);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_TIME);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_PRODUCTIVITY);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_QUANTITY);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_TIME);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_PRODUCTIVITY);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_QUANTITY);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_TIME);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_PRODUCTIVITY);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_QUANTITY);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_TIME);
            row.getCell(columnCount++).setCellStyle(style);
            row.createCell(columnCount).setCellValue(MONITOR_COMMENTS);
            row.getCell(columnCount++).setCellStyle(style);

            for(TestMonitorMember monitor: monitorMembers) {
                int column = 0;
                row = sheet.createRow(rowCount++);
                row.createCell(column).setCellValue(monitor.getMember().getDisplayName());
                row.getCell(column++).setCellStyle(style);
                row.createCell(column).setCellValue(monitor.getTestExecute().getQuantity());
                row.getCell(column++).setCellStyle(style);
                row.createCell(column).setCellValue(monitor.getTestExecute().getTime());
                row.getCell(column++).setCellStyle(style);
                if (monitor.getTestExecute().getProductivity().equals(Constants.CELL_NOT_APPLICABLE)) {
                    row.createCell(column).setCellValue(monitor.getTestExecute().getProductivity().toString());
                } else {
                    row.createCell(column).setCellValue((Double) monitor.getTestExecute().getProductivity());
                }
                row.getCell(column++).setCellStyle(style);

                row.createCell(column).setCellValue(monitor.getDefectSubmit().getQuantity());
                row.getCell(column++).setCellStyle(style);
                row.createCell(column).setCellValue(monitor.getDefectSubmit().getTime());
                row.getCell(column++).setCellStyle(style);
                if (monitor.getDefectSubmit().getProductivity().equals(Constants.CELL_NOT_APPLICABLE)) {
                    row.createCell(column).setCellValue(monitor.getDefectSubmit().getProductivity().toString());
                } else {
                    row.createCell(column).setCellValue((Double)monitor.getDefectSubmit().getProductivity());
                }
                row.getCell(column++).setCellStyle(style);

                row.createCell(column).setCellValue(monitor.getQaSubmit().getQuantity());
                row.getCell(column++).setCellStyle(style);
                row.createCell(column).setCellValue(monitor.getQaSubmit().getTime());
                row.getCell(column++).setCellStyle(style);
                if (monitor.getQaSubmit().getProductivity().equals(Constants.CELL_NOT_APPLICABLE)) {
                    row.createCell(column).setCellValue(monitor.getQaSubmit().getProductivity().toString());
                } else {
                    row.createCell(column).setCellValue((Double) monitor.getQaSubmit().getProductivity());
                }
                row.getCell(column++).setCellStyle(style);

                row.createCell(column).setCellValue(monitor.getDefectFix().getQuantity());
                row.getCell(column++).setCellStyle(style);
                row.createCell(column).setCellValue(monitor.getDefectFix().getTime());
                row.getCell(column++).setCellStyle(style);
                if(monitor.getDefectFix().getProductivity().equals(Constants.CELL_NOT_APPLICABLE)) {
                    row.createCell(column).setCellValue(monitor.getDefectFix().getProductivity().toString());
                } else {
                    row.createCell(column).setCellValue((Double)monitor.getDefectFix().getProductivity());
                }
                row.getCell(column++).setCellStyle(style);

                row.createCell(column).setCellValue(monitor.getDfAndQaFollow().getQuantity());
                row.getCell(column++).setCellStyle(style);
                row.createCell(column).setCellValue(monitor.getDfAndQaFollow().getTime());
                row.getCell(column++).setCellStyle(style);
                if (monitor.getDfAndQaFollow().getProductivity().equals(Constants.CELL_NOT_APPLICABLE)) {
                    row.createCell(column).setCellValue(monitor.getDfAndQaFollow().getProductivity().toString());
                } else {
                    row.createCell(column).setCellValue((Double) monitor.getDfAndQaFollow().getProductivity());
                }
                row.getCell(column++).setCellStyle(style);

                row.createCell(column).setCellValue(monitor.getOther().getQuantity());
                row.getCell(column++).setCellStyle(style);
                row.createCell(column).setCellValue(monitor.getOther().getTime());
                row.getCell(column++).setCellStyle(style);
                row.createCell(column).setCellValue(monitor.getOther().getProductivity().toString());
                row.getCell(column).setCellStyle(style);
            }
            for (int i = 0; i <columnCount; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(resp.getOutputStream());
        }

    }
}