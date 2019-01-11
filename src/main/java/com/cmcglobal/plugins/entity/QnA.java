package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import org.springframework.core.annotation.Order;

import java.util.Date;

@Preload
public interface QnA extends Entity {

    @Order(value = 1)
    String getDescriptionTranslate();

    void setDescriptionTranslate(String descriptionTranslate);

    @Order(value = 2)
    String getProduct();

    void setProduct(String product);

    @Order(value = 3)
    String getExternalTicketId();

    void setExternalTicketId(String externalTicketId);

    @Order(value = 4)
    String getUnit();

    void setUnit(String unit);

    @Order(value = 5)
    String getConclusion();

    void setConclusion(String conclusion);

    @Order(value = 6)
    String getConclusionTranslate();

    void setConclusionTranslate(String conclusionTranslate);
}
