package com.cmcglobal.plugins.jira.scheduler;

import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface ImportBigFileSalJobs {

    void reschedule(int intervalInSeconds);

    void unschedule();

}
