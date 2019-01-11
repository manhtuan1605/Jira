package com.cmcglobal.plugins.service;

import com.cmcglobal.plugins.dto.ExternalDeviceTypeDTO;
import com.cmcglobal.plugins.entity.ExternalDeviceType;

import java.util.List;

public interface ExternalDeviceTypeService {
    List<ExternalDeviceType> findByProjectId(final long projectID);

    ExternalDeviceType create(ExternalDeviceTypeDTO typeDto);

    Boolean isExistedDeviceType(final String deviceType, final Long thePid);

    Boolean findById(final long theTypeid);

    boolean isExistedTypeNameSecond(String name, Long theTypeId, Long projectKey);

    ExternalDeviceTypeDTO findByTypeId(final long typeId);

    boolean update(ExternalDeviceTypeDTO externalDeviceTypeDTO);
}
