package com.cmcglobal.plugins.jira.scheduler;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.scheduling.PluginJob;
import com.cmcglobal.plugins.service.CSVTestResultService;
import com.cmcglobal.plugins.service.TestResultService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.cmcglobal.plugins.utils.Constants.CSV_EXTENSION;
import static com.cmcglobal.plugins.utils.Constants.PATH_BACKUP_FILE_CSV;
import static com.cmcglobal.plugins.utils.Constants.PATH_FILE_CSV;
import static com.cmcglobal.plugins.utils.Constants.TIMESTAMP_FORMAT;

@Scanned
public class SyncTestResultSalJob implements PluginJob {

    private static final Logger LOG = LoggerFactory.getLogger(SyncTestResultSalJob.class);

    @Override
    public void execute(final Map<String, Object> jobDataMap) {
        final SyncTestResultSalJobsImpl monitor = (SyncTestResultSalJobsImpl) jobDataMap.get(
                SyncTestResultSalJobsImpl.KEY);
        notNull("monitor", monitor);

        try {
            //SYNC TEST RESULT
            LOG.info("==================================================");
            LOG.info("Sync test result");
            final TestResultService testResultService = monitor.getTestResultService();
            final CSVTestResultService csvTestResultService = monitor.getCsvTestResultService();
            final File directory = new File(PATH_FILE_CSV);
            if (directory.exists()) {
                final File[] listFiles = directory.listFiles();
                if (listFiles == null || listFiles.length == 0) {
                    return;
                }
                final File directoryBackup = new File(PATH_BACKUP_FILE_CSV);
                if (!directoryBackup.exists()) {
                    directoryBackup.mkdir();
                }
                for (int i = 0; i < listFiles.length; i++) {
                    final File file = listFiles[i];
                    if (file.isFile() && FilenameUtils.getExtension(file.getName()).equalsIgnoreCase(CSV_EXTENSION)) {
                        testResultService.syncTestResult(file.getAbsolutePath());
                        csvTestResultService.createOrUpdate(file.getAbsolutePath());
                        // Move file into directory backup
                        final Date date = new Date();
                        final DateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
                        final String strDate = dateFormat.format(date);
                        final File fileBackup = new File(PATH_BACKUP_FILE_CSV + File.separator + file.getName());
                        Files.move(file.toPath(), fileBackup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        fileBackup.renameTo(
                                new File(PATH_BACKUP_FILE_CSV + File.separator + file.getName() + "." + strDate));
                    }
                }
            } else {
                directory.mkdir();
            }
            LOG.info("DONE");
            LOG.info("==================================================");
        } catch (final Exception e) {
            LOG.error("Sync test result error: " + e.getMessage());
        }
    }

}
