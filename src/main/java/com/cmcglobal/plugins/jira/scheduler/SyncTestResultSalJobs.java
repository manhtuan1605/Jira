package com.cmcglobal.plugins.jira.scheduler;

import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface SyncTestResultSalJobs {

    void reschedule(int intervalInSeconds);

    void unschedule();

}
