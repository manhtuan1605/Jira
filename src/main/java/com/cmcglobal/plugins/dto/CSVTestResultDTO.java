package com.cmcglobal.plugins.dto;

public class CSVTestResultDTO {
    private String blockingTicketId;
    private String blockingTicketType;
    private String BlockingTicketStatus;

    public CSVTestResultDTO(String blockingTicketId, String blockingTicketType, String blockingTicketStatus) {
        this.blockingTicketId = blockingTicketId;
        this.blockingTicketType = blockingTicketType;
        BlockingTicketStatus = blockingTicketStatus;
    }

    public String getBlockingTicketId() {
        return blockingTicketId;
    }

    public String getBlockingTicketType() {
        return blockingTicketType;
    }

    public String getBlockingTicketStatus() {
        return BlockingTicketStatus;
    }
}
