package com.cmcglobal.plugins.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.cmcglobal.plugins.entity.TestResult;

import java.util.List;

@Transactional
public interface TestResultService {
    TestResult create(final long testCaseId);

    TestResult update(TestResult testResult);

    List<TestResult> findAll();

    TestResult detail(final long testResultId);

    TestResult getDetailFromIssueId(final long issueId);

    void delete(final long testResultId);

    boolean syncTestResult(final String fileName);
}
