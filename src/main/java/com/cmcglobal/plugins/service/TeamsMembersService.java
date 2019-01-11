package com.cmcglobal.plugins.service;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Map;
import java.util.Set;

public interface TeamsMembersService {
    Set<ApplicationUser> findTeamMembers(long thePid, long theTeamId);

    Map<String, ApplicationUser> getUserByProjectId(long thePid);

    boolean isExistedMember(long thePid, long theTeamId, long theUserId);

    boolean isExistedOnlyMember(long thePid, long theTeamId, long theUserId);

    @Transactional
    void addMember(long thePid, long theTeamId, long theUserId);

    @Transactional
    void removeMember(long thePid, long theTeamId, long theUserId);
}