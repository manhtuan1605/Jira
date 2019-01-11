package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
import com.cmcglobal.plugins.dto.TestHuMarketDTO;
import com.cmcglobal.plugins.entity.TestCaseType;
import com.cmcglobal.plugins.entity.TestHuMarket;
import com.cmcglobal.plugins.jira.validators.FieldValidator;
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
public class TestHuMarketServiceImpl implements TestHuMarketService {

    private static final Logger logger = LoggerFactory.getLogger(TestHuMarketServiceImpl.class);

    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public TestHuMarketServiceImpl(final ActiveObjects ao, final UserManager userManager) {
        this.ao = checkNotNull(ao);
    }

    @Override
    public List<TestHuMarket> findByProjectId(long projectId) {
        return newArrayList(ao.find(TestHuMarket.class,
                                           Query.select().where("PROJECT_ID=?", projectId).order("CREATE_DATE DESC")));
    }

    @Override
    public TestHuMarket create(TestHuMarketDTO testHuMarketDTO) {
        final TestHuMarket testHuMarketCreated = ao.create(TestHuMarket.class,
                                                                   new DBParam("PROJECT_ID", testHuMarketDTO.getProjectId()));
        testHuMarketCreated.setTestHuMarketName(testHuMarketDTO.getTestHuMarketName());
        if (null != testHuMarketDTO.getCreateDate()) {
            testHuMarketCreated.setCreateDate(testHuMarketDTO.getCreateDate());
        }
        if (null != testHuMarketDTO.getCreateUser()) {
            testHuMarketCreated.setCreateUser(testHuMarketDTO.getCreateUser());
        }
        if (null != testHuMarketDTO.getUpdateDate()) {
            testHuMarketCreated.setUpdateDate(testHuMarketDTO.getUpdateDate());
        }
        if (null != testHuMarketDTO.getUpdateUser()) {
            testHuMarketCreated.setUpdateUser(testHuMarketDTO.getUpdateUser());
        }

        testHuMarketCreated.save();
        return testHuMarketCreated;
    }

    @Override
    public Boolean isExistedTestHuMarketName(String testHuMarketName, long projectId) {
        return (ao.find(TestHuMarket.class,
                        Query.select().where("TEST_HU_MARKET_NAME =? AND PROJECT_ID = ?",
                                             testHuMarketName, projectId)).length > 0);
    }

    @Override
    public boolean isExistedTestHuMarketNameSecond(String testHuMarketName, Long theTestHuMarketId, Long projectKey) {
        return (ao.find(TestHuMarket.class, Query.select()
                                                 .where("TEST_HU_MARKET_NAME = ? AND ID != ? AND PROJECT_ID = ?",
                                                        testHuMarketName, theTestHuMarketId, projectKey)).length > 0);
    }

    @Override
    public TestHuMarketDTO findByTestHuMarketId(long testHuMarketId) {
        final TestHuMarket [] list = ao.find(TestHuMarket.class, Query.select().where(Constants.QUERRY_ID, testHuMarketId));
        return (new TestHuMarketDTO(list[0].getID(), list[0].getProjectId(), list[0].getTestHuMarketName(),
                                        list[0].getCreateDate(), list[0].getCreateUser(),
                                        list[0].getUpdateDate(), list[0].getUpdateUser()));
    }

    @Override
    public boolean update(TestHuMarketDTO testHuMarketDTO) {
        try {
            final TestHuMarket [] currentTestHuMarket = ao.find(TestHuMarket.class, Query.select().
                    where(Constants.QUERRY_ID, testHuMarketDTO.getId()));
            currentTestHuMarket[0].setProjectId(testHuMarketDTO.getProjectId());
            currentTestHuMarket[0].setTestHuMarketName(testHuMarketDTO.getTestHuMarketName());
            if (null != testHuMarketDTO.getCreateDate()) {
                currentTestHuMarket[0].setCreateDate(testHuMarketDTO.getCreateDate());
            }
            if (null != testHuMarketDTO.getCreateUser()) {
                currentTestHuMarket[0].setCreateUser(testHuMarketDTO.getCreateUser());
            }
            if (null != testHuMarketDTO.getUpdateDate()) {
                currentTestHuMarket[0].setUpdateDate(testHuMarketDTO.getUpdateDate());
            }
            if (null != testHuMarketDTO.getUpdateUser()) {
                currentTestHuMarket[0].setUpdateUser(testHuMarketDTO.getUpdateUser());
            }

            currentTestHuMarket[0].save();
        } catch (final Exception ex) {
            logger.error(ex.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean findById(long testHuMarketId) {
        final List<TestHuMarket> testHuMarketList = newArrayList(
                ao.find(TestHuMarket.class, Query.select().where(Constants.QUERRY_ID, testHuMarketId)));
        return !testHuMarketList.isEmpty();
    }


}
