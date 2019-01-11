package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import org.springframework.core.annotation.Order;

@Preload
public interface ScreenDevice extends Entity {
    @Order(value = 1)
    String getDeviceId();

    void setDeviceId(String deviceId);

    @Order(value = 2)
    String getDeviceName();
    void setDeviceName(String deviceName);

    @Order(value = 3)
    String getScreenDeviceType();
    void setScreenDeviceType(String deviceType);

    @Order(value = 4)
    String getDeviceSubType();
    void setDeviceSubType(String deviceSubType);

    @Order(value = 5)
    String getExternalTicketId();
    void setExternalTicketId(String externalTicketId);

    @Order(value = 6)
    String getSystemDeviceName();
    void setSystemDeviceName(String systemDeviceName);
}
