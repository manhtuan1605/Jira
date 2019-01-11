package com.cmcglobal.plugins.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.user.ApplicationUser;
import com.cmcglobal.plugins.dto.TesterPerformanceDTO;
import com.cmcglobal.plugins.dto.TesterPerformanceMemberDTO;
import com.cmcglobal.plugins.entity.TeamsMembers;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Transactional
public interface TesterPerformanceService {
    /**
     * Get user's role in project
     *
     * @param user
     * @param projectId
     * @return
     */
    String getRole(ApplicationUser user, final long projectId);

    /**
     * Get member list of project
     *
     * @param leader
     * @param projectId
     * @return
     */
    List<TeamsMembers> getListMember(ApplicationUser leader, Long projectId);

    /**
     * Get tester performance of project member
     *
     * @param leader
     * @param teamsMembers
     * @param projectId
     * @return
     */
    TesterPerformanceDTO getTesterPerformanceMember(ApplicationUser leader, List<TeamsMembers> teamsMembers, Long projectId);

    /**
     * Get quantity test case resolve
     *
     * @param user
     * @param projectId
     * @return
     */
    int getResolve(ApplicationUser user, Long projectId);

    /**
     * Calculate percent done test case
     *
     * @param user
     * @param tc
     * @param projectId
     * @return
     */
    float percent(ApplicationUser user,int tc, Long projectId);

    /**
     * Calculate estimate time to complete test case
     * @param user
     * @param tc
     * @param projectId
     * @return
     */
    String getEstimate(ApplicationUser user,int tc, Long projectId);

    /**
     * Get member performance of team
     * @param user
     * @param projectId
     * @return
     */
    TesterPerformanceMemberDTO getTesterOfMember(ApplicationUser user, Long projectId);

    /**
     * Get team member
     * @param userId
     * @return
     */
    Optional<ApplicationUser> getMember(long userId);

    /**
     * Get member performance of PM
     * @param teamsMembers
     * @param projectId
     * @return
     */
    TesterPerformanceDTO getTesterPerfomancePM(Set<ApplicationUser> teamsMembers, Long projectId);

    /**
     * get all user of project
     * @param projectId
     * @return
     */
    Set<ApplicationUser> getMemberOfProject(Long projectId);
    Set<ApplicationUser> getMemberProjectWithRole(Long projectId,String projectRole);
}
