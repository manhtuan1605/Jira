package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import org.springframework.core.annotation.Order;

@Preload
public interface SetOfDevice extends Entity {

    @Order(value = 1)
    String getModel();

    void setModel(String model);

    @Order(value = 2)
    String getHuDevice();

    void setHuDevice(String huDevice);

    @Order(value = 3)
    String getScreenDevice();

    void setScreenDevice(String screenDevice);

    @Order(value = 4)
    String getHuIndex();

    void setHuIndex(String huIndex);

    @Order(value = 5)
    String getSiIndex();

    void setSiIndex(String siIndex);

    @Order(value = 6)
    String getAmigoPgVersion();

    void setAmigoPgVersion(String amigoPgVersion);

    @Order(value = 7)
    String getAmigoDataVersion();

    void setAmigoDataVersion(String amigoDataVersion);

    @Order(value = 8)
    String getBuildNumber();

    void setBuildNumber(String buildNumber);

    @Order(value = 9)
    String getAmpType();

    void setAmpType(String ampType);

    @Order(value = 10)
    String getScreenDeviceType();

    void setScreenDeviceType(String screenDeviceType);

    @Order(value = 11)
    String getSysSoft();

    void setSysSoft(String sysSoft);

    @Order(value = 12)
    String getSysSetIdVersion();

    void setSysSetIdVersion(String sysSetIdVersion);

    @Order(value = 13)
    String getSysDataVersion();

    void setSysDataVersion(String sysDataVersion);

    @Order(value = 14)
    String getVrTssVersion();

    void setVrTssVersion(String vrTssVersion);

    @Order(value = 15)
    String getDeviceType();

    void setDeviceType(String deviceType);

    @Order(value = 16)
    String getDeviceSubType();

    void setDeviceSubType(String deviceSubType);

    @Order(value = 17)
    String getVehicleParameter();

    void setVehicleParameter(String vehicleParameter);

    @Order(value = 18)
    String getTestHuMarket();

    void setTestHuMarket(String testHuMarket);

    @Order(value = 20)
    String getExternalTicketId();

    void setExternalTicketId(String externalTicketId);

    @Order(value = 21)
    String getDeviceName();

    void setDeviceName(String deviceName);

    @Order(value = 22)
    String getDataMap();

    void setDataMap(String dataMap);

    @Order(value = 23)
    String getSysDataPrefix();

    void setSysDataPrefix(String sysDataPrefix);

    @Order(value = 24)
    String getSiIndexValue();

    void setSiIndexValue(String siIndexValue);



}
