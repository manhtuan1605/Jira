package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cmcglobal.plugins.dto.AssigneeDeviceDTO;
import com.cmcglobal.plugins.dto.CustomFieldValueDTO;
import com.cmcglobal.plugins.dto.DeviceDTO;
import com.cmcglobal.plugins.dto.IssueMessageDTO;
import com.cmcglobal.plugins.dto.ResultMessage;
import com.cmcglobal.plugins.entity.UploadFile;
import com.cmcglobal.plugins.service.DeviceService;
import com.cmcglobal.plugins.service.IssueHelperService;
import com.cmcglobal.plugins.service.TeamsMembersService;
import com.cmcglobal.plugins.service.UploadFileService;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.FileUtils;
import com.cmcglobal.plugins.utils.Helper;
import com.cmcglobal.plugins.utils.ImportStatus;
import com.cmcglobal.plugins.utils.Utilities;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.ValidationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.cmcglobal.plugins.utils.Constants.CUSTOM_FIELD_CMC_CODE;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_A2DP_VERSION;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_ANDROID_AUTO;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_AT_COMMAND;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_AVRCP;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_BLUETOOTH;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_BLUETOOTH_VERSION;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_CAR_PLAY;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_CMC_CODE;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_HFP_VERSION;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_MAP_VERSION;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_MIRRORLINK;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_NAME;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_NOTE;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_OS;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_OS_VERSION;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_PART_NUMBER;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_PART_NUMBER_INVOICE;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_PART_NUMBER_INVOICE_GROUP;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_PART_NUMBER_JAPANESE_NAME;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_PBAP_VERSION;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_PRDCV_CODE;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_SUMMARY;
import static com.cmcglobal.plugins.utils.Constants.DEVICE_WIFI_MAC;
import static com.cmcglobal.plugins.utils.Constants.ERROR_APLICATION_USER;
import static com.cmcglobal.plugins.utils.Constants.ERROR_CMC_CODE;
import static com.cmcglobal.plugins.utils.Constants.ERROR_CREATE_ISSUE;
import static com.cmcglobal.plugins.utils.Constants.ERROR_DEVICE_NAME;
import static com.cmcglobal.plugins.utils.Constants.ERROR_HANDLE_FILE;
import static com.cmcglobal.plugins.utils.Constants.ERROR_OS;
import static com.cmcglobal.plugins.utils.Constants.ERROR_OS_VERSION;
import static com.cmcglobal.plugins.utils.Constants.ERROR_PART_NUMBER;
import static com.cmcglobal.plugins.utils.Constants.ERROR_UPDATE_DEVICE;
import static com.cmcglobal.plugins.utils.Constants.FORMAT_DATE_EXPORT;
import static com.cmcglobal.plugins.utils.Constants.FORMAT_DATE_ISSUE;
import static com.cmcglobal.plugins.utils.Constants.IMPORT_TESTCASE_SUCCESS;
import static com.cmcglobal.plugins.utils.Constants.IMPORT_TYPE_UPDATE_ASSIGNEE;
import static com.cmcglobal.plugins.utils.Constants.INVALID_FILE;
import static com.cmcglobal.plugins.utils.Constants.ISSUE_TYPE_AUTOMOTIVE_DEVICE;
import static com.cmcglobal.plugins.utils.Constants.ISSUE_TYPE_PERIPHERAL_DEVICE;
import static com.cmcglobal.plugins.utils.Constants.JAVA_IO_TMP_DIR;
import static com.cmcglobal.plugins.utils.Constants.MESSAGE_ERROR_FILE_SIZE;
import static com.cmcglobal.plugins.utils.Constants.MESSAGE_ERROR_MAX_ROWS;
import static com.cmcglobal.plugins.utils.Constants.MESSAGE_UPLOAD_ERROR;
import static com.cmcglobal.plugins.utils.Constants.MESSAGE_UPLOAD_SUCCESS;
import static com.cmcglobal.plugins.utils.Constants.PARAMETER_PROJECT_ID;
import static com.cmcglobal.plugins.utils.Constants.PART_NUMBER;
import static com.google.common.base.Preconditions.checkNotNull;

@Scanned
@Named
public class DeviceServiceImpl implements DeviceService {

    @ComponentImport
    private final ActiveObjects ao;

    private final IssueHelperService  issueHelperService;
    @ComponentImport
    private final ProjectManager      projectManager;
    private final UploadFileService   uploadFileService;
    private final TeamsMembersService teamsMembersService;

    private static final String MESSAGE_KEY = "message";
    private              int    totalRows   = 0;

    private static final Logger log = LoggerFactory.getLogger(DeviceServiceImpl.class);

    @Inject
    public DeviceServiceImpl(final ActiveObjects ao, final IssueHelperService issueHelperService,
                             final ProjectManager projectManager, final UploadFileService uploadFileService,
                             final TeamsMembersService teamsMembersService) {
        this.ao = checkNotNull(ao);
        this.issueHelperService = issueHelperService;
        this.projectManager = projectManager;
        this.uploadFileService = uploadFileService;
        this.teamsMembersService = teamsMembersService;
    }

    @Override
    public ResultMessage imports(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final Long projectId = Long.parseLong(request.getParameter(PARAMETER_PROJECT_ID));
        final Project project = projectManager.getProjectObj(projectId);
        final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            request.setAttribute(MESSAGE_KEY, Constants.MESSAGE_ATTRIBUTE_VALUE);
        }
        String fileUploadName = "";
        try {
            final DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(Constants.MAX_MEMORY_SIZE);
            factory.setRepository(new File(System.getProperty(JAVA_IO_TMP_DIR)));
            final ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(Constants.MAX_REQUEST_SIZE);
            final List<FileItem> items = upload.parseRequest(request);
            final List<String> fields = new ArrayList<>();
            String fileName = "";
            if (!items.isEmpty()) {
                for (final FileItem item : items) {
                    if (!item.isFormField()) {
                        fileName = item.getName();
                    } else {
                        fields.add(item.getString());
                    }
                }
            }
            final Iterator<FileItem> iter = items.iterator();
            String pathUploadFile = null;
            File invalidTestCase = null;
            String pathInvalidFile = "";
            final I18nHelper i18nHelper = ComponentAccessor.getI18nHelperFactory().getInstance(Locale.getDefault());
            final int maxFileSize = Integer.parseInt(i18nHelper.getText("import-test-case.max-file-size"));
            while (iter.hasNext() && !fields.isEmpty()) {
                final FileItem item = iter.next();
                if (!item.isFormField()) {
                    pathUploadFile = FileUtils.setPathFile(Constants.ADDRESS, project.getKey(), fields.get(0),
                                                           FileUtils.getFileNameCurrent(fileName, Constants.PREFIX));
                    pathInvalidFile = FileUtils.setPathFile(Constants.ADDRESS_INVALID, project.getKey(), fields.get(0),
                                                            FileUtils.getFileNameCurrent(fileName,
                                                                                         Constants.PREFIX_INVALID));
                    final File uploadedFile = new File(pathUploadFile);
                    if (item.getSize() > maxFileSize) {
                        throw new RuntimeException(MESSAGE_ERROR_FILE_SIZE);
                    }
                    item.write(uploadedFile);
                    invalidTestCase = new File(pathInvalidFile);
                    item.write(invalidTestCase);
                    request.setAttribute(MESSAGE_KEY, MESSAGE_UPLOAD_SUCCESS);
                    break;
                }
            }
            fileUploadName = new File(pathUploadFile).getName();
            final List<Object> lstInvalid = readFile(fields.get(0), pathUploadFile, fileName, project, fields.get(1));

            final UploadFile uploadFile = uploadFileService.findByProjectIdAndFileName(projectId, fileUploadName);
            if (!lstInvalid.isEmpty()) {
                final Workbook workbook = FileUtils.getWorkbookByExtension(pathInvalidFile, fileName);
                final CreationHelper createHelper = workbook.getCreationHelper();
                createRowTestCaseErr(lstInvalid, workbook, createHelper,
                                     fields.get(0).equals(Constants.IMPORT_TYPE_UPDATE_ASSIGNEE) ?
                                     fields.get(0) :
                                     fields.get(1));
                final FileOutputStream fileOutputStream = new FileOutputStream(invalidTestCase);
                workbook.write(fileOutputStream);
                fileOutputStream.close();
                workbook.close();
                if (uploadFile != null) {
                    uploadFile.setUploadFileNameInvalid(new File(pathInvalidFile).getName());
                    uploadFile.setPathFileInvalid(new File(pathInvalidFile).getParent());
                }
            }
            if (lstInvalid.isEmpty() && invalidTestCase != null) {
                Files.deleteIfExists(invalidTestCase.toPath());
            }
            // Save information upload into DB
            if (uploadFile != null) {
                uploadFile.setStatus(ImportStatus.IMPORTED.getValue());
                uploadFile.save();
            }
            return new ResultMessage(HttpServletResponse.SC_OK, IMPORT_TESTCASE_SUCCESS,
                                     this.totalRows - lstInvalid.size(), lstInvalid.size(), this.totalRows);
        } catch (final FileUploadException e) {
            log.error(MESSAGE_UPLOAD_ERROR + e.getMessage());
            return new ResultMessage(HttpServletResponse.SC_BAD_REQUEST, MESSAGE_UPLOAD_ERROR);
        } catch (final IOException e) {
            log.error(ERROR_HANDLE_FILE + e.getMessage());
            return new ResultMessage(HttpServletResponse.SC_BAD_REQUEST, ERROR_HANDLE_FILE);
        } catch (ValidationException e) {
            return new ResultMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (final RuntimeException re) {
            log.error(re.getMessage());
            return new ResultMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, re.getMessage());
        } catch (final Exception ex) {
            log.error(ex.getMessage());
            return new ResultMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        } finally {
            final UploadFile uploadFile = uploadFileService.findByProjectIdAndFileName(projectId, fileUploadName);
            if (uploadFile != null) {
                uploadFile.setStatus(ImportStatus.IMPORTED.getValue());
                uploadFile.save();
            }
        }
    }

    private void validateAutoDevice(Row row) throws ValidationException {
        boolean errorRow = false;
        if (row.getCell(0) == null || !Objects.equals(row.getCell(0).toString(), DEVICE_SUMMARY)) {
            errorRow = true;
        }
        if (row.getCell(1) == null || !Objects.equals(row.getCell(1).toString(), DEVICE_CMC_CODE)) {
            errorRow = true;
        }
        if (row.getCell(2) == null || !Objects.equals(row.getCell(2).toString(), DEVICE_PART_NUMBER_JAPANESE_NAME)) {
            errorRow = true;
        }
        if (row.getCell(3) == null || !Objects.equals(row.getCell(3).toString(), DEVICE_PART_NUMBER)) {
            errorRow = true;
        }
        if (row.getCell(4) == null || !Objects.equals(row.getCell(4).toString(), DEVICE_PART_NUMBER_INVOICE)) {
            errorRow = true;
        }
        if (row.getCell(5) == null || !Objects.equals(row.getCell(5).toString(), DEVICE_PART_NUMBER_INVOICE_GROUP)) {
            errorRow = true;
        }
        if (row.getCell(6).toString() == null || !Objects.equals(row.getCell(6).toString(), DEVICE_NOTE)) {
            errorRow = true;
        }
        if (errorRow) {
            throw new ValidationException(INVALID_FILE);
        }
    }

    private void validatePeriDevice(Row row) throws ValidationException {
        boolean errorRow = false;
        if (row.getCell(0) == null || !Objects.equals(row.getCell(0).toString(), DEVICE_SUMMARY)) {
            errorRow = true;
        }
        if (row.getCell(1) == null || !Objects.equals(row.getCell(1).toString(), DEVICE_CMC_CODE)) {
            errorRow = true;
        }
        if (row.getCell(2) == null || !Objects.equals(row.getCell(2).toString(), DEVICE_PRDCV_CODE)) {
            errorRow = true;
        }
        if (row.getCell(3) == null || !Objects.equals(row.getCell(3).toString(), DEVICE_NAME)) {
            errorRow = true;
        }
        if (row.getCell(4) == null || !Objects.equals(row.getCell(4).toString(), DEVICE_OS)) {
            errorRow = true;
        }
        if (row.getCell(5) == null || !Objects.equals(row.getCell(5).toString(), DEVICE_OS_VERSION)) {
            errorRow = true;
        }
        if (row.getCell(6) == null || !Objects.equals(row.getCell(6).toString(), DEVICE_WIFI_MAC)) {
            errorRow = true;
        }
        if (row.getCell(7) == null || !Objects.equals(row.getCell(7).toString(), DEVICE_BLUETOOTH)) {
            errorRow = true;
        }
        if (row.getCell(8) == null || !Objects.equals(row.getCell(8).toString(), DEVICE_BLUETOOTH_VERSION)) {
            errorRow = true;
        }
        if (row.getCell(9) == null || !Objects.equals(row.getCell(9).toString(), DEVICE_HFP_VERSION)) {
            errorRow = true;
        }
        if (row.getCell(10) == null || !Objects.equals(row.getCell(10).toString(), DEVICE_AVRCP)) {
            errorRow = true;
        }
        if (row.getCell(11) == null || !Objects.equals(row.getCell(11).toString(), DEVICE_A2DP_VERSION)) {
            errorRow = true;
        }
        if (row.getCell(12) == null || !Objects.equals(row.getCell(12).toString(), DEVICE_MAP_VERSION)) {
            errorRow = true;
        }
        if (row.getCell(13) == null || !Objects.equals(row.getCell(13).toString(), DEVICE_PBAP_VERSION)) {
            errorRow = true;
        }
        if (row.getCell(14) == null || !Objects.equals(row.getCell(14).toString(), DEVICE_AT_COMMAND)) {
            errorRow = true;
        }
        if (row.getCell(15) == null || !Objects.equals(row.getCell(15).toString(), DEVICE_MIRRORLINK)) {
            errorRow = true;
        }
        if (row.getCell(16) == null || !Objects.equals(row.getCell(16).toString(), DEVICE_CAR_PLAY)) {
            errorRow = true;
        }
        if (row.getCell(17) == null || !Objects.equals(row.getCell(17).toString(), DEVICE_ANDROID_AUTO)) {
            errorRow = true;
        }
        if (row.getCell(18) == null || !Objects.equals(row.getCell(18).toString(), DEVICE_NOTE)) {
            errorRow = true;
        }
        if (errorRow) {
            throw new ValidationException(INVALID_FILE);
        }
    }

    private List<Object> readFile(final String importType, final String fileUploadPath, final String fileName,
                                  final Project project, final String deviceType) throws Exception {
        final List<Object> lstTestCaseErr = new ArrayList<>();
        DeviceDTO deviceDTO;
        final Workbook workbook = FileUtils.getWorkbookByExtension(fileUploadPath, fileName);
        // get current sheet to get data
        final Sheet sheet = workbook.getSheetAt(0);
        final int sumOfRow = sheet.getLastRowNum();
        this.totalRows = sumOfRow - 1;
        final I18nHelper i18nHelper = ComponentAccessor.getI18nHelperFactory().getInstance(Locale.getDefault());
        final int maxRows = Integer.parseInt(i18nHelper.getText("import-test-case.max-rows"));
        if (sumOfRow > maxRows) {
            throw new RuntimeException(MESSAGE_ERROR_MAX_ROWS + maxRows);
        }
        final ApplicationUser applicationUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        if (applicationUser == null) {
            throw new RuntimeException(ERROR_APLICATION_USER);
        }
        StringBuilder builder = new StringBuilder();
        final IssueType[] issueTypes = new IssueType[5];
        project.getIssueTypes().toArray(issueTypes);

        final Set<String> testCaseNoSet = new HashSet<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_DATE_ISSUE, Locale.ENGLISH);
        simpleDateFormat.setLenient(false);
        final UploadFile uploadFile = ao.create(UploadFile.class);
        final String fileNameUpload = new File(fileUploadPath).getName();
        uploadFile.setUploadFileName(fileNameUpload);
        uploadFile.setPathFileValid(new File(fileUploadPath).getParent());
        uploadFile.setProjectId(project.getId());
        uploadFile.setType(importType);
        uploadFile.setTestCaseType(deviceType);
        uploadFile.setStatus(ImportStatus.IMPORTING.getValue());
        uploadFile.setCreateDate(new Date());
        uploadFile.setCreateUser(applicationUser.getKey());
        uploadFile.save();
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        Collection<Long> idIssues = issueManager.getIssueIdsForProject(project.getId());
        List<Issue> issues = issueManager.getIssueObjects(idIssues);
        CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
        final DataFormatter formatter = new DataFormatter();
        if (importType.equals(Constants.IMPORT_TYPE_IMPORT_DEVICE)) {
            switch (deviceType) {
                case ISSUE_TYPE_AUTOMOTIVE_DEVICE:
                    //get list issue id
                    validateAutoDevice(sheet.getRow(0));
                    CustomField customField;
                    Map<String, Issue> lstValue = new HashMap<>();
                    for (Issue issue : issues) {
                        customField = customFieldManager.getCustomFieldObjectByName(PART_NUMBER);
                        if (customField.getValue(issue) == null) {
                            continue;
                        }
                        lstValue.put(customField.getValue(issue).toString(), issue);
                    }
                    for (int i = 1; i <= sumOfRow; i++) {
                        final Row currentRow = sheet.getRow(i);
                        deviceDTO = setValueDeviceAutoDTO(currentRow, formatter);
                        final List<CustomFieldValueDTO> customFieldValueDTOMap = Utilities.getFieldNamesAndValues(
                                deviceDTO, false, ImmutableMap.of("Cmc Code", "CMC Code"));
                        if (StringUtils.isEmpty(formatter.formatCellValue(currentRow.getCell(3)))) {
                            builder.append(ERROR_PART_NUMBER);
                        }
                        if (builder.length() > 0) {
                            deviceDTO.setErrorDevice(builder.toString());
                            deviceDTO.setCurrentIndex(i + 1);
                            lstTestCaseErr.add(deviceDTO);
                            builder.delete(0, builder.length());
                            //uploadFile.setProgress(i + "/" + sumOfRow);
                            uploadFile.save();
                            continue;
                        }
                        if (lstValue.containsKey(deviceDTO.getPartNumber())) {
                            // Update test case if it exist in DB
                            final boolean isSuccess = issueHelperService.updateIssue(applicationUser, lstValue.get(
                                    deviceDTO.getPartNumber()).getId(), customFieldValueDTOMap);
                            if (isSuccess) {
                                testCaseNoSet.add(formatter.formatCellValue(currentRow.getCell(3)));
                            } else {
                                deviceDTO.setErrorDevice(ERROR_UPDATE_DEVICE);
                                lstTestCaseErr.add(deviceDTO);
                            }
                        } else {
                            //Create new test case if it not exist in DBs
                            IssueMessageDTO issue = issueHelperService.createIssue(applicationUser, project.getId(),
                                                                                   ISSUE_TYPE_AUTOMOTIVE_DEVICE,
                                                                                   formatter.formatCellValue(
                                                                                           currentRow.getCell(3)),
                                                                                   customFieldValueDTOMap);
                            if (issue.getId() != null) {
                                testCaseNoSet.add(currentRow.getCell(0).toString());
                            } else {
                                deviceDTO.setErrorDevice(ERROR_CREATE_ISSUE + issue.getMessage());
                                lstTestCaseErr.add(deviceDTO);
                            }
                        }
                        //uploadFile.setProgress(i + "/" + sumOfRow);
                        uploadFile.save();
                    }
                    break;
                case ISSUE_TYPE_PERIPHERAL_DEVICE:
                    validatePeriDevice(sheet.getRow(0));
                    CustomField customFieldP;
                    Map<String, Issue> lstValueP = new HashMap<>();
                    for (Issue issue : issues) {
                        customFieldP = customFieldManager.getCustomFieldObjectByName(CUSTOM_FIELD_CMC_CODE);
                        if (customFieldP.getValue(issue) == null) {
                            continue;
                        }
                        lstValueP.put(customFieldP.getValue(issue).toString(), issue);
                    }
                    for (int i = 1; i <= sumOfRow; i++) {
                        final Row currentRow = sheet.getRow(i);
                        deviceDTO = setValueDevicePeriDTO(currentRow, formatter);
                        final List<CustomFieldValueDTO> customFieldValueDTOMap = Utilities.getFieldNamesAndValues(
                                deviceDTO, false, ImmutableMap.<String, String>builder().put("Cmc Code", "CMC Code")
                                                                                        .put("Os", "OS")
                                                                                        .put("Os Version", "OS Version")
                                                                                        .put("Hfp Version",
                                                                                             "HFP Version")
                                                                                        .put("Avrcp Version",
                                                                                             "AVRCP Version")
                                                                                        .put("A 2dp Version",
                                                                                             "A2DP Version")
                                                                                        .put("Map Version",
                                                                                             "MAP Version")
                                                                                        .put("Pbap Version",
                                                                                             "PBAP Version")
                                                                                        .put("At Command", "AT Command")
                                                                                        .build());
                        if (StringUtils.isEmpty(currentRow.getCell(1).toString())) {
                            builder.append(ERROR_CMC_CODE);
                        }
                        if (StringUtils.isEmpty(currentRow.getCell(3).toString())) {
                            builder.append(ERROR_DEVICE_NAME);
                        }
                        if (StringUtils.isEmpty(currentRow.getCell(4).toString())) {
                            builder.append(ERROR_OS);
                        }
                        if (StringUtils.isEmpty(currentRow.getCell(5).toString())) {
                            builder.append(ERROR_OS_VERSION);
                        }
                        if (builder.length() > 0) {
                            deviceDTO.setErrorDevice(builder.toString());
                            deviceDTO.setCurrentIndex(i + 1);
                            lstTestCaseErr.add(deviceDTO);
                            builder.delete(0, builder.length());
                            //uploadFile.setProgress(i + "/" + sumOfRow);
                            uploadFile.save();
                            continue;
                        }
                        if (lstValueP.containsKey(deviceDTO.getCmcCode())) {
                            // Update test case if it exist in DB
                            final boolean isSuccess = issueHelperService.updateIssue(applicationUser, lstValueP.get(
                                    deviceDTO.getCmcCode()).getId(), customFieldValueDTOMap);
                            if (isSuccess) {
                                testCaseNoSet.add(currentRow.getCell(3).toString());
                            } else {
                                deviceDTO.setErrorDevice(ERROR_UPDATE_DEVICE);
                                lstTestCaseErr.add(deviceDTO);
                            }
                        } else {
                            //Create new test case if it not exist in DB
                            IssueMessageDTO issue = issueHelperService.createIssue(applicationUser, project.getId(),
                                                                                   ISSUE_TYPE_PERIPHERAL_DEVICE,
                                                                                   formatter.formatCellValue(
                                                                                           currentRow.getCell(1)),
                                                                                   customFieldValueDTOMap);
                            if (issue.getId() != null) {
                                testCaseNoSet.add(currentRow.getCell(0).toString());
                            } else {
                                deviceDTO.setErrorDevice(ERROR_CREATE_ISSUE + issue.getMessage());
                                lstTestCaseErr.add(deviceDTO);
                            }
                        }
                        //uploadFile.setProgress(i + "/" + sumOfRow);
                        uploadFile.save();
                    }
                    break;
                default:
                    break;

            }
        } else if (importType.equals(Constants.IMPORT_TYPE_UPDATE_ASSIGNEE)) {
            AssigneeDeviceDTO assigneeDevice;
            ApplicationUser loggedInUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
            Set<ApplicationUser> users = this.getUserByLoggedInUserAndProject(loggedInUser, project);
            for (int i = 1; i <= sumOfRow; i++) {
                assigneeDevice = this.updateAssignDevice(sheet.getRow(i), issues, users);
                if (assigneeDevice != null) {
                    lstTestCaseErr.add(assigneeDevice);
                }
            }
        }
        workbook.close();
        return lstTestCaseErr;
    }

    private void createRowTestCaseErr(final List<Object> lst, final Workbook workbook,
                                      final CreationHelper createHelper, final String deviceType) throws IOException {
        int rownum = 1;
        final Sheet sheet = workbook.getSheetAt(0);
        final CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        FileUtils.removeRow(sheet, 1);
        final List<DeviceDTO> testCaseObjects = Helper.convertObj(lst, DeviceDTO.class);
        switch (deviceType) {
            case ISSUE_TYPE_AUTOMOTIVE_DEVICE:
                for (final DeviceDTO deviceDTO : testCaseObjects) {
                    final Row row = sheet.createRow(rownum++);
                    int cellnum = 0;
                    int currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getSumary());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getCmcCode());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getPartNumberJapaneseName());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getPartNumber());
                    if (StringUtils.containsIgnoreCase(deviceDTO.getErrorDevice(), ERROR_PART_NUMBER)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getPartNumberForInvoice());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getPartNumberForInvoiceGroup());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getNote());
                    currentCellNum = cellnum;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getErrorDevice());
                }
                break;
            case ISSUE_TYPE_PERIPHERAL_DEVICE:
                final CellStyle styleDate = workbook.createCellStyle();
                styleDate.setDataFormat(createHelper.createDataFormat().getFormat(FORMAT_DATE_EXPORT));
                for (DeviceDTO deviceDTO : testCaseObjects) {
                    final Row row = sheet.createRow(rownum++);
                    int cellnum = 0;
                    int currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getSumary());

                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getCmcCode());
                    if (StringUtils.containsIgnoreCase(deviceDTO.getErrorDevice(), ERROR_CMC_CODE)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getPrdcvCode());

                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getDeviceName());
                    if (StringUtils.containsIgnoreCase(deviceDTO.getErrorDevice(), ERROR_DEVICE_NAME)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }

                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getOs());
                    if (StringUtils.containsIgnoreCase(deviceDTO.getErrorDevice(), ERROR_OS)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }

                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getOsVersion());
                    if (StringUtils.containsIgnoreCase(deviceDTO.getErrorDevice(), ERROR_OS_VERSION)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getWifiMacAddress());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getBluetoothMacAddress());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getBluetoothVersion());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getHfpVersion());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getAvrcpVersion());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getA2dpVersion());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getMapVersion());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getPbapVersion());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getAtCommand());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getMirrorLink());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getCarPlay());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getAndroidAuto());
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getNote());
                    currentCellNum = cellnum;
                    row.createCell(currentCellNum).setCellValue(deviceDTO.getErrorDevice());
                }
                break;
            case IMPORT_TYPE_UPDATE_ASSIGNEE:
                this.writeInvalidAssignee(lst, sheet, style, workbook);
                break;
            default:
                break;
        }
    }

    private DeviceDTO setValueDeviceAutoDTO(final Row currentRow, final DataFormatter formatter) {
        final DeviceDTO deviceDTO = new DeviceDTO();
        int cellNum = 0;
        deviceDTO.setSumary(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setCmcCode(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setPartNumberJapaneseName(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setPartNumber(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setPartNumberForInvoice(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setPartNumberForInvoiceGroup(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setNote(formatter.formatCellValue(currentRow.getCell(cellNum)));
        return deviceDTO;
    }

    private DeviceDTO setValueDevicePeriDTO(final Row currentRow, final DataFormatter formatter) {
        final DeviceDTO deviceDTO = new DeviceDTO();
        int cellNum = 0;
        deviceDTO.setSumary(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setCmcCode(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setPrdcvCode(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setDeviceName(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setOs(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setOsVersion(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setWifiMacAddress(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setBluetoothMacAddress(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setBluetoothVersion(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setHfpVersion(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setAvrcpVersion(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setA2dpVersion(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setMapVersion(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setPbapVersion(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setAtCommand(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setMirrorLink(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setCarPlay(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setAndroidAuto(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        deviceDTO.setNote(formatter.formatCellValue(currentRow.getCell(cellNum)));
        return deviceDTO;
    }

    /**
     * Validate field in row of imported file
     *
     * @param row    A row of excel file
     * @param issues List of issue of project
     * @param users  Set of user what can assign
     * @return StringBuilder
     */
    private AssigneeDeviceDTO updateAssignDevice(Row row, List<Issue> issues, Set<ApplicationUser> users) {
        final DataFormatter formatter = new DataFormatter();
        CustomField customField = ComponentAccessor.getCustomFieldManager()
                                                   .getCustomFieldObjectByName(Constants.PART_NUMBER);
        StringBuilder builder = new StringBuilder();
        boolean check = false;
        Issue assignIssue = null;
        for (Issue issue : issues) {
            if (formatter.formatCellValue(row.getCell(0)).equals(customField.getValue(issue))) {
                check = true;
                assignIssue = issue;
                break;
            }
        }
        if (!check) {
            builder.append(Constants.DEVICE_PART_NUMBER_IS_NOT_EXIST);
            builder.append(System.getProperty(Constants.BUIDER_LINE_SPERATOR));
        }
        ApplicationUser user = ComponentAccessor.getUserManager()
                                                .getUserByName(formatter.formatCellValue(row.getCell(1)));
        if (user == null) {
            builder.append(Constants.USER_IS_NOT_EXIST_IN_SYSTEM);
        } else if (!users.contains(user)) {
            builder.append(Constants.USER_IS_NOT_EXIST_IN_PROJECT);
        }
        AssigneeDeviceDTO assigneeDevice = new AssigneeDeviceDTO();
        if (check && users.contains(user)) {
            MutableIssue mutableIssue = ComponentAccessor.getIssueManager().getIssueByCurrentKey(assignIssue.getKey());
            mutableIssue.setAssignee(user);
            IssueManager issueManager = ComponentAccessor.getIssueManager();
            issueManager.updateIssue(user, mutableIssue, EventDispatchOption.ISSUE_ASSIGNED, true);
            assigneeDevice = null;
        } else {
            assigneeDevice.setPartNumber(formatter.formatCellValue(row.getCell(0)));
            assigneeDevice.setAssignee(formatter.formatCellValue(row.getCell(1)));
            assigneeDevice.setMessage(builder.toString());
        }
        return assigneeDevice;
    }

    /**
     * Get set of user which logged in user can assign for device issue
     *
     * @param loggedInUser Application User who is logging in system
     * @param project      Project which user is working
     * @return Set<ApplicationUser>
     */
    private Set<ApplicationUser> getUserByLoggedInUserAndProject(ApplicationUser loggedInUser, Project project) {
        Set<ApplicationUser> users = new LinkedHashSet<>();
        ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager.class);
        Collection<ProjectRole> userRoles = projectRoleManager.getProjectRoles(loggedInUser, project);
        if (userRoles.contains(projectRoleManager.getProjectRole(Constants.PROJECT_ROLE_PM))) {
            Collection<ProjectRole> roles = projectRoleManager.getProjectRoles();
            for (ProjectRole role : roles) {
                ProjectRoleActors roleActors = projectRoleManager.getProjectRoleActors(role, project);
                users.addAll(roleActors.getApplicationUsers());
            }
        } else if (userRoles.contains(projectRoleManager.getProjectRole(Constants.PROJECT_ROLE_QC_LEAD))) {
            users.add(loggedInUser);
            users.addAll(teamsMembersService.findTeamMembers(project.getId(), loggedInUser.getId()));
        }

        return users;
    }

    /**
     * Write invalid object to excel file
     *
     * @param object List of invalid object
     * @param sheet  sheet of workbook
     */
    private void writeInvalidAssignee(List<Object> object, Sheet sheet, CellStyle style, Workbook workbook) {
        int rowNum = 1;
        List<AssigneeDeviceDTO> assignees = Helper.convertObj(object, AssigneeDeviceDTO.class);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        for (AssigneeDeviceDTO assignee : assignees) {
            final Row row = sheet.createRow(rowNum++);
            int cellColumn = 0;
            int column = cellColumn++;
            row.createCell(column).setCellValue(assignee.getPartNumber());
            if (StringUtils.containsIgnoreCase(assignee.getMessage(), Constants.DEVICE_PART_NUMBER_IS_NOT_EXIST)) {
                row.getCell(column).setCellStyle(style);
            }
            column = cellColumn++;
            row.createCell(column).setCellValue(assignee.getAssignee());
            if (StringUtils.containsIgnoreCase(assignee.getMessage(), Constants.USER_IS_NOT_EXIST_IN_SYSTEM) ||
                StringUtils.containsIgnoreCase(assignee.getMessage(), Constants.USER_IS_NOT_EXIST_IN_PROJECT)) {
                row.getCell(column).setCellStyle(style);
            }
            row.createCell(cellColumn).setCellValue(assignee.getMessage());
            row.getCell(cellColumn).setCellStyle(cellStyle);
        }
    }
}
