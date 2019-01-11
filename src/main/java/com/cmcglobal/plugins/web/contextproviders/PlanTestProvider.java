package com.cmcglobal.plugins.web.contextproviders;

import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by User on 7/12/2018.
 */
public class PlanTestProvider extends AbstractJiraContextProvider {


    @Override
    public Map getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        return new HashMap<>();
    }
}
