package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import org.springframework.core.annotation.Order;

@Preload
public interface HuDevice extends Entity {

    @Order(value = 1)
    String getHuId();

    void setHuId(String huId);

    @Order(value = 2)
    String getTestHuMarket();

    void setTestHuMarket(String testHuMarket);

    @Order(value = 3)
    String getDeviceType();

    void setDeviceType(String deviceType);

    @Order(value = 4)
    String getDeviceSubType();

    void setDeviceSubType(String deviceSubType);

    @Order(value = 5)
    String getAmpType();

    void setAmpType(String ampType);


    @Order(value = 6)
    String getVoiceRecognizeVersion();

    void setVoiceRecognizeVersion(String voiceRecognizeVersion);

    @Order(value = 7)
    String getExternalTicketId();
    void setExternalTicketId(String externalTicketId);

}
