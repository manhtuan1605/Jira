package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
import com.cmcglobal.plugins.dto.VehicleParameterDTO;
import com.cmcglobal.plugins.entity.TestHuMarket;
import com.cmcglobal.plugins.entity.VehicleParameter;
import com.cmcglobal.plugins.service.VehicleParameterService;
import com.cmcglobal.plugins.utils.Constants;
import net.java.ao.DBParam;
import net.java.ao.Query;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

@Scanned
@Named
public class VehicleParameterServiceImpl implements VehicleParameterService {

    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public VehicleParameterServiceImpl(final ActiveObjects ao, final UserManager userManager) {
        this.ao = checkNotNull(ao);
    }

    @Override
    public List<VehicleParameter> findByProjectId(final long projectId) {
        return newArrayList(ao.find(VehicleParameter.class,
                                    Query.select().where("PROJECT_ID=?", projectId).order("CREATE_DATE DESC")));
    }

    @Override
    public VehicleParameter create(VehicleParameterDTO vehicleParameterDTO) {
        final VehicleParameter vehicleParameterCreated = ao.create(VehicleParameter.class,
                                                            new DBParam("PROJECT_ID", vehicleParameterDTO.getProjectId()));
        vehicleParameterCreated.setVehicleParameterName(vehicleParameterDTO.getVehicleParameterName());
        if (null != vehicleParameterDTO.getCreateDate()) {
            vehicleParameterCreated.setCreateDate(vehicleParameterDTO.getCreateDate());
        }
        if (null != vehicleParameterDTO.getCreateUser()) {
            vehicleParameterCreated.setCreateUser(vehicleParameterDTO.getCreateUser());
        }
        if (null != vehicleParameterDTO.getUpdateDate()) {
            vehicleParameterCreated.setUpdateDate(vehicleParameterDTO.getUpdateDate());
        }
        if (null != vehicleParameterDTO.getUpdateUser()) {
            vehicleParameterCreated.setUpdateUser(vehicleParameterDTO.getUpdateUser());
        }

        vehicleParameterCreated.save();
        return vehicleParameterCreated;
    }

    @Override
    public Boolean isExistedVehicleParameterName(String vehicleParameterName, long projectId) {
        return (ao.find(VehicleParameter.class,
                        Query.select().where("VEHICLE_PARAMETER_NAME =? AND PROJECT_ID = ?", vehicleParameterName, projectId)).length >
                0);
    }

    @Override
    public boolean isExistedVehicleParameterNameSecond(String vehicleParameterName, long vehicleParameterId, long projectId) {
        return (ao.find(VehicleParameter.class, Query.select()
                                                 .where("VEHICLE_PARAMETER_NAME = ? AND ID != ? AND PROJECT_ID = ?",
                                                        vehicleParameterName, vehicleParameterId, projectId)).length > 0);
    }

    @Override
    public boolean findById(long vehicleParameterId) {
        final List<VehicleParameter> vehicleParameterList = newArrayList(
                ao.find(VehicleParameter.class, Query.select().where(Constants.QUERRY_ID, vehicleParameterId)));
        return !vehicleParameterList.isEmpty();
    }


    @Override
    public VehicleParameterDTO findByVehicleParameterId(long vehicleParameterId) {
        final VehicleParameter [] list = ao.find(VehicleParameter.class, Query.select().where(Constants.QUERRY_ID, vehicleParameterId));
        return (new VehicleParameterDTO(list[0].getID(), list[0].getProjectId(), list[0].getVehicleParameterName(),
                                        list[0].getCreateDate(), list[0].getCreateUser(),
                                        list[0].getUpdateDate(), list[0].getUpdateUser()));
    }


    @Override
    public boolean update(VehicleParameterDTO vehicleParameterDTO) {
        try {
            final VehicleParameter [] currentVehicleParameter = ao.find(VehicleParameter.class, Query.select().
                    where(Constants.QUERRY_ID, vehicleParameterDTO.getId()));
            currentVehicleParameter[0].setProjectId(vehicleParameterDTO.getProjectId());
            currentVehicleParameter[0].setVehicleParameterName(vehicleParameterDTO.getVehicleParameterName());
            if (null != vehicleParameterDTO.getCreateDate()) {
                currentVehicleParameter[0].setCreateDate(vehicleParameterDTO.getCreateDate());
            }
            if (null != vehicleParameterDTO.getCreateUser()) {
                currentVehicleParameter[0].setCreateUser(vehicleParameterDTO.getCreateUser());
            }
            if (null != vehicleParameterDTO.getUpdateDate()) {
                currentVehicleParameter[0].setUpdateDate(vehicleParameterDTO.getUpdateDate());
            }
            if (null != vehicleParameterDTO.getUpdateUser()) {
                currentVehicleParameter[0].setUpdateUser(vehicleParameterDTO.getUpdateUser());
            }

            currentVehicleParameter[0].save();
        } catch (final Exception ex) {
            return false;
        }
        return true;
    }
}
