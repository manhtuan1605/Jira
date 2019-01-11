package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
import com.cmcglobal.plugins.dto.ExternalDeviceTypeDTO;
import com.cmcglobal.plugins.entity.ExternalDeviceType;
import com.cmcglobal.plugins.service.ExternalDeviceTypeService;
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
public class ExternalDeviceTypeImpl implements ExternalDeviceTypeService {
    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public ExternalDeviceTypeImpl(final ActiveObjects ao, final UserManager userManager) {
        this.ao = checkNotNull(ao);
    }

    @Override
    public List<ExternalDeviceType> findByProjectId(long projectID) {
        return newArrayList(ao.find(ExternalDeviceType.class,
                                    Query.select().where("PROJECT_ID = ?", projectID).order("CREATE_DATE DESC")));
    }

    @Override
    public ExternalDeviceType create(ExternalDeviceTypeDTO typeDto) {
        final ExternalDeviceType createdType = ao.create(ExternalDeviceType.class,
                                                         new DBParam("PROJECT_ID", typeDto.getProjectId()));
        createdType.setDeviceType(typeDto.getDeviceType());
        if (null != typeDto.getCreateDate()) {
            createdType.setCreateDate(typeDto.getCreateDate());
        }
        if (null != typeDto.getCreateUser()) {
            createdType.setCreateUser(typeDto.getCreateUser());
        }
        if (null != typeDto.getUpdateDate()) {
            createdType.setUpdateDate(typeDto.getUpdateDate());
        }
        if (null != typeDto.getUpdateUser()) {
            createdType.setUpdateUser(typeDto.getUpdateUser());
        }
        if (null != typeDto.getActive()) {
            createdType.setIsActive(typeDto.getActive());
        }
        createdType.save();
        return createdType;
    }

    @Override
    public Boolean isExistedDeviceType(String deviceType, Long thePid) {
        return (ao.find(ExternalDeviceType.class,
                        Query.select().where("DEVICE_TYPE =? AND PROJECT_ID = ?", deviceType, thePid)).length > 0);
    }

    @Override
    public Boolean findById(long theTypeid) {
        final List<ExternalDeviceType> externalDeviceTypes = newArrayList(
                ao.find(ExternalDeviceType.class, Query.select().where(Constants.QUERRY_ID, theTypeid)));
        return (!externalDeviceTypes.isEmpty());
    }

    @Override
    public boolean isExistedTypeNameSecond(String name, Long theTypeId, Long projectKey) {
        return (ao.find(ExternalDeviceType.class, Query.select()
                                                       .where("DEVICE_TYPE = ? AND ID != ? AND PROJECT_ID = ?", name,
                                                              theTypeId, projectKey)).length > 0);
    }

    @Override
    public ExternalDeviceTypeDTO findByTypeId(long typeId) {
        final ExternalDeviceType[] list = ao.find(ExternalDeviceType.class,
                                                  Query.select().where(Constants.QUERRY_ID, typeId));
        if (list.length > 0) {
            return (new ExternalDeviceTypeDTO(list[0].getID(), list[0].getProjectId(), list[0].getDeviceType(),
                                              list[0].getCreateDate(), list[0].getCreateUser(), list[0].getUpdateDate(),
                                              list[0].getUpdateUser(), list[0].getIsActive()));
        }
        return null;
    }

    @Override
    public boolean update(ExternalDeviceTypeDTO externalDeviceTypeDTO) {
        final ExternalDeviceType[] currentType = ao.find(ExternalDeviceType.class, Query.select()
                                                                                        .where(Constants.QUERRY_ID,
                                                                                               externalDeviceTypeDTO.getID()));
        if (currentType.length > 0) {
            currentType[0].setProjectId(externalDeviceTypeDTO.getProjectId());
            currentType[0].setDeviceType(externalDeviceTypeDTO.getDeviceType());
            if (null != externalDeviceTypeDTO.getCreateDate()) {
                currentType[0].setCreateDate(externalDeviceTypeDTO.getCreateDate());
            }
            if (null != externalDeviceTypeDTO.getCreateUser()) {
                currentType[0].setCreateUser(externalDeviceTypeDTO.getCreateUser());
            }
            if (null != externalDeviceTypeDTO.getUpdateDate()) {
                currentType[0].setUpdateDate(externalDeviceTypeDTO.getUpdateDate());
            }
            if (null != externalDeviceTypeDTO.getUpdateUser()) {
                currentType[0].setUpdateUser(externalDeviceTypeDTO.getUpdateUser());
            }
            if (null != externalDeviceTypeDTO.getActive()) {
                currentType[0].setIsActive(externalDeviceTypeDTO.getActive());
            }
            currentType[0].save();
            return true;
        }
        return false;
    }
}
