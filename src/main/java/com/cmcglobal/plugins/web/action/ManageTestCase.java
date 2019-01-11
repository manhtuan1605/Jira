package com.cmcglobal.plugins.web.action;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cmcglobal.plugins.entity.TestCase;
import net.java.ao.Query;

import javax.inject.Inject;

/**
 * Created by User on 7/15/2018.
 */
public class ManageTestCase extends JiraWebActionSupport {

    private ActiveObjects ao;

    @Inject
    public ManageTestCase(@ComponentImport ActiveObjects ao) {
        this.ao = ao;
    }

    @Override
    protected String doExecute() throws Exception {

        return SUCCESS;
    }

    private void deleteTestCase(TestCase testCase) {
        ao.delete(testCase);
    }

    private void editStatus(TestCase testCase, String status) {
        testCase.save();
    }

    private TestCase getTestCase(String id) {
        TestCase[] testCaseEntities = ao.find(TestCase.class, Query.select().where("testCaseNumber = ?", id));
        return testCaseEntities[0];
    }

    private void addTestCase(String id, String name) {
        TestCase testCaseEntity = ao.create(TestCase.class);

        testCaseEntity.save();
    }
}