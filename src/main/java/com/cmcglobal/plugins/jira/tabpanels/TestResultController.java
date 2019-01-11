package com.cmcglobal.plugins.jira.tabpanels;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.entity.TestResult;
import com.cmcglobal.plugins.jira.tabpanels.resultaction.TestResultAction;
import com.cmcglobal.plugins.service.TestResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class TestResultController extends AbstractIssueTabPanel implements IssueTabPanel {
    private static final Logger log = LoggerFactory.getLogger(TestResultController.class);
    private TestResultService testResultService;

    @Inject
    public TestResultController(TestResultService testResultService) {
        this.testResultService = testResultService;
    }

    @Override
    public List getActions(Issue issue, ApplicationUser remoteUser) {
        final List<TestResultAction> panelActions = new ArrayList<>();
        TestResult testResult = testResultService.getDetailFromIssueId(issue.getId()) == null ? null:testResultService.getDetailFromIssueId(issue.getId());
        panelActions.add(new TestResultAction(descriptor, testResult));
        return panelActions;
    }

    public boolean showPanel(Issue issue, ApplicationUser remoteUser) {
        return (testResultService.getDetailFromIssueId(issue.getId()) != null);
    }


}
