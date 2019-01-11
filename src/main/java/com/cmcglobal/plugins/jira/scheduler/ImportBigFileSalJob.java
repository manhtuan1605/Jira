package com.cmcglobal.plugins.jira.scheduler;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.scheduling.PluginJob;
import com.cmcglobal.plugins.entity.UploadFile;
import com.cmcglobal.plugins.service.TestCaseService;
import com.cmcglobal.plugins.service.UploadFileService;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.ImportStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@Scanned
public class ImportBigFileSalJob implements PluginJob {

    private static final Logger LOG = LoggerFactory.getLogger(ImportBigFileSalJob.class);

    @Override
    public void execute(final Map<String, Object> jobDataMap) {
        final ImportBigFileSalJobsImpl monitor = (ImportBigFileSalJobsImpl) jobDataMap.get(
                ImportBigFileSalJobsImpl.KEY);
        notNull("monitor", monitor);

        try {
            //IMPORT BIG FILE
            final TestCaseService testCaseService = monitor.getTestCaseService();
            final UploadFileService uploadFileService = monitor.getUploadFileService();
            LOG.info("==================================================");
            LOG.info("IMPORT BIG FILE");

            // Get list file bigdata with status waiting
            final List<UploadFile> uploadFiles = uploadFileService.findAllBigFile(ImportStatus.BIG_FILE_WAITING.getValue());

            if (!CollectionUtils.isEmpty(uploadFiles)) {
                for (final UploadFile uploadFile : uploadFiles) {
                    LOG.info("Processing file " + uploadFile.getUploadFileName());
                    final Project project = ComponentAccessor.getProjectManager().getProjectObj(uploadFile.getProjectId());
                    final ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByKey(uploadFile.getCreateUser());
                    boolean isOK = true;
                    if (project == null) {
                        isOK = false;
                        LOG.error("Project id: " + uploadFile.getProjectId() + " not found");
                    }
                    if (applicationUser == null) {
                        isOK = false;
                        LOG.error("User: " + uploadFile.getCreateUser() + " not found");
                    }

                    if (isOK) {
                        try {
                            LOG.info("Init thread...");
                            testCaseService.initThread(Constants.IMPORT_TESTCASE_TYPE,
                                                       uploadFile.getPathFileValid() + File.separator + uploadFile.getUploadFileName(),
                                                       uploadFile.getUploadFileName(), project, uploadFile.getPhase(),
                                                       uploadFile.getTestCaseType(), applicationUser, ImportStatus.BIG_FILE_WAITING);
                        } catch (final Exception e) {
                            uploadFileService.updateStatusFile(uploadFile.getProjectId(), uploadFile.getUploadFileName(),
                                                               ImportStatus.ERROR.getValue(), "Import error: " + e.getMessage());
                            LOG.error(e.getMessage());
                        }
                    } else {
                        // Update status file upload
                        uploadFileService.updateStatusFile(uploadFile.getProjectId(), uploadFile.getUploadFileName(),
                                                           uploadFile.getStatus(),
                                                           "Can not found project id: [" + uploadFile.getProjectId() +
                                                           "] or user name [" + uploadFile.getCreateUser() + "]");
                    }
                }
            } else {
                LOG.info("Big file with status waiting not found. No need to perform anything.");
            }

            LOG.info("DONE");
            LOG.info("==================================================");
        } catch (final Exception e) {
            LOG.error("Import big file error: " + e.getMessage());
        }
    }

    private String getOption(final Long projectId, final String value) {
        Option returnOption = null;
        final CustomField customField = ComponentAccessor.getCustomFieldManager()
                                                         .getCustomFieldObjectByName(Constants.CUSTOM_FIELD_PHASE);
        final List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();
        FieldConfigScheme fieldConfigScheme = null;
        for (final FieldConfigScheme scheme : schemes) {
            if (scheme.getAssociatedProjectIds().contains(projectId)) {
                fieldConfigScheme = scheme;
                break;
            }
        }
        if (fieldConfigScheme != null) {
            final Map configs = fieldConfigScheme.getConfigsByConfig();
            if (configs != null && !configs.isEmpty()) {
                final FieldConfig config = (FieldConfig) configs.keySet().iterator().next();
                final OptionsManager optionsManager = ComponentManager.getComponentInstanceOfType(OptionsManager.class);
                final List<Option> options = optionsManager.getOptions(config);
                for (final Option option : options) {
                    if (option.getValue().equalsIgnoreCase(value)) {
                        returnOption = option;
                        break;
                    }
                }
            }
        }

        return returnOption != null ? returnOption.getOptionId().toString() : "";
    }

}
