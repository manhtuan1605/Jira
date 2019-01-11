package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.StringLength;
import org.springframework.core.annotation.Order;

import static net.java.ao.schema.StringLength.UNLIMITED;

@Preload
public interface AutomotiveDevice extends Entity {
    @Order(value = 1)
    String getCmcCode();
    void setCmcCode(String cmcCode);

    @Order(value = 2)
    String getPartNumberJapaneseName();
    void setPartNumberJapaneseName(String partNumberJapaneseName);

    @Order(value = 3)
    String getPartNumber();
    void setPartNumber(String partNumber);

    @Order(value = 4)
    String getPartNumberForInvoice();
    void setPartNumberForInvoice(String partNumberForInvoice);

    @Order(value = 5)
    String getPartNumberForInvoiceGroup();
    void setPartNumberForInvoiceGroup(String setPartNumberForInvoiceGroup);

    @Order(value = 6)
    String getNote();
    void setNote(String note);

    @Order(value = 7)
    String getExternalDeviceType();
    void setExternalDeviceType(String externalDeviceType);

}
