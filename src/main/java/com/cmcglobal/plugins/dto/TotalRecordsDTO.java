package com.cmcglobal.plugins.dto;

public class TotalRecordsDTO {
    private int headerIndex;
    private int totalRecords;

    public TotalRecordsDTO(int headerIndex, int totalRecords) {
        this.headerIndex = headerIndex;
        this.totalRecords = totalRecords;
    }

    public int getHeaderIndex() {
        return headerIndex;
    }

    public void setHeaderIndex(int headerIndex) {
        this.headerIndex = headerIndex;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }
}
