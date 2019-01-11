package com.cmcglobal.plugins.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.cmcglobal.plugins.dto.TestHuMarketDTO;
import com.cmcglobal.plugins.dto.TestingPhaseDto;
import com.cmcglobal.plugins.dto.VehicleParameterDTO;
import com.cmcglobal.plugins.entity.TestHuMarket;
import com.cmcglobal.plugins.entity.TestingPhase;
import com.cmcglobal.plugins.entity.VehicleParameter;

import java.util.List;

@Transactional
public interface TestHuMarketService {
    List<TestHuMarket> findByProjectId(final long projectId);

    TestHuMarket create(TestHuMarketDTO testHuMarketDTO);

    Boolean isExistedTestHuMarketName(final String testHuMarketName, final long projectId);

    boolean isExistedTestHuMarketNameSecond(String name, Long theTypeId, Long projectKey);

    TestHuMarketDTO findByTestHuMarketId(final long testHuMarketId);

    boolean update(TestHuMarketDTO testHuMarketDTO);

    boolean findById(final long testHuMarketId);
}