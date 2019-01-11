package com.cmcglobal.plugins.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;

import java.util.Date;

@Preload
public interface TeamsMembers extends Entity {
    @NotNull
    long getTeamId();

    @NotNull
    void setTeamId(long teamId);

    @NotNull
    long getProjectId();

    @NotNull
    long setProjectId(long projectId);

    @NotNull
    long getMemberId();

    @NotNull
    void setMemberId(long memberId);

    Date getCreateDate();

    void setCreateDate(Date createDate);
}
