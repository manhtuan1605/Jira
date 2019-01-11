package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import org.springframework.core.annotation.Order;

import java.util.Date;
@Preload
public interface SiSysInfo extends Entity {

    @Order(value = 1)
    String getReleaseDate();
    void setReleaseDate(String releaseDate);

    @Order(value = 2)
    String getBuildNumber();
    void setBuildNumber(String buildNumber);

    @Order(value = 3)
    String getScreenType();
    void setScreenType(String screenType);

    @Order(value = 4)
    String getSysSetIdVersion();
    void setSysSetIdVersion(String sysSetIdVersion);

    @Order(value = 5)
    String getSysDataVersion();
    void setSysDataVersion(String sysDataVersion);

    @Order(value = 6)
    String getSysSoft();
    void setSysSoft(String sysSoft);

    @Order(value = 7)
    String getExternalTicketId();
    void setExternalTicketId(String externalTicketId);

    @Order(value = 8)
    String getDescription();
    void setDescription(String description);
}
