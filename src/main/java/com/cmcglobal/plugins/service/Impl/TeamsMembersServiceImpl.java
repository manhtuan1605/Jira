package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cmcglobal.plugins.entity.TeamsMembers;
import com.cmcglobal.plugins.service.TeamsMembersService;
import com.cmcglobal.plugins.utils.Constants;
import net.java.ao.DBParam;
import net.java.ao.Query;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

@Scanned
@Named
public class TeamsMembersServiceImpl implements TeamsMembersService {
    @ComponentImport
    private final ActiveObjects ao;

    @Inject
    public TeamsMembersServiceImpl(final ActiveObjects ao) {
        this.ao = checkNotNull(ao);
    }

    @Override
    public Set<ApplicationUser> findTeamMembers(final long thePid, final long theTeamId) {
        final TeamsMembers[] listTeamsMembers = ao.find(TeamsMembers.class, Query.select()
                                                                                 .where("PROJECT_ID=? AND TEAM_ID=?",
                                                                                        thePid, theTeamId)
                                                                                 .order("CREATE_DATE DESC"));
        final Set<ApplicationUser> applicationUsers = new LinkedHashSet<>();
        for (final TeamsMembers teamsMember : listTeamsMembers) {
            final Optional<ApplicationUser> applicationUser = ComponentAccessor.getUserManager()
                                                                               .getUserById(teamsMember.getMemberId());
            if (applicationUser.isPresent()) {
                applicationUsers.add(applicationUser.get());
            }
        }
        return applicationUsers;
    }

    @Override
    public Map<String, ApplicationUser> getUserByProjectId(final long projectId) {
        final Map<String, ApplicationUser> applicationUserMap = new HashMap<>();
        final Project project = ComponentAccessor.getProjectManager().getProjectObj(projectId);
        final ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager.class);
        final Collection<ProjectRole> projectRoles = projectRoleManager.getProjectRoles();
        for (final ProjectRole projectRole : projectRoles) {
            final ProjectRoleActors roleActors = projectRoleManager.getProjectRoleActors(projectRole, project);
            final Set<ApplicationUser> userSet = roleActors.getApplicationUsers();
            for (final ApplicationUser u : userSet) {
                applicationUserMap.put(u.getUsername(), u);
            }
        }
        return applicationUserMap;
    }

    public Map<String, ApplicationUser> getMemberByProjectId(final long thePid) {
        final Map<String, ApplicationUser> teamsMembersMap = new HashMap<>();
        ApplicationUser appUser;
        final List<TeamsMembers> teamsMembers = newArrayList(
                ao.find(TeamsMembers.class, Query.select().where(Constants.QUERY_PROJECT_ID, thePid)));
        for (final TeamsMembers teamsMember : teamsMembers) {
            final Optional<ApplicationUser> applicationUser = ComponentAccessor.getUserManager()
                                                                               .getUserById(teamsMember.getMemberId());
            if (applicationUser.isPresent()) {
                appUser = applicationUser.get();
                teamsMembersMap.put(appUser.getUsername(), appUser);
            }
        }
        return teamsMembersMap;
    }

    @Override
    public boolean isExistedMember(final long thePid, final long theTeamId, final long theUserId) {
        return ao.find(TeamsMembers.class, Query.select()
                .where("PROJECT_ID=? AND TEAM_ID=? AND MEMBER_ID=?", thePid, theTeamId,
                        theUserId)).length > 0;
    }

    @Override
    public boolean isExistedOnlyMember(final long thePid, final long theTeamId, final long theUserId) {
        return ao.find(TeamsMembers.class, Query.select()
                                                .where("PROJECT_ID=? AND TEAM_ID!=? AND MEMBER_ID=?", thePid, theTeamId,
                                                       theUserId)).length > 0;
    }


    @Override
    public void addMember(final long thePid, final long theTeamId, final long theUserId) {
        final TeamsMembers createdMember = ao.create(TeamsMembers.class, new DBParam("PROJECT_ID", thePid),
                                                     new DBParam("TEAM_ID", theTeamId),
                                                     new DBParam("MEMBER_ID", theUserId));
        createdMember.setCreateDate(new Date());
        createdMember.save();
    }

    @Override
    public void removeMember(final long thePid, final long theTeamId, final long theUserId) {
        final TeamsMembers[] list = ao.find(TeamsMembers.class, Query.select()
                                                                     .where("PROJECT_ID=? AND TEAM_ID=? AND MEMBER_ID=?",
                                                                            thePid, theTeamId, theUserId));
        ao.delete(list[0]);
    }
}
