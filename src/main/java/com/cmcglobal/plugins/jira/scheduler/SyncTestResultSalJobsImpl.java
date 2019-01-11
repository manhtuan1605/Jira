package com.cmcglobal.plugins.jira.scheduler;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.scheduling.PluginScheduler;
import com.cmcglobal.plugins.service.CSVTestResultService;
import com.cmcglobal.plugins.service.TestResultService;
import com.google.common.collect.ImmutableMap;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.cmcglobal.plugins.utils.Constants.SCHEDULE_JOB_IMPORT_START_HOUR;
import static com.cmcglobal.plugins.utils.Constants.SCHEDULE_JOB_IMPORT_START_MINUTE;
import static com.cmcglobal.plugins.utils.Constants.SCHEDULE_JOB_IMPORT_START_SECOND;

@Scanned
@Named
public class SyncTestResultSalJobsImpl implements SyncTestResultSalJobs {

    static final         String KEY      = SyncTestResultSalJobsImpl.class.getName() + ":instance";
    private static final String JOB_NAME = SyncTestResultSalJobsImpl.class.getName() + ":job";

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SyncTestResultSalJobsImpl.class);

    private final AtomicBoolean scheduled = new AtomicBoolean();

    private final PluginScheduler pluginScheduler;  // provided by SAL

    private TestResultService testResultService;

    private CSVTestResultService csvTestResultService;

    public TestResultService getTestResultService() {
        return testResultService;
    }

    public CSVTestResultService getCsvTestResultService() {
        return csvTestResultService;
    }

    @Inject
    public SyncTestResultSalJobsImpl(
            @ComponentImport
                    PluginScheduler pluginScheduler, TestResultService testResultService,
            CSVTestResultService csvTestResultService) {
        this.pluginScheduler = pluginScheduler;
        this.testResultService = testResultService;
        this.csvTestResultService = csvTestResultService;
    }

    public void reschedule(int intervalInSeconds) {
        Map<String, Object> jobDataMap = ImmutableMap.of(KEY, (Object) SyncTestResultSalJobsImpl.this);
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, SCHEDULE_JOB_IMPORT_START_HOUR);
        calendar.set(Calendar.MINUTE, SCHEDULE_JOB_IMPORT_START_MINUTE);
        calendar.set(Calendar.SECOND, SCHEDULE_JOB_IMPORT_START_SECOND);
        pluginScheduler.scheduleJob(JOB_NAME,                    // unique name of the job
                                    SyncTestResultSalJob.class,   // class of the job
                                    jobDataMap,                  // key and class of the job to start
                                    calendar.getTime(),          // the time the job is to start
                                    intervalInSeconds * 1000L);  // interval between repeats, in milliseconds
        scheduled.set(true);
        LOG.info(String.format("Task monitor scheduled to run every %d seconds.", intervalInSeconds));
    }

    public void unschedule() {
        try {
            if (scheduled.getAndSet(false)) {
                pluginScheduler.unscheduleJob(JOB_NAME);
            }
        } catch (IllegalArgumentException iae) {
            LOG.warn("Looks like the job was not scheduled, after all", iae);
        }
    }

}
