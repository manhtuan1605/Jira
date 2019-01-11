package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
import com.cmcglobal.plugins.dto.TestingPhaseDto;
import com.cmcglobal.plugins.entity.TestingPhase;
import com.cmcglobal.plugins.service.TestingPhasesService;
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
public class TestingPhasesServiceImpl implements TestingPhasesService {
    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public TestingPhasesServiceImpl(final ActiveObjects ao, final UserManager userManager) {
        this.ao = checkNotNull(ao);
    }

    @Override
    public TestingPhase create(final TestingPhaseDto theTestingPhaseDto) {
        final TestingPhase createdTestingPhase = ao.create(TestingPhase.class, new DBParam("PROJECT_ID",
                                                                                           theTestingPhaseDto.getProjectId()));
        createdTestingPhase.setPhaseName(theTestingPhaseDto.getPhaseName());
        if (null != theTestingPhaseDto.getStartDate()) {
            createdTestingPhase.setStartDate(theTestingPhaseDto.getStartDate());
        }
        if (null != theTestingPhaseDto.getEndDate()) {
            createdTestingPhase.setEndDate(theTestingPhaseDto.getEndDate());
        }
        if (null != theTestingPhaseDto.getCreateDate()) {
            createdTestingPhase.setCreateDate(theTestingPhaseDto.getCreateDate());
        }
        if (null != theTestingPhaseDto.getCreateUser()) {
            createdTestingPhase.setCreateUser(theTestingPhaseDto.getCreateUser());
        }
        if (null != theTestingPhaseDto.getUpdateDate()) {
            createdTestingPhase.setUpdateDate(theTestingPhaseDto.getUpdateDate());
        }
        if (null != theTestingPhaseDto.getUpdateUser()) {
            createdTestingPhase.setUpdateUser(theTestingPhaseDto.getUpdateUser());
        }
        if (null != theTestingPhaseDto.getIsActive()) {
            createdTestingPhase.setIsActive(theTestingPhaseDto.getIsActive());
        }
        createdTestingPhase.save();
        return createdTestingPhase;
    }

    @Override
    public List<TestingPhase> findByProjectId(final long theProjectId) {
        return newArrayList(ao.find(TestingPhase.class,
                                    Query.select().where("PROJECT_ID=?", theProjectId).order("CREATE_DATE DESC")));
    }

    @Override
    public boolean findById(final long thePhaseId) {
        final List<TestingPhase> testCaseList = newArrayList(
                ao.find(TestingPhase.class, Query.select().where("ID=?", thePhaseId).order("PHASE_NAME ASC")));
        return testCaseList.isEmpty() ? false : true;
    }

    @Override
    public Boolean isExistedPhaseName(final String thePhaseName, final Long thePid) {
        return (ao.find(TestingPhase.class,
                        Query.select().where("PHASE_NAME=? AND PROJECT_ID=?", thePhaseName, thePid)).length > 0) ?
               true :
               false;
    }

    @Override
    public boolean update(final TestingPhaseDto theTestingPhase) {
        try {
            final TestingPhase[] currentPhase = ao.find(TestingPhase.class, Query.select()
                                                                                 .where(Constants.QUERRY_ID,
                                                                                        theTestingPhase.getID()));
            currentPhase[0].setProjectId(theTestingPhase.getProjectId());
            currentPhase[0].setPhaseName(theTestingPhase.getPhaseName());
            if (null != theTestingPhase.getStartDate()) {
                currentPhase[0].setStartDate(theTestingPhase.getStartDate());
            }
            if (null != theTestingPhase.getEndDate()) {
                currentPhase[0].setEndDate(theTestingPhase.getEndDate());
            }
            if (null != theTestingPhase.getCreateDate()) {
                currentPhase[0].setCreateDate(theTestingPhase.getCreateDate());
            }
            if (null != theTestingPhase.getCreateUser()) {
                currentPhase[0].setCreateUser(theTestingPhase.getCreateUser());
            }
            if (null != theTestingPhase.getUpdateDate()) {
                currentPhase[0].setUpdateDate(theTestingPhase.getUpdateDate());
            }
            if (null != theTestingPhase.getUpdateUser()) {
                currentPhase[0].setUpdateUser(theTestingPhase.getUpdateUser());
            }
            if (null != theTestingPhase.getIsActive()) {
                currentPhase[0].setIsActive(theTestingPhase.getIsActive());
            }
            currentPhase[0].save();
        } catch (final Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean delete(final long theTestingPhaseId) {
        try {
            final TestingPhase[] list = ao.find(TestingPhase.class,
                                                Query.select().where(Constants.QUERRY_ID, theTestingPhaseId));
            ao.delete(list[0]);
        } catch (final Exception exception) {
            return false;
        }
        return true;
    }

    @Override
    public TestingPhaseDto findByPhaseId(final long thePhaseId) {
        final TestingPhase[] list = ao.find(TestingPhase.class, Query.select().where(Constants.QUERRY_ID, thePhaseId));
        return (new TestingPhaseDto(list[0].getID(), list[0].getProjectId(), list[0].getPhaseName(),
                                    list[0].getStartDate(), list[0].getEndDate(), list[0].getCreateDate(),
                                    list[0].getCreateUser(), list[0].getUpdateDate(), list[0].getUpdateUser(),
                                    list[0].getIsActive()));
    }

    @Override
    public boolean isExistedPhaseNameSecond(final String thePhaseName, final Long thePhaseId, final Long projectKey) {
        return (ao.find(TestingPhase.class, Query.select()
                                                 .where("PHASE_NAME=? AND ID != ? AND PROJECT_ID = ?", thePhaseName,
                                                        thePhaseId, projectKey)).length > 0) ? true : false;
    }
}
