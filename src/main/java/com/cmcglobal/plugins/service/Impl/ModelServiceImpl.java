package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
import com.cmcglobal.plugins.dto.ModelDTO;
import com.cmcglobal.plugins.dto.TestHuMarketDTO;
import com.cmcglobal.plugins.entity.Model;
import com.cmcglobal.plugins.entity.TestHuMarket;
import com.cmcglobal.plugins.service.ModelService;
import com.cmcglobal.plugins.service.TestHuMarketService;
import com.cmcglobal.plugins.utils.Constants;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

@Scanned
@Named
public class ModelServiceImpl implements ModelService {

    private static final Logger logger = LoggerFactory.getLogger(ModelServiceImpl.class);

    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public ModelServiceImpl(final ActiveObjects ao, final UserManager userManager) {
        this.ao = checkNotNull(ao);
    }

    @Override
    public List<Model> findByProjectId(long projectId) {
        return newArrayList(
                ao.find(Model.class, Query.select().where("PROJECT_ID=?", projectId).order("CREATE_DATE DESC")));
    }

    @Override
    public Model create(ModelDTO modelDTO) {
        final Model modelCreated = ao.create(Model.class, new DBParam("PROJECT_ID", modelDTO.getProjectId()));
        modelCreated.setModelName(modelDTO.getModelName());
        if (null != modelDTO.getCreateDate()) {
            modelCreated.setCreateDate(modelDTO.getCreateDate());
        }
        if (null != modelDTO.getCreateUser()) {
            modelCreated.setCreateUser(modelDTO.getCreateUser());
        }
        if (null != modelDTO.getUpdateDate()) {
            modelCreated.setUpdateDate(modelDTO.getUpdateDate());
        }
        if (null != modelDTO.getUpdateUser()) {
            modelCreated.setUpdateUser(modelDTO.getUpdateUser());
        }

        modelCreated.save();
        return modelCreated;
    }

    @Override
    public Boolean isExistedModelName(String modelName, long projectId) {
        return (ao.find(Model.class,
                        Query.select().where("MODEL_NAME =? AND PROJECT_ID = ?", modelName, projectId)).length > 0);
    }

    @Override
    public boolean isExistedModelNameSecond(String modelName, Long theModelId, Long projectKey) {
        return (ao.find(Model.class, Query.select()
                                                 .where("MODEL_NAME = ? AND ID != ? AND PROJECT_ID = ?", modelName,
                                                        theModelId, projectKey)).length > 0);
    }

    @Override
    public ModelDTO findByModelId(long modelId) {
        final Model[] list = ao.find(Model.class, Query.select().where(Constants.QUERRY_ID, modelId));
        return (new ModelDTO(list[0].getID(), list[0].getProjectId(), list[0].getModelName(), list[0].getCreateDate(),
                             list[0].getCreateUser(), list[0].getUpdateDate(), list[0].getUpdateUser()));
    }

    @Override
    public boolean update(ModelDTO modelDTO) {
        try {
            final Model[] currentModel = ao.find(Model.class, Query.select().
                    where(Constants.QUERRY_ID, modelDTO.getId()));
            currentModel[0].setProjectId(modelDTO.getProjectId());
            currentModel[0].setModelName(modelDTO.getModelName());
            if (null != modelDTO.getCreateDate()) {
                currentModel[0].setCreateDate(modelDTO.getCreateDate());
            }
            if (null != modelDTO.getCreateUser()) {
                currentModel[0].setCreateUser(modelDTO.getCreateUser());
            }
            if (null != modelDTO.getUpdateDate()) {
                currentModel[0].setUpdateDate(modelDTO.getUpdateDate());
            }
            if (null != modelDTO.getUpdateUser()) {
                currentModel[0].setUpdateUser(modelDTO.getUpdateUser());
            }

            currentModel[0].save();
        } catch (final Exception ex) {
            logger.error(ex.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean findById(long modelId) {
        final List<Model> models = newArrayList(
                ao.find(Model.class, Query.select().where(Constants.QUERRY_ID, modelId)));
        return !models.isEmpty();
    }

}
