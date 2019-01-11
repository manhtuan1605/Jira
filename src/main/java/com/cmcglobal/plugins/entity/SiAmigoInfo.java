package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import org.springframework.core.annotation.Order;

@Preload
public interface SiAmigoInfo extends Entity {
    @Order(value = 1)
    String getReleaseDate();
    void setReleaseDate(String releaseDate);

    @Order(value = 2)
    String getBuildNumber();
    void setBuildNumber(String buildNumber);

    @Order(value = 3)
    String getDeviceType();
    void setDeviceType(String deviceType);

    @Order(value = 4)
    String getAmpType();
    void setAmpType(String ampType);

    @Order(value = 5)
    String getAmigoPgVersion();
    void setAmigoPgVersion(String amigoPgVersion);

    @Order(value = 6)
    String getAmigoDataVersion();
    void setAmigoDataVersion(String amigoDataVersion);

    @Order(value = 7)
    String getDeviceSubType();
    void setDeviceSubType(String deviceSubType);

    @Order(value = 8)
    String getAndroidKernelVersion();
    void setAndroidKernelVersion(String androidKernelVersion);

    @Order(value = 9)
    String getExternalTicketId();
    void setExternalTicketId(String externalTicketId);
}
