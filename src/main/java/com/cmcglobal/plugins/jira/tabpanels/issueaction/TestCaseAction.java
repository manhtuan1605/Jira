package com.cmcglobal.plugins.jira.tabpanels.issueaction;

import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.cmcglobal.plugins.entity.TestCase;

import java.util.Date;
import java.util.Map;

public class TestCaseAction extends AbstractIssueAction {

	private TestCase testCase;
	public TestCaseAction(IssueTabPanelModuleDescriptor descriptor, TestCase testCase) {
		super(descriptor);
		this.testCase = testCase;
	}

	@Override
	public Date getTimePerformed() {
		return new Date();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void populateVelocityParams(Map params) {
	    params.put("testCase", this.testCase);
	}

}
