package com.cmcglobal.plugins.service.Impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cmcglobal.plugins.dto.CustomFieldValueDTO;
import com.cmcglobal.plugins.dto.IssueMessageDTO;
import com.cmcglobal.plugins.dto.ResultMessage;
import com.cmcglobal.plugins.dto.TestCaseDTO;
import com.cmcglobal.plugins.dto.TestPlanDTO;
import com.cmcglobal.plugins.dto.TestResultDto;
import com.cmcglobal.plugins.dto.TotalRecordsDTO;
import com.cmcglobal.plugins.dto.UploadFileDTO;
import com.cmcglobal.plugins.entity.TestCase;
import com.cmcglobal.plugins.entity.UploadFile;
import com.cmcglobal.plugins.service.IssueHelperService;
import com.cmcglobal.plugins.service.TeamsMembersService;
import com.cmcglobal.plugins.service.TestCaseService;
import com.cmcglobal.plugins.service.UploadFileService;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.FileUtils;
import com.cmcglobal.plugins.utils.Helper;
import com.cmcglobal.plugins.utils.ImportStatus;
import com.cmcglobal.plugins.utils.Utilities;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.cmcglobal.plugins.utils.Constants.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.poi.ss.usermodel.CellType.FORMULA;

@Scanned
@Named
public class TestCaseServiceImpl implements TestCaseService {

    @ComponentImport
    private final ActiveObjects ao;

    private IssueHelperService issueHelperService;

    @Autowired
    public void setIssueHelperService(final IssueHelperService issueHelperService) {
        this.issueHelperService = issueHelperService;
    }

    @ComponentImport
    private final ProjectManager projectManager;

    private final UploadFileService uploadFileService;

    private final TeamsMembersService teamsMembersService;

    private static final String          MESSAGE_KEY      = "message";
    private static final Logger          log              = LoggerFactory.getLogger(TestCaseServiceImpl.class);
    private              int             totalRowsSuccess = 0;
    private              int             totalRows        = 0;
    private              ExecutorService executor         = null;
    private              AtomicBoolean   isAlive          = new AtomicBoolean();

    @Inject
    public TestCaseServiceImpl(final ActiveObjects ao, final ProjectManager projectManager,
                               final UploadFileService uploadFileService,
                               final TeamsMembersService teamsMembersService) {
        this.ao = checkNotNull(ao);
        this.projectManager = projectManager;
        this.uploadFileService = uploadFileService;
        this.teamsMembersService = teamsMembersService;
    }

    @Override
    public TestCase create(final long issueId) {
        final TestCase testCase = ao.create(TestCase.class, new DBParam(ISSUE_ID_KEY, issueId));
        testCase.setCreatorCompany(CREATE_COMPANY);
        testCase.setCreatorPerson(CREATE_PERSON);
        testCase.save();
        return testCase;
    }

    @Override
    public void update(final TestCase testCase) {
        testCase.setUpdateDate(new Date());
        testCase.save();
    }

    @Override
    public List<TestCase> findAll() {
        return newArrayList(ao.find(TestCase.class));
    }

    @Override
    public TestCase detail(final long testcaseId) {
        final List<TestCase> testCaseList = newArrayList(
                ao.find(TestCase.class, Query.select().where("ID = ?", testcaseId)));
        return testCaseList.isEmpty() ? null : testCaseList.get(0);
    }

    @Override
    public TestCase findByIssueId(final long issueId) {
        final TestCase[] testCaseList = ao.find(TestCase.class, Query.select().where(QUERY_ISSUE_ID, issueId));
        return testCaseList.length == 0 ? null : testCaseList[0];
    }

    @Override
    public ResultMessage imports(final HttpServletRequest request, final HttpServletResponse response) {
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
            final int maxRows = Integer.parseInt(i18nHelper.getText("import-test-case.max-rows"));
            final UploadFile uploadFile = ao.create(UploadFile.class);
            while (iter.hasNext() && !fields.isEmpty()) {
                final FileItem item = iter.next();
                if (!item.isFormField()) {
                    pathUploadFile = FileUtils.setPathFile(Constants.ADDRESS, project.getKey(), fields.get(0),
                                                           FileUtils.getFileNameCurrent(fileName, Constants.PREFIX));
                    pathInvalidFile = FileUtils.setPathFile(Constants.ADDRESS_INVALID, project.getKey(), fields.get(0),
                                                            FileUtils.getFileNameCurrent(fileName,
                                                                                         Constants.PREFIX_INVALID));
                    final File uploadedFile = new File(pathUploadFile);
                    if (item.getSize() > MAX_REQUEST_SIZE) {
                        throw new RuntimeException(MESSAGE_ERROR_FILE_SIZE);
                    }
                    // Count rows
                    final Workbook workbook = FileUtils.getWorkbookByExtension(item, fileName);
                    final Sheet sheet = workbook.getSheetAt(0);
                    final TotalRecordsDTO totalRecordsDTO = countTotalRecords(sheet, fields.get(0));
                    final int totalRowsOfSheet = totalRecordsDTO.getTotalRecords();

                    // Check limit rows
                    if (totalRowsOfSheet < 1) {
                        throw new RuntimeException(MESSAGE_ERROR_EMPTY_ROW);
                    }
                    if (totalRowsOfSheet > 10 * maxRows) {
                        throw new RuntimeException(MESSAGE_ERROR_MAX_ROWS + (10 * maxRows));
                    }

                    // Upload file
                    item.write(uploadedFile);
                    invalidTestCase = new File(pathInvalidFile);
                    item.write(invalidTestCase);
                    // Specify file to perform import

                    if ((item.getSize() > maxFileSize || totalRowsOfSheet > maxRows) && IMPORT_TESTCASE_TYPE.equalsIgnoreCase(fields.get(0))) {
                        uploadFile.setStatus(ImportStatus.BIG_FILE_WAITING.getValue());
                        request.setAttribute(MESSAGE_KEY, MESSAGE_UPLOAD_BIG_FILE);
                    } else {
                        uploadFile.setStatus(ImportStatus.WAITING.getValue());
                        request.setAttribute(MESSAGE_KEY, MESSAGE_UPLOAD_NORMAL_FILE);
                    }
                    break;
                }
            }

            fileUploadName = new File(pathUploadFile).getName();
            //List File Up load
            final JiraAuthenticationContext jiraAuthenticationContext
                    = ComponentAccessor.getJiraAuthenticationContext();
            final ApplicationUser applicationUser = jiraAuthenticationContext.getLoggedInUser();
            uploadFile.setUploadFileName(fileUploadName);
            uploadFile.setPathFileValid(new File(pathUploadFile).getParent());
            uploadFile.setProjectId(project.getId());
            uploadFile.setType(fields.get(0));
            uploadFile.setPhase(fields.get(1));
            uploadFile.setTestCaseType(fields.get(2));
            uploadFile.setCreateDate(new Date());
            uploadFile.setCreateUser(applicationUser.getKey());
            uploadFile.setPathFileInvalid(pathInvalidFile);
            uploadFile.setLastMessage(
                    StringUtils.equalsIgnoreCase(uploadFile.getStatus(), ImportStatus.WAITING.getValue()) ?
                    MESSAGE_UPLOAD_NORMAL_FILE :
                    MESSAGE_UPLOAD_BIG_FILE);
            uploadFile.save();
            return new ResultMessage(HttpServletResponse.SC_OK, IMPORT_TESTCASE_SUCCESS, totalRowsSuccess, 1, totalRows,
                                     fileUploadName);
        } catch (final FileUploadException e) {
            final String message = MESSAGE_UPLOAD_ERROR + e.getMessage();
            log.error(message);
            uploadFileService.updateStatusFile(project.getId(), fileUploadName, ImportStatus.IMPORTED.getValue(),
                                               message);
            return new ResultMessage(HttpServletResponse.SC_BAD_REQUEST, MESSAGE_UPLOAD_ERROR);
        } catch (final IOException e) {
            final String message = ERROR_HANDLE_FILE + e.getMessage();
            log.error(message);
            uploadFileService.updateStatusFile(project.getId(), fileUploadName, ImportStatus.IMPORTED.getValue(),
                                               message);
            return new ResultMessage(HttpServletResponse.SC_BAD_REQUEST, ERROR_HANDLE_FILE);
        } catch (final InvalidFormatException e) {
            log.error(e.getMessage());
            uploadFileService.updateStatusFile(project.getId(), fileUploadName, ImportStatus.IMPORTED.getValue(),
                                               e.getMessage());
            return new ResultMessage(INVALID_FORMAT_EXCEPTION, e.getMessage());
        } catch (final Exception ex) {
            log.error(ex.getMessage());
            uploadFileService.updateStatusFile(project.getId(), fileUploadName, ImportStatus.IMPORTED.getValue(),
                                               ex.getMessage());
            return new ResultMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private TestCase setValueTestCase(final Long issueId, final TestCaseDTO testCaseDTO, final boolean isCreate,
                                      final SimpleDateFormat simpleDateFormat) {
        final TestCase testCase;
        if (isCreate) {
            testCase = ao.create(TestCase.class, new DBParam(ISSUE_ID_KEY, issueId));
        } else {
            final TestCase[] testCaseList = ao.find(TestCase.class, Query.select()
                                                                         .where(QUERY_ISSUE_ID, issueId)
                                                                         .where(QUERY_TESTCASE_NO,
                                                                                testCaseDTO.getTestcaseNo()));
            testCase = testCaseList[0];
        }
        testCase.setTestcaseNo(testCaseDTO.getTestcaseNo());
        testCase.setTestcaseId(testCaseDTO.getTestcaseId());
        testCase.setTestCategory(testCaseDTO.getTestCategory());
        testCase.setTestViewPoint(testCaseDTO.getTestViewPoint());
        testCase.setEvaluationTarget1(testCaseDTO.getEvaluationTarget1());
        testCase.setEvaluationTarget2(testCaseDTO.getEvaluationTarget2());
        testCase.setEvaluationTarget3(testCaseDTO.getEvaluationTarget3());
        testCase.setEvaluationTarget4(testCaseDTO.getEvaluationTarget4());
        testCase.setEvaluationTarget5(testCaseDTO.getEvaluationTarget5());
        testCase.setEvaluationTarget6(testCaseDTO.getEvaluationTarget6());
        testCase.setTestClassification(testCaseDTO.getTestClassification());
        testCase.setAnalysisFlag1(testCaseDTO.getAnalysisFlag1());
        testCase.setAnalysisFlag2(testCaseDTO.getAnalysisFlag2());
        testCase.setPurpose(testCaseDTO.getPurpose());
        testCase.setPrecondition(testCaseDTO.getPrecondition());
        testCase.setSteps(testCaseDTO.getSteps());
        testCase.setNumberOfExecutions(testCaseDTO.getNumberOfExecutions());
        testCase.setConfirmationDisplay(testCaseDTO.getConfirmationDisplay());
        testCase.setConfirmationAudio(testCaseDTO.getConfirmationAudio());
        testCase.setRemark(testCaseDTO.getRemark());
        testCase.setPseudoEnvironment(testCaseDTO.getPseudoEnvironment());
        testCase.setSystemEnvironment(testCaseDTO.getSystemEnvironment());
        testCase.setUserEnvironment(testCaseDTO.getUserEnvironment());
        testCase.setTestData(testCaseDTO.getTestData());
        testCase.setEvaluationEnvironment(testCaseDTO.getEvaluationEnvironment());
        testCase.setEvaluationExecutionPlace(testCaseDTO.getEvaluationExecutionPlace());
        testCase.setChangeIndicator(testCaseDTO.getChangeIndicator());
        testCase.setReasonForChange(testCaseDTO.getReasonForChange());
        testCase.setSortItemNumber(testCaseDTO.getSortItemNumber());
        testCase.setReleaseVersionNumber(testCaseDTO.getReleaseVersionNumber());
        testCase.setRequestListVersion(testCaseDTO.getRequestListVersion());
        testCase.setReimplementationFlag(testCaseDTO.getReimplementationFlag());
        testCase.setPrecheckFlag(testCaseDTO.getPrecheckFlag());
        testCase.setTestCaseVersion(testCaseDTO.getTestCaseVersion());
        testCase.setIncidentId(testCaseDTO.getIncidentId());
        testCase.setGroupingNumber(testCaseDTO.getGroupingNumber());
        testCase.setTestPhase(testCaseDTO.getTestPhase());
        testCase.setReserved3(testCaseDTO.getReserved3());
        testCase.setReserved4(testCaseDTO.getReserved4());
        testCase.setReserved5(testCaseDTO.getReserved5());
        if (StringUtils.isNotEmpty(testCaseDTO.getUpdateDate())) {
            try {
                testCase.setUpdateDate(simpleDateFormat.parse(testCaseDTO.getUpdateDate()));
            } catch (final ParseException e) {
                log.error(UPDATE_DATE_MESSAGE_INVALID);
            }
        }
        testCase.setCreatorCompany(testCaseDTO.getCreatorCompany());
        testCase.setCreatorPerson(testCaseDTO.getCreatorPerson());
        testCase.setImportRequest(testCaseDTO.getImportRequest());
        testCase.setFree1(testCaseDTO.getFree1());
        testCase.setFree2(testCaseDTO.getFree2());
        testCase.setFree3(testCaseDTO.getFree3());
        testCase.setFree4(testCaseDTO.getFree4());
        testCase.setFree5(testCaseDTO.getFree5());
        testCase.setDestination(testCaseDTO.getDestination());
        testCase.setHandle(testCaseDTO.getHandle());
        testCase.setMotorModel(testCaseDTO.getMotorModel());
        testCase.setAudio(testCaseDTO.getAudio());
        testCase.setBandType(testCaseDTO.getBandType());
        testCase.setRadioDataSystem(testCaseDTO.getRadioDataSystem());
        testCase.setXmRadio(testCaseDTO.getXmRadio());
        testCase.setDigitalAudioBroadcasting(testCaseDTO.getDigitalAudioBroadcasting());
        testCase.setTrafficInformation(testCaseDTO.getTrafficInformation());
        testCase.setHdRadio(testCaseDTO.getHdRadio());
        testCase.setTv(testCaseDTO.getTv());
        testCase.setNavigationType(testCaseDTO.getNavigationType());
        testCase.setLanguage(testCaseDTO.getLanguage());
        testCase.setVoiceRecognition(testCaseDTO.getVoiceRecognition());
        testCase.setAntiTheft(testCaseDTO.getAntiTheft());
        testCase.setCameraRear(testCaseDTO.getCameraRear());
        testCase.setLanewatch(testCaseDTO.getLanewatch());
        testCase.setMultiViewCameraSystem(testCaseDTO.getMultiViewCameraSystem());
        testCase.setCameraMonitorMirrorSystem(testCaseDTO.getCameraMonitorMirrorSystem());
        testCase.setUsbJack(testCaseDTO.getUsbJack());
        testCase.setHdmiJack(testCaseDTO.getHdmiJack());
        testCase.setAuxJack(testCaseDTO.getAuxJack());
        testCase.setCarplay(testCaseDTO.getCarplay());
        testCase.setAndroidAuto(testCaseDTO.getAndroidAuto());
        testCase.setMirrorLink(testCaseDTO.getMirrorLink());
        testCase.setTelematics(testCaseDTO.getTelematics());
        testCase.setEmergencyCall(testCaseDTO.getEmergencyCall());
        testCase.setSoundSystem(testCaseDTO.getSoundSystem());
        testCase.setPlant(testCaseDTO.getPlant());
        testCase.setVariation30(testCaseDTO.getVariation30());
        testCase.setPhase(testCaseDTO.getPhase());
        testCase.setTestcaseType(testCaseDTO.getTestcaseType());

        return testCase;
    }

    private TestCaseDTO setValueTestCaseDTO(final Row currentRow, final String phase, final String testCaseType,
                                            final SimpleDateFormat simpleDateFormat,
                                            final FormulaEvaluator formulaEvaluator) {
        int cellNum = 0;
        final TestCaseDTO testCaseDTO = new TestCaseDTO();
        final StringBuilder stringBuilderErr = new StringBuilder();
        try {
            final DataFormatter formatter = new DataFormatter();
            int cellNumTestcaseNo = cellNum++;
            testCaseDTO.setTestcaseNoFomula(formatter.formatCellValue(currentRow.getCell(cellNumTestcaseNo)));
            if (currentRow.getCell(cellNumTestcaseNo) != null) {
                try {
                    testCaseDTO.setTestcaseNo(currentRow.getCell(cellNumTestcaseNo).getStringCellValue());
                } catch (final Exception e) {
                    stringBuilderErr.append(e.getMessage() + "\n");
                }
            }
            testCaseDTO.setTestcaseId(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setTestCategory(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setTestViewPoint(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setEvaluationTarget1(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setEvaluationTarget2(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setEvaluationTarget3(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setEvaluationTarget4(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setEvaluationTarget5(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setEvaluationTarget6(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setTestClassification(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setAnalysisFlag1(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setAnalysisFlag2(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setPurpose(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setPrecondition(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setSteps(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setNumberOfExecutions(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setConfirmationDisplay(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setConfirmationAudio(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setRemark(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setPseudoEnvironment(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setSystemEnvironment(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setUserEnvironment(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setTestData(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setEvaluationEnvironment(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setEvaluationExecutionPlace(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setChangeIndicator(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setReasonForChange(formatter.formatCellValue(currentRow.getCell(cellNum++)));

            cellNumTestcaseNo = cellNum++;
            testCaseDTO.setSortItemNumberFomula(formatter.formatCellValue(currentRow.getCell(cellNumTestcaseNo)));
            if (currentRow.getCell(cellNumTestcaseNo) != null) {
                testCaseDTO.setSortItemNumber(
                        formatAsString(formulaEvaluator.evaluate(currentRow.getCell(cellNumTestcaseNo))));
            }

            testCaseDTO.setReleaseVersionNumber(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setRequestListVersion(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setReimplementationFlag(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setPrecheckFlag(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setTestCaseVersion(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setIncidentId(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setGroupingNumber(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setTestPhase(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setReserved3(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setReserved4(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setReserved5(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            cellNumTestcaseNo = cellNum++;
            try {
                testCaseDTO.setUpdateDate(
                        simpleDateFormat.format(currentRow.getCell(cellNumTestcaseNo).getDateCellValue()));
            } catch (final Exception e) {
                testCaseDTO.setUpdateDate(formatter.formatCellValue(currentRow.getCell(cellNumTestcaseNo)));
            }
            testCaseDTO.setCreatorCompany(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setCreatorPerson(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setImportRequest(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setFree1(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setFree2(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setFree3(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setFree4(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setFree5(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setDestination(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setHandle(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setMotorModel(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setAudio(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setBandType(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setRadioDataSystem(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setXmRadio(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setDigitalAudioBroadcasting(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setTrafficInformation(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setHdRadio(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setTv(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setNavigationType(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setLanguage(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setVoiceRecognition(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setAntiTheft(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setCameraRear(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setLanewatch(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setMultiViewCameraSystem(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setCameraMonitorMirrorSystem(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setUsbJack(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setHdmiJack(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setAuxJack(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setCarplay(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setAndroidAuto(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setMirrorLink(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setTelematics(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setEmergencyCall(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setSoundSystem(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setPlant(formatter.formatCellValue(currentRow.getCell(cellNum++)));
            testCaseDTO.setVariation30(formatter.formatCellValue(currentRow.getCell(cellNum)));

            testCaseDTO.setPhase(phase);
            testCaseDTO.setTestcaseType(testCaseType);
        } catch (final Exception e) {
            stringBuilderErr.append("Error read cell: " + cellNum + ", " + e.getMessage());
            testCaseDTO.setErrorTestCase(stringBuilderErr.toString());
            return testCaseDTO;
        }
        if (stringBuilderErr.length() > 0) {
            testCaseDTO.setErrorTestCase(stringBuilderErr.toString());
        }
        return testCaseDTO;
    }

    private TestPlanDTO setValueTestPlanDTO(final Row currentRow, final String phase,
                                            final SimpleDateFormat simpleDateFormat) {
        final TestPlanDTO testPlanDTO = new TestPlanDTO();
        final DataFormatter formatter = new DataFormatter();
        testPlanDTO.setTestCaseNo(formatter.formatCellValue(currentRow.getCell(0)));
        testPlanDTO.setAssign(formatter.formatCellValue(currentRow.getCell(1)));
        try {
            testPlanDTO.setStartDate(simpleDateFormat.format(currentRow.getCell(2).getDateCellValue()));
        } catch (final Exception e) {
            testPlanDTO.setStartDate(formatter.formatCellValue(currentRow.getCell(2)));
        }
        try {
            testPlanDTO.setEndDate(simpleDateFormat.format(currentRow.getCell(3).getDateCellValue()));
        } catch (final Exception e) {
            testPlanDTO.setEndDate(formatter.formatCellValue(currentRow.getCell(3)));
        }
        testPlanDTO.setPhase(phase);
        return testPlanDTO;
    }

    @Override
    public void initThread(final String importType, final String fileUploadPath, final String fileName,
                           final Project project, final String phase, final String testCaseType,
                           final ApplicationUser user, final ImportStatus importStatus) throws Exception {
        final List<Object> lstTestCaseErr = new ArrayList<>();
        final Workbook workbook = FileUtils.getWorkbookByExtension(fileUploadPath, fileName);
        final FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        // get current sheet to get data
        final Sheet sheet = workbook.getSheetAt(0);
        final TotalRecordsDTO totalRecordsDTO = countTotalRecords(sheet, importType);
        final int totalRowsOfSheet;
        final int headerIndex;
        totalRowsOfSheet = totalRecordsDTO.getTotalRecords();
        headerIndex = totalRecordsDTO.getHeaderIndex();
        final int sumOfRow = sheet.getLastRowNum() - headerIndex + 1;

        final IssueType[] issueTypes = new IssueType[5];
        project.getIssueTypes().toArray(issueTypes);
        final IssueType issueType = issueTypes[0];

        final Map<String, TestCase> testCaseMap = getTestCaseNo(phase);

        //init thread
        final int numberThreadInit = sumOfRow > Constants.NUMBER_THREAD ? Constants.NUMBER_THREAD : sumOfRow;
        final int numberRowOfOneThread = sumOfRow / numberThreadInit;
        List<Future<List<Object>>> futures = null;
        if (this.executor == null || this.executor.isShutdown()) {
            this.executor = Executors.newFixedThreadPool(Constants.NUMBER_THREAD);
        }

        if (this.executor == null) {
            throw new RuntimeException("Error executor must be not null");
        }

        if (this.executor != null && !this.isAlive.get()) {
            log.info("Init thread process file: " + fileName);
            this.isAlive.set(true);
            try {
                uploadFileService.updateStatusFile(project.getId(), fileName, ImportStatus.IMPORTING.getValue(), "");
                Set<Callable<List<Object>>> callables = new HashSet<>();
                for (int i = 0; i < numberThreadInit; i++) {
                    final int begin = headerIndex + (numberRowOfOneThread + 1) * i;
                    final int end = (i + 1) == numberThreadInit ?
                                    (sumOfRow + headerIndex) :
                                    (begin + numberRowOfOneThread);
                    callables.add(() -> readFile(importType, project, phase, testCaseType, sheet, user, issueType, testCaseMap, begin, end, formulaEvaluator));

                }
                futures = this.executor.invokeAll(callables);
                for (Future future : futures) {
                    if (future.get() != null) {
                        lstTestCaseErr.addAll((Collection<?>) future.get());
                    }
                }
            } catch (final InterruptedException e) {
                workbook.close();
                this.isAlive.set(false);
                log.error("Thread invoke all error: " + e.getMessage());
                uploadFileService.updateStatusFile(project.getId(), fileName, ImportStatus.ERROR.getValue(),
                                                   "Thread invoke all error: " + e.getMessage());
                throw new RuntimeException("Thread invoke all error");
            } catch (final Exception e) {
                workbook.close();
                this.isAlive.set(false);
                log.error("Thread processing file error: " + e.getMessage());
                uploadFileService.updateStatusFile(project.getId(), fileName, ImportStatus.ERROR.getValue(),
                                                   "Thread processing file error: " + e.getMessage());
                throw new RuntimeException("Thread processing file error");
            }
        }

        if (futures != null && isAllThreadDone(futures)) {
            log.info("All thread process done, file: " + fileName);
            this.isAlive.set(false);
            workbook.close();
            final UploadFile uploadFile = uploadFileService.findByProjectIdAndFileName(project.getId(), fileName);
            final File invalidTestCase = new File(uploadFile.getPathFileInvalid());
            if (!lstTestCaseErr.isEmpty()) {
                final Workbook workbookFileInvalid = FileUtils.getWorkbookByExtension(invalidTestCase.getAbsolutePath(),
                                                                                      fileName);
                final CreationHelper createHelper = workbookFileInvalid.getCreationHelper();
                createRowTestCaseErr(lstTestCaseErr, workbookFileInvalid, createHelper, importType, headerIndex);
                final FileOutputStream fileOutputStream = new FileOutputStream(invalidTestCase);
                workbookFileInvalid.write(fileOutputStream);
                fileOutputStream.close();
                workbookFileInvalid.close();
                if (uploadFile != null) {
                    final int listErrSize = lstTestCaseErr.size();
                    uploadFile.setUploadFileNameInvalid(invalidTestCase.getName());
                    uploadFile.setLastMessage(
                            "Import failed, success/total : " + (totalRowsOfSheet - listErrSize) + "/" +
                            totalRowsOfSheet + ", failed/total : " + listErrSize + "/" + totalRowsOfSheet);
                }
            }
            if (lstTestCaseErr.isEmpty() && invalidTestCase != null) {
                Files.deleteIfExists(invalidTestCase.toPath());
                if (uploadFile != null) {
                    uploadFile.setLastMessage(
                            "Import success, success/total: " + totalRowsOfSheet + "/" + totalRowsOfSheet);
                }
            }
            if (uploadFile != null) {
                uploadFile.setStatus(ImportStatus.IMPORTED.getValue());
                uploadFile.save();
            } else {
                log.error("Can not found upload file with project id: " + project.getId() + ", file name: " + fileName);
            }
        }

        if (ImportStatus.BIG_FILE_WAITING.getValue().equalsIgnoreCase(importStatus.getValue())) {
            executor.shutdown();
        } else {
            // Continue check import files normal (less than 2000 testcases)
            // Get list file normal is waiting
            final List<UploadFileDTO> lstUploadFileDTO = uploadFileService.findAllByProjectId(project.getId(), importType)
                                                                          .stream()
                                                                          .filter(x -> ImportStatus.WAITING.getValue()
                                                                                                           .equals(x.getStatus()))
                                                                          .collect(Collectors.toList());
            if (lstUploadFileDTO.isEmpty()) {
                executor.shutdown();
            } else {
                final UploadFileDTO uploadFileDTO = lstUploadFileDTO.get(0);
                initThread(uploadFileDTO.getImportType(),
                           uploadFileDTO.getPathFileValid() + File.separator + uploadFileDTO.getUploadFileName(),
                           uploadFileDTO.getUploadFileName(),
                           ComponentAccessor.getProjectManager().getProjectObj(uploadFileDTO.getProjectId()),
                           uploadFileDTO.getPhase(), uploadFileDTO.getTestCaseType(), user, ImportStatus.WAITING);
            }
        }
    }

    /**
     * @param project
     * @throws IOException
     */
    public List<Object> readFile(final String importType, final Project project, final String phase,
                                 final String testCaseType, final Sheet sheet, final ApplicationUser applicationUser,
                                 final IssueType issueType, final Map<String, TestCase> testCaseMap,
                                 final int rowBeginReadOfThread, final int rowEndReadOfThread,
                                 final FormulaEvaluator formulaEvaluator) throws Exception {
        this.isAlive.set(true);
        final List<Object> lstTestCaseErr = new ArrayList<>();
        TestCaseDTO testCaseDTO;
        TestResultDto testResultDto;
        Issue issueSetOfDevice = null;
        final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();

        final JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        jiraAuthenticationContext.setLoggedInUser(applicationUser);
        StringBuilder builder = new StringBuilder();

        final Set<String> testCaseNoSet = new HashSet<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_DATE_ISSUE, Locale.ENGLISH);
        if(simpleDateFormat!=null) {
            simpleDateFormat.setLenient(false);
        }

        switch (importType) {
            case Constants.IMPORT_TESTCASE_TYPE:
                final DataFormatter formatter = new DataFormatter();
                for (int i = rowBeginReadOfThread; i <= rowEndReadOfThread; i++) {
                    // get current row
                    final Row currentRow = sheet.getRow(i);
                    // Ignore row empty or column 1 & 28 is empty
                    if (currentRow == null ||
                        !(StringUtils.isNotBlank(formatter.formatCellValue(currentRow.getCell(1))) &&
                          StringUtils.isNotBlank(formatter.formatCellValue(currentRow.getCell(13))))) {
                        continue;
                    }
                    testCaseDTO = setValueTestCaseDTO(currentRow, phase, testCaseType, simpleDateFormat,
                                                      formulaEvaluator);
                    testCaseDTO.setCurrentIndex(i + 1);
                    if (testCaseDTO.getErrorTestCase() != null) {
                        final String message = "Error read data from row number: " + testCaseDTO.getCurrentIndex() +
                                               ". " + testCaseDTO.getErrorTestCase();
                        log.error(message);
                        testCaseDTO.setErrorTestCase(message);
                        lstTestCaseErr.add(testCaseDTO);
                        continue;
                    }
                    final List<CustomFieldValueDTO> customFieldValueDTOMap = Utilities.getFieldNamesAndValues(
                            testCaseDTO, false, null);

                    // Validate testcaseNo
                    final String testcaseNo = testCaseDTO.getTestcaseNo();
                    final String messageValidTestcaseNo = Utilities.isValidData(testCaseNoSet, testcaseNo);
                    if (messageValidTestcaseNo != null) {
                        builder.append(messageValidTestcaseNo);
                    }
                    // Validate Testcase Id
                    try {
                        Long.parseLong(testCaseDTO.getTestcaseId());
                    } catch (final Exception e) {
                        builder.append(TESTCASE_ID_MESSAGE_INVALID);
                    }
                    // Validate Update Date
                    try {
                        if (StringUtils.isNotEmpty(testCaseDTO.getUpdateDate())) {
                            simpleDateFormat.parse(testCaseDTO.getUpdateDate());
                        }
                    } catch (final ParseException e) {
                        builder.append(UPDATE_DATE_MESSAGE_INVALID);
                    }

                    if (builder.length() > 0) {
                        testCaseDTO.setErrorTestCase(builder.toString());
                        lstTestCaseErr.add(testCaseDTO);
                        builder.delete(0, builder.length());
                        /*uploadFile.setProgress(i + "/" + rowEndReadOfThread);
                        uploadFile.save();*/
                        continue;
                    }
                    if (testCaseMap.containsKey(testcaseNo)) {
                        // Update test case if it exist in DB
                        final TestCase testCaseDetail = setValueTestCase(testCaseMap.get(testcaseNo).getIssueId(),
                                                                         testCaseDTO, false, simpleDateFormat);
                        final IssueMessageDTO issueMessageDTO = issueHelperService.updateIssue(applicationUser,
                                                                                               testCaseDetail,
                                                                                               customFieldValueDTOMap);
                        if (issueMessageDTO.getId() != null) {
                            testCaseNoSet.add(testcaseNo);
                        } else {
                            testCaseDTO.setErrorTestCase(ERROR_UPDATE_ISSUE + issueMessageDTO.getMessage());
                            lstTestCaseErr.add(testCaseDTO);
                        }
                    } else {
                        // Create new test case if it not exist in DB
                        final IssueMessageDTO issueMessageDTO = issueHelperService.createIssue(applicationUser,
                                                                                               project.getId(),
                                                                                               issueType.getName(),
                                                                                               testcaseNo,
                                                                                               customFieldValueDTOMap);
                        if (issueMessageDTO.getId() != null) {
                            testCaseNoSet.add(testcaseNo);
                        } else {
                            testCaseDTO.setErrorTestCase(ERROR_CREATE_ISSUE + issueMessageDTO.getMessage());
                            lstTestCaseErr.add(testCaseDTO);
                        }
                    }
                }
                break;

            case Constants.IMPORT_TESTPLAN_TYPE:
                TestPlanDTO testPlanDTO = null;
                final Map<String, ApplicationUser> applicationUserMap = teamsMembersService.getUserByProjectId(
                        project.getId());
                simpleDateFormat = new SimpleDateFormat(FORMAT_DATE_ISSUE, Locale.ENGLISH);
                for (int i = rowBeginReadOfThread; i <= rowEndReadOfThread; i++) {
                    final Row currentRow = sheet.getRow(i);
                    if (currentRow == null) {
                        continue;
                    }
                    testPlanDTO = setValueTestPlanDTO(currentRow, phase, simpleDateFormat);
                    if (StringUtils.isAllBlank(testPlanDTO.getTestCaseNo(), testPlanDTO.getAssign(),
                                               testPlanDTO.getStartDate(), testPlanDTO.getEndDate())) {
                        continue;
                    }
                    builder = validateTestPlan(testPlanDTO, testCaseMap, testCaseNoSet, applicationUserMap);

                    if (!testPlanDTO.getStartDate().equals("")) {
                        try {
                            simpleDateFormat.parse(testPlanDTO.getStartDate());
                        } catch (final Exception e) {
                            builder.append(START_DATE_INVALID);
                            builder.append(BUIDER_LINE_SPERATOR);
                        }
                    }
                    if (!testPlanDTO.getEndDate().equals("")) {
                        try {
                            simpleDateFormat.parse(testPlanDTO.getEndDate());
                        } catch (final Exception e) {
                            builder.append(END_DATE_INVALID);
                        }
                    }
                    if (builder.length() > 0) {
                        testPlanDTO.setErrTestPlan(builder.toString());
                        lstTestCaseErr.add(testPlanDTO);
                        builder.delete(0, builder.length());
                    } else {
                        final TestCase testCase = testCaseMap.get(testPlanDTO.getTestCaseNo());
                        final List<CustomFieldValueDTO> customFieldValueDTOMap = new ArrayList<>();
                        customFieldValueDTOMap.add(new CustomFieldValueDTO(Constants.CUSTOM_FIELD_TESTCASE_NO,
                                                                           testPlanDTO.getTestCaseNo()));
                        customFieldValueDTOMap.add(
                                new CustomFieldValueDTO(Constants.CUSTOM_FIELD_START_DATE, testPlanDTO.getStartDate()));
                        customFieldValueDTOMap.add(
                                new CustomFieldValueDTO(Constants.CUSTOM_FIELD_END_DATE, testPlanDTO.getEndDate()));

                        final boolean checkUpdate = issueHelperService.updateIssue(applicationUser, testCase,
                                                                                   customFieldValueDTOMap,
                                                                                   testPlanDTO.getAssign());
                        if (!checkUpdate) {
                            testPlanDTO.setErrTestPlan(ERR_UPDATE_ISSUE);
                            lstTestCaseErr.add(testPlanDTO);
                        }
                    }
                }
                break;
            case Constants.IMPORT_TESTRESULT_TYPE:
                for (int i = rowBeginReadOfThread; i <= rowEndReadOfThread; i++) {
                    MutableIssue mutableIssue = null;
                    // Get current row from imported file
                    final Row currentRow = sheet.getRow(i);
                    if (currentRow == null) {
                        continue;
                    }
                    testResultDto = setValueTestResultDto(currentRow, phase, simpleDateFormat);

                    // Ignore empty row
                    if (StringUtils.isAllBlank(testResultDto.getTestcaseNo(), testResultDto.getResult(),
                                               testResultDto.getHuId(), testResultDto.getHuIndex(),
                                               testResultDto.getExternalDeviceCode(), testResultDto.getTestDuration(),
                                               testResultDto.getTesterComments())) {
                        if (Constants.TEST_RESULT_OK.equals(testResultDto.getResult()) ||
                            StringUtils.isAllBlank(testResultDto.getTimeForQnASubmission(),
                                                   testResultDto.getTimeForDefectSubmission(),
                                                   testResultDto.getBlockingTicketId())) {
                            continue;
                        }
                    }
                    // Validate testCaseNo
                    // >> Validate blank and repeat in file
                    final String testcaseNo = testResultDto.getTestcaseNo();
                    if (StringUtils.isBlank(testcaseNo)) {
                        builder.append(Constants.ERR_INVALID_TESTCASE_NO);
                    } else {// >> Validate test case exist in DB
                        testCaseNoSet.add(testcaseNo);
                        if (!testCaseMap.containsKey(testcaseNo)) {
                            builder.append(Constants.ERR_TEST_CASE_IS_NOT_EXIST);
                        } else {// Validate test case co status = waiting to test
                            final long issueId = findByTestCaseNoAndPhase(testcaseNo, phase).getIssueId();
                            mutableIssue = ComponentAccessor.getIssueManager().getIssueObject(issueId);
                            final Status statusCur = mutableIssue.getStatusObject();
                            if (!Constants.STATUS_WAITING_TO_TEST.equals(statusCur.getName())) {
                                builder.append(Constants.ERR_TEST_CASE_STATUS_MUST_BE_WAITING_TO_TEST);
                            } else {
                                testResultDto.setIssueId(issueId);
                            }
                        }
                    }
                    // Validate result
                    if (StringUtils.isBlank(testResultDto.getResult())) {// >> Validate blank
                        builder.append(Constants.ERR_RESULT_MUST_NOT_BE_BLANK);
                    } else {// >> Validate exist in DB
                        if (!(Constants.TEST_RESULTS.contains(testResultDto.getResult())
                            || Constants.TEST_RESULTS_PN.contains(testResultDto.getResult()))) {
                            builder.append(Constants.ERR_RESULT_IS_INVALID);
                        } else {// Validation NG/Pending/OutOfScope
                            if (!Constants.TEST_RESULT_OK.equals(testResultDto.getResult())) {
                                // Validate RTC Code
                                if (StringUtils.isBlank(testResultDto.getRTCCode())) {
                                    builder.append(Constants.ERR_KEY_RTC_CODE_MUST_NOT_BE_BLANK);
                                }
                                // Validate ExternalTicketId
                                if (StringUtils.isBlank(testResultDto.getExternalTicketId())) {
                                    builder.append(Constants.ERR_EXTERNAL_TICKET_ID_MUST_NOT_BE_BLANK);
                                }
                                // Validate timeForDefectSubmission
                                /*if (StringUtils.isBlank(testResultDto.getTimeForDefectSubmission())) {
                                    builder.append(Constants.ERR_TIME_FOR_DEFECT_SUBMISSION_MUST_NOT_BE_BLANK);
                                } else {
                                    try {
                                        Float.parseFloat(testResultDto.getTimeForDefectSubmission());
                                    } catch (final NumberFormatException e) {
                                        log.info(e.getMessage());
                                        builder.append(Constants.ERR_TIME_FOR_DEFECT_SUBMISSION_MUST_BE_NUMBER);
                                    }
                                }*/

                            }
                            if (Constants.TEST_RESULT_OK.equals(testResultDto.getResult())
                            || Constants.TEST_RESULT_NG.equals(testResultDto.getResult())) {
                                // Validate EVALUATION SOFTWARE
                                if (StringUtils.isBlank(testResultDto.getEvaluationSoftware())) {
                                    builder.append(Constants.ERR_EVALUATION_SOFTWARE_MUST_NOT_BE_BLANK);
                                }
                                // Validate Evaluation Hard
                                if (StringUtils.isBlank(testResultDto.getEvaluationHard())) {
                                    builder.append(Constants.ERR_EVALUATION_HARD_MUST_NOT_BE_BLANK);
                                }
                                // Validate Peripheral Device Number
                                if (StringUtils.isBlank(testResultDto.getPeripheralDeviceNumber())) {
                                    builder.append(Constants.ERR_PERIPHERAL_DEVICE_NUMBER_MUST_NOT_BE_BLANK);
                                }

                            }
                        }
                    }
                    // Validate huId
                    if (StringUtils.isBlank(testResultDto.getHuId())) {
                        builder.append(Constants.ERR_HU_ID_MUST_NOT_BE_BLANK);
                    }
                    // Validate huIndex
                    if (StringUtils.isBlank(testResultDto.getHuIndex())) {
                        builder.append(Constants.ERR_HU_INDEX_MUST_NOT_BE_BLANK);
                    }
                    // Validate findSetOfDevice By huId & huIndex
                    if (!StringUtils.isBlank(testResultDto.getHuId()) &&
                        !StringUtils.isBlank(testResultDto.getHuIndex())) {
                        final CustomField customFieldHuDevice = customFieldManager.getCustomFieldObjectByName(
                                Constants.CUSTOM_FIELD_HU_DEVICE);
                        final CustomField customFieldHuIndex = customFieldManager.getCustomFieldObjectByName(
                                Constants.HU_INDEX);
                        final ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
                        final PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
                        final JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
                        final SearchService searchService = ComponentAccessor.getComponentOfType(SearchService.class);
                        final com.atlassian.query.Query query = jqlClauseBuilder.issueType(Constants.SET_OF_DEVICE)
                                                                                .buildQuery();
                        final SearchResults searchResults = searchService.search(user, query, pagerFilter);
                        final List<Issue> tempIssues = searchResults.getIssues();
                        boolean isExistSetOfDevice = false;
                        for (final Issue issue : tempIssues) {
                            if (issue.getCustomFieldValue(customFieldHuDevice)
                                     .toString()
                                     .equals(testResultDto.getHuId()) && issue.getCustomFieldValue(customFieldHuIndex)
                                                                              .toString()
                                                                              .equals(testResultDto.getHuIndex())) {
                                isExistSetOfDevice = true;
                                issueSetOfDevice = issue;
                                break;
                            }
                        }
                        if (!isExistSetOfDevice) {
                            builder.append(Constants.ERR_SET_OF_DEVICE_IS_NOT_EXIST);
                        }
                    }
                    // Validate ExternalDeviceCode
                    if (StringUtils.isBlank(testResultDto.getExternalDeviceCode())) {
                        builder.append(Constants.ERR_EXTERNAL_TEST_DEVICE_ID_MUST_NOT_BE_BLANK);
                    }
/*                    else {
                        try {
                            Integer.parseInt(testResultDto.getExternalDeviceCode());
                        } catch (final NumberFormatException e) {
                            log.info(e.getMessage());
                            builder.append(Constants.ERR_EXTERNAL_TEST_DEVICE_ID_MUST_BE_POSITIVE_INTEGER);
                        }
                    }*/
                    // Validate testDuration
                    if (StringUtils.isBlank(testResultDto.getTestDuration())) {
                        builder.append(Constants.ERR_TEST_DURATION_MUST_NOT_BE_BLANK);
                    } else {
                        try {
                            Float.parseFloat(testResultDto.getTestDuration());
                        } catch (final NumberFormatException e) {
                            log.info(e.getMessage());
                            builder.append(Constants.ERR_TEST_DURATION_MUST_BE_NUMBER);
                        }
                    }
                    // Validate Result Comment -> only NG/Pending
                    /*if (!Constants.TEST_RESULT_OK.equals(testResultDto.getResult()) &&
                        StringUtils.isBlank(testResultDto.getTesterComments())) {
                        builder.append(Constants.ERR_TESTER_COMMENT_MUST_NOT_BE_BLANK);
                    }*/
                    // Ghi error row vao invalid file
                    if (builder.length() > 0) {
                        testResultDto.setErrorTestResult(builder.toString());
                        testResultDto.setCurrentIndex(i + 1);
                        lstTestCaseErr.add(testResultDto);
                        builder.delete(0, builder.length());
                        continue;
                    } else {// File is valid
                        // Set HuId HuIndex
                        testResultDto.setHuIdHuIndex(
                                testResultDto.getHuId() + Constants.SRING_ONE_SPACE + testResultDto.getHuIndex());
                        // Auto fill Hu Market
                        final CustomField customFieldHuMarket = customFieldManager.getCustomFieldObjectByName(
                                Constants.CUSTOM_FIELD_TEST_HU_MARKET);
                        testResultDto.setHuMarket(
                                StringUtils.join((ArrayList) issueSetOfDevice.getCustomFieldValue(customFieldHuMarket),
                                                 Constants.STRING_TO_JOIN));
                        // Auto fill Vehicle Parameter
                        CustomField customFieldVehicleParameter = null;
                        final Collection<CustomField> customFieldVehicles
                                = customFieldManager.getCustomFieldObjectsByName(Constants.VEHICLE_ATTRIBUTE);
                        for (final CustomField customField : customFieldVehicles) {
                            if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_SELECT)) {
                                customFieldVehicleParameter = customField;
                            }
                        }
                        testResultDto.setVehicleParameter(
                                issueSetOfDevice.getCustomFieldValue(customFieldVehicleParameter).toString());
                        // Auto fill Amp Type
                        CustomField customFieldAmpType = null;
                        final Collection<CustomField> customFieldAmpTypes
                                = customFieldManager.getCustomFieldObjectsByName(Constants.AMP_TYPE_ATTRIBUTE);
                        for (final CustomField customField : customFieldAmpTypes) {
                            if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_SELECT)) {
                                customFieldAmpType = customField;
                            }
                        }
                        testResultDto.setAmpType(issueSetOfDevice.getCustomFieldValue(customFieldAmpType).toString());
                        // Auto fill Si Index
                        final CustomField customFieldSiIndex = customFieldManager.getCustomFieldObjectByName(
                                Constants.CUSTOM_FIELD_SI_INDEX);
                        testResultDto.setSiIndex(issueSetOfDevice.getCustomFieldValue(customFieldSiIndex).toString());
                        // Nếu Result = OK, set LastResult = OK, neu khong set Pending Type = Result
                        if (Constants.PENDING_TYPES.contains(testResultDto.getResult())) {
                            final CustomField customFieldPendingType = customFieldManager.getCustomFieldObjectByName(
                                    Constants.CUSTOM_FIELD_PENDING_TYPE);
                            final List<FieldConfigScheme> schemes = customFieldPendingType.getConfigurationSchemes();
                            if (!schemes.isEmpty()) {
                                final FieldConfigScheme scheme = schemes.get(0);
                                final Map configs = scheme.getConfigsByConfig();
                                if (configs != null && !configs.isEmpty()) {
                                    final FieldConfig config = (FieldConfig) configs.keySet().iterator().next();
                                    final OptionsManager optionsManager = ComponentManager.getComponentInstanceOfType(
                                            OptionsManager.class);
                                    final Options options = optionsManager.getOptions(config);
                                    for (final Option option : options) {
                                        if (option.getValue().equals(testResultDto.getResult())) {
                                            testResultDto.setPendingType(option.getOptionId().toString());
                                        }
                                    }
                                }
                            }
                        }
                        testResultDto.setLatestResult(testResultDto.getResult());
                        // Convert to update custom fields
                        final List<CustomFieldValueDTO> customFieldValueDTOMaps = Utilities.getFieldNamesAndValues(
                                testResultDto, false, null);
                        for (final CustomFieldValueDTO customFieldValueDTOMap : customFieldValueDTOMaps) {
                            if (customFieldValueDTOMap.getKey()
                                                      .equals(Constants.CUSTOM_FIELD_WRONG_TIME_FOR_QNA_SUBMISSION)) {
                                customFieldValueDTOMap.setKey(Constants.CUSTOM_FIELD_TIME_FOR_QNA_SUBMISSION);
                                break;
                            }
                        }
                        String action;
                        switch (testResultDto.getLatestResult()) {
                            case Constants.TEST_RESULT_OK:
                                action = Constants.WORKFLOW_TESTCASE_ACTION_OK;
                                break;
                            case Constants.TEST_RESULT_NG:
                                action = Constants.WORKFLOW_TESTCASE_ACTION_NG;
                                break;
                            case Constants.TEST_RESULT_PN_QA:
                            case Constants.TEST_RESULT_PN_BUG:
                            case Constants.TEST_RESULT_PN_EQUIPMENT:
                            case Constants.TEST_RESULT_PN_INTERNAL:
                            case Constants.TEST_RESULT_PN_DG:
                                action = Constants.WORKFLOW_TESTCASE_ACTION_PENDING;
                                break;

                            default:
                                action = Constants.WORKFLOW_TESTCASE_ACTION_OUT_OF_SCOPE;
                                break;
                        }
                        boolean isSuccess = issueHelperService.updateIssue(action, applicationUser,
                                                                           findByTestCaseNoAndPhase(testcaseNo, phase),
                                                                           customFieldValueDTOMaps);
                        if (!isSuccess) {
                            testResultDto.setErrorTestResult(Constants.ERROR_UPDATE_TEST_RESULT);
                            lstTestCaseErr.add(testResultDto);
                        }
                    }
                }
                break;
            default:
                break;
        }
        return lstTestCaseErr;
    }

    private StringBuilder validateTestPlan(final TestPlanDTO testPlanDTO, final Map<String, TestCase> testCaseMap,
                                           final Set<String> testCaseNoSet,
                                           final Map<String, ApplicationUser> applicationUserMap) {
        final StringBuilder builder = new StringBuilder();
        final String messageValidTestcaseNo = Utilities.isValidData(testCaseNoSet, testPlanDTO.getTestCaseNo());
        if (messageValidTestcaseNo != null) {
            builder.append(messageValidTestcaseNo);
            builder.append(System.getProperty(BUIDER_LINE_SPERATOR));
        } else if (!testCaseMap.containsKey(testPlanDTO.getTestCaseNo())) {
            builder.append(Constants.ERR_NOT_EXIST_TESTCASE_NO);
            builder.append(System.getProperty(BUIDER_LINE_SPERATOR));
        }
        if (StringUtils.isBlank(testPlanDTO.getAssign())) {
            builder.append(Constants.ERR_INVALID_ASSIGNEE);
            builder.append(System.getProperty(BUIDER_LINE_SPERATOR));
        } else if (!applicationUserMap.containsKey(testPlanDTO.getAssign())) {
            builder.append(Constants.ERR_NOT_EXITS_ASSIGNEE);
            builder.append(System.getProperty(BUIDER_LINE_SPERATOR));
        }
        return builder;
    }

    private Map<String, TestCase> getTestCaseNo(final String phaseName) {
        final Map<String, TestCase> testCaseMap = new HashMap<>();
        final List<TestCase> testCases = newArrayList(
                ao.find(TestCase.class, Query.select().where(QUERY_PHASE_NAME, phaseName)));
        for (final TestCase testCase : testCases) {
            testCaseMap.put(testCase.getTestcaseNo(), testCase);
        }
        return testCaseMap;
    }

    private void createRowTestCaseErr(final List<Object> lst, final Workbook workbook,
                                      final CreationHelper createHelper, final String importType, final int headerIndex)
            throws IOException {
        int rownum = headerIndex;
        final Sheet sheet = workbook.getSheetAt(0);
        final CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        FileUtils.removeRow(sheet, rownum);
        switch (importType) {
            case Constants.IMPORT_TESTCASE_TYPE:
                final List<TestCaseDTO> testCaseObjects = Helper.convertObj(lst, TestCaseDTO.class);
                testCaseObjects.sort((TestCaseDTO t1, TestCaseDTO t2) -> t1.getCurrentIndex() - t2.getCurrentIndex());
                for (final TestCaseDTO testCaseDTO : testCaseObjects) {
                    final Row row = sheet.createRow(rownum++);
                    int cellnum = 0;
                    int currentCellNum = cellnum++;
                    try {
                        row.createCell(currentCellNum, FORMULA)
                           .setCellFormula(testCaseDTO.getTestcaseNoFomula()
                                                      .replace(CELL_FORMULA_PATTERN_KEY + testCaseDTO.getCurrentIndex(),
                                                               CELL_FORMULA_PATTERN_KEY + rownum));
                    } catch (final Exception ex) {
                        row.createCell(currentCellNum).setCellValue(testCaseDTO.getTestcaseNo());
                    }
                    // Set style for TestcaseNo
                    if (StringUtils.containsIgnoreCase(testCaseDTO.getErrorTestCase(), TEST_CASE_NO_KEY)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }

                    // Set style for Testcase id
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testCaseDTO.getTestcaseId());
                    if (StringUtils.containsIgnoreCase(testCaseDTO.getErrorTestCase(), CUSTOM_FIELD_TESTCASE_ID)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getTestCategory());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getTestViewPoint());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getEvaluationTarget1());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getEvaluationTarget2());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getEvaluationTarget3());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getEvaluationTarget4());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getEvaluationTarget5());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getEvaluationTarget6());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getTestClassification());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getAnalysisFlag1());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getAnalysisFlag2());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getPurpose());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getPrecondition());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getSteps());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getNumberOfExecutions());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getConfirmationDisplay());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getConfirmationAudio());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getRemark());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getPseudoEnvironment());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getSystemEnvironment());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getUserEnvironment());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getTestData());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getEvaluationEnvironment());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getEvaluationExecutionPlace());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getChangeIndicator());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getReasonForChange());

                    currentCellNum = cellnum++;
                    try {
                        row.createCell(currentCellNum, FORMULA)
                           .setCellFormula(testCaseDTO.getSortItemNumberFomula()
                                                      .replace(CELL_FORMULA_SORT_ITEM_KEY +
                                                               testCaseDTO.getCurrentIndex(),
                                                               CELL_FORMULA_SORT_ITEM_KEY + rownum));
                    } catch (final Exception ex) {
                        row.createCell(currentCellNum).setCellValue(testCaseDTO.getSortItemNumber());
                    }

                    row.createCell(cellnum++).setCellValue(testCaseDTO.getReleaseVersionNumber());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getRequestListVersion());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getReimplementationFlag());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getPrecheckFlag());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getTestCaseVersion());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getIncidentId());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getGroupingNumber());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getTestPhase());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getReserved3());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getReserved4());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getReserved5());

                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testCaseDTO.getUpdateDate());
                    // Set style for Update Date
                    if (StringUtils.containsIgnoreCase(testCaseDTO.getErrorTestCase(), UPDATE_DATE_KEY)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }

                    row.createCell(cellnum++).setCellValue(testCaseDTO.getCreatorCompany());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getCreatorPerson());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getImportRequest());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getFree1());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getFree2());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getFree3());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getFree4());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getFree5());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getDestination());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getHandle());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getMotorModel());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getAudio());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getBandType());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getRadioDataSystem());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getXmRadio());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getDigitalAudioBroadcasting());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getTrafficInformation());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getHdRadio());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getTv());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getNavigationType());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getLanguage());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getVoiceRecognition());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getAntiTheft());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getCameraRear());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getLanewatch());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getMultiViewCameraSystem());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getCameraMonitorMirrorSystem());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getUsbJack());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getHdmiJack());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getAuxJack());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getCarplay());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getAndroidAuto());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getMirrorLink());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getTelematics());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getEmergencyCall());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getSoundSystem());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getPlant());
                    row.createCell(cellnum++).setCellValue(testCaseDTO.getVariation30());
                    row.createCell(cellnum).setCellValue(testCaseDTO.getErrorTestCase());
                }
                break;
            case Constants.IMPORT_TESTPLAN_TYPE:
                final List<TestPlanDTO> testPlanObjects = Helper.convertObj(lst, TestPlanDTO.class);
                final CellStyle styleDate = workbook.createCellStyle();
                styleDate.setDataFormat(createHelper.createDataFormat().getFormat(FORMAT_DATE_EXPORT));
                for (final TestPlanDTO testPlanDTO : testPlanObjects) {
                    final Row row = sheet.createRow(rownum++);
                    int cellnum = 0;
                    int currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testPlanDTO.getTestCaseNo());
                    if (StringUtils.containsIgnoreCase(testPlanDTO.getErrTestPlan(), TEST_CASE_NO_KEY)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testPlanDTO.getAssign());
                    if (StringUtils.containsIgnoreCase(testPlanDTO.getErrTestPlan(), ASSIGNEE_KEY)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testPlanDTO.getStartDate());
                    if (StringUtils.containsIgnoreCase(testPlanDTO.getErrTestPlan(), START_DATE_KEY)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    } else {
                        row.getCell(currentCellNum).setCellStyle(styleDate);
                    }
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testPlanDTO.getEndDate());
                    if (StringUtils.containsIgnoreCase(testPlanDTO.getErrTestPlan(), END_DATE_KEY)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    } else {
                        row.getCell(currentCellNum).setCellStyle(styleDate);
                    }
                    row.createCell(cellnum).setCellValue(testPlanDTO.getErrTestPlan());
                }
                break;
            case Constants.IMPORT_TESTRESULT_TYPE:
                final List<TestResultDto> testResultObjects = Helper.convertObj(lst, TestResultDto.class);
                for (final TestResultDto testResultDto : testResultObjects) {
                    final String errorTestResult = testResultDto.getErrorTestResult();
                    final Row row = sheet.createRow(rownum++);
                    int cellnum = 0;
                    int currentCellNum = cellnum++;
                    // Set value for TestcaseNo
                    row.createCell(currentCellNum).setCellValue(testResultDto.getTestcaseNo());
                    // Set style for TestcaseNo
                    if (StringUtils.containsIgnoreCase(errorTestResult, TEST_CASE_NO_KEY) ||
                        StringUtils.containsIgnoreCase(errorTestResult, ERR_KEY_TEST_CASE)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }
                    currentCellNum = cellnum++;
                    // Set value for Result
                    row.createCell(currentCellNum).setCellValue(testResultDto.getResult());
                    // Set style for Result
                    if (StringUtils.containsIgnoreCase(errorTestResult, ERR_KEY_RESULT)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }
                    currentCellNum = cellnum++;
                    // Set value for HU ID
                    row.createCell(currentCellNum).setCellValue(testResultDto.getHuId());
                    // Set style for HU ID
                    if (StringUtils.containsIgnoreCase(errorTestResult, ERR_KEY_HU_ID)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }
                    currentCellNum = cellnum++;
                    // Set value for HU Index
                    row.createCell(currentCellNum).setCellValue(testResultDto.getHuIndex());
                    // Set style for HU Index
                    if (StringUtils.containsIgnoreCase(errorTestResult, ERR_KEY_HU_INDEX)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }
                    // Set style for HU ID and HU Index when SetOfDevice is not found.
                    if (StringUtils.containsIgnoreCase(errorTestResult, ERR_KEY_SET_OF_DEVICE)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                        row.getCell(--currentCellNum).setCellStyle(style);
                        currentCellNum++;
                    }
                    currentCellNum = cellnum++;
                    // Set value for External Test Device ID
                    row.createCell(currentCellNum).setCellValue(testResultDto.getExternalDeviceCode());
                    // Set style for External Test Device ID
                    if (StringUtils.containsIgnoreCase(errorTestResult, ERR_KEY_EXTERNAL_DEVICE_CODE)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }
                    currentCellNum = cellnum++;
                    // Set value for Test Duration
                    row.createCell(currentCellNum).setCellValue(testResultDto.getTestDuration());
                    // Set style for Test Duration
                    if (StringUtils.containsIgnoreCase(errorTestResult, ERR_KEY_TEST_DURATION)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }

                    // Set value for Date of Implementation
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testResultDto.getDateOfImplementation());

                    // Set value for Tester Remark
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testResultDto.getTesterRemark());

                    // Set value for Correction Request
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testResultDto.getCorrectionRequest());

                    // Set value for Evaluation Software
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testResultDto.getEvaluationSoftware());
                    // Set style for Evaluation Software
                    if (StringUtils.containsIgnoreCase(errorTestResult, ERR_KEY_EVALUATION_SOFTWARE)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }

                    // Set value for Evaluation Hard
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testResultDto.getEvaluationHard());
                    // Set style for Evaluation Hard
                    if (StringUtils.containsIgnoreCase(errorTestResult, ERR_KEY_EVALUATION_HARD)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }


                    // Set value for Peripheral Device Number
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testResultDto.getPeripheralDeviceNumber());
                    // Set style for Peripheral Device Number
                    if (StringUtils.containsIgnoreCase(errorTestResult, ERR_KEY_PERIPHERAL_DEVICE_NUMBER)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }

                    // Set value for Rtc Code
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testResultDto.getRTCCode());
                    // Set style for Rtc Code
                    if (StringUtils.containsIgnoreCase(errorTestResult, ERR_KEY_RTC_CODE)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }

                    // Set value for External Ticket Id
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testResultDto.getExternalTicketId());
                    // Set style for External Ticket Id
                    if (StringUtils.containsIgnoreCase(errorTestResult, ERR_KEY_EXTERNAL_TICKET_ID)) {
                        row.getCell(currentCellNum).setCellStyle(style);
                    }

                    // Set value for Free Description
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testResultDto.getFreeDescription());

                    // Set value for Evaluation Variation
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testResultDto.getEvaluationVariation());

                    // Set value for Revision Flag
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testResultDto.getRevisionFlag());

                    // Set value for Incomplete Content
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testResultDto.getIncompleteContent());

                    // Set value for Tester Comments
                    currentCellNum = cellnum++;
                    row.createCell(currentCellNum).setCellValue(testResultDto.getTesterComments());

                    // Only pending
                    if (!Constants.TEST_RESULT_OK.equals(testResultDto.getResult())) {
                        //currentCellNum = cellnum++;
                        // Set value for Time for Q&A submission
                        //row.createCell(currentCellNum).setCellValue(testResultDto.getTimeForQnASubmission());
                        // Set style for Time for Q&A submission
                        //if (StringUtils.containsIgnoreCase(errorTestResult, ERR_KEY_TIME_FOR_QA_SUBMISSION)) {
                        //    row.getCell(currentCellNum).setCellStyle(style);
                        //}
                        //currentCellNum = cellnum++;
                        // Set value for Time for defect submission
                        //row.createCell(currentCellNum).setCellValue(testResultDto.getTimeForDefectSubmission());
                        // Set style for Time for defect submission
                        //if (StringUtils.containsIgnoreCase(errorTestResult, ERR_KEY_TIME_FOR_DEFECT_SUBMISSION)) {
                        //row.getCell(currentCellNum).setCellStyle(style);
                        //}
                        // Set value for Time for Jira code
                        //row.createCell(cellnum++).setCellValue(testResultDto.getRtcCode());
                        //currentCellNum = cellnum++;
                        // Set value for External Ticket ID
                        //row.createCell(currentCellNum).setCellValue(testResultDto.getExternalTicketId());
                        // Set style for Blocking Ticket ID
                        //if (StringUtils.containsIgnoreCase(errorTestResult, ERR_KEY_EXTERNAL_TICKET_ID)) {
                        //    row.getCell(currentCellNum).setCellStyle(style);
                        //}
                    }
                    row.createCell(cellnum).setCellValue(testResultDto.getErrorTestResult());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean synchronizeTestCase(final long issueId, final Map<String, String> testcaseValueMap,
                                       final Date updateDate, final boolean isCreate) {
        final TestCase testCase;
        if (isCreate) {
            testCase = ao.create(TestCase.class, new DBParam(ISSUE_ID_KEY, issueId));
        } else {
            testCase = findByIssueId(issueId);
        }
        final BeanWrapper beanWrapper = new BeanWrapperImpl(testCase);
        beanWrapper.setPropertyValue(CUSTOM_FIELD_UPDATE_DATE.replaceAll(" ", ""), updateDate);
        testcaseValueMap.forEach((key, value) -> beanWrapper.setPropertyValue(key.replaceAll(" ", ""), value));
        testCase.save();
        return true;
    }

    @Override
    public boolean deleteTestcase(final long issueId) {
        final TestCase testCase = findByIssueId(issueId);
        if (testCase == null) {
            return false;
        }
        final int number = ao.deleteWithSQL(TestCase.class, QUERRY_ID, testCase.getID());
        return number > 0;
    }

    @Override
    public boolean updateWhenIssueMoved(final long issueId, final String phase, final String testcaseType) {
        final TestCase testCase = findByIssueId(issueId);
        if (testCase == null) {
            return false;
        }
        testCase.setPhase(phase);
        testCase.setTestcaseType(testcaseType);
        testCase.save();
        return true;
    }

    /**
     *
     * @param currentRow
     * @param phase
     * @return
     */
    private TestResultDto setValueTestResultDto(final Row currentRow, final String phase, SimpleDateFormat simpleDateFormat) {
        final TestResultDto testResultDto = new TestResultDto();
        final DataFormatter formatter = new DataFormatter();
        int cellNum = 0;
        testResultDto.setPhase(phase);
        testResultDto.setTestcaseNo(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setResult(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setHuId(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setHuIndex(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setExternalDeviceCode(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setTestDuration(formatter.formatCellValue(currentRow.getCell(cellNum++)));

        int currentCellNum = cellNum++;
        try {
            testResultDto.setDateOfImplementation(simpleDateFormat.format(currentRow.getCell(currentCellNum).getDateCellValue()));
        } catch (final Exception e) {
            testResultDto.setDateOfImplementation(formatter.formatCellValue(currentRow.getCell(currentCellNum)));
        }

        testResultDto.setTesterRemark(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setCorrectionRequest(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setEvaluationSoftware(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setEvaluationHard(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setPeripheralDeviceNumber(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setRTCCode(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setExternalTicketId(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setFreeDescription(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setEvaluationVariation(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setRevisionFlag(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setIncompleteContent(formatter.formatCellValue(currentRow.getCell(cellNum++)));
        testResultDto.setTesterComments(formatter.formatCellValue(currentRow.getCell(cellNum++)));

        //reset some fields which don't exist in each screens
        if (Constants.TEST_RESULT_OK.equals(testResultDto.getResult())) {
            testResultDto.setRTCCode(null);
            testResultDto.setExternalTicketId(null);
        }else if (Constants.TEST_RESULTS_PN.contains(testResultDto.getResult())) {
            testResultDto.setEvaluationSoftware(null);
            testResultDto.setEvaluationHard(null);
            testResultDto.setPeripheralDeviceNumber(null);
        }else if (Constants.TEST_RESULTS_OUTOFSCOPE.contains(testResultDto.getResult())) {
            testResultDto.setEvaluationSoftware(null);
            testResultDto.setEvaluationHard(null);
            testResultDto.setPeripheralDeviceNumber(null);
        }
        return testResultDto;
    }

    @Override
    public TestCase findByTestCaseNoAndPhase(final String testCaseNo, final String phase) {
        final List<TestCase> testCaseList = newArrayList(
                ao.find(TestCase.class, Query.select().where("TESTCASE_NO = ? AND PHASE = ?", testCaseNo, phase)));
        return testCaseList.isEmpty() ? null : testCaseList.get(0);
    }

    private TotalRecordsDTO countTotalRecords(final Sheet sheet, final String type) {
        final int lastRowIndex = sheet.getLastRowNum();
        int totalRowsOfSheet = 0;
        int headerIndex = 0;
        boolean isFoundHeader = false;
        Row currentRow;
        final DataFormatter formatter = new DataFormatter();
        if (IMPORT_TESTCASE_TYPE.equalsIgnoreCase(type)) {
            for (int i = 0; i <= lastRowIndex; i++) {
                // get current row
                currentRow = sheet.getRow(i);
                if (!isFoundHeader) {
                    headerIndex++;
                    if (currentRow == null || (StringUtils.isBlank(formatter.formatCellValue(currentRow.getCell(1))) &&
                                               StringUtils.isBlank(
                                                       formatter.formatCellValue(currentRow.getCell(13))))) {
                        continue;
                    }
                    if (StringUtils.equalsIgnoreCase(formatter.formatCellValue(currentRow.getCell(0)),
                                                     CELL_HEADER_START)) {
                        isFoundHeader = true;
                        continue;
                    }
                }
                if (isFoundHeader && currentRow != null &&
                    StringUtils.isNotBlank(formatter.formatCellValue(currentRow.getCell(1))) &&
                    StringUtils.isNotBlank(formatter.formatCellValue(currentRow.getCell(13)))) {
                    totalRowsOfSheet++;
                }
            }
        }
        if (IMPORT_TESTPLAN_TYPE.equalsIgnoreCase(type)) {
            for (int i = 0; i <= lastRowIndex; i++) {
                // get current row
                currentRow = sheet.getRow(i);
                if (!isFoundHeader) {
                    headerIndex++;
                    if (currentRow == null) {
                        continue;
                    }
                    if (TEST_PLAN_HEADER_FIRST_COLUMN.equalsIgnoreCase(
                            formatter.formatCellValue(currentRow.getCell(0)))) {
                        isFoundHeader = true;
                        continue;
                    }
                }
                if (currentRow == null) {
                    continue;
                }
                if (isFoundHeader && ((currentRow.getCell(0) != null &&
                                       StringUtils.isNotBlank(formatter.formatCellValue(currentRow.getCell(0)))) ||
                                      (currentRow.getCell(1) != null &&
                                       StringUtils.isNotBlank(formatter.formatCellValue(currentRow.getCell(1)))) ||
                                      (currentRow.getCell(2) != null &&
                                       StringUtils.isNotBlank(formatter.formatCellValue(currentRow.getCell(2)))) ||
                                      (currentRow.getCell(3) != null &&
                                       StringUtils.isNotBlank(formatter.formatCellValue(currentRow.getCell(3)))))) {
                    totalRowsOfSheet++;
                }
            }
        }
        if (IMPORT_TESTRESULT_TYPE.equalsIgnoreCase(type)) {
            for (int i = 0; i <= lastRowIndex; i++) {
                // get current row
                currentRow = sheet.getRow(i);
                if (!isFoundHeader) {
                    headerIndex++;
                    if (currentRow == null) {
                        continue;
                    }
                    if (TEST_RESULT_HEADER_FIRST_COLUMN.equalsIgnoreCase(
                            formatter.formatCellValue(currentRow.getCell(0)))) {
                        isFoundHeader = true;
                        continue;
                    }
                }
                if (currentRow == null) {
                    continue;
                }
                if (isFoundHeader) {
                    for (int j = 0; j < currentRow.getLastCellNum(); j++) {
                        // Column "Jira Code" not requirement
                        if (j == 8) {
                            continue;
                        }
                        if (currentRow.getCell(j) != null &&
                            StringUtils.isNotBlank(formatter.formatCellValue(currentRow.getCell(j)))) {
                            totalRowsOfSheet++;
                            break;
                        }
                    }
                }
            }
        }
        return new TotalRecordsDTO(headerIndex, totalRowsOfSheet);
    }

    private boolean isAllThreadDone(List<Future<List<Object>>> futures) {
        for (Future<List<Object>> future : futures) {
            if (!future.isDone()) {
                return false;
            }
        }
        return true;
    }

    public String formatAsString(final CellValue cellValue) {
        if (cellValue == null) {
            return null;
        }
        final CellType _cellType = cellValue.getCellTypeEnum();
        switch (_cellType) {
            case NUMERIC:
                return String.valueOf(Math.round(cellValue.getNumberValue()));
            case STRING:
                return '"' + cellValue.getStringValue() + '"';
            case BOOLEAN:
                return cellValue.getBooleanValue() ? "TRUE" : "FALSE";
            case ERROR:
                return ErrorEval.getText(cellValue.getErrorValue());
            default:
                return "<error unexpected cell type " + _cellType + ">";
        }
    }
}
