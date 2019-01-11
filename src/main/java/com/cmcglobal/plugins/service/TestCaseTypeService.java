package com.cmcglobal.plugins.service;

import com.cmcglobal.plugins.dto.TestCaseTypeDTO;
import com.cmcglobal.plugins.entity.TestCaseType;

import java.util.List;

public interface TestCaseTypeService {
    List<TestCaseType> findByProjectId(final long projectID);

    TestCaseType create(TestCaseTypeDTO typeDto);

    Boolean isExistedTypeName(final String typeName, final Long thePid);

    boolean findById(final long theTypeid);

    boolean isExistedTypeNameSecond(String name, Long theTypeId, Long projectKey);

    TestCaseTypeDTO findByTypeId(final long typeId);

    boolean update(TestCaseTypeDTO testCaseTypeDTO);

    boolean delete(final long typeId);
}
