package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cmcglobal.plugins.entity.TeamsMembers;
import com.cmcglobal.plugins.entity.TestResultHistory;
import com.cmcglobal.plugins.service.TestResultHistoryService;
import com.cmcglobal.plugins.utils.Constants;
import net.java.ao.DBParam;
import net.java.ao.Query;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

@Scanned
@Named
public class TestResultHistoryServiceImpl implements TestResultHistoryService {
    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public TestResultHistoryServiceImpl(final ActiveObjects ao) {
        this.ao = checkNotNull(ao);
    }


    @Override
    public List<TestResultHistory> getTestResultHistoryByIssue(final long issueId) {
        Calendar c = new GregorianCalendar();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date date = c.getTime();
        final List<TestResultHistory> testResultHistories = newArrayList(
        ao.find(TestResultHistory.class, Query.select().where("ISSUE_ID = ? AND DATE_OF_IMPLEMENTATION < ?", issueId, date).order("CREATED_DATE DESC")));
        return testResultHistories;
    }

    @Override
    public List<TestResultHistory> getTestResultHistoryByIssueAndEndDate(final long issueId, Date end) {
         final List<TestResultHistory> testResultHistories = newArrayList(
         ao.find(TestResultHistory.class, Query.select().where("ISSUE_ID = ? AND DATE_OF_IMPLEMENTATION = ?", issueId, end).order("CREATED_DATE DESC")));
         return testResultHistories;
    }

    @Override
    public List<TestResultHistory> getTestResultHistoryByIssueAndImplementedDateByEndDate(final long issueId, Date end) {
        final List<TestResultHistory> testResultHistories = newArrayList(
                ao.find(TestResultHistory.class, Query.select().where("ISSUE_ID = ? AND DATE_OF_IMPLEMENTATION <= ?", issueId, end).order("CREATED_DATE DESC")));
        return testResultHistories;
    }

    @Override
    public List<TestResultHistory> getHistoryByIssueTheDayBeforeEndDate(long issueId, Date end){
         final List<TestResultHistory> testResultHistories = newArrayList(
         ao.find(TestResultHistory.class, Query.select().where("ISSUE_ID = ? AND DATE_OF_IMPLEMENTATION < ?", issueId, end).order("CREATED_DATE DESC")));
         return testResultHistories;
    }

    @Override
    public List<Long> getListIdIssuesWithImpletedDateIsEndate(Date end) {
        final List<TestResultHistory> testResultHistories = newArrayList(
        ao.find(TestResultHistory.class, Query.select().where("DATE_OF_IMPLEMENTATION = ?", end)));
        Set<Long> ids = new HashSet<>();
        for(TestResultHistory testResultHistory : testResultHistories){
            ids.add(testResultHistory.getIssueId());
        }
        List<Long> result = new ArrayList<>(ids);
        return result;
    }

    @Override
    public Map<String, Integer> countTotalTestResult(long projectId, ProjectComponent projectComponent, Date start, Date end) {
//        if (start == null) {
//            start = new Date(2000, 1, 1);
//        }
        //lấy list các history trước ngày endDate
        final List<TestResultHistory> allHistories = newArrayList(
                ao.find(TestResultHistory.class, Query.select().where("DATE_OF_IMPLEMENTATION <= ? ", end).order("CREATED_DATE DESC")));
        Set<Long> ids = new HashSet<>();
        //Loại bỏ id trùng nhau
        for (TestResultHistory testResultHistory : allHistories) {
            ids.add(testResultHistory.getIssueId());
        }
        //lấy ra các issue thuộc component
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        List<Issue> issueList = issueManager.getIssueObjects(ids);
        List<Issue> resolveIssues = new ArrayList<>();
        for (Issue issue : issueList) {
            if(issue.getComponents()==null || issue.getComponents().isEmpty()){
                continue;
            }
            if (issue.getComponents().iterator().next().getName().equals(projectComponent.getName())) {
                resolveIssues.add(issue);
            }
        }
        int oK = 0;
        int nG = 0;
        int pN = 0;
        for (Issue issue : resolveIssues) {
            //Lấy lịch sử cuối cùng của issue trong ngày kết thúc để tính toán.
            List<TestResultHistory> testResultHistories = getTestResultHistoryByIssueAndImplementedDateByEndDate(issue.getId(), end);
            if (!testResultHistories.isEmpty()) {
                if(testResultHistories.get(0).getLatestResult().contains(Constants.PN)){
                    pN++;
                    continue;
                }
                switch (testResultHistories.get(0).getLatestResult()) {
                    case Constants.OK:
                        oK++;
                        break;
                    case Constants.NG:
                        nG++;
                        break;
                    default:
                        break;
                }
            }

        }
        HashMap<String, Integer> map = new HashMap<>();
        map.put("totalOK", oK);
        map.put("totalNG", nG);
        map.put("totalPN", pN);
        map.put("totalTestcase", resolveIssues.size());
        return map;
    }
}
