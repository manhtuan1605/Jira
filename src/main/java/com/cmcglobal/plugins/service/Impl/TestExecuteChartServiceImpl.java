package com.cmcglobal.plugins.service.Impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.cmcglobal.plugins.dto.ApplicationUserDTO;
import com.cmcglobal.plugins.dto.TestExecuteChartDTO;
import com.cmcglobal.plugins.entity.TestCaseType;
import com.cmcglobal.plugins.service.TestExecuteChartService;
import com.cmcglobal.plugins.service.TestExecuteDetailService;
import com.cmcglobal.plugins.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cmcglobal.plugins.utils.Constants.FORMAT_DATE_IMPORT;

@Scanned
@Named
public class TestExecuteChartServiceImpl implements TestExecuteChartService {
    private static final Logger                   log = LoggerFactory.getLogger(TestExecuteChartServiceImpl.class);
    private final        TestExecuteDetailService testExecuteDetailService;

    @Inject
    public TestExecuteChartServiceImpl(final TestExecuteDetailService testExecuteDetailService) {
        this.testExecuteDetailService = testExecuteDetailService;
    }

    @Override
    public List<TestExecuteChartDTO> getTestExecuteChartByProjectIdAndAssigneeAndDate(final Long projectId,
                                                                                      final List<ApplicationUser> assignees,
                                                                                      final LocalDate startDate,
                                                                                      final LocalDate endDate) {
        final List<TestCaseType> testCaseTypes = testExecuteDetailService.getTestcaseTypesByProjectId(projectId);
        final TestCaseType functionTestcaseType = testExecuteDetailService.getFunctionTestcaseType(testCaseTypes);

        if (null == functionTestcaseType) {
            return Collections.emptyList();
        } else {
            final List<TestExecuteChartDTO> testExecuteChartDTOs = new ArrayList<>();
            for (final ApplicationUser assignee : assignees) {
                final List<Issue> executedTestCases
                        = testExecuteDetailService.getExecutedTestCasesByProjectIdAndAssigneeAndDate(projectId,
                                                                                                     assignee,
                                                                                                     startDate,
                                                                                                     endDate);
                if (startDate.isEqual(endDate)) {
                    final List<List<Issue>> executedTestCasesByHours = filterExecutedTestCasesByHour(executedTestCases);
                    setValueToTestExecuteChartDTOs(executedTestCasesByHours, assignee, testCaseTypes,
                                                   functionTestcaseType, testExecuteChartDTOs);
                } else {
                    final List<List<Issue>> executedTestCasesByDays = filterExecutedTestCasesByDay(executedTestCases);
                    setValueToTestExecuteChartDTOs(executedTestCasesByDays, assignee, testCaseTypes,
                                                   functionTestcaseType, testExecuteChartDTOs);
                }
            }
            return testExecuteChartDTOs;
        }
    }

    private List<List<Issue>> filterExecutedTestCasesByHour(final List<Issue> executedTestCases) {
        final List<List<Issue>> testCasesLists = new ArrayList<>();
        final Map<Integer, List<Issue>> testCasesMap = new HashMap<>();
        for (final Issue issue : executedTestCases) {
            final int updatedHour = issue.getUpdated().toLocalDateTime().getHour();
            setValueToTestCasesMap(testCasesMap, updatedHour, issue);
        }
        testCasesMap.forEach((key, value) -> testCasesLists.add(value));
        return testCasesLists;
    }

    private List<List<Issue>> filterExecutedTestCasesByDay(final List<Issue> executedTestCases) {
        final List<List<Issue>> testCasesLists = new ArrayList<>();
        final Map<String, List<Issue>> testCasesMap = new HashMap<>();
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(FORMAT_DATE_IMPORT)
                                                                          .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                                                                          .parseDefaulting(ChronoField.MINUTE_OF_HOUR,
                                                                                           0)
                                                                          .parseDefaulting(ChronoField.SECOND_OF_MINUTE,
                                                                                           0)
                                                                          .toFormatter();
        for (final Issue issue : executedTestCases) {
            final String updatedDate = issue.getUpdated().toLocalDateTime().toLocalDate().format(formatter);
            setValueToTestCasesMap(testCasesMap, updatedDate, issue);
        }
        testCasesMap.forEach((key, value) -> testCasesLists.add(value));
        return testCasesLists;
    }

    private <G> void setValueToTestCasesMap(final Map<G, List<Issue>> testCasesMap, final G updatedTime,
                                            final Issue issue) {
        if (testCasesMap.containsKey(updatedTime)) {
            testCasesMap.get(updatedTime).add(issue);
        } else {
            final List<Issue> issues = new ArrayList<>();
            issues.add(issue);
            testCasesMap.put(updatedTime, issues);
        }
    }

    private void setValueToTestExecuteChartDTOs(final List<List<Issue>> executedTestCasesByTimes,
                                                final ApplicationUser assignee, final List<TestCaseType> testCaseTypes,
                                                final TestCaseType functionTestcaseType,
                                                final List<TestExecuteChartDTO> testExecuteChartDTOs) {
        final DateTimeFormatter FORMATTER_YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern(
                Constants.FORMATTER_YYYY_MM_DD_HH_MM_SS);
        for (final List<Issue> executedTestCasesByTime : executedTestCasesByTimes) {
            final TestExecuteChartDTO testExecuteChartDTO = new TestExecuteChartDTO();
            testExecuteChartDTO.setAssignee(ApplicationUserDTO.create(assignee));
            testExecuteChartDTO.setExecuteTime(executedTestCasesByTime.get(0)
                                                                      .getUpdated()
                                                                      .toLocalDateTime()
                                                                      .format(FORMATTER_YYYY_MM_DD_HH_MM_SS));
            testExecuteChartDTO.setProductivity(
                    getAverageProductivity(testCaseTypes, functionTestcaseType, executedTestCasesByTime));
            testExecuteChartDTOs.add(testExecuteChartDTO);
        }
    }

    private Double getAverageProductivity(final List<TestCaseType> testCaseTypes,
                                          final TestCaseType functionTestcaseType,
                                          final List<Issue> executedTestCases) {
        final List<Double> productivityByTypes = new ArrayList<>();
        for (final TestCaseType testCaseType : testCaseTypes) {
            final List<Issue> executedTestCasesByType = testExecuteDetailService.filterExecutedTestCasesByTestCaseType(
                    executedTestCases, testCaseType.getTestCaseTypeName());
            if (!executedTestCasesByType.isEmpty()) {
                productivityByTypes.add(getProductivityByType(
                        testCaseType.getPerformance().doubleValue() / functionTestcaseType.getPerformance(),
                        executedTestCasesByType.size(),
                        testExecuteDetailService.getSumOfIssueTestDuration(executedTestCasesByType)));
            }
        }
        double sumOfProductivity = 0;
        for (final Double productivityByType : productivityByTypes) {
            sumOfProductivity = sumOfProductivity + productivityByType;
        }
        return sumOfProductivity / productivityByTypes.size();
    }

    private Double getProductivityByType(final double conversionRate, final long numberOfTestCases, final double time) {
        return (conversionRate == 0 || time == 0) ? 0 : numberOfTestCases / conversionRate / time * 8;
    }
}