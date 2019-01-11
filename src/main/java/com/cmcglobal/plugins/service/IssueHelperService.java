package com.cmcglobal.plugins.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.dto.CustomFieldValueDTO;
import com.cmcglobal.plugins.dto.IssueMessageDTO;
import com.cmcglobal.plugins.entity.TestCase;

import java.util.List;

@Transactional
public interface IssueHelperService {
    IssueMessageDTO createIssue(ApplicationUser applicationUser, Long projectId, String issueTypeName, String summary,
                                List<CustomFieldValueDTO> customFieldValueDTOList);

    IssueMessageDTO updateIssue(ApplicationUser applicationUser, TestCase testCase,
                        List<CustomFieldValueDTO> customFieldValueDTOList);

    boolean updateIssue(String action, ApplicationUser applicationUser, TestCase testCase,
                        List<CustomFieldValueDTO> customFieldValueDTOList);

    boolean updateIssue(ApplicationUser applicationUser, long issueId,
                        List<CustomFieldValueDTO> customFieldValueDTOList);
    boolean updateIssue(ApplicationUser applicationUser, TestCase testCase,
                        List<CustomFieldValueDTO> customFieldValueDTOList, String assignee);

    Long create(ApplicationUser applicationUser, IssueService.CreateValidationResult createValidationResult);

    boolean createOrUpdateIssue(final MutableIssue issue, final boolean isCreate);

    boolean updateIssueOnEventMove(final ApplicationUser applicationUser, final Long issueId, final String phase,
                                   final String testcaseType);

    String updateIssueStatusByAction(final ApplicationUser applicationUser, final Long issueId, final String action, final IssueInputParameters issueInputParameters);

}
