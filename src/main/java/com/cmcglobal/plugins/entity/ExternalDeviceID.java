package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import org.springframework.core.annotation.Order;

@Preload
public interface ExternalDeviceID extends Entity {


    @Order(value = 1)
    String getExternalDeviceID();

    void setExternalDeviceID(String externalDeviceID);

    @Order(value = 2)
    String getBluetooth1();

    void setBluetooth1(String bluetooth1);

    @Order(value = 3)
    String getBluetooth2();

    void setBluetooth2(String bluetooth2);

    @Order(value = 4)
    String getBluetooth3();

    void setBluetooth3(String bluetooth3);

    @Order(value = 5)
    String getBluetooth4();

    void setBluetooth4(String bluetooth4);

    @Order(value = 6)
    String getBluetooth5();

    void setBluetooth5(String bluetooth5);

    @Order(value = 7)
    String getBluetooth6();

    void setBluetooth6(String bluetooth6);

    @Order(value = 8)
    String getUSB1();

    void setUSB1(String USB1);

    @Order(value = 9)
    String getUSB2();

    void setUSB2(String USB2);

    @Order(value = 10)
    String getNFC();

    void setNFC(String NFC);

    @Order(value = 11)
    String getAUX();

    void setAUX(String AUX);

    @Order(value = 12)
    String getHDMI();

    void setHDMI(String HDMI);
}
