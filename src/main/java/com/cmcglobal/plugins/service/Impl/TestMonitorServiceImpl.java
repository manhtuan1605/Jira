package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.NotFoundException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cmcglobal.plugins.dto.ApplicationUserDTO;
import com.cmcglobal.plugins.dto.TestCaseProductivity;
import com.cmcglobal.plugins.dto.TestMonitor;
import com.cmcglobal.plugins.dto.TestMonitorMember;
import com.cmcglobal.plugins.entity.TeamsMembers;
import com.cmcglobal.plugins.entity.TestCaseType;
import com.cmcglobal.plugins.service.TestMonitorService;
import com.cmcglobal.plugins.service.TestResultHistoryService;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Helper;
import net.java.ao.Query;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

@Scanned
@Named
public class TestMonitorServiceImpl implements TestMonitorService {
    private static final Logger logger = LoggerFactory.getLogger(TestMonitorServiceImpl.class);
    private static final String TEST_EXECUTED_QUANTITY = "Quantity";
    private static final String TEST_EXECUTED_PRODUCTIVITY = "Productivity";
    @ComponentImport
    private ActiveObjects ao;

    private TestResultHistoryService testResultHistoryService;

    public TestMonitorServiceImpl() {
    }

    @Inject
    public TestMonitorServiceImpl(ActiveObjects ao, TestResultHistoryService testResultHistoryService) {
        this.testResultHistoryService = testResultHistoryService;
        this.ao = ao;
    }

    @Override
    public ProjectRole getRole(ApplicationUser user, long projectId) {
        Project projectManager = ComponentAccessor.getProjectManager().getProjectObj(projectId);
        ProjectRoleManager prm = ComponentAccessor.getComponent(ProjectRoleManager.class);
        Collection<ProjectRole> lstRole = prm.getProjectRoles(user, projectManager);
        ProjectRole userRole = null;
        for (ProjectRole role: lstRole) {
            if (role.getName().equals(Constants.PROJECT_ROLE_PM)) {
                    userRole = role;
                break;
            }
            if (role.getName().equals(Constants.PROJECT_ROLE_QC_LEAD) &&
                    (userRole == null || userRole.getName().equals(Constants.PROJECT_ROLE_QC))) {
                userRole = role;
            }
            if (role.getName().equals(Constants.PROJECT_ROLE_QC) && userRole == null) {
                userRole = role;
            }

        }
        return userRole;
    }

    @Override
    public TestCaseType getTestCaseTypeIsFunction(long projectId) {
        List<TestCaseType> types = this.getTestCaseTypesByProject(projectId);
        return this.getFunctionTestCaseType(types);
    }

    @Override
    public Set<ApplicationUser> getLeaderObjectsByProject(Long projectId) {
        Project projectManager = ComponentAccessor.getProjectManager().getProjectObj(projectId);
        ProjectRoleManager prm = ComponentAccessor.getComponent(ProjectRoleManager.class);
        ProjectRole projectRole = prm.getProjectRole(Constants.PROJECT_ROLE_QC_LEAD);
        if (projectRole != null) {
            ProjectRoleActors projectRoleActors = prm.getProjectRoleActors(projectRole, projectManager);
            return projectRoleActors.getApplicationUsers();
        }
        return new HashSet<>();
    }
    @Override
    public List<TestMonitorMember> getMonitorAllMembers(final long projectId, final LocalDate start, final LocalDate end) {
        Set<ApplicationUser> members = this.getMemberByProject(projectId);
        return this.getDetailMonitor(members, projectId, start, end);
    }

    @Override
    public List<TestMonitorMember> getMonitorTeamMembers(long leaderId, long projectId, LocalDate start, LocalDate end) {
        Set<ApplicationUser> members = this.getMemberObjectsByLeader(leaderId, projectId);
        return this.getDetailMonitor(members, projectId, start, end);
    }
    @Override
    public List<TestMonitorMember> getUserMonitor(ApplicationUser user, long projectId, LocalDate start, LocalDate end) {
        Set<ApplicationUser> members = new HashSet<>();
        members.add(user);
        return this.getDetailMonitor(members, projectId, start, end);
    }

    /**
     * Get users in a team by leader
     * @param leaderId is id of user who is team leader
     * @param projectId is id of project
     * @return Set
     */
    private Set<ApplicationUser> getMemberObjectsByLeader(long leaderId, long projectId) {
        List<TeamsMembers> teamsMembers = this.getTeamMembersByLeader(leaderId, projectId);
        Set<ApplicationUser> users = new HashSet<>();
        users.add(ComponentAccessor.getUserManager().getUserById(leaderId).orElse(null));
        for (TeamsMembers teamsMember : teamsMembers) {
            Optional<ApplicationUser> user = ComponentAccessor.getUserManager().getUserById(teamsMember.getMemberId());
            if(user.isPresent()) {
                users.add(user.get());
            }
        }
        return users;
    }

    /**
     * Get team members of team by leader
     * @param leaderId is id of user who is team leader
     * @param projectId is id of project
     * @return List
     */
    private List<TeamsMembers> getTeamMembersByLeader(long leaderId, long projectId) {
        return newArrayList(ao.find(TeamsMembers.class, Query.select().where("TEAM_ID = ? and PROJECT_ID = ?", leaderId, projectId)));
    }

    /**
     * Get detail monitor of list of user
     * @param members is set of user who are project member
     * @param projectId is id of project which members are joined
     * @param start is start date condition
     * @param end is end date condition
     * @return List<TestMonitorMember>
     */
    private List<TestMonitorMember> getDetailMonitor(Set<ApplicationUser> members, long projectId, LocalDate start, LocalDate end) {
        List<TestMonitorMember> monitors = new ArrayList<>();
        TestMonitorMember monitorMember;
        for (ApplicationUser member : members) {
            monitorMember = new TestMonitorMember();

            monitorMember.setMember(ApplicationUserDTO.create(member));

            monitorMember.setTestExecute(this.getTestExecute(member, projectId, start, end));

            monitorMember.setQaSubmit(this.getQASubmit(member, projectId, start, end));

            monitorMember.setDefectSubmit(this.getDefectSubmit(member, projectId, start, end));

            monitorMember.setDefectFix(this.getDefectFixConfirmation(member, projectId, start, end));

            monitorMember.setDfAndQaFollow(this.getDefectAndQaFollowUp(member, projectId, start, end));

            monitorMember.setOther(this.getOtherMonitor(member, projectId, start, end));

            monitors.add(monitorMember);
        }
        return monitors;
    }

    /**
     * Get all member of project
     * @param projectId is id of project
     * @return Set<ApplicationUser>
     */
    private Set<ApplicationUser> getMemberByProject(final long projectId) {
        Set<ApplicationUser> members = new HashSet<>();
        Project project = ComponentAccessor.getProjectManager().getProjectObj(projectId);
        ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager.class);
        ProjectRole role = projectRoleManager.getProjectRole(Constants.PROJECT_ROLE_PM);
        if (role != null) {
            ProjectRoleActors roleActors = projectRoleManager.getProjectRoleActors(role, project);
            members.addAll(roleActors.getApplicationUsers());
        }
        role = projectRoleManager.getProjectRole(Constants.PROJECT_ROLE_QC_LEAD);
        if (role != null) {
            ProjectRoleActors roleActors = projectRoleManager.getProjectRoleActors(role, project);
            members.addAll(roleActors.getApplicationUsers());
        }
        role = projectRoleManager.getProjectRole(Constants.PROJECT_ROLE_QC);
        if (role != null) {
            ProjectRoleActors roleActors = projectRoleManager.getProjectRoleActors(role, project);
            members.addAll(roleActors.getApplicationUsers());
        }
        return members;
    }

    /**
     * Get test executed information
     * @param user is member of project
     * @param projectId is id of project
     * @param start is start date condition
     * @param end is end date condition
     * @return TestMonitor
     */
    private TestMonitor getTestExecute(ApplicationUser user, Long projectId, LocalDate start, LocalDate end) {
        TestMonitor testMonitor = new TestMonitor();
        List<Issue> issues = this.getIssuesByUserAndProject(user, projectId, start, end);
        List<TestCaseType> types = this.getTestCaseTypesByProject(projectId);
        TestCaseType typeFunction = this.getFunctionTestCaseType(types);
        testMonitor.setQuantity(issues.size());
        testMonitor.setTime(this.sumTestDuration(issues));
        Map<String, Double> map = this.computeExecuteProductivity(issues, projectId);
        testMonitor.setProductivity(map.get(TEST_EXECUTED_PRODUCTIVITY));
        testMonitor.setEnoughQuantity(map.get(TEST_EXECUTED_QUANTITY) < typeFunction.getPerformance());
        testMonitor.setEnoughProductivity(map.get(TEST_EXECUTED_PRODUCTIVITY) < typeFunction.getPerformance());
        return testMonitor;
    }

    private TestMonitor getDefectSubmit(ApplicationUser user, Long projectId, LocalDate start, LocalDate end) {
        List<Issue> tasks = this.getTaskByUserAndProject(projectId, Constants.TYPE_OF_WORK_DEFECT_SUBMIT);
        return this.summaryWorklog(user, tasks, start, end);
    }

    private TestMonitor getQASubmit(ApplicationUser user, Long projectId, LocalDate start, LocalDate end) {
        List<Issue> tasks = this.getTaskByUserAndProject(projectId, Constants.TYPE_OF_WORK_QA_SUBMIT);
        return this.summaryWorklog(user, tasks, start, end);
    }

    private TestMonitor getDefectFixConfirmation(ApplicationUser user, Long projectId, LocalDate start, LocalDate end) {
        List<Issue> tasks = this.getTaskByUserAndProject(projectId, Constants.TYPE_OF_WORK_DEFECT_FIX);
        return this.summaryWorklog(user, tasks, start, end);
    }

    private TestMonitor getDefectAndQaFollowUp(ApplicationUser user, Long projectId, LocalDate start, LocalDate end) {
        List<Issue> tasks = this.getTaskByUserAndProject(projectId, Constants.TYPE_OF_WORK_DF_QA_FOLLOW_UP);
        return this.summaryWorklog(user, tasks, start, end);
    }

    private TestMonitor getOtherMonitor(ApplicationUser user, Long projectId, LocalDate start, LocalDate end) {
        List<Issue> tasks = this.getTaskByUserAndProject(projectId, Constants.TYPE_OF_WORK_OTHER);
        return this.getWorklogOtherMonitor(user, tasks, start, end);
    }

    /**
     * Summary log time of user
     * @param user is member of project
     * @param tasks is a issue list of project
     * @param start is start date condition
     * @param end is end date condition
     * @return TestMonitor
     */
    private TestMonitor summaryWorklog(ApplicationUser user, List<Issue> tasks, LocalDate start, LocalDate end) {
        List<Worklog> worklogs = new ArrayList<>();
        tasks.forEach(task -> worklogs.addAll(this.getWorklogByUserAndIssue(user, task, start, end)));
        double spentTime = worklogs.stream().mapToDouble(Worklog::getTimeSpent).sum() / 3600;
        TestMonitor monitor = new TestMonitor();
        monitor.setQuantity(worklogs.size());
        monitor.setTime(Helper.roundDoubleTwoDecimal(spentTime));
        if(worklogs.isEmpty()) {
            monitor.setProductivity(Constants.CELL_NOT_APPLICABLE);
        } else {
            monitor.setProductivity(Helper.roundDoubleTwoDecimal(worklogs.size()/spentTime));
        }
        return monitor;
    }

    /**
     * Get summary log time type of work is other
     * @param user is member of project
     * @param tasks is a issue list of project
     * @param start is start date condition
     * @param end is end date condition
     * @return TestMonitor
     */
    private TestMonitor getWorklogOtherMonitor(ApplicationUser user, List<Issue> tasks, LocalDate start, LocalDate end) {
        List<Worklog> worklogs = new ArrayList<>();
        tasks.forEach(task -> worklogs.addAll(this.getWorklogByUserAndIssue(user, task, start, end)));
        double spentTime = 0;
        StringBuilder comment = new StringBuilder();
        for(Worklog worklog: worklogs) {
            spentTime += worklog.getTimeSpent();
            comment.append(worklog.getComment());
            comment.append(". ");
            comment.append(System.getProperty(Constants.BUIDER_LINE_SPERATOR));
        }
        TestMonitor monitor = new TestMonitor();
        monitor.setQuantity(worklogs.size());
        monitor.setTime(Helper.roundDoubleTwoDecimal(spentTime/3600));
        if (comment.length() > 0) {
            monitor.setProductivity(comment);
        } else {
            monitor.setProductivity(Constants.CELL_NOT_APPLICABLE);
        }
        return monitor;
    }

    /**
     * Get function test case type of project
     *
     * @param testCaseTypes list of test case type
     * @return TestCaseType
     */
    private TestCaseType getFunctionTestCaseType(List<TestCaseType> testCaseTypes) {
        TestCaseType type = null;
        for (TestCaseType testCaseType : testCaseTypes) {
            if (testCaseType.getTestCaseTypeName().equals(Constants.TEST_CASE_FUNCTION)) {
                type = testCaseType;
            }
        }
        if (type == null || type.getPerformance() == 0) {
            throw new NotFoundException(Constants.FUNCTION_NOT_FOUND);
        }
        return type;
    }

    /**
     * Get test case type list of project
     *
     * @param projectId Id of project
     * @return List<E>
     */
    private List<TestCaseType> getTestCaseTypesByProject(Long projectId) {
        return newArrayList(ao.find(TestCaseType.class, Query.select().where("PROJECT_ID = ? and IS_ACTIVE = ?", projectId, true)));
    }

    /**
     * Get issue list of user in project with condition start date and end date
     *
     * @param user      is ApplicationUser who is member in project
     * @param projectId is id of project
     * @param start     is a start date condition and its dataType is LocalDate
     * @param end       is a end date condition and its dataType is LocalDate
     * @return List<Issue>
     */
    private List<Issue> getIssuesByUserAndProject(ApplicationUser user, Long projectId, LocalDate start, LocalDate end) {
        List<Issue> resolveIssues = new ArrayList<>();
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        try {
            Collection<Long> issueIds = issueManager.getIssueIdsForProject(projectId);
            List<Issue> issues = issueManager.getIssueObjects(issueIds);
            for (Issue issue : issues) {
                LocalDate updateDate = issue.getUpdated().toLocalDateTime().toLocalDate();
                if (issue.getIssueType() != null && issue.getIssueType().getName().equals(Constants.ISSUE_TYPE_TESTCASE_NAME)
                        && Objects.equals(user, issue.getAssignee())
                        && Helper.isBetweenLocalDate(updateDate, start, end)
                        && issue.getStatus().getName().equals(Constants.STATUS_EXECUTED)) {
                    resolveIssues.add(issue);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return resolveIssues;
    }

    /**
     * Get issue list of user in project with condition start date and end date
     *
     * @param projectId is id of project
     * @return List<Issue>
     */
    @Override
    public List<Issue> getIssuesByProject(Long projectId) {
        List<Issue> resolveIssues = new ArrayList<>();
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        try {
            Collection<Long> issueIds = issueManager.getIssueIdsForProject(projectId);
            List<Issue> issues = issueManager.getIssueObjects(issueIds);
            for (Issue issue : issues) {
                if (issue.getIssueType() != null && issue.getIssueType().getName().equals(Constants.ISSUE_TYPE_TESTCASE_NAME)) {
                    resolveIssues.add(issue);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return resolveIssues;
    }

    /**
     * Get list issue by project and component and its implemanted date is today.
     *
     * @param projectId is id of project
     * @return List<Issue>
     */
    @Override
    public List<Issue> getIssuesByProjectandComponent(Long projectId, ProjectComponent projectComponent) {
        List<Issue> resolveIssues = new ArrayList<>();
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        try {
            Collection<Long> issueIds = issueManager.getIssueIdsForProject(projectId);
            List<Issue> issues = issueManager.getIssueObjects(issueIds);
            CustomField customFieldImplementDate = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(Constants.CUSTOM_FIELD_DATE_OF_IMPLEMENTATION);
            for (Issue issue : issues) {
                Date implementedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(customFieldImplementDate.getValue(issue).toString());
                if (issue.getIssueType() != null && issue.getIssueType().getName().equals(Constants.ISSUE_TYPE_TESTCASE_NAME)) {
                    if(issue.getComponents().iterator().next().getName().equals(projectComponent.getName()) && DateUtils.isSameDay(implementedDate, new Date())) {
                        resolveIssues.add(issue);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return resolveIssues;
    }


    /**
     * Get issue list which issue type is task by type of work
     * @param projectId is id of project
     * @param type is type of work whick is issue custom field
     * @return List<Issue>
     */
    private List<Issue> getTaskByUserAndProject(Long projectId, String type) {
        List<Issue> tasks = new ArrayList<>();
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        try {
            Collection<Long> issueIds = issueManager.getIssueIdsForProject(projectId);
            List<Issue> issues = issueManager.getIssueObjects(issueIds);
            CustomField customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(Constants.CUSTOM_FIELD_TYPE_OF_WORK);
            for (Issue issue : issues) {
                if (issue.getIssueType() != null && issue.getIssueType().getName().equals(Constants.ISSUE_TYPE_TASK_NAME)
                        && type.equals(customField.getValue(issue).toString())) {
                    tasks.add(issue);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return tasks;
    }

    /**
     * Get list of work log by user and issue
     * @param user is application user
     * @param issue is a issue of project
     * @param start is start date condition
     * @param end is end date condition
     * @return List<Worklog>
     */
    private List<Worklog> getWorklogByUserAndIssue(ApplicationUser user, Issue issue, LocalDate start, LocalDate end) {
        List<Worklog> worklogs = ComponentAccessor.getWorklogManager().getByIssue(issue);
        worklogs = worklogs.stream()
                .filter(log -> log.getAuthorObject().equals(user)
                        && Helper.isBetweenLocalDate(log.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), start, end))
                .collect(Collectors.toList());
        return worklogs;
    }

    /**
     * Compute summary test duration
     *
     * @param issues List of issue which member is in charge
     * @return Double
     */
    private double sumTestDuration(List<Issue> issues) {
        double testDuration = 0;
        for (Issue issue : issues) {
            List<CustomField> customFields = ComponentAccessor.getCustomFieldManager().getCustomFieldObjects(issue);
            for (CustomField customField : customFields) {
                if (Constants.CUSTOM_FIELD_TEST_DURATION.equals(customField.getFieldName())) {
                    testDuration += (Double) Optional.ofNullable(customField.getValue(issue)).orElse(0.0);
                }
            }
        }
        return Helper.roundDoubleTwoDecimal(testDuration/60);
    }

    /**
     * Compute productivity of user
     *
     * @param issues    Issue list which user is incharged
     * @param projectId is identify of project
     * @return Object
     */
    private Map<String, Double> computeExecuteProductivity(List<Issue> issues, Long projectId) {

        List<TestCaseType> testCaseTypes = this.getTestCaseTypesByProject(projectId);
        List<TestCaseProductivity> productivities = new ArrayList<>();
        for (TestCaseType testCaseType : testCaseTypes) {
            long sum = 0;
            double time = 0.0;
            for (Issue issue : issues) {
                CustomField customField = ComponentAccessor.getCustomFieldManager()
                        .getCustomFieldObjectByName(Constants.CUSTOM_FIELD_TESTCASE_TYPE);

                if (customField != null && testCaseType.getTestCaseTypeName().equals(customField.getValue(issue).toString())) {
                    CustomField testDuration = ComponentAccessor.getCustomFieldManager()
                            .getCustomFieldObjectByName(Constants.CUSTOM_FIELD_TEST_DURATION);
                    sum++;
                    time += (Double) Optional.ofNullable(testDuration.getValue(issue)).orElse(0.0);
                }
            }

            if (time != 0) {
                productivities.add(new TestCaseProductivity(sum,time/60, testCaseType));
            }
        }
        double testCase = productivities.stream()
                .mapToDouble(i -> (double)i.getVolume()/i.getType().getPerformance())
                .sum();
        double productivity = productivities.stream()
                .mapToDouble(i -> i.getProductivity(this.getFunctionTestCaseType(testCaseTypes)))
                .average()
                .orElse(0.0);

        Map<String, Double> map = new HashMap<>();
        map.put(TEST_EXECUTED_QUANTITY, testCase);
        map.put(TEST_EXECUTED_PRODUCTIVITY, Helper.roundDoubleTwoDecimal(productivity));
        return map;
    }

    @Override
    public Map<String, Integer> countTotalIssueByTestResult(Long projectId, ProjectComponent projectComponent){
        HashMap<String, Integer> mapNumber = new HashMap<>();
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        try {
            Collection<Long> issueIds = issueManager.getIssueIdsForProject(projectId);
            List<Issue> issues = issueManager.getIssueObjects(issueIds);
            CustomField customFieldLatestResult = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(Constants.CUSTOM_FIELD_LATEST_RESULT);
            int totalNG = 0;
            int totalPN = 0;
            int totalOK = 0;
            for (Issue issue : issues) {
                if(issue.getComponents().iterator().next().getName().equals(projectComponent.getName())) {
                    switch (customFieldLatestResult.getValue(issue).toString()) {
                        case Constants.NG:
                            totalNG++;
                            break;
                        case Constants.PN:
                            totalPN++;
                            break;
                        case Constants.OK:
                            totalOK++;
                            break;
                        default:
                            break;
                    }
                }
            }
            mapNumber.put("totalNG", totalNG);
            mapNumber.put("totalPN", totalPN);
            mapNumber.put("totalOK", totalOK);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return mapNumber;
    }

    @Override
    public Map<String, Integer> countTotalIssueByTestResult(Long projectId, ProjectComponent projectComponent, Date start, Date end){
        HashMap<String, Integer> mapNumber = new HashMap<>();
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        try {
            Collection<Long> issueIds = issueManager.getIssueIdsForProject(projectId);
            List<Issue> issues = issueManager.getIssueObjects(issueIds);
            CustomField customFieldLatestResult = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(Constants.CUSTOM_FIELD_LATEST_RESULT);
            int totalNG = 0;
            int totalPN = 0;
            int totalOK = 0;
            for (Issue issue : issues) {
                if(issue.getComponents().iterator().next().getName().equals(projectComponent.getName())) {
                    switch (customFieldLatestResult.getValue(issue).toString()) {
                        case Constants.NG:
                            totalNG++;
                            break;
                        case Constants.PN:
                            totalPN++;
                            break;
                        case Constants.OK:
                            totalOK++;
                            break;
                        default:
                            break;
                    }
                }
            }
            mapNumber.put("totalNG", totalNG);
            mapNumber.put("totalPN", totalPN);
            mapNumber.put("totalOK", totalOK);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return mapNumber;
    }

    @Override
    public List<Issue> getIssuesByProjectandComponentAndEndDate(Long projectId, ProjectComponent projectComponent, Date end){
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        Collection<Long> issueIds = testResultHistoryService.getListIdIssuesWithImpletedDateIsEndate(end);
        List<Issue> issueList = issueManager.getIssueObjects(issueIds);
        List<Issue> resolveIssues = new ArrayList<>();
        for (Issue issue: issueList) {
            if (issue.getComponents()==null || issue.getComponents().isEmpty()){
                continue;
            }
            if(issue.getComponents().iterator().next().getName().equals(projectComponent.getName())){
                resolveIssues.add(issue);
            }
        }
        return resolveIssues;
    }
}
