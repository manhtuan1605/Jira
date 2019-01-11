package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.NotFoundException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cmcglobal.plugins.dto.ApplicationUserDTO;
import com.cmcglobal.plugins.dto.TesterPerformanceDTO;
import com.cmcglobal.plugins.dto.TesterPerformanceMemberDTO;
import com.cmcglobal.plugins.entity.TeamsMembers;
import com.cmcglobal.plugins.entity.TestCaseType;
import com.cmcglobal.plugins.service.TesterPerformanceService;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Helper;
import net.java.ao.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

@Scanned
@Named
public class TesterPerformanceServiceImpl implements TesterPerformanceService {

    @ComponentImport
    private final ActiveObjects ao;

    private static final Logger log = LoggerFactory.getLogger(TesterPerformanceServiceImpl.class);

    @Inject
    public TesterPerformanceServiceImpl(ActiveObjects ao) {
        this.ao = ao;
    }

    /**
     * Get issue list of user in project
     *
     * @param user
     * @param projectId
     * @return
     */
    private List<Issue> getListIssue(ApplicationUser user, Long projectId) {
        List<Issue> newIssue = new ArrayList<>();
        try {
            IssueManager issueManager = ComponentAccessor.getIssueManager();
            Collection<Long> issueIds = issueManager.getIssueIdsForProject(projectId);
            List<Issue> issueList = issueManager.getIssueObjects(issueIds);
            for (Issue issue : issueList) {
                Date date = new Date(issue.getUpdated().getTime());
                if (Objects.equals(user, issue.getAssignee()) && Helper.compareDate(date, new Date())) {
                    if (issue.getStatus().getName().equals(Constants.STATUS_OK) ||
                        issue.getStatus().getName().equals(Constants.STATUS_NG)) {
                        newIssue.add(issue);
                    }
                }
            }
            return newIssue;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return newIssue;
    }

    @Override
    public int getResolve(ApplicationUser user, Long projectId) {
        int tc = 0;
        List<Issue> newIssue = getListIssue(user, projectId);
        for (Issue issue : newIssue) {
            if (issue.getStatus().getName().equals(Constants.STATUS_OK) ||
                issue.getStatus().getName().equals(Constants.STATUS_NG)) {
                tc++;
            }
        }
        return tc;

    }

    /**
     * Get test case type is Function
     *
     * @param lstTC
     * @return
     */
    private TestCaseType getFunctionTestcase(List<TestCaseType> lstTC) {
        TestCaseType functionTC = null;
        for (TestCaseType testCaseType : lstTC) {
            if (testCaseType.getTestCaseTypeName().equals(Constants.TEST_CASE_FUNCTION)) {
                functionTC = testCaseType;
            }
        }
        if (functionTC == null) {
            throw new NotFoundException(Constants.FUNCTION_NOT_FOUND);
        }

        return functionTC;
    }

    /**
     * Caculate resolved test case follow test case type
     *
     * @param user
     * @param projectId
     * @return
     */
    private float caculateRT(ApplicationUser user, Long projectId) {
        List<TestCaseType> lstTestCaseType = getListTestCaseType(projectId);
        float countRT = 0;
        TestCaseType functionTC = getFunctionTestcase(lstTestCaseType);
        List<Issue> lstIssue = getListIssue(user, projectId);

        for (Issue issue : lstIssue) {
            String value = getTestCaseType(issue);
            TestCaseType testCaseType = getTestCaseTypeByName(value, projectId);
            if (testCaseType == null || testCaseType.getPerformance() == 0) {
                continue;
            }
            float rt = (float) functionTC.getPerformance() / testCaseType.getPerformance();
            countRT += rt;
        }
        return countRT;
    }

    private int countTC(List<TesterPerformanceMemberDTO> lstperformance) {
        int countTC = 0;
        for (TesterPerformanceMemberDTO testerPerformanceMemberDTO : lstperformance) {
            countTC += testerPerformanceMemberDTO.getResolve();
        }
        return countTC;
    }

    private float caculateRTTeam(Set<ApplicationUser> teamsMembers, Long projectId) {
        float countRTTeam = 0;
        for (ApplicationUser teamsMember : teamsMembers) {
            countRTTeam += caculateRT(teamsMember, projectId);
        }
        return countRTTeam;
    }

    private float caculateRTTeam(ApplicationUser leader, List<TeamsMembers> teamsMembers, Long projectId) {
        float countRTTeam = 0;
        for (TeamsMembers teamsMember : teamsMembers) {
            Optional<ApplicationUser> member = getMember(teamsMember.getMemberId());
            if (member.isPresent()) {
                countRTTeam += caculateRT(member.get(), projectId);
            }
        }
        countRTTeam += caculateRT(leader, projectId);
        return countRTTeam;
    }

    private String getEstimateTime(Set<ApplicationUser> teamsMembers, Long projectId) {
        float countRT = caculateRTTeam(teamsMembers, projectId);
        if (countRT == 0) {
            return Constants.INFINITI_EST;
        }
        List<TestCaseType> lstTestCaseType = getListTestCaseType(projectId);
        TestCaseType functionTC = getFunctionTestcase(lstTestCaseType);
        int hour = LocalDateTime.now().getHour();
        int munite = LocalDateTime.now().getMinute();
        float currentTime = hour + (float) munite * 10 / 600;
        float est =
                (currentTime - Constants.START_TIME) * ((functionTC.getPerformance() * teamsMembers.size()) - countRT) /
                countRT + Constants.LUNCH_TIME + currentTime;
        est = Helper.roundTwoDecimals(est);
        return Helper.convertStringToHour(est);
    }

    private String getEstimateTime(ApplicationUser leader, List<TeamsMembers> teamsMembers, Long projectId) {
        float countRT = caculateRTTeam(leader, teamsMembers, projectId);
        if (countRT == 0) {
            return Constants.INFINITI_EST;
        }
        List<TestCaseType> lstTestCaseType = getListTestCaseType(projectId);
        TestCaseType functionTC = getFunctionTestcase(lstTestCaseType);
        int hour = LocalDateTime.now().getHour();
        int munite = LocalDateTime.now().getMinute();
        float currentTime = hour + (float) munite * 10 / 600;
        float est = (currentTime - Constants.START_TIME) *
                    ((functionTC.getPerformance() * (teamsMembers.size() + 1)) - countRT) / countRT +
                    Constants.LUNCH_TIME + currentTime;
        est = Helper.roundTwoDecimals(est);
        return Helper.convertStringToHour(est);
    }

    private TestCaseType getTestCaseTypeByName(String name, Long projectId) {
        List<TestCaseType> testCaseTypes = newArrayList(ao.find(TestCaseType.class, Query.select()
                                                                                         .where("TEST_CASE_TYPE_NAME = ? and IS_ACTIVE = ? and PROJECT_ID = ?",
                                                                                                name, true,
                                                                                                projectId)));
        if (!testCaseTypes.isEmpty()) {
            return testCaseTypes.get(0);
        } else {
            return null;
        }
    }

    public TestCaseType getTestCaseTypeById(Long id) {
        return newArrayList(
                ao.find(TestCaseType.class, Query.select().where("ID = ? and IS_ACTIVE = ?", id, true))).get(0);
    }

    private String getTestCaseType(Issue issue) {
        String value = null;
        CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
        List<CustomField> lstCustumField = customFieldManager.getCustomFieldObjects(issue);
        for (CustomField customField : lstCustumField) {
            if (customField.getFieldName().equals(Constants.TESTCASE_TYPE)) {
                Object valueObject = customField.getValue(issue);
                if (valueObject != null) {
                    value = customField.getValue(issue).toString();
                }
                break;
            }

        }
        return value;
    }

    @Override
    public float percent(ApplicationUser user, int tc, Long projectId) {
        List<TestCaseType> lstTestCaseType = getListTestCaseType(projectId);
        TestCaseType functionTC = getFunctionTestcase(lstTestCaseType);
        if (functionTC.getPerformance() == 0) {
            return 0;
        }
        float result = caculateRT(user, projectId) / functionTC.getPerformance() * 100;
        return Helper.roundTwoDecimals(result);
    }

    private float percentTeam(Set<ApplicationUser> members, Long projectId) {
        List<TestCaseType> lstTestCaseType = getListTestCaseType(projectId);
        TestCaseType functionTC = getFunctionTestcase(lstTestCaseType);
        if (functionTC.getPerformance() == 0) {
            return 0;
        }
        float result = caculateRTTeam(members, projectId) / (functionTC.getPerformance() * members.size()) * 100;
        return Helper.roundTwoDecimals(result);
    }

    private float percentTeam(ApplicationUser leader, List<TeamsMembers> members, Long projectId) {
        List<TestCaseType> lstTestCaseType = getListTestCaseType(projectId);
        TestCaseType functionTC = getFunctionTestcase(lstTestCaseType);
        if (functionTC.getPerformance() == 0) {
            return 0;
        }
        float result =
                caculateRTTeam(leader, members, projectId) / (functionTC.getPerformance() * (members.size() + 1)) * 100;
        return Helper.roundTwoDecimals(result);
    }

    private List<TestCaseType> getListTestCaseType(Long projectId) {
        return newArrayList(
                ao.find(TestCaseType.class, Query.select().where("PROJECT_ID = ? and IS_ACTIVE = ?", projectId, true)));
    }

    @Override
    public String getEstimate(ApplicationUser user, int tc, Long projectId) {
        float countRT = caculateRT(user, projectId);
        if (countRT == 0) {
            return Constants.INFINITI_EST;
        }
        List<TestCaseType> lstTestCaseType = getListTestCaseType(projectId);
        TestCaseType functionTC = getFunctionTestcase(lstTestCaseType);
        int hour = LocalDateTime.now().getHour();
        int munite = LocalDateTime.now().getMinute();
        float currentTime = hour + (float) munite * 10 / 600;
        float est = (currentTime - Constants.START_TIME) * (functionTC.getPerformance() - countRT) / countRT +
                    Constants.LUNCH_TIME + currentTime;
        est = Helper.roundTwoDecimals(est);
        return Helper.convertStringToHour(est);
    }

    @Override
    public String getRole(ApplicationUser user, final long projectId) {
        Project projectManager = ComponentAccessor.getProjectManager().getProjectObj(projectId);
        ProjectRoleManager prm = ComponentAccessor.getComponent(ProjectRoleManager.class);
        Collection<ProjectRole> lstRole = prm.getProjectRoles(user, projectManager);
        String userRole = null;
        for (ProjectRole role : lstRole) {
            if (role.getName().equals(Constants.PROJECT_ROLE_PM)) {
                userRole = role.getName();
                break;
            }
            if (role.getName().equals(Constants.PROJECT_ROLE_QC_LEAD) &&
                (userRole == null || userRole.equals(Constants.PROJECT_ROLE_QC))) {
                userRole = role.getName();
            }
            if (role.getName().equals(Constants.PROJECT_ROLE_QC) && userRole == null) {
                userRole = role.getName();
            }

        }
        return userRole;
    }

    @Override
    public TesterPerformanceMemberDTO getTesterOfMember(ApplicationUser user, Long projectId) {
        TesterPerformanceMemberDTO testerPerformanceMemberDTO = new TesterPerformanceMemberDTO();
        testerPerformanceMemberDTO.setMember(ApplicationUserDTO.create(user));
        int tc = getResolve(user, projectId);
        testerPerformanceMemberDTO.setResolve(tc);
        testerPerformanceMemberDTO.setPercent(percent(user, tc, projectId));
        testerPerformanceMemberDTO.setEstimate(getEstimate(user, tc, projectId));
        testerPerformanceMemberDTO.setCountTimeMonitor(
                this.countTimeMonitor(this.getResolvedTestCase(user, projectId)));
        return testerPerformanceMemberDTO;
    }

    @Override
    public TesterPerformanceDTO getTesterPerformanceMember(ApplicationUser leader, List<TeamsMembers> teamsMembers,
                                                           Long projectId) {
        TesterPerformanceDTO testerPerformanceDTO = new TesterPerformanceDTO();
        testerPerformanceDTO.setLeader(ApplicationUserDTO.create(leader));

        List<TesterPerformanceMemberDTO> lstperformance = new ArrayList<>();
        lstperformance.add(this.getTesterOfMember(leader, projectId));
        for (TeamsMembers teamsMember : teamsMembers) {
            Optional<ApplicationUser> member = getMember(teamsMember.getMemberId());
            if (member.isPresent()) {
                TesterPerformanceMemberDTO testerPerformanceMemberDTO = getTesterOfMember(member.get(), projectId);
                lstperformance.add(testerPerformanceMemberDTO);
            }
        }
        testerPerformanceDTO.setMembers(lstperformance);
        int countTC = countTC(lstperformance);
        testerPerformanceDTO.setResolve(countTC);
        testerPerformanceDTO.setPercent(percentTeam(leader, teamsMembers, projectId));
        testerPerformanceDTO.setEstimate(getEstimateTime(leader, teamsMembers, projectId));
        return testerPerformanceDTO;
    }

    @Override
    public TesterPerformanceDTO getTesterPerfomancePM(Set<ApplicationUser> teamsMembers, Long projectId) {
        List<TesterPerformanceMemberDTO> lstMember = new ArrayList<>();
        for (ApplicationUser member : teamsMembers) {
            TesterPerformanceMemberDTO testerPerformanceMemberDTO = getTesterOfMember(member, projectId);
            lstMember.add(testerPerformanceMemberDTO);
        }
        int countTC = countTC(lstMember);

        TesterPerformanceDTO testerPerformanceDTO = new TesterPerformanceDTO();
        testerPerformanceDTO.setMembers(lstMember);
        testerPerformanceDTO.setResolve(countTC);
        testerPerformanceDTO.setPercent(percentTeam(teamsMembers, projectId));
        testerPerformanceDTO.setEstimate(getEstimateTime(teamsMembers, projectId));
        return testerPerformanceDTO;
    }

    @Override
    public Optional<ApplicationUser> getMember(long userId) {
        UserManager userManager = ComponentAccessor.getUserManager();
        return userManager.getUserById(userId);
    }

    @Override
    public List<TeamsMembers> getListMember(ApplicationUser leader, Long projectId) {
        return newArrayList(ao.find(TeamsMembers.class,
                                    Query.select().where("TEAM_ID = ? and PROJECT_ID = ?", leader.getId(), projectId)));
    }

    @Override
    public Set<ApplicationUser> getMemberOfProject(Long projectId) {
        Set<ApplicationUser> allUser = new HashSet<>();
        Project project = ComponentAccessor.getProjectManager().getProjectObj(projectId);
        ProjectRoleManager prm = ComponentAccessor.getComponent(ProjectRoleManager.class);
        ProjectRole projectRoleLeader = prm.getProjectRole(Constants.PROJECT_ROLE_QC_LEAD);
        if (projectRoleLeader != null) {
            ProjectRoleActors projectActorRoleLeader = prm.getProjectRoleActors(projectRoleLeader, project);
            Set<ApplicationUser> userLeader = projectActorRoleLeader.getApplicationUsers();
            allUser.addAll(userLeader);
        }
        ProjectRole projectRolePM = prm.getProjectRole(Constants.PROJECT_ROLE_PM);
        if (projectRolePM != null) {
            ProjectRoleActors projectActorRolePM = prm.getProjectRoleActors(projectRolePM, project);
            Set<ApplicationUser> userPM = projectActorRolePM.getApplicationUsers();
            allUser.addAll(userPM);
        }
        ProjectRole projectRoleQC = prm.getProjectRole(Constants.PROJECT_ROLE_QC);
        if (projectRoleQC != null) {
            ProjectRoleActors projectActorRoleQC = prm.getProjectRoleActors(projectRoleQC, project);
            Set<ApplicationUser> userQC = projectActorRoleQC.getApplicationUsers();
            allUser.addAll(userQC);
        }

        return allUser;
    }

    /**
     * count monitor time of member
     *
     * @param issues List of issues which is assigned for member
     * @return Map
     */
    private Map<String, Double> countTimeMonitor(List<Issue> issues) {
        double testDuration = 0;
        double qaTime = 0;
        double defectTime = 0;
        for (Issue issue : issues) {
            List<CustomField> customFields = ComponentAccessor.getCustomFieldManager().getCustomFieldObjects(issue);
            for (CustomField customField : customFields) {
                switch (customField.getFieldName()) {
                    case Constants.CUSTOM_FIELD_TEST_DURATION:
                        testDuration += (Double) Optional.ofNullable(customField.getValue(issue)).orElse(0.0);
                        break;
                    case Constants.CUSTOM_FIELD_TIME_FOR_QNA_SUBMISSION:
                        qaTime += (Double) Optional.ofNullable(customField.getValue(issue)).orElse(0.0);
                        break;
                    case Constants.CUSTOM_FIELD_TIME_FOR_DEFECT_SUBMISSION:
                        defectTime += (Double) Optional.ofNullable(customField.getValue(issue)).orElse(0.0);
                        break;
                    default:
                        break;
                }
            }
        }
        Map<String, Double> time = new HashMap<>();
        time.put(Constants.CUSTOM_FIELD_TEST_DURATION, testDuration);
        time.put(Constants.CUSTOM_FIELD_TIME_FOR_QNA_SUBMISSION, qaTime);
        time.put(Constants.CUSTOM_FIELD_TIME_FOR_DEFECT_SUBMISSION, defectTime);
        return time;
    }

    /**
     * Get list of issue which member resolved
     *
     * @param user      user is member
     * @param projectId Identify of project
     * @return List
     */
    private List<Issue> getResolvedTestCase(ApplicationUser user, long projectId) {
        List<Issue> issues = this.getListIssue(user, projectId);
        return issues.stream()
                     .filter(issue -> issue.getStatus().getName().equals(Constants.STATUS_OK) ||
                                      issue.getStatus().getName().equals(Constants.STATUS_NG))
                     .collect(Collectors.toList());
    }

    public Set<ApplicationUser> getMemberProjectWithRole(Long projectId, String projectRole) {
        Set<ApplicationUser> user = new HashSet<>();
        Project project = ComponentAccessor.getProjectManager().getProjectObj(projectId);
        ProjectRoleManager prm = ComponentAccessor.getComponent(ProjectRoleManager.class);
        ProjectRole projectRoleLeader = prm.getProjectRole(projectRole);
        if (projectRoleLeader != null) {
            ProjectRoleActors projectActorRoleLeader = prm.getProjectRoleActors(projectRoleLeader, project);
            user = projectActorRoleLeader.getApplicationUsers();
        }
        return user;
    }
}
