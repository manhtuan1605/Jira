package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cmcglobal.plugins.dto.ResultMessage;
import com.cmcglobal.plugins.dto.UploadFileDTO;
import com.cmcglobal.plugins.entity.UploadFile;
import com.cmcglobal.plugins.service.UploadFileService;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.ImportStatus;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static com.cmcglobal.plugins.utils.Constants.DEFAULT_INTERVAL_IN_SECONDS;
import static com.cmcglobal.plugins.utils.Constants.ERROR_REMOVE_UPLOAD_FILE;
import static com.cmcglobal.plugins.utils.Constants.MESSAGE_REMOVE_FILE_ERROR;
import static com.cmcglobal.plugins.utils.Constants.MESSAGE_REMOVE_FILE_IMPORTING_ERROR;
import static com.cmcglobal.plugins.utils.Constants.MESSAGE_REMOVE_FILE_SUCCESS;
import static com.cmcglobal.plugins.utils.Constants.PARAMETER_ACTION_DOWNLOAD_VALUE;
import static com.cmcglobal.plugins.utils.Constants.PARAMETER_ACTION_DOWNLOAD_VALUE_INVALID;
import static com.cmcglobal.plugins.utils.Constants.PARAMETER_ACTION_REMOVE_VALUE;
import static com.cmcglobal.plugins.utils.Constants.PARAMETER_ACTION_REMOVE_VALUE_INVALID;
import static com.cmcglobal.plugins.utils.Constants.QUERRY_ID;
import static com.cmcglobal.plugins.utils.Constants.QUERY_ORDER_BY_CREATE_DATE;
import static com.cmcglobal.plugins.utils.Constants.QUERY_ORDER_BY_DATE_ASC;
import static com.cmcglobal.plugins.utils.Constants.QUERY_PROJECT_ID;
import static com.cmcglobal.plugins.utils.Constants.QUERY_PROJECT_ID_AND_TYPE;
import static com.cmcglobal.plugins.utils.Constants.SCHEDULE_JOB_IMPORT_PERIOD_TIME_SAFE_TO_DELETED;
import static com.cmcglobal.plugins.utils.Constants.SCHEDULE_JOB_IMPORT_START_HOUR;
import static com.cmcglobal.plugins.utils.Constants.SCHEDULE_JOB_IMPORT_START_MINUTE;
import static com.cmcglobal.plugins.utils.Constants.SCHEDULE_JOB_IMPORT_START_SECOND;
import static com.cmcglobal.plugins.utils.Constants.UPLOAD_FILE_NOT_EXIST;
import static com.google.common.collect.Lists.newArrayList;

@Scanned
@Named
public class UploadFileServiceImpl implements UploadFileService {
    @ComponentImport
    private final        ActiveObjects ao;
    private static final Logger        log = LoggerFactory.getLogger(UploadFileServiceImpl.class);

    @Inject
    public UploadFileServiceImpl(final ActiveObjects ao) {
        this.ao = ao;
    }

    @Override
    public ResultMessage handleEvent(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final Long projectId = Long.parseLong(request.getParameter(Constants.PARAMETER_PROJECT_ID));
        final String projectKey = ComponentAccessor.getProjectManager().getProjectObj(projectId).getKey();
        final String fileName = request.getParameter(Constants.PARAMETER_FILE_NAME);

        final String action = request.getParameter(Constants.PARAMETER_ACTION);
        final File file = new File(fileName);

        if (!file.exists()) {
            response.sendRedirect(
                    Constants.BASE_URL + Constants.RESPONSE_PROJECT + projectKey + Constants.RESPONSE_PRAMA);
            return new ResultMessage(HttpServletResponse.SC_BAD_REQUEST, UPLOAD_FILE_NOT_EXIST, 0, 0,
                                     0, fileName);
        }

        switch (action) {
            case PARAMETER_ACTION_DOWNLOAD_VALUE:
            case PARAMETER_ACTION_DOWNLOAD_VALUE_INVALID:
                response.setContentType(Files.probeContentType(file.toPath()));
                response.setHeader("Content-disposition", "attachment; filename=" + file.getName());
                final OutputStream out = response.getOutputStream();
                try (final FileInputStream in = new FileInputStream(file)) {
                    final byte[] buffer = new byte[4096];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
                out.flush();
                break;
            case PARAMETER_ACTION_REMOVE_VALUE:
            case PARAMETER_ACTION_REMOVE_VALUE_INVALID:
                final UploadFile uploadFile = findByProjectIdAndFileName(projectId, file.getName());
                if (uploadFile != null) {
                    if (StringUtils.equalsIgnoreCase(uploadFile.getStatus(), ImportStatus.IMPORTING.getValue())) {
                        uploadFile.setLastMessage(MESSAGE_REMOVE_FILE_IMPORTING_ERROR);
                        uploadFile.save();
                        log.error(MESSAGE_REMOVE_FILE_IMPORTING_ERROR);
                    } else {
                        // Check file is a big file type
                        if (StringUtils.equalsIgnoreCase(uploadFile.getStatus(), ImportStatus.BIG_FILE_WAITING.getValue())) {
                            final Date date = new Date();
                            final Calendar calendarCurrentTime = new GregorianCalendar();
                            calendarCurrentTime.setTime(date);

                            final Calendar calendarSchedule = new GregorianCalendar();
                            calendarSchedule.setTime(date);
                            calendarSchedule.set(Calendar.HOUR_OF_DAY, SCHEDULE_JOB_IMPORT_START_HOUR);
                            calendarSchedule.set(Calendar.MINUTE, SCHEDULE_JOB_IMPORT_START_MINUTE);
                            calendarSchedule.set(Calendar.SECOND, SCHEDULE_JOB_IMPORT_START_SECOND);

                            final long timeSchedule = calendarSchedule.getTimeInMillis();
                            final long timeCurrent = calendarCurrentTime.getTimeInMillis();
                            final long periodImportTime = Math.abs((timeCurrent - timeSchedule) / 1000) %
                                                          DEFAULT_INTERVAL_IN_SECONDS;
                            if ((timeSchedule < timeCurrent &&
                                 (periodImportTime + SCHEDULE_JOB_IMPORT_PERIOD_TIME_SAFE_TO_DELETED) <
                                 DEFAULT_INTERVAL_IN_SECONDS) || (timeSchedule > timeCurrent && periodImportTime >
                                                                                                SCHEDULE_JOB_IMPORT_PERIOD_TIME_SAFE_TO_DELETED)) {
                                Files.deleteIfExists(file.toPath());
                                updateOrdelete(projectId, file.toPath().toString(), MESSAGE_REMOVE_FILE_SUCCESS + fileName);
                                log.info(MESSAGE_REMOVE_FILE_SUCCESS + fileName);
                            } else {
                                uploadFile.setLastMessage(MESSAGE_REMOVE_FILE_ERROR);
                                uploadFile.save();
                                log.error(MESSAGE_REMOVE_FILE_ERROR);
                            }
                        } else {
                            Files.deleteIfExists(file.toPath());
                            updateOrdelete(projectId, file.toPath().toString(), MESSAGE_REMOVE_FILE_SUCCESS + fileName);
                            log.info(MESSAGE_REMOVE_FILE_SUCCESS + fileName);
                        }
                    }
                } else {
                    log.error(ERROR_REMOVE_UPLOAD_FILE);
                }
                response.sendRedirect(
                        Constants.BASE_URL + Constants.RESPONSE_PROJECT + projectKey + Constants.RESPONSE_PRAMA);
                break;
            case Constants.PARAMETER_ACTION_REMOVE_DEVICE_VALUE:
            case Constants.PARAMETER_ACTION_REMOVE_DEVICE_VALUE_INVALID:
                final UploadFile uploadFileD = findByProjectIdAndFileName(projectId, file.getName());
                if (uploadFileD != null &&
                    StringUtils.equalsIgnoreCase(uploadFileD.getStatus(), ImportStatus.IMPORTED.getValue())) {
                    Files.deleteIfExists(file.toPath());
                    updateOrdelete(projectId, file.toPath().toString(), "");
                } else {
                    log.error(ERROR_REMOVE_UPLOAD_FILE);
                }
                response.sendRedirect(
                        Constants.BASE_URL + Constants.RESPONSE_PROJECT + projectKey + Constants.RESPONSE_PRAMA_DEVICE);
                break;
            default:
                break;
        }
        return new ResultMessage();
    }

    @Override
    public void handleEventDevice(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        final Long projectId = Long.parseLong(request.getParameter(Constants.PARAMETER_PROJECT_ID));
        final String projectKey = ComponentAccessor.getProjectManager().getProjectObj(projectId).getKey();
        final String fileName = request.getParameter(Constants.PARAMETER_FILE_NAME);

        final String action = request.getParameter(Constants.PARAMETER_ACTION);
        final File file = new File(fileName);

        if (!file.exists()) {
            response.sendRedirect(
                    Constants.BASE_URL + Constants.RESPONSE_PROJECT + projectKey + Constants.RESPONSE_PRAMA_DEVICE);
            return;
        }

        switch (action) {
            case PARAMETER_ACTION_DOWNLOAD_VALUE:
            case PARAMETER_ACTION_DOWNLOAD_VALUE_INVALID:
                response.setContentType(Files.probeContentType(file.toPath()));
                response.setHeader("Content-disposition", "attachment; filename=" + file.getName());
                final OutputStream out = response.getOutputStream();
                try (final FileInputStream in = new FileInputStream(file)) {
                    final byte[] buffer = new byte[4096];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
                out.flush();
                break;
            case PARAMETER_ACTION_REMOVE_VALUE:
            case PARAMETER_ACTION_REMOVE_VALUE_INVALID:
                final UploadFile uploadFile = findByProjectIdAndFileName(projectId, file.getName());
                if (uploadFile != null &&
                    StringUtils.equalsIgnoreCase(uploadFile.getStatus(), ImportStatus.IMPORTED.getValue())) {
                    Files.deleteIfExists(file.toPath());
                    updateOrdelete(projectId, file.toPath().toString(), "");
                } else {
                    log.error(ERROR_REMOVE_UPLOAD_FILE);
                }
                response.sendRedirect(
                        Constants.BASE_URL + Constants.RESPONSE_PROJECT + projectKey + Constants.RESPONSE_PRAMA_DEVICE);
                break;
            default:
                break;
        }
    }

    @Override
    public UploadFile create(final UploadFileDTO uploadFileDTO) {
        final UploadFile uploadFile = ao.create(UploadFile.class);
        uploadFile.setUploadFileName(uploadFileDTO.getUploadFileName());
        uploadFile.setUploadFileNameInvalid(uploadFileDTO.getUploadFileNameInvalid());
        uploadFile.setProjectId(uploadFileDTO.getProjectId());
        uploadFile.setType(uploadFileDTO.getType());
        uploadFile.setPhase(uploadFileDTO.getPhase());
        uploadFile.setTestCaseType(uploadFileDTO.getTestCaseType());
        uploadFile.setPathFileValid(uploadFileDTO.getPathFileValid());
        uploadFile.setPathFileInvalid(uploadFileDTO.getPathFileInvalid());
        uploadFile.setCreateDate(new Date());
        final ApplicationUser applicationUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        uploadFile.setCreateUser(applicationUser.getKey());
        uploadFile.save();
        return uploadFile;
    }

    @Override
    public void updateOrdelete(final long projectId, final String fileName, final String message) {
        final boolean isFileInValid = StringUtils.contains(fileName,
                                                           File.separator + Constants.INVALID + File.separator);
        final String name = new File(fileName).getName();
        final UploadFile uploadFile = findByProjectIdAndFileName(projectId, name);
        if (uploadFile != null) {
            if (uploadFile.getUploadFileName() != null && uploadFile.getUploadFileNameInvalid() != null) {
                // update record
                if (isFileInValid) {
                    uploadFile.setUploadFileNameInvalid(null);
                } else {
                    uploadFile.setUploadFileName(null);
                }
                uploadFile.setLastMessage(message);
                uploadFile.save();
            } else {
                // delete record
                ao.deleteWithSQL(UploadFile.class, QUERRY_ID, uploadFile.getID());
            }
        }
    }

    @Override
    public List<UploadFileDTO> findAllByProjectId(final long projectId, final String importType) {
        final Query query;
        if (importType == null) {
            query = Query.select()
                         .where(QUERY_PROJECT_ID, projectId)
                         .order(QUERY_ORDER_BY_CREATE_DATE);
        } else {
            query = Query.select()
                         .where(QUERY_PROJECT_ID_AND_TYPE, projectId, importType)
                         .order(QUERY_ORDER_BY_CREATE_DATE);
        }
        final UploadFile[] uploadFiles = ao.find(UploadFile.class, query);
        final List<UploadFileDTO> uploadFileDTOS = new ArrayList<>();
        for (final UploadFile uploadFile : uploadFiles) {
            final UploadFileDTO uploadFileDTO = new UploadFileDTO();
            uploadFileDTO.setId(uploadFile.getID());
            uploadFileDTO.setProjectId(uploadFile.getProjectId());
            uploadFileDTO.setCreateDate(uploadFile.getCreateDate());
            uploadFileDTO.setCreateUser(uploadFile.getCreateUser());
            uploadFileDTO.setPhase(uploadFile.getPhase());
            uploadFileDTO.setTestCaseType(uploadFile.getTestCaseType());
            uploadFileDTO.setType(uploadFile.getType());
            uploadFileDTO.setUploadFileName(uploadFile.getUploadFileName());
            uploadFileDTO.setUploadFileNameInvalid(uploadFile.getUploadFileNameInvalid());
            uploadFileDTO.setPathFileValid(uploadFile.getPathFileValid());
            uploadFileDTO.setPathFileInvalid(uploadFile.getPathFileInvalid());
            //            uploadFileDTO.setProgress(uploadFile.getProgress());
            uploadFileDTO.setStatus(uploadFile.getStatus());
            uploadFileDTO.setImportType(uploadFile.getType());
            uploadFileDTO.setLastMessage(uploadFile.getLastMessage());
            uploadFileDTOS.add(uploadFileDTO);
        }
        return uploadFileDTOS;
    }

    @Override
    public UploadFile findByProjectIdAndFileName(final long projectId, final String fileName) {
        final UploadFile[] uploadFiles = ao.find(UploadFile.class, Query.select()
                                                                        .where("PROJECT_ID = ? AND ( UPLOAD_FILE_NAME = ? OR UPLOAD_FILE_NAME_INVALID = ? )",
                                                                               projectId, fileName, fileName));
        return uploadFiles.length > 0 ? uploadFiles[0] : null;
    }

    @Override
    public UploadFile findByProjectIdAndFileNameStatus(final long projectId, final String fileName, final String status) {
        final UploadFile[] uploadFiles = ao.find(UploadFile.class, Query.select()
                                                                        .where("PROJECT_ID = ? AND ( UPLOAD_FILE_NAME = ? OR UPLOAD_FILE_NAME_INVALID = ? ) AND STATUS = ? ",
                                                                               projectId, fileName, fileName, status));
        return uploadFiles.length > 0 ? uploadFiles[0] : null;
    }

    @Override
    public List<UploadFile> findAllBigFile(final String status) {
        final UploadFile[] uploadFiles = ao.find(UploadFile.class, Query.select()
                                                                        .where(" STATUS = ? ", status)
                                                                        .order(QUERY_ORDER_BY_DATE_ASC));
        return newArrayList(uploadFiles);
    }

    @Override
    public void updateStatusFile(final Long projectId, final String fileUploadName, final String status, final String message) {
        final UploadFile uploadFile = findByProjectIdAndFileName(projectId, fileUploadName);
        if (uploadFile != null) {
            uploadFile.setStatus(status);
            uploadFile.setLastMessage(message);
            uploadFile.save();
        } else {
            log.error("Can not found Upload file id with projectId: " + projectId + " and file upload name: " +
                      fileUploadName);
        }
    }
}
