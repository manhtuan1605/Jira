package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
import com.cmcglobal.plugins.dto.TestCaseTypeDTO;
import com.cmcglobal.plugins.entity.TestCaseType;
import com.cmcglobal.plugins.service.TestCaseTypeService;
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
public class TestCaseTypeServiceImpl implements TestCaseTypeService {
    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public TestCaseTypeServiceImpl(final ActiveObjects ao, final UserManager userManager) {
        this.ao = checkNotNull(ao);
    }

    @Override
    public List<TestCaseType> findByProjectId(long projectID) {
        return newArrayList(ao.find(TestCaseType.class,
                                    Query.select().where("PROJECT_ID = ?", projectID).order("CREATE_DATE DESC")));
    }

    @Override
    public TestCaseType create(TestCaseTypeDTO typeDTO) {
        final TestCaseType createdType = ao.create(TestCaseType.class,
                                                   new DBParam("PROJECT_ID", typeDTO.getProjectId()));
        createdType.setTestCaseTypeName(typeDTO.getTestCaseTypeName());
        if (null != typeDTO.getPerformance()) {
            createdType.setPerformance(typeDTO.getPerformance());
        }
        if (null != typeDTO.getCreateDate()) {
            createdType.setCreateDate(typeDTO.getCreateDate());
        }
        if (null != typeDTO.getCreateUser()) {
            createdType.setCreateUser(typeDTO.getCreateUser());
        }
        if (null != typeDTO.getUpdateDate()) {
            createdType.setUpdateDate(typeDTO.getUpdateDate());
        }
        if (null != typeDTO.getUpdateUser()) {
            createdType.setUpdateUser(typeDTO.getUpdateUser());
        }
        if (null != typeDTO.getActive()) {
            createdType.setIsActive(typeDTO.getActive());
        }
        createdType.save();
        return createdType;
    }

    @Override
    public Boolean isExistedTypeName(String typeName, Long thePid) {
        return (ao.find(TestCaseType.class,
                        Query.select().where("TEST_CASE_TYPE_NAME =? AND PROJECT_ID = ?", typeName, thePid)).length >
                0);
    }

    @Override
    public boolean findById(long theTypeid) {
        final List<TestCaseType> testTypeList = newArrayList(
                ao.find(TestCaseType.class, Query.select().where(Constants.QUERRY_ID, theTypeid)));
        return !testTypeList.isEmpty();
    }

    @Override
    public boolean isExistedTypeNameSecond(String typeName, Long theTypeId, Long projectKey) {
        return (ao.find(TestCaseType.class, Query.select()
                                                 .where("TEST_CASE_TYPE_NAME = ? AND ID != ? AND PROJECT_ID = ?",
                                                        typeName, theTypeId, projectKey)).length > 0);
    }

    @Override
    public TestCaseTypeDTO findByTypeId(long typeId) {
        final TestCaseType[] list = ao.find(TestCaseType.class, Query.select().where(Constants.QUERRY_ID, typeId));
        return (new TestCaseTypeDTO(list[0].getID(), list[0].getProjectId(), list[0].getTestCaseTypeName(),
                                    list[0].getPerformance(), list[0].getCreateDate(), list[0].getCreateUser(),
                                    list[0].getUpdateDate(), list[0].getUpdateUser(), list[0].getIsActive()));
    }

    @Override
    public boolean update(TestCaseTypeDTO testCaseTypeDTO) {
        try {
            final TestCaseType[] currentType = ao.find(TestCaseType.class, Query.select()
                                                                                .where(Constants.QUERRY_ID,
                                                                                       testCaseTypeDTO.getID()));
            currentType[0].setProjectId(testCaseTypeDTO.getProjectId());
            currentType[0].setTestCaseTypeName(testCaseTypeDTO.getTestCaseTypeName());
            if (null != testCaseTypeDTO.getPerformance()) {
                currentType[0].setPerformance(testCaseTypeDTO.getPerformance());
            }
            if (null != testCaseTypeDTO.getCreateDate()) {
                currentType[0].setCreateDate(testCaseTypeDTO.getCreateDate());
            }
            if (null != testCaseTypeDTO.getCreateUser()) {
                currentType[0].setCreateUser(testCaseTypeDTO.getCreateUser());
            }
            if (null != testCaseTypeDTO.getUpdateDate()) {
                currentType[0].setUpdateDate(testCaseTypeDTO.getUpdateDate());
            }
            if (null != testCaseTypeDTO.getUpdateUser()) {
                currentType[0].setUpdateUser(testCaseTypeDTO.getUpdateUser());
            }
            if (null != testCaseTypeDTO.getActive()) {
                currentType[0].setIsActive(testCaseTypeDTO.getActive());
            }
            currentType[0].save();
        } catch (final Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean delete(long typeId) {
        try {
            final TestCaseType[] list = ao.find(TestCaseType.class, Query.select().where(Constants.QUERRY_ID, typeId));
            ao.delete(list[0]);
        } catch (final Exception exception) {
            return false;
        }
        return true;
    }

}
