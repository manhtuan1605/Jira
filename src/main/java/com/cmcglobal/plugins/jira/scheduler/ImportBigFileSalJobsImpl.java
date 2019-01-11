package com.cmcglobal.plugins.jira.scheduler;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.scheduling.PluginScheduler;
import com.cmcglobal.plugins.service.TestCaseService;
import com.cmcglobal.plugins.service.UploadFileService;
import com.google.common.collect.ImmutableMap;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.cmcglobal.plugins.utils.Constants.SCHEDULE_JOB_IMPORT_START_HOUR;
import static com.cmcglobal.plugins.utils.Constants.SCHEDULE_JOB_IMPORT_START_MINUTE;
import static com.cmcglobal.plugins.utils.Constants.SCHEDULE_JOB_IMPORT_START_SECOND;

@Scanned
@Named
public class ImportBigFileSalJobsImpl implements ImportBigFileSalJobs {

    static final        String KEY      = ImportBigFileSalJobsImpl.class.getName() + ":instance";
    public static final String JOB_NAME = ImportBigFileSalJobsImpl.class.getName() + ":job";

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ImportBigFileSalJobsImpl.class);

    private final AtomicBoolean scheduled = new AtomicBoolean();

    private final PluginScheduler pluginScheduler;  // provided by SAL

    private TestCaseService testCaseService;

    private UploadFileService uploadFileService;

    TestCaseService getTestCaseService() {
        return testCaseService;
    }

    UploadFileService getUploadFileService() {
        return uploadFileService;
    }

    @Inject
    public ImportBigFileSalJobsImpl(
            @ComponentImport
                    PluginScheduler pluginScheduler, TestCaseService testCaseService,
            UploadFileService uploadFileService) {
        this.pluginScheduler = pluginScheduler;
        this.testCaseService = testCaseService;
        this.uploadFileService = uploadFileService;
    }

    public void reschedule(int intervalInSeconds) {
        Map<String, Object> jobDataMap = ImmutableMap.of(KEY, (Object) ImportBigFileSalJobsImpl.this);
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, SCHEDULE_JOB_IMPORT_START_HOUR);
        calendar.set(Calendar.MINUTE, SCHEDULE_JOB_IMPORT_START_MINUTE);
        calendar.set(Calendar.SECOND, SCHEDULE_JOB_IMPORT_START_SECOND);
        pluginScheduler.scheduleJob(JOB_NAME,                    // unique name of the job
                                    ImportBigFileSalJob.class,   // class of the job
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
