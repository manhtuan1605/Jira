package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;

@Preload
public interface SiIndex extends Entity {

    String getSiIndexAmigoPgVersion();
    void setSiIndexAmigoPgVersion(String siIndexAmigoPgVersion);

    String getSiIndexAmigoDataVersion();
    void setSiIndexAmigoDataVersion(String siIndexAmigoDataVersion);

    String getSiIndexSysSetIdVersion();
    void setSiIndexSysSetIdVersion(String siIndexSysSetIdVersion);

    String getSiIndexSysDataVersion();
    void setSiIndexSysDataVersion(String siIndexSysDataVersion);

    String getSiIndexSysSoft();
    void setSiIndexSysSoft(String siIndexSysSoft);

    String getSiIndexTestHuMarket();
    void setSiIndexTestHuMarket(String siIndexTestHuMarket);
}
