package com.cmcglobal.plugins.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.cmcglobal.plugins.dto.VehicleParameterDTO;
import com.cmcglobal.plugins.entity.VehicleParameter;

import java.util.List;

@Transactional
public interface VehicleParameterService {
    List<VehicleParameter> findByProjectId(final long projectId);

    VehicleParameter create(VehicleParameterDTO vehicleParameterDTO);

    Boolean isExistedVehicleParameterName(final String vehicleParameterName, final long projectId);

    boolean isExistedVehicleParameterNameSecond (String vehicleParameterName, long vehicleParameterId, long projectId);

    VehicleParameterDTO findByVehicleParameterId(final long vehicleParameterId);

    boolean update(VehicleParameterDTO vehicleParameterDTO);

    boolean findById(final long vehicleParameterId);
}
