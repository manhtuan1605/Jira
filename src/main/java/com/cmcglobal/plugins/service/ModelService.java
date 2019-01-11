package com.cmcglobal.plugins.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.cmcglobal.plugins.dto.ModelDTO;
import com.cmcglobal.plugins.dto.TestHuMarketDTO;
import com.cmcglobal.plugins.entity.Model;
import com.cmcglobal.plugins.entity.TestHuMarket;

import java.util.List;

@Transactional
public interface ModelService {
    List<Model> findByProjectId(final long projectId);

    Model create(ModelDTO modelDTO);

    Boolean isExistedModelName(final String modelName, final long projectId);

    boolean isExistedModelNameSecond(String name, Long theTypeId, Long projectKey);

    ModelDTO findByModelId(final long modelId);

    boolean update(ModelDTO modelDTO);

    boolean findById(final long modelId);
}