package com.cmcglobal.plugins.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.cmcglobal.plugins.dto.TestingPhaseDto;
import com.cmcglobal.plugins.entity.TestingPhase;

import java.util.List;

@Transactional
public interface TestingPhasesService {
    TestingPhase create(TestingPhaseDto theTestingPhaseDto);

    List<TestingPhase> findByProjectId(final long theProjectId);

    Boolean isExistedPhaseName(final String thePhaseName, final Long thePid);

    boolean findById(final long thePhaseId);

    boolean update(TestingPhaseDto theTestingPhase);

    boolean delete(final long theTestingPhaseId);

    TestingPhaseDto findByPhaseId(final long thePhaseId);

    boolean isExistedPhaseNameSecond(String name, Long thePhaseId, Long projectKey);
}
