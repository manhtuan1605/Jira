package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.operator.Operator;
import com.atlassian.sal.api.user.UserManager;
import com.cmcglobal.plugins.dto.CSVTestResultDTO;
import com.cmcglobal.plugins.entity.TestResult;
import com.cmcglobal.plugins.service.TestResultService;
import com.cmcglobal.plugins.utils.Constants;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.cmcglobal.plugins.utils.Constants.BLOCKING_TICKET_ID_COLUMN;
import static com.cmcglobal.plugins.utils.Constants.BLOCKING_TICKET_STATUS_COLUMN;
import static com.cmcglobal.plugins.utils.Constants.BLOCKING_TICKET_TYPE_COLUMN;
import static com.cmcglobal.plugins.utils.Constants.SYNC_TEST_RESULT;
import static com.cmcglobal.plugins.utils.Constants.SYNC_TEST_RESULT_VALIDATE;
import static com.cmcglobal.plugins.utils.Constants.USER_ADMIN_KEY;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

@Scanned
@Named
public class TestResultServiceImpl implements TestResultService {
    @ComponentImport
    private final        ActiveObjects ao;
    @ComponentImport
    private final        UserManager   userManager;
    private static final Logger        log = LoggerFactory.getLogger(TestResultServiceImpl.class);

    @Inject
    public TestResultServiceImpl(final ActiveObjects ao, final UserManager userManager) {
        this.ao = checkNotNull(ao);
        this.userManager = checkNotNull(userManager);
    }

    @Override
    public TestResult create(final long testCaseId) {
        final TestResult testResult = ao.create(TestResult.class, new DBParam("TEST_CASE_ID", testCaseId));
        testResult.setAmpType("amptype");
        testResult.save();
        return testResult;
    }

    @Override
    public TestResult update(final TestResult testCase) {
        return null;
    }

    @Override
    public List<TestResult> findAll() {
        return newArrayList(ao.find(TestResult.class));
    }

    @Override
    public TestResult detail(final long testResultId) {
        final List<TestResult> testResultList = newArrayList(
                ao.find(TestResult.class, Query.select().where("ID = ?", testResultId)));
        return testResultList == null ? null : testResultList.get(0);
    }

    @Override
    public TestResult getDetailFromIssueId(final long issueId) {
        final List<TestResult> testResultList = newArrayList(
                ao.find(TestResult.class, Query.select().where("ISSUE_ID = ?", issueId)));
        return (testResultList == null || testResultList.size() == 0) ? null : testResultList.get(0);
    }

    @Override
    public void delete(final long issueId) {
    }

    @Override
    public boolean syncTestResult(final String fileName) {
        try {
            // 1. Read file CSV
            final List<CSVTestResultDTO> csvTestResultDTOS = getCSVTestResultData(fileName);

            // 2. Update Issue
            final ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByKey(USER_ADMIN_KEY);
            final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
            final CustomField customFieldTicketType = customFieldManager.getCustomFieldObjectByName(
                    BLOCKING_TICKET_TYPE_COLUMN);
            final CustomField customFieldTicketStatus = customFieldManager.getCustomFieldObjectByName(
                    BLOCKING_TICKET_STATUS_COLUMN);
            if (customFieldTicketType == null || customFieldTicketStatus == null) {
                throw new RuntimeException(
                        "Custom field: " + BLOCKING_TICKET_TYPE_COLUMN + "," + BLOCKING_TICKET_STATUS_COLUMN +
                        " not found");
            }
            final IssueService issueService = ComponentAccessor.getIssueService();
            for (final CSVTestResultDTO csvTestResultDTO : csvTestResultDTOS) {
                final List<Issue> issues = getIssues(applicationUser, csvTestResultDTO);
                if (CollectionUtils.isEmpty(issues)) {
                    continue;
                }
                for (final Issue issue : issues) {
                    final IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
                    issueInputParameters.addCustomFieldValue(customFieldTicketType.getIdAsLong(),
                                                             csvTestResultDTO.getBlockingTicketType());
                    issueInputParameters.addCustomFieldValue(customFieldTicketStatus.getIdAsLong(),
                                                             csvTestResultDTO.getBlockingTicketStatus());
                    final IssueService.UpdateValidationResult updateValidationResult = issueService.validateUpdate(
                            applicationUser, issue.getId(), issueInputParameters);

                    if (updateValidationResult.isValid()) {
                        updateValidationResult.getIssue().setCustomFieldValue(customFieldTicketStatus, csvTestResultDTO.getBlockingTicketStatus());
                        updateValidationResult.getIssue().setCustomFieldValue(customFieldTicketType, csvTestResultDTO.getBlockingTicketType());
                        final IssueService.IssueResult updateResult = issueService.update(applicationUser,
                                                                                          updateValidationResult);
                        if (updateResult.isValid()) {
                            log.info("Sync test result ok, issue id: " + updateResult.getIssue().getKey());
                            continue;
                        }
                        log.error(SYNC_TEST_RESULT + updateResult.getErrorCollection().toString());
                    } else {
                        log.error(SYNC_TEST_RESULT_VALIDATE + updateValidationResult.getErrorCollection().toString());
                    }
                }
            }
            return true;
        } catch (final Exception e) {
            log.error("Read csv error: " + e.getMessage());
            return false;
        }
    }

    private List<CSVTestResultDTO> getCSVTestResultData(final String fileName) throws IOException {
        final Path pathFile = Paths.get(fileName);
        if (Files.notExists(pathFile) || Files.isDirectory(pathFile)) {
            log.error("File not found, file name: " + fileName);
            return null;
        }
        final List<CSVTestResultDTO> csvTestResultDTOS = new ArrayList<>();
        try (final Reader reader = Files.newBufferedReader(pathFile);
             final CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader()
                                                                                .withIgnoreHeaderCase()
                                                                                .withTrim())) {
            final Map<String, Integer> headerMap = csvParser.getHeaderMap();
            if (!(headerMap.containsKey(BLOCKING_TICKET_ID_COLUMN) &&
                  headerMap.containsKey(BLOCKING_TICKET_TYPE_COLUMN) &&
                  headerMap.containsKey(BLOCKING_TICKET_STATUS_COLUMN))) {
                throw new RuntimeException(
                        "Header of CSV not matching columns name: " + BLOCKING_TICKET_ID_COLUMN + "," + BLOCKING_TICKET_TYPE_COLUMN +
                        "," + BLOCKING_TICKET_STATUS_COLUMN);
            }
            for (final CSVRecord csvRecord : csvParser) {
                // Accessing values by Header names
                csvTestResultDTOS.add(new CSVTestResultDTO(csvRecord.get(BLOCKING_TICKET_ID_COLUMN),
                                                           csvRecord.get(BLOCKING_TICKET_TYPE_COLUMN),
                                                           csvRecord.get(BLOCKING_TICKET_STATUS_COLUMN)));
            }
        }
        return csvTestResultDTOS;
    }

    private List<Issue> getIssues(final ApplicationUser applicationUser, final CSVTestResultDTO csvTestResultDTO)
            throws Exception {
        final JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        final SearchService searchService = ComponentAccessor.getComponentOfType(SearchService.class);
        jqlClauseBuilder.issueType(Constants.ISSUE_TYPE_TESTCASE_NAME)
                        .and()
                        .addStringCondition(BLOCKING_TICKET_ID_COLUMN, Operator.LIKE,
                                            csvTestResultDTO.getBlockingTicketId());
        final SearchResults searchResults = searchService.search(applicationUser, jqlClauseBuilder.buildQuery(), PagerFilter.getUnlimitedFilter());
        final List<Issue> tempIssues = searchResults.getIssues();
        return tempIssues;
    }
}