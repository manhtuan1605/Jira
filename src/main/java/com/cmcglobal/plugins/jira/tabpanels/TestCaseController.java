package com.cmcglobal.plugins.jira.tabpanels;

import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.entity.TestCase;
import com.cmcglobal.plugins.service.TestCaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel;
import com.atlassian.jira.issue.Issue;

import java.util.ArrayList;
import java.util.List;

import com.cmcglobal.plugins.jira.tabpanels.issueaction.TestCaseAction;

import javax.inject.Inject;

public class TestCaseController extends AbstractIssueTabPanel implements IssueTabPanel {
    private TestCaseService testCaseService;

    private static final Logger log = LoggerFactory.getLogger(TestCaseController.class);

    @Inject
    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @Override
    public List getActions(final Issue issue, final ApplicationUser remoteUser) {
        final List<TestCaseAction> panelActions = new ArrayList<>();
        TestCase testCase = testCaseService.findByIssueId(issue.getId());
        panelActions.add(new TestCaseAction(descriptor, testCase));
        return panelActions;
    }

    @Override
    public boolean showPanel(final Issue issue, final ApplicationUser remoteUser) {
        return true;
    }
}
