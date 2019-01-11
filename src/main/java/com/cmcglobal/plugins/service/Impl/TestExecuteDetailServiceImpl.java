package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cmcglobal.plugins.dto.TestExecuteDetailDTO;
import com.cmcglobal.plugins.entity.TestCaseType;
import com.cmcglobal.plugins.service.TestExecuteDetailService;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Helper;
import net.java.ao.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Lists.newArrayList;

@Scanned
@Named
public class TestExecuteDetailServiceImpl implements TestExecuteDetailService {
    private static final Logger log = LoggerFactory.getLogger(TestExecuteDetailServiceImpl.class);

    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public TestExecuteDetailServiceImpl(final ActiveObjects ao) {
        this.ao = ao;
    }

    @Override
    public List<TestExecuteDetailDTO> getTestExecuteDetailByProjectIdAndAssigneeAndDate(final Long projectId,
                                                                                        final ApplicationUser assignee,
                                                                                        final LocalDate startDate,
                                                                                        final LocalDate endDate) {
        final List<TestExecuteDetailDTO> testExecuteDetailDTOs = new ArrayList<>();
        final List<TestCaseType> testCaseTypes = getTestcaseTypesByProjectId(projectId);
        final List<Issue> executedTestCases = getExecutedTestCasesByProjectIdAndAssigneeAndDate(projectId, assignee,
                                                                                                startDate, endDate);
        final TestCaseType functionTestcaseType = getFunctionTestcaseType(testCaseTypes);
        if (null == functionTestcaseType) {
            return Collections.emptyList();
        }
        int orderNo = 0;
        for (final TestCaseType testCaseType : testCaseTypes) {
            final List<Issue> executedTestCasesByType = filterExecutedTestCasesByTestCaseType(executedTestCases,
                                                                                              testCaseType.getTestCaseTypeName());
            if (!executedTestCasesByType.isEmpty()) {
                orderNo++;
                final TestExecuteDetailDTO testExecuteDetailDTO = new TestExecuteDetailDTO(orderNo,
                                                                                           Constants.TEST_CASE_TYPE_FUNCTION_TITLE
                                                                                                   .replace(
                                                                                                           Constants.TEST_CASE_TYPE_FUNCTION,
                                                                                                           testCaseType.getTestCaseTypeName()),
                                                                                           Arrays.asList(
                                                                                                   Constants.TEST_CASE_TYPE_FUNCTION_DEFINITION_SCOPE
                                                                                                           .replace(
                                                                                                                   Constants.TEST_CASE_TYPE_FUNCTION_LOWER,
                                                                                                                   testCaseType
                                                                                                                           .getTestCaseTypeName()
                                                                                                                           .toLowerCase()),
                                                                                                   Constants.TEST_CASE_TYPE_FUNCTION_DEFINITION_WORK_SIZE,
                                                                                                   Constants.TEST_CASE_TYPE_FUNCTION_DEFINITION_EFFORT),
                                                                                           testCaseType.getPerformance(),
                                                                                           testCaseType.getPerformance()
                                                                                                       .doubleValue() /
                                                                                           functionTestcaseType.getPerformance(),
                                                                                           executedTestCasesByType.size(),
                                                                                           getSumOfIssueTestDuration(
                                                                                                   executedTestCasesByType));
                testExecuteDetailDTOs.add(testExecuteDetailDTO);
            }
        }

        return testExecuteDetailDTOs;
    }

    @Override
    public List<Issue> getExecutedTestCasesByProjectIdAndAssigneeAndDate(final Long projectId,
                                                                         final ApplicationUser assignee,
                                                                         final LocalDate startDate,
                                                                         final LocalDate endDate) {
        final List<Issue> executedTestCases = new ArrayList<>();
        try {
            final IssueManager issueManager = ComponentAccessor.getIssueManager();
            final Collection<Long> issueIds = issueManager.getIssueIdsForProject(projectId);
            final List<Issue> projectIssues = issueManager.getIssueObjects(issueIds);
            for (final Issue projectIssue : projectIssues) {
                final LocalDate executeDate = projectIssue.getUpdated().toLocalDateTime().toLocalDate();
                if (Objects.equals(assignee, projectIssue.getAssignee()) && null != projectIssue.getIssueType() &&
                    Constants.ISSUE_TYPE_TESTCASE_NAME.equals(projectIssue.getIssueType().getName()) &&
                    Constants.STATUS_EXECUTED.equals(projectIssue.getStatus().getName()) &&
                    Helper.isBetweenLocalDate(executeDate, startDate, endDate)) {
                    executedTestCases.add(projectIssue);
                }
            }
        } catch (final Exception e) {
            return Collections.emptyList();
        }
        return executedTestCases;
    }

    @Override
    public List<Issue> filterExecutedTestCasesByTestCaseType(final List<Issue> executedTestcases,
                                                             final String testCaseType) {
        final List<Issue> testCases = new ArrayList<>();
        final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
        final CustomField customFieldTestcaseType = customFieldManager.getCustomFieldObjectByName(
                Constants.CUSTOM_FIELD_TESTCASE_TYPE);
        for (final Issue executedTestcase : executedTestcases) {
            final Option actualTestcaseType = (Option) executedTestcase.getCustomFieldValue(customFieldTestcaseType);
            if (testCaseType.equals(actualTestcaseType.getValue())) {
                testCases.add(executedTestcase);
            }
        }
        return testCases;
    }

    @Override
    public double getSumOfIssueTestDuration(final List<Issue> testcases) {
        final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
        final CustomField customFieldTestDuration = customFieldManager.getCustomFieldObjectByName(
                Constants.CUSTOM_FIELD_TEST_DURATION);
        float sumOfIssueTestDuration = 0;
        for (final Issue testcase : testcases) {
            final Double testDurationValue = (Double) testcase.getCustomFieldValue(customFieldTestDuration);
            sumOfIssueTestDuration += testDurationValue;
        }
        return sumOfIssueTestDuration / Constants.MINUTES_IN_AN_HOUR;
    }

    @Override
    public List<TestCaseType> getTestcaseTypesByProjectId(final Long projectId) {
        return newArrayList(
                ao.find(TestCaseType.class, Query.select().where("PROJECT_ID = ? and IS_ACTIVE = ?", projectId, true)));
    }

    @Override
    public TestCaseType getFunctionTestcaseType(final List<TestCaseType> testcaseTypes) {
        for (final TestCaseType testcaseType : testcaseTypes) {
            if (Constants.TEST_CASE_TYPE_FUNCTION.equalsIgnoreCase(testcaseType.getTestCaseTypeName())) {
                return testcaseType;
            }
        }
        return null;
    }
}
