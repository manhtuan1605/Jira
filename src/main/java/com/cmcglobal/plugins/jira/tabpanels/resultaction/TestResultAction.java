package com.cmcglobal.plugins.jira.tabpanels.resultaction;

import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.cmcglobal.plugins.entity.TestResult;

import java.util.Date;
import java.util.Map;

public class TestResultAction extends AbstractIssueAction {

	private TestResult testResult;
	public TestResultAction(IssueTabPanelModuleDescriptor descriptor, TestResult testResult) {
		super(descriptor);
		this.testResult = testResult;
	}

	@Override
	public Date getTimePerformed() {
		return new Date();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void populateVelocityParams(Map params) {
	    params.put("resultTest", this.testResult);
	}

}
