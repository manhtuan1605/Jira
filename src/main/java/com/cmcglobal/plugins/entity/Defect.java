package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import org.springframework.core.annotation.Order;

@Preload
public interface Defect extends Entity {

    @Order(value = 1)
    String getSeverity();

    void setSeverity(String severity);

    @Order(value = 2)
    String getExternalTicketId();

    void setExternalTicketId(String externalTicketId);

    @Order(value = 3)
    String getQcActivity();

    void setQcActivity(String qcActivity);

    @Order(value = 4)
    String getCauseAnalysis();

    void setCauseAnalysis(String causeAnalysis);

    @Order(value = 5)
    String getCorrectiveAction();

    void setCorrectiveAction(String correctiveAction);

    @Order(value = 6)
    String getCorrectiveActionTranslate();

    void setCorrectiveActionTranslate(String correctiveActionTranslate);

    @Order(value = 7)
    String getRole();

    void setRole(String role);

}
