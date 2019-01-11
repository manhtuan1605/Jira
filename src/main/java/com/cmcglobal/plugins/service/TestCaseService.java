package com.cmcglobal.plugins.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.dto.ResultMessage;
import com.cmcglobal.plugins.entity.TestCase;
import com.cmcglobal.plugins.utils.ImportStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Transactional
public interface TestCaseService {

    TestCase create(final long issueId);

    void update(TestCase testCase);

    List<TestCase> findAll();

    TestCase detail(final long testcaseId);

    TestCase findByIssueId(final long issueId);

    ResultMessage imports(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;

    boolean synchronizeTestCase(final long issueId, final Map<String, String> testcaseValueMap, final Date updateDate,
                                final boolean isCreate);

    boolean deleteTestcase(final long issueId);

    boolean updateWhenIssueMoved(final long issueId, final String phase, final String testcaseType);

    void initThread(final String importType, final String fileUploadPath, final String fileName, final Project project,
                    final String phase, final String testCaseType, final ApplicationUser user, final ImportStatus importStatus) throws Exception;

    TestCase findByTestCaseNoAndPhase(final String testCaseNo, final String phase);

}
