package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import org.springframework.core.annotation.Order;

@Preload
public interface HuTypeVehicle extends Entity {

    @Order(value = 1)
    String getDeviceType();

    void setDeviceType();

    @Order(value = 2)
    String getDeviceSubType();

    void setDeviceSubType();

    @Order(value = 3)
    String getAmpType();

    void setAmpType(String ampType);

    @Order(value = 4)
    String getVehicleParameter();

    void setVehicleParameter(String vehicleParameter);

    @Order(value = 5)
    String getSysDataPrefix();

    void setSysDataPrefix(String sysDataPrefix);

    @Order(value = 6)
    String getTestHuMarket();

    void setTestHuMarket(String testHuMarket);

    @Order(value = 7)
    String getExternalTicketId();

    void setExternalTicketId(String externalTicketId);

}
