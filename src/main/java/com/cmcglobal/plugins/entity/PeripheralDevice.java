package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import org.springframework.core.annotation.Order;

@Preload
public interface PeripheralDevice extends Entity {

    @Order(value = 1)
    String getCmcCode();
    void setCmcCode(String cmcCode);

    @Order(value = 2)
    String getDeviceName();
    void setDeviceName(String deviceName);

    @Order(value = 3)
    String getOs();
    void setOS(String os);

    @Order(value = 4)
    String getOsVersion();
    void setOsVersion(String osVersion);

    @Order(value = 5)
    String getWifiMacAddress();
    void setWifiMacAddress(String wifiMacAddress);

    @Order(value = 6)
    String getBluetoothMacAddress();
    void setBluetoothMacAddress(String bluetoothMacAddress);

    @Order(value = 7)
    String getBluetoothVersion();
    void setBluetoothVersion(String bluetoothVersion);

    @Order(value = 8)
    String getHfpVersion();
    void setHfpVersion(String hfpVersion);

    @Order(value = 9)
    String getAvrcpVersion();
    void setAvrcpVersion(String avrcpVersion);

    @Order(value = 10)
    String getA2dpVersion();
    void setA2dpVersion(String a2DPVersion);

    @Order(value = 11)
    String getMapVersion();
    void setMapVersion(String mapVersion);

    @Order(value = 12)
    String getPbapVersion();
    void setPbapVersion(String pbapVersion);

    @Order(value = 13)
    String getAtCommand();
    void setAtCommand(String atCommand);

    @Order(value = 14)
    String getMirrorLink();
    void setMirrorLink(String mirrorLink);

    @Order(value = 15)
    String getCarPlay();
    void setCarPlay(String carPlay);

    @Order(value = 16)
    String getAndroidAuto();
    void setAndroidAuto(String androidAuto);

    @Order(value = 17)
    String getNote();
    void setNote(String note);

    @Order(value = 18)
    String getExternalDeviceType();
    void setExternalDeviceType(String externalDeviceType);
}
