package com.cmcglobal.plugins.dto;

import com.atlassian.jira.user.ApplicationUser;

public class ApplicationUserDTO {
    private long id;
    private String username;
    private String name;
    private String displayName;

    public ApplicationUserDTO(long id, String username, String name, String displayName) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.displayName = displayName;
    }
    public static ApplicationUserDTO create(ApplicationUser user) {
        return new ApplicationUserDTO(user.getId(), user.getUsername(), user.getName(), user.getDisplayName());
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
