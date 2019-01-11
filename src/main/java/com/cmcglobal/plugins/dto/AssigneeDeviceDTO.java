package com.cmcglobal.plugins.dto;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

public class AssigneeDeviceDTO {
    private String partNumber;
    private String assignee;
    private String message;

    public AssigneeDeviceDTO() {
    }

    private AssigneeDeviceDTO(String partNumber, String assignee, String message) {
        this.partNumber = partNumber;
        this.assignee = assignee;
        this.message = message;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static AssigneeDeviceDTO create(Row row, StringBuilder builder) {
        final DataFormatter formatter = new DataFormatter();
        return new AssigneeDeviceDTO(formatter.formatCellValue(row.getCell(0)), formatter.formatCellValue(row.getCell(1)), builder.toString());
    }
}
