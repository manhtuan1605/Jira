package com.cmcglobal.plugins.utils;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Constants {

    private Constants() {
    }

    public static final String COMMA      = ",";
    public static final String UNDERSCORE = "_";

    public static final String GROUP_LEADER = "Leader";
    public static final String GROUP_TESTER = "Tester";

    public static final String USER_ADMIN_KEY = "admin";

    public static final String PROPERTY_EXPORT_EXCEL        = "jira.export.excel.enabled";
    public static final String PROPERTY_DISPLAY_DATE_FORMAT = "jira.lf.date.dmy";

    public static final String ISSUE_TYPE_TESTCASE_NAME        = "Test Case";
    public static final String ISSUE_TYPE_TESTCASE_DESCRIPTION = "The Test Case issue type description";
    public static final String ISSUE_TYPE_QNA_NAME             = "QnA";
    public static final String ISSUE_TYPE_QNA_DESCRIPTION      = "The QnA issue type description";
    public static final String ISSUE_TYPE_DEFECT_NAME          = "Defect";
    public static final String ISSUE_TYPE_DEFECT_DESCRIPTION   = "The Defect issue type description";
    public static final String ISSUE_TYPE_AUTOMOTIVE_DEVICE    = "Automotive Device";
    public static final String ISSUE_TYPE_PERIPHERAL_DEVICE    = "Peripheral Device";
    public static final String ISSUE_TYPE_EXTERNAL_DEVICE      = "External Device ID";
    public static final String ISSUE_TYPE_TASK_NAME            = "Task";
    public static final String ISSUE_TYPE_TASK_DESCRIPTION     = "The Task issue type description";

    public static final String CUSTOM_FIELD_TYPE_TEXTAREA
                                                              = "com.atlassian.jira.plugin.system.customfieldtypes:textarea";
    public static final String CUSTOM_FIELD_TYPE_TEXT
                                                              = "com.atlassian.jira.plugin.system.customfieldtypes:textfield";
    public static final String CUSTOM_FIELD_TYPE_SELECT
                                                              = "com.atlassian.jira.plugin.system.customfieldtypes:select";
    public static final String CUSTOM_FIELD_TYPE_DATE_PICKER
                                                              = "com.atlassian.jira.plugin.system.customfieldtypes:datepicker";
    public static final String CUSTOM_FIELD_TYPE_MULTI_SELECT
                                                              = "com.atlassian.jira.plugin.system.customfieldtypes:multiselect";
    public static final String CUSTOM_FIELD_TYPE_NUMBER
                                                              = "com.atlassian.jira.plugin.system.customfieldtypes:float";

    public static final String CUSTOM_FIELD_TYPE_TEXT_SEARCHER
                                                                       = "com.atlassian.jira.plugin.system.customfieldtypes:textsearcher";
    public static final String CUSTOM_FIELD_TYPE_DATE_PICKER_SEARCHER
                                                                       = "com.atlassian.jira.plugin.system.customfieldtypes:daterange";
    public static final String CUSTOM_FIELD_TYPE_MULTI_SELECT_SEARCHER
                                                                       = "com.atlassian.jira.plugin.system.customfieldtypes:multiselectsearcher";
    public static final String CUSTOM_FIELD_TYPE_NUMBER_SEARCHER
                                                                       = "com.atlassian.jira.plugin.system.customfieldtypes:exactnumber";
    public static final int    CUSTOM_FIELD_BEGIN_POSITION             = 4;

    public static final String CUSTOM_FIELD_EXTERNAL_TICKET_ID         = "External Ticket Id";
    public static final String CUSTOM_FIELD_START_DATE                 = "Original Plan Date";
    public static final String CUSTOM_FIELD_END_DATE                   = "Current Plan Date";
    public static final String CUSTOM_FIELD_LATEST_RESULT              = "Latest Result";
    public static final String CUSTOM_FIELD_PHASE                      = "Phase";
    public static final String CUSTOM_FIELD_TESTCASE_TYPE              = "Testcase Type";
    public static final String CUSTOM_FIELD_EXECUTE_COUNT              = "Execute Count";
    public static final String CUSTOM_FIELD_PENDING_TYPE               = "Pending Type";
    public static final String CUSTOM_FIELD_LAST_RECENT_TEST           = "Last Recent Test";
    public static final String CUSTOM_FIELD_HU_DEVICE                  = "Hu Device";
    public static final String CUSTOM_FIELD_AMIGO_PG_VERSION           = "Amigo Pg Version";
    public static final String CUSTOM_FIELD_AMIGO_DATA_VERSION         = "Amigo Data Version";
    public static final String CUSTOM_FIELD_SCREEN_DEVICE              = "Screen Device";
    public static final String CUSTOM_FIELD_SCREEN_DEVICE_TYPE         = "Screen Device Type";
    public static final String CUSTOM_FIELD_DEVICE_ID                  = "Device Id";
    public static final String CUSTOM_FIELD_DEVICE_NAME                = "Device Name";
    public static final String CUSTOM_FIELD_SCREEN_TYPE                = "Screen Type";
    public static final String CUSTOM_FIELD_SYS_SET_ID_VERSION         = "Sys Set Id Version";
    public static final String CUSTOM_FIELD_SYS_DATA_VERSION           = "Sys Data Version";
    public static final String CUSTOM_FIELD_SYS_SOFT                   = "Sys Soft";
    public static final String CUSTOM_FIELD_SYS_DATA_PREFIX            = "Sys Data Prefix";
    public static final String CUSTOM_FIELD_RESULT_COMMENT             = "Result Comment";
    public static final String CUSTOM_FIELD_HU_ID_HU_INDEX             = "Hu Id Hu Index";
    public static final String CUSTOM_FIELD_HU_ID                      = "Hu Id";
    public static final String CUSTOM_FIELD_HU_INDEX                   = "Hu Index";
    public static final String CUSTOM_FIELD_HU_MARKET                  = "Hu Market";
    public static final String CUSTOM_FIELD_TESTCASE_NO                = "Testcase No";
    public static final String CUSTOM_FIELD_TEST_HU_MARKET             = "Test Hu Market";
    public static final String CUSTOM_FIELD_VEHICLE_PARAMETER          = "Vehicle Parameter";
    public static final String CUSTOM_FIELD_AMP_TYPE                   = "Amp Type";
    public static final String CUSTOM_FIELD_SI_INDEX                   = "Si Index";
    public static final String CUSTOM_FIELD_UPDATE_DATE                = "Update Date";
    public static final String CUSTOM_FIELD_VR_TSS_VERSION             = "Vr Tss Version";
    public static final String CUSTOM_FIELD_DATA_MAP                   = "Data Map";
    public static final String CUSTOM_FIELD_SYSTEM_DEVICE_NAME         = "System Device Name";
    public static final String CUSTOM_FIELD_EXTERNAL_TEST_DEVICE_ID    = "External Test Device Id";
    public static final String CUSTOM_FIELD_TEST_DURATION              = "Test Duration";
    public static final String CUSTOM_FIELD_TIME_FOR_QA_SUBMISSION     = "Time For QA Submission";
    public static final String CUSTOM_FIELD_TIME_FOR_QNA_SUBMISSION    = "Time For QnA Submission";
    public static final String CUSTOM_FIELD_TIME_FOR_DEFECT_SUBMISSION = "Time For Defect Submission";
    public static final String CUSTOM_FIELD_RTC_CODE                   = "RTC Code";
    public static final String CUSTOM_FIELD_JIRA_STATUS                = "Jira Status";
    public static final String CUSTOM_FIELD_TESTCASE_ID                = "Testcase Id";
    public static final String CUSTOM_FIELD_BLOCKING_TICKET_TYPE       = "Blocking Ticket Type";
    public static final String CUSTOM_FIELD_BLOCKING_TICKET_STATUS     = "Blocking Ticket Status";
    public static final String CUSTOM_FIELD_CMC_CODE                   = "CMC Code";
    public static final String CUSTOM_FIELD_TYPE_OF_WORK               = "Type Of Work";
    public static final String CUSTOM_FIELD_COMPONENTS                 = "Component/s";
    public static final String CUSTOM_FIELD_MODEL                      = "Model";
    public static final String CUSTOM_FIELD_DATE_OF_IMPLEMENTATION     = "Date Of Implementation";
    public static final String CUSTOM_FIELD_FREE_DESCRIPTION_FIELD     = "Free Description";
    public static final String CUSTOM_FIELD_EVALUATION_VARIATION       = "Evaluation Variation";
    public static final String CUSTOM_FIELD_TESTER_COMMENTS            = "Tester Comments";
    public static final String CUSTOM_FIELD_TESTER_REMARK              = "Tester Remark";
    public static final String CUSTOM_FIELD_CORRECTION_REQUESTS        = "Correction Request";

    public static final String KEY_CREATE_SCREEN         = "TESTCASECREATESCREEN";
    public static final String KEY_DEFAULT_SCREEN        = "TESTCASEDEFAULTSCREEN";
    public static final String KEY_EDIT_SCREEN           = "TESTCASEEDITSCREEN";
    public static final String KEY_OK_SCREEN             = "TESTCASEOKSCREEN";
    public static final String KEY_NG_SCREEN             = "TESTCASENGSCREEN";
    public static final String KEY_OUT_OF_SCOPE_SCREEN   = "TESTCASEOUTOFSCOPESCREEN";
    public static final String KEY_PENDING_SCREEN        = "TESTCASEPENDINGSCREEN";
    public static final String KEY_PLAN_SCREEN           = "TESTCASEPLANSCREEN";
    public static final String KEY_DEFAULT_QNA_SCREEN    = "QNADEFAULTSCREEN";
    public static final String KEY_DEFAULT_DEFECT_SCREEN = "DEFECTDEFAULTSCREEN";
    public static final String KEY_TASK_SCREEN           = "TASKDEFAULTSCREEN";

    public static final String KEY_WORKFLOW_TESTCASE    = "WF3";
    public static final String KEY_WORKFLOW_TASK        = "WF5";
    public static final String KEY_WORKFLOW_DEVICE      = "WF7";
    public static final String KEY_WORKFLOW_ENVIRONMENT = "WF9";

    public static final String WORKFLOW_ATTRIBUTE = "jira.fieldscreen.id";

    public static final String WORKFLOW_TESTCASE_ACTION_OK           = "OK";
    public static final String WORKFLOW_TESTCASE_ACTION_NG           = "NG";
    public static final String WORKFLOW_TESTCASE_ACTION_PENDING      = "Pending";
    public static final String WORKFLOW_TESTCASE_ACTION_OUT_OF_SCOPE = "Out of Scope";
    public static final String WORKFLOW_TESTCASE_ACTION_CREATE       = "Create";
    public static final String WORKFLOW_TESTCASE_ACTION_PLAN         = "Plan";
    public static final String WORKFLOW_ENVIRONMENT_ACTION_CREATE    = "Create Issue";

    public static final String TEST_PHASE_VALIDATOR          = "Entering doValidation";
    public static final String TEST_PHASE_DEFAULT            = "Entering doDefault";
    public static final String TEST_PHASE_EXERCUTE           = "Entering doExecute";
    public static final String TEST_PHASE_VIEW               = "testing-phases";
    public static final String TEST_PHASE_ERROR              = "error";
    public static final String TEST_PHASE_REQUIRE_PROJECT_ID = "The project id is required!";

    public static final String IMPORT_TEST_CASE_VIEW = "import-test-case-success";

    public static final String VALIDATOR_DESCRIPTOR_TYPE                     = "class";
    public static final String VALIDATOR_DESCRIPTOR_ARGS_CLASS_NAME          = "class.name";
    public static final String VALIDATOR_DESCRIPTOR_ARGS_CUSTOM_FIELD        = "selectedCustomField";
    public static final String VALIDATOR_DESCRIPTOR_OBJECT_CUSTOM_FIELD_NAME = "selectedCustomFieldName";
    public static final String VALIDATOR_DESCRIPTOR_OBJECT_LIST_CUSTOM_FIELD = "customFields";
    public static final String VALIDATOR_DESCRIPTOR_NAME                     = "Validate Enviroment";
    public static final String VALIDATOR_DESCRIPTOR_ARGS_LIST_BLUETOOTH      = "bluetoothCustomField";
    public static final String VALIDATOR_DESCRIPTOR_ARGS_LIST_USB            = "usbCustomField";

    public static final String VALIDATOR_DESCRIPTOR_CF_START_DATE         = "startDate";
    public static final String VALIDATOR_DESCRIPTOR_CF_END_DATE           = "endDate";
    public static final String VALIDATOR_START_DATE_AND_END_DATE_REQUIRED
                                                                          = "Start Date and End Date must not be empty.";
    public static final String VALIDATOR_START_DATE_AND_END_DATE_COMPARE
                                                                          = "Start Date must be before or equal End Date.";
    public static final String VALIDATOR_START_DATE_WITH_TEST_PHASE
                                                                          = "Start date must be after start date of test phase: ";
    public static final String VALIDATOR_END_DATE_WITH_TEST_PHASE
                                                                          = "End date must be before end date of test phase: ";
    public static final String VALIDATOR_DESCRIPTOR_ARGS_ISSUE            = "issue";

    public static final int    MAX_MEMORY_SIZE  = 104857600;
    public static final int    MAX_REQUEST_SIZE = 104857600;
    public static final String ADDRESS          = System.getProperty("user.home") + File.separator + "JiraUpload";
    public static final String ADDRESS_INVALID  = System.getProperty("user.home") + File.separator + "JiraUpload" +
                                                  File.separator + "Invalid";
    public static final String INVALID          = "Invalid";

    //environment project
    public static final String SYS_DATA_PREFIX                              = "Sys Data Prefix";
    public static final String SI_INDEX                                     = "Si Index";
    public static final String HU_INDEX                                     = "Hu Index";
    public static final String HU_DEVICE                                    = "Hu Device";
    public static final String HU_DEVICE_DESCRIPTION                        = "Hu device description";
    public static final String SCREEN_DEVICE                                = "Screen Device";
    public static final String SCREEN_DEVICE_DESCRIPTION                    = "Screen device description";
    public static final String SI_SYS_INFO                                  = "Si Sys Info";
    public static final String SI_SYS_INFO_DESCRIPTION                      = "Si Sys Info description";
    public static final String HU_TYPE_VEHICLE                              = "Hu Type Vehicle";
    public static final String HU_TYPE_VEHICLE_DESCRIPTION                  = "Hu Type Vehicle description";
    public static final String SI_AMIGO_INFO                                = "Si Amigo Info";
    public static final String SI_AMIGO_INFO_DESCRIPTION                    = "Si Amigo Info Description";
    public static final String SET_OF_DEVICE                                = "Set Of Device";
    public static final String AUTOMOTIVE_DEVICE                            = "Automotive Device";
    public static final String AUTOMOTIVE_DEVICE_DESCRIPTION                = "Automotive Device Description";
    public static final String PERIPHERAL_DEVICE                            = "Peripheral Device";
    public static final String PERIPHERAL_DEVICE_DESCRIPTION                = "Peripheral Device Description";
    public static final String EXTERNAL_DEVICE                              = "External Device ID";
    public static final String EXTERNAL_DEVICE_DESCRIPTION                  = "External Device ID Description";
    public static final String SET_OF_DEVICE_DESCRIPTION                    = "Set Of Device description";
    public static final String TEST_HU_MARKET_ATTRIBUTE                     = "Test Hu Market";
    public static final String HU_ID_ATTRIBUTE                              = "Hu Id";
    public static final String VEHICLE_ATTRIBUTE                            = "Vehicle Parameter";
    public static final String AMP_TYPE_ATTRIBUTE                           = "Amp Type";
    public static final String DEVICE_TYPE_ATTRIBUTE                        = "Device Type";
    public static final String DEVICE_SUB_TYPE_ATTRIBUTE                    = "Device Sub Type";
    public static final String SYSTEM_DEVICE_NAME_ATTRIBUTE                 = "System Device Name";
    public static final String RELEASE_DATE_ATTRIBUTE                       = "Release Date";
    public static final String BUILD_NUMBER_ATTRIBUTE                       = "Build Number";
    public static final String HU_DEVICE_DEFAULT_SCREEN                     = "HUDEVICEDEFAULTSCREEN";
    public static final String SCREEN_DEVICE_DEFAULT_SCREEN                 = "SCREENDEVICEDEFAULTSCREEN";
    public static final String HU_TYPE_VEHICLE_DEFAULT_SCREEN               = "HUTYPEVEHICLEDEFAULTSCREEN";
    public static final String SI_SYS_INFO_DEFAULT_SCREEN                   = "SISYSINFODEFAULTSCREEN";
    public static final String SI_AMIGO_INFO_DEFAULT_SCREEN                 = "SIAMIGOINFODEFAULTSCREEN";
    public static final String SET_OF_DEVICE_DEFAULT_SCREEN                 = "SETOFDEVICEDEFAULTSCREEN";
    public static final String SET_OF_DEVICE_CREATE_SCREEN                  = "SETOFDEVICECREATESCREEN";
    public static final String SET_OF_DEVICE_EDIT_SCREEN                    = "SETOFDEVICEEDITSCREEN";
    public static final String TEST                                         = "TEST";
    public static final String INVALID_PROJECT_KEY                          = "Invalid Project Key";
    public static final String PENDING_TYPE_CF                              = "Pending Type";
    public static final String TEST_CASE_NO_KEY                             = "TestcaseNo";
    public static final String ASSIGNEE_KEY                                 = "Assigne";
    public static final String ERR_EXIST_TESTCASE_NO                        = TEST_CASE_NO_KEY + " is duplicate. ";
    public static final String TEST_HU_MARKET                               = "Test Hu Market";
    public static final String HU_DEVICE_ATTRIBUTE                          = "Hu Device";
    public static final String SCREEN_DEVICE_ATTRIBUTE                      = "Screen Device";
    public static final String DEVICE_ID_ATTRIBUTE                          = "Device Id";
    public static final String DEVICE_NAME_ATTRIBUTE                        = "Device Name";
    public static final String AMIGO_PG_VERSION_ATTRIBUTE                   = "Amigo Pg Version";
    public static final String AMIGO_DATA_VERSION_ATTRIBUTE                 = "Amigo Data Version";
    public static final String SYS_SET_ID_VERSION_ATTRIBUTE                 = "Sys Set Id Version";
    public static final String SYS_DATA_VERSION_ATTRIBUTE                   = "Sys Data Version";
    public static final String SYS_SOFT_ATTRIBUTE                           = "Sys Soft";
    public static final String SYS_DATA_PREFIX_ATTRIBUTE                    = "Sys Data Prefix";
    public static final String SCREEN_TYPE                                  = "Screen Type";
    public static final String ERR_HU_DEVICE_NOT_EXIST                      = "Hu Device does not exist";
    public static final String ERR_SCREEN_DEVICE_NOT_EXIST                  = "Screen Device does not exist";
    public static final String ERR_SI_AMIGO_NOT_EXIST                       = "Si Amigo Info does not exist.";
    public static final String ERR_SI_SYS_NOT_EXIST                         = "Si Sys Info does not exist.";
    public static final String ERR_HU_TYPE_VEHICLE_EXIST                    = "Hu Type Vehicle does not exist.";
    public static final String VR_TTS_VERSION_ATTRIBUTE                     = "VR-TTS Version";
    public static final String DATA_MAP_ATTRIBUTE                           = "Data Map";
    public static final String SCREEN_DEVICE_TYPE                           = "Screen Device Type";
    public static final String SI_INDEX_VALUE                               = "Si Index Value";
    public static final String SCREEN_DEVICE_TYPE_ATTRIBUTE                 = "Screen Device Type";
    public static final String PARAMETER_PROJECT_ID                         = "pid";
    public static final String PARAMETER_FILE_NAME                          = "filename";
    public static final String PARAMETER_ACTION                             = "action";
    public static final String PARAMETER_ACTION_DOWNLOAD_VALUE              = "download";
    public static final String PARAMETER_ACTION_REMOVE_VALUE                = "remove";
    public static final String PARAMETER_ACTION_REMOVE_DEVICE_VALUE         = "removedevice";
    public static final String PARAMETER_ACTION_DOWNLOAD_VALUE_INVALID      = "download-invalid";
    public static final String PARAMETER_ACTION_REMOVE_VALUE_INVALID        = "remove-invalid";
    public static final String PARAMETER_ACTION_REMOVE_DEVICE_VALUE_INVALID = "removedevice-invalid";
    public static final String MODEL                                        = "Model";

    // DEVICES PROJECT
    public static final String AUTOMOTIVE_DEVICE_DEFAULT_SCREEN  = "AUTOMOTIVEDEVICEDEFAULTSCREEN";
    public static final String PERIPHERAL_DEVICE_DEFAULT_SCREEN  = "PERIPHERALDEVICEDEFAULTSCREEN";
    public static final String EXTERNAL_DEVICE_DEFAULT_SCREEN    = "EXTERNALDEVICEDEFAULTSCREEN";
    public static final String EXTERNAL_DEVICE_CREATE_SCREEN     = "EXTERNALDEVICECREATESCREEN";
    public static final String NOTE                              = "Note";
    public static final String CUSTOM_FIELD_MIRROR_LINK          = "Mirror Link";
    public static final String CUSTOM_FIELD_ANDROID_AUTO         = "Android Auto";
    public static final String CUSTOM_FIELD_EXTERNAL_DEVICE_TYPE = "External Device Type";

    public static final String VIEW_TESTING_PHASES                      = "testing-phases";
    public static final String ERROR_MESSAGE_PHASE_ID_NOT_EXIST         = "Phase Id not exist!";
    public static final String ERROR_MESSAGE_REQUIRED_PHASE_NAME        = "Phase name is required.";
    public static final String ERROR_MESSAGE_EXISTED_PROJECT_ID         = "Phase of such name already exists.";
    public static final String ERROR_MESSAGE_INVALID_START_DATE         = "Invalid Start date.";
    public static final String ERROR_MESSAGE_INVALID_END_DATE           = "Invalid End date.";
    public static final String ERROR_MESSAGE_START_DATE_AFTER_END_DATE  = "Start date must be before End date.";
    public static final String ERROR_MESSAGE_END_DATE_BEFORE_START_DATE = "End date must be after Start date.";
    public static final String ERROR_MESSAGE_PHASE_NAME_MAX_LENGTH
                                                                        = "Phase name should not be more than 100 characters.";
    public static final String PHASE_NAME                               = "phaseName";
    public static final String PHASE_START_DATE                         = "startDate";
    public static final String PHASE_END_DATE                           = "endDate";
    public static final String PHASE_ID                                 = "phaseId";
    public static final String CONTENT_TYPE                             = "application/json";
    public static final String CHARACTER_ENCODING                       = "UTF-8";
    public static final String QUERRY_ID                                = "ID = ?";
    // test case type
    public static final String VIEW_TEST_CASE_TYPE                      = "testcase-types";
    public static final String TYPE_NAME                                = "typeName";
    public static final String TYPE_PERFORMANCE                         = "typePerformance";
    public static final String TYPE_ID                                  = "typeId";
    public static final String ERROR_TYPE_NAME_REQUIRE                  = "Type name is required.";
    public static final String ERROR_TYPE_NAME_EXISTED                  = "Type name is existed.";
    public static final String ERROR_TYPE_NAME_RANGE
                                                                        = "Type name should not more than 100 characters.";
    public static final String ERROR_TYPE_PERFORMANCE_REQUIRE           = "Type performance is required.";
    public static final String ERROR_TYPE_PERFORMANCE_NOT_VALID         = "Type performance not valid.";
    public static final String ERROR_TYPE_PERFORMANCE_RANGE             = "Type performance should >0 and <= 1000000.";
    public static final String ERROR_ID_NOT_EXIST                       = "Type Id not exist !";

    public static final String BASE_URL = ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL);

    //Import file
    public static final String       TEST_CASE_TYPE               = "testcasetype";
    public static final String       IMPORT_TYPE                  = "importType";
    public static final String       PARAM_PHASE_ID               = "phaseID";
    public static final String       PHASE_ID_REQUIRED            = "Phase Id is null";
    public static final String       IMPORT_FILE_TEST_CASE        = "test case";
    public static final String       MESSAGE_ERROR_TEST_CASE_TYPE = "testCaseType";
    public static final String       MESSAGE_ERROR_SERVICE        = "errorService";
    public static final String       TEST_CASE_TYPE_REQUIRED      = "Test Case Type is null";
    public static final String       LIST_FILE_UP_LOAD            = "listUploadFiles";
    public static final String       TOTAL_ROWS_IMPORT_SUCCESS    = "totalRowsImportSuccess";
    public static final String       TOTAL_ROWS_IMPORT_ERROR      = "totalRowsImportError";
    public static final String       TOTAL_ROWS                   = "totalRows";
    public static final String       FILE_NAME                    = "fileName";
    //import provider
    public static final String       TEST_CASE_TYPES              = "testcaseTypes";
    public static final String       PHASES                       = "phases";
    public static final String       PROJECT_ID                   = "projectId";
    public static final String       URI                          = "base_url";
    public static final String       DATE_FORMAT_DD_MM_YYYY       = "dd/MM/yyyy";
    public static final List<String> RESULT_TYPES                 = Arrays.asList("OK", "NG", "PN", "ER", "NT", "DG",
                                                                                  "DL", "PN(QA)", "PN(Bug)", "PN(Equipment)", "PN(Internal)", "PN(DG)");
    public static final List<String> PENDING_TYPES                = Arrays.asList("ER", "NT", "DG", "DL", "PN(QA)", "PN(Bug)", "PN(Equipment)", "PN(Internal)", "PN(DG)");
    public static final List<String> AMP_TYPES                    = Arrays.asList("内臓 AMP", "外部 AMP");
    public static final List<String> TYPE_OF_WORKS                = Arrays.asList("Defect submit", "Q&A Submit",
                                                                                  "Defect fix confirmation",
                                                                                  "Defect and Q&A follow up", "Other");
    public static final String       TYPE_OF_WORK_DEFECT_SUBMIT   = "Defect submit";
    public static final String       TYPE_OF_WORK_QA_SUBMIT       = "Q&A Submit";
    public static final String       TYPE_OF_WORK_DEFECT_FIX      = "Defect fix confirmation";
    public static final String       TYPE_OF_WORK_DF_QA_FOLLOW_UP = "Defect and Q&A follow up";
    public static final String       TYPE_OF_WORK_OTHER           = "Other";
    public static final String       TYPE_OF_TEST_EXECUTE_STATUS  = "Test Execute";

    public static       String PREFIX_INVALID = "_Invalid_";
    public static       String PREFIX         = "_";
    public static final String PREFIX_DOT     = ". ";
    public static final String MESSAGE_KEY    = "message";

    public static final String ACTION_CREATE              = "Create";
    public static final String ACTION_EDIT                = "Edit";
    public static final String ACTION_DELETE              = "Delete";
    public static final String JAVA_IO_TMP_DIR            = "java.io.tmpdir";
    public static final String SERVER_KEY                 = "Server";
    public static final String ISSUE_ID_KEY               = "ISSUE_ID";
    public static final String MESSAGE_SERVER_ERROR       = "Server Error. ";
    public static final String MESSAGE_UPLOAD_ERROR       = "File upload fail. ";
    public static final String MESSAGE_UPLOAD_SUCCESS     = "Upload file success!";
    public static final String MESSAGE_ATTRIBUTE_VALUE    = "Check enctype: multipart/form-data";
    public static final String QUERY_ISSUE_ID             = "ISSUE_ID = ?";
    public static final String QUERY_TESTCASE_NO          = "TESTCASE_NO = ?";
    public static final String QUERY_PHASE_NAME           = "PHASE = ?";
    public static final String QUERY_PROJECT_ID           = "PROJECT_ID = ?";
    public static final String QUERY_PROJECT_ID_AND_TYPE  = "PROJECT_ID = ? AND TYPE = ? ";
    public static final String QUERY_FILE_UPLOAD_VALID    = "UPLOAD_FILE_NAME = ?";
    public static final String QUERY_FILE_UPLOAD_INVALID  = "UPLOAD_FILE_NAME_INVALID = ?";
    public static final String QUERY_ORDER_BY_CREATE_DATE = "CREATE_DATE DESC";
    public static final String QUERY_ORDER_BY_DATE_ASC    = "CREATE_DATE ASC";

    public static final String MESSAGE_UPLOAD_SUCCESS_BIG_FILE = "Upload file success!";
    public static final int    INVALID_FORMAT_EXCEPTION        = 422;

    public static final String IMPORT_TESTCASE_TYPE   = "Test Case";
    public static final String IMPORT_TESTPLAN_TYPE   = "Test Plan";
    public static final String IMPORT_TESTRESULT_TYPE = "Test Result";

    public static final String ERR_INVALID_TESTCASE_NO   = TEST_CASE_NO_KEY + " must not be blank. ";
    public static final String ERR_NOT_EXIST_TESTCASE_NO = TEST_CASE_NO_KEY + " not exist. ";
    public static final String ERR_INVALID_ASSIGNEE      = ASSIGNEE_KEY + " must not be blank. ";
    public static final String ERR_NOT_EXITS_ASSIGNEE    = ASSIGNEE_KEY + " not exist in project. ";

    public static final String ERR_SEARCH_QUERY              = "System has encountered a problem when trying to validate the form.";
    public static final String ERR_ISSUE_TYPE_NOT_EXIST      = "Can't find Task issue type. ";
    public static final String ERR_CREATE_ISSUE              = "Create issue error: ";
    public static final String ERR_UPDATE_ISSUE              = "Update issue error: ";
    public static final String ERR_VALIDATION_CREATE_ISSUE   = "Create validation issue error: ";
    public static final String ERR_VALIDATION_UPDATE_ISSUE   = "Update validation issue error: ";
    public static final String YOU_MUST_SPECIFY_A            = "You must specify a ";
    public static final String OF_THE_ISSUE                  = " of the issue. ";
    public static final String TESTER_PERFORMANCE_AJAX_ERROR = "Invalid value %s and %s";

    public static final String ERR_INPUT_PARAM    = ", issueInputParameters = ";
    public static final String CLASS_HELPER       = "Helper class";
    public static final String FORMAT_DATE_IMPORT = "MM/dd/yy";
    public static final String FORMAT_DATE_EXPORT = "MM/dd/yyyy";
    //public static final String FORMAT_DATE_ISSUE  = "d/MMM/yy";
    public static final String FORMAT_DATE_ISSUE  = ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_DATE_PICKER_JAVA_FORMAT);
    public static final String CREATE_COMPANY     = "company";
    public static final String CREATE_PERSON      = "person";

    public static final String BUIDER_LINE_SPERATOR        = "line.separator";
    public static final String UPDATE_DATE_KEY             = "UpdateDate";
    public static final String START_DATE_KEY              = "StartDate";
    public static final String END_DATE_KEY                = "EndDate";
    public static final String UPDATE_DATE_MESSAGE_INVALID = UPDATE_DATE_KEY + " is invalid, the format is " +
                                                             FORMAT_DATE_EXPORT + PREFIX_DOT;
    public static final String TESTCASE_ID_MESSAGE_INVALID = CUSTOM_FIELD_TESTCASE_ID + " must be a number";

    public static final String DATE_MESSAGE_INVALID = " is invalid, the format is " + FORMAT_DATE_EXPORT + PREFIX_DOT;

    public static final String PROJECT_ROLE_PM            = "PM";
    public static final String PROJECT_ROLE_QC_LEAD       = "QC Leader";
    public static final String PROJECT_ROLE_QC            = "QC";
    public static final String CELL_FORMULA_PATTERN_KEY   = "&TEXT(B";
    public static final String CELL_FORMULA_SORT_ITEM_KEY = "RANK(AJ";
    //Upload file
    public static final String RESPONSE_PROJECT           = "/projects/";
    public static final String RESPONSE_PRAMA
                                                          = "?selectedItem=com.cmcglobal.plugins.jira-customize:import-test-case";
    public static final String RESPONSE_PRAMA_DEVICE
                                                          = "?selectedItem=com.cmcglobal.plugins.jira-customize:import-device";
    //End Upload file

    public static final String STATUS_WAITING_TO_TEST = "Waiting to test";

    public static final String STATUS_OK       = "Tested";
    public static final String STATUS_NG       = "Pending";
    public static final String STATUS_EXECUTED = "Executed";

    public static final int    START_TIME         = 8;
    public static final int    LUNCH_TIME         = 1;
    public static final String PATTERM_DATE       = "yyyy-MM-dd";
    public static final String TEST_CASE_FUNCTION = "Function";
    public static final String INFINITI_EST       = "-";
    public static final String TESTCASE_TYPE      = "Testcase Type";

    public static final String START_DATE_INVALID    = START_DATE_KEY + " wrong format Date. ";
    public static final String END_DATE_INVALID      = END_DATE_KEY + " wrong format Date. ";
    public static final String ISSUE_PLAN            = "Plan";
    public static final String ISSUE_ACTION_NOT_PLAN = "Action is not plan ";

    // Teams management
    public static final String STRING_BASER_URL                       = "baseUrl";
    public static final String STRING_QC_LEADERS                      = "qcLeaders";
    public static final String STRING_QC_LEADER                       = "QC Leader";
    public static final String STRING_QCS                             = "qcs";
    public static final String STRING_QC                              = "QC";
    public static final String STRING_MEMBERS                         = "members";
    public static final String OPEN_PARENTHESES                       = " (";
    public static final String CLOSE_PARENTHESES                      = ")";
    public static final String TEAM_MANAGEMENT_ERRORS_KEY             = "errors";
    public static final String TEAM_MANAGEMENT_ADDING_ERRORS_KEY      = "addingErrors";
    public static final String ERROR_SELECTED_USER_EXISTED            = "Selected user already exists in the team.";
    public static final String ERROR_SELECTED_USER_IN_OTHER_TEAM      = "Selected user already exists in another team.";
    public static final String ERROR_SELECTED_USER_IS_CURRENT_QC_LEAD = "Selected user already this team QC Leader";
    public static final String ERROR_SELECTED_USER_LEADER
                                                                      = "Selected user already QC Leader in another team.";
    public static final String STRING_PID                             = "pid";
    //condition project role
    public static final String ROLE_PM                                = "PM";
    public static final String ROLE_QC_LEADER                         = "QC Leader";
    //Helper
    public static final String STRING_EMPTY                           = "";
    public static final String PATTERN                                = "\\w+$";
    public static final String ERROR_UPDATE_DATE                      = "Update date invalid";
    public static final String FORMAT_UPDATE_DATE                     = "yyyy-MM-dd HH:mm:ss.S";

    public static final String START_IMPORT        = "========> START IMPORT  <========";
    public static final String START_READ_FILE     = "========> START READ FILE  <========";
    public static final String START_VALIDATE_DATA = "========> START VALIDATE ";
    public static final String MILISECONDS         = " milisecond";

    public static final String END_UPLOAD_FILE            = "========> End Upload File at time ";
    public static final String END_READ_FILE              = "========> End Read File in time ";
    public static final String END_VALIDATE_DATA          = "========> End Validate data in time ";
    public static final String END_INSERT_DATABASE        = "========> Total Insert database in time ";
    public static final String END_UPDATE_ISSUE           = "========> End Update Issue in time ";
    public static final String END_CREATE_ISSUE           = "========> End create Issue in time ";
    public static final String END_SAVE_TESTCASE          = "========> End Save Testcase in time ";
    public static final String END_WIRTE_INVALID_FILE     = "========> End write invalid Testcase in time ";
    public static final String END_SAVE_MANAGE_FILE       = "========> End Insert DB Manage File in time ";
    public static final String END_IMPORT                 = "========> End Import TestCase in time ";
    public static final String ERROR_HANDLE_FILE          = "Error. File invalid.";
    public static final String IMPORT_TESTCASE_SUCCESS    = "Import Testcase success.";
    public static final String FUNCTION_NOT_FOUND         = "Not found Function test case type";
    public static final String FUNCTION_NOT_FOUND_MESSAGE = "Please, add a test case type is Function!";
    public static final String ERROR_APLICATION_USER      = "Application user must not be null";
    public static final String ERROR_REMOVE_UPLOAD_FILE   = "File upload not exist or status upload is importing";
    public static final String ERROR_CREATE_ISSUE         = "Create issue error: ";
    public static final String ERROR_UPDATE_ISSUE         = "Update issue error: ";
    public static final String UPLOAD_FILE_NOT_EXIST      = "File upload not exist";

    //config number thread
    public static final int NUMBER_THREAD = 10;

    public static final String IMPORT_TYPE_IMPORT_DEVICE   = "Import Device";
    public static final String IMPORT_TYPE_UPDATE_ASSIGNEE = "Update Assignee";
    public static final String MESSAGE_ERROR_FILE_SIZE     = "Invalid file size. The max size is 10MB";
    public static final String MESSAGE_ERROR_MAX_ROWS      = "The max number of rows in file allowed import is ";
    public static final String MESSAGE_ERROR_EMPTY_ROW     = "Total test cases in file import equal zero.";

    public static final String PERMISSION_SCHEME_NAME_TESTCASE = "Testcase Permission Scheme";
    public static final String PERMISSION_SCHEME_NAME_DEVICE   = "Device Permission Scheme";

    public static final String SCHEME_ENTITY_TYPE_PROJECT_ROLE = "projectrole";
    // external device type
    public static final String EXTERNAL_DEVICE_TYPE            = "deviceType";
    public static final String EXTERNAL_DEVICE_ID              = "deviceId";
    public static final String EXTERNAL_DEVICE_ID_NOT_EXIST    = "Device id not exist !";
    public static final String EXTERNAL_DEVICE_ID_EXISTED      = "Device type is existed";
    public static final String EXTERNAL_DEVICE_IS_REQUIRED     = "Device type is required";
    public static final String EXTERNAL_DEVICE_RANGE           = "Device type's length should < 100";
    public static final String EXTERNAL_DEVICE_TYPE_VIEW       = "device-types";

    public static final String DEVICE_TYPE         = "deviceType";
    public static final String IMPORT_DEVICE_TYPE  = "importType";
    public static final String PART_NUMBER         = "Part Number";
    public static final String ERROR_PART_NUMBER   = "Part Number must be not empty";
    public static final String ERROR_UPDATE_DEVICE = "Error to update device";
    public static final String ERROR_CMC_CODE      = "CMC code must be not empty";
    public static final String ERROR_OS            = "OS must be not empty";
    public static final String ERROR_OS_VERSION    = "OS version must be not empty";
    public static final String ERROR_DEVICE_NAME   = "Device name must be not empty";
    public static final String INVALID_FILE        = "Invalid File";

    //Vehicle Parameter Management
    public static final String VEHICLE_PARAMETER_ID             = "vehicleParameterId";
    public static final String VEHICLE_PARAMETER_IS_NOT_EXISTED = "Vehicle Parameter is not existed";
    public static final String VEHICLE_PARAMETER_NAME           = "vehicleParameterName";
    public static final String VEHICLE_PARAMETER_IS_REQUIRED    = "Vehicle Parameter is required";
    public static final String VEHICLE_PARAMETER_IS_EXISTED     = "Vehicle Parameter is existed";
    public static final String VEHICLE_PARAMETER_MORE_THAN_100
                                                                = "Vehicle Parameter should not more than 100 characters";

    public static final String DEVICE_NAME = "Device name";

    //Message import update assignee for device
    public static final String DEVICE_PART_NUMBER_IS_NOT_EXIST = "Device part number is not exist";
    public static final String USER_IS_NOT_EXIST_IN_SYSTEM     = "User is not exist in this system";
    public static final String USER_IS_NOT_EXIST_IN_PROJECT    = "User is not exist in this project or team";

    public static final String DEVICE_SUMMARY                   = "Summary";
    public static final String DEVICE_CMC_CODE                  = "CMC code";
    public static final String DEVICE_PART_NUMBER_JAPANESE_NAME = "Part number (Japanese name)";
    public static final String DEVICE_PART_NUMBER               = "Part number";
    public static final String DEVICE_PART_NUMBER_INVOICE       = "Part number (for Invoice)";
    public static final String DEVICE_PART_NUMBER_INVOICE_GROUP = "Part number (for Invoice)(group)";
    public static final String DEVICE_NOTE                      = "Note";
    public static final String DEVICE_PRDCV_CODE                = "PRDCV code";
    public static final String DEVICE_OS                        = "OS";
    public static final String DEVICE_OS_VERSION                = "OS version";
    public static final String DEVICE_WIFI_MAC                  = "Wifi Mac address";
    public static final String DEVICE_BLUETOOTH                 = "Bluetooth Mac address";
    public static final String DEVICE_BLUETOOTH_VERSION         = "Bluetooth version";
    public static final String DEVICE_HFP_VERSION               = "HFP version";
    public static final String DEVICE_AVRCP                     = "AVRCP version";
    public static final String DEVICE_A2DP_VERSION              = "A2DP version";
    public static final String DEVICE_MAP_VERSION               = "MAP Version";
    public static final String DEVICE_PBAP_VERSION              = "PBAP Version";
    public static final String DEVICE_AT_COMMAND                = "AT Command";
    public static final String DEVICE_MIRRORLINK                = "MirrorLink";
    public static final String DEVICE_CAR_PLAY                  = "CarPlay";
    public static final String DEVICE_ANDROID_AUTO              = "AndroidAuto";

    public static final String DEVICE_PERMISSION = "You don't have permission to import device";

    //Test Hu Market Management
    public static final String TEST_HU_MARKET_ID             = "testHuMarketId";
    public static final String TEST_HU_MARKET_IS_NOT_EXISTED = "Test Hu Market is not existed";
    public static final String TEST_HU_MARKET_NAME           = "testHuMarketName";
    public static final String TEST_HU_MARKET_IS_REQUIRED    = "Test Hu Market is required";
    public static final String TEST_HU_MARKET_IS_EXISTED     = "Test Hu Market is existed";
    public static final String TEST_HU_MARKET_MORE_THAN_100  = "Test Hu Market should not more than 100 characters";

    // assign permission device
    public static final String ERROR_ASSIGN                 = "error";
    public static final String ERROR_ASSIGN_CANT_ASSSIGN    = "You dont have permission to assign this issue !";
    public static final String ERROR_ASSIGN_CANT_UNASSSIGN
                                                            = "You can't unassign this issue, please choose a member in you team !";
    public static final String ERROR_ASSIGN_TO_ANOTHER_TEAM
                                                            = "You can't assign this issue to another team member, please choose a member in you team !";

    // Import test result
    public static final List<String> TEST_RESULTS   = Arrays.asList("OK", "NG", "PN", "ER", "NT", "DG", "DL");
    public static final List<String> TEST_RESULTS_PN   = Arrays.asList("PN(QA)", "PN(Bug)", "PN(Equipment)", "PN(Internal)", "PN(DG");
    public static final List<String> TEST_RESULTS_OUTOFSCOPE   = Arrays.asList("ER", "NT", "DG", "DL");
    public static final String       TEST_RESULT_OK = "OK";
    public static final String       TEST_RESULT_NG = "NG";
    public static final String       TEST_RESULT_PN = "PN";
    public static final String       TEST_RESULT_PN_QA = "PN(QA)";
    public static final String       TEST_RESULT_PN_BUG = "PN(Bug)";
    public static final String       TEST_RESULT_PN_EQUIPMENT = "PN(Equipment)";
    public static final String       TEST_RESULT_PN_INTERNAL = "PN(Internal)";
    public static final String       TEST_RESULT_PN_DG = "PN(DG)";

    public static final String ERR_KEY_TEST_CASE                                    = "Test case";
    public static final String MUST_NOT_BE_BLANK                                    = " must not be blank. ";
    public static final String MUST_BE_POSITIVE_NUMBER                              = " must be number. ";
    public static final String ERR_TEST_CASE_IS_NOT_EXIST                           = ERR_KEY_TEST_CASE +
                                                                                      " is not exist. ";
    public static final String ERR_KEY_RESULT                                       = "Result";
    public static final String ERR_RESULT_MUST_NOT_BE_BLANK                         = ERR_KEY_RESULT +
                                                                                      MUST_NOT_BE_BLANK;
    public static final String ERR_RESULT_IS_INVALID                                = ERR_KEY_RESULT + " is invalid. ";
    public static final String ERR_KEY_HU_ID                                        = "HU ID";
    public static final String ERR_HU_ID_MUST_NOT_BE_BLANK                          = ERR_KEY_HU_ID + MUST_NOT_BE_BLANK;
    public static final String ERR_KEY_HU_INDEX                                     = "HU Index";
    public static final String ERR_HU_INDEX_MUST_NOT_BE_BLANK                       = ERR_KEY_HU_INDEX +
                                                                                      MUST_NOT_BE_BLANK;
    public static final String ERR_KEY_EXTERNAL_TEST_DEVICE_ID                      = "External Test Device ID";
    public static final String ERR_KEY_EXTERNAL_DEVICE_CODE                      = "External Device Code";
    public static final String ERR_EXTERNAL_TEST_DEVICE_ID_MUST_NOT_BE_BLANK        = ERR_KEY_EXTERNAL_TEST_DEVICE_ID +
                                                                                      MUST_NOT_BE_BLANK;
    public static final String ERR_EXTERNAL_DEVICE_CODE_MUST_NOT_BE_BLANK        = ERR_KEY_EXTERNAL_TEST_DEVICE_ID + MUST_NOT_BE_BLANK;

    public static final String ERR_EXTERNAL_TEST_DEVICE_ID_MUST_BE_POSITIVE_INTEGER = ERR_KEY_EXTERNAL_TEST_DEVICE_ID +
                                                                                      "must be positive integer.";
    public static final String ERR_KEY_TEST_DURATION                                = "Test Duration";
    public static final String ERR_TEST_DURATION_MUST_NOT_BE_BLANK                  = ERR_KEY_TEST_DURATION +
                                                                                      MUST_NOT_BE_BLANK;
    public static final String ERR_TEST_DURATION_MUST_BE_NUMBER                     = ERR_KEY_TEST_DURATION +
                                                                                      MUST_BE_POSITIVE_NUMBER;
    public static final String ERR_KEY_RTC_CODE                                     = "RTC Code";
    public static final String ERR_KEY_RTC_CODE_MUST_NOT_BE_BLANK         = ERR_KEY_RTC_CODE + MUST_NOT_BE_BLANK;

    public static final String ERR_KEY_TIME_FOR_QA_SUBMISSION                       = "Time for Q&A submission";
    public static final String ERR_TIME_FOR_QA_SUBMISSION_MUST_NOT_BE_BLANK         = ERR_KEY_TIME_FOR_QA_SUBMISSION +
                                                                                      MUST_NOT_BE_BLANK;
    public static final String ERR_TIME_FOR_QA_SUBMISSION_MUST_BE_NUMBER            = ERR_KEY_TIME_FOR_QA_SUBMISSION +
                                                                                      MUST_BE_POSITIVE_NUMBER;
    public static final String ERR_KEY_TIME_FOR_DEFECT_SUBMISSION                   = "Time for defect submission";
    public static final String ERR_TIME_FOR_DEFECT_SUBMISSION_MUST_NOT_BE_BLANK     =
            ERR_KEY_TIME_FOR_DEFECT_SUBMISSION + MUST_NOT_BE_BLANK;
    public static final String ERR_TIME_FOR_DEFECT_SUBMISSION_MUST_BE_NUMBER        =
            ERR_KEY_TIME_FOR_DEFECT_SUBMISSION + MUST_BE_POSITIVE_NUMBER;
    public static final String ERR_KEY_BLOCKING_TICKET_ID                           = "Blocking Ticket ID";
    public static final String ERR_KEY_EXTERNAL_TICKET_ID                           = "External Ticket ID";
    public static final String ERR_KEY_EVALUATION_SOFTWARE                           = "Evaluation Software";
    public static final String ERR_KEY_EVALUATION_HARD                           = "Evaluation Hard";
    public static final String ERR_KEY_PERIPHERAL_DEVICE_NUMBER                           = "Peripheral Device Number";
    public static final String ERR_BLOCKING_TICKET_ID_MUST_NOT_BE_BLANK             = ERR_KEY_BLOCKING_TICKET_ID +
                                                                                      MUST_NOT_BE_BLANK;
    public static final String ERR_EXTERNAL_TICKET_ID_MUST_NOT_BE_BLANK             = ERR_KEY_EXTERNAL_TICKET_ID + MUST_NOT_BE_BLANK;
    public static final String ERR_EVALUATION_SOFTWARE_MUST_NOT_BE_BLANK             = ERR_KEY_EVALUATION_SOFTWARE + MUST_NOT_BE_BLANK;
    public static final String ERR_EVALUATION_HARD_MUST_NOT_BE_BLANK             = ERR_KEY_EVALUATION_HARD + MUST_NOT_BE_BLANK;
    public static final String ERR_PERIPHERAL_DEVICE_NUMBER_MUST_NOT_BE_BLANK             = ERR_KEY_PERIPHERAL_DEVICE_NUMBER + MUST_NOT_BE_BLANK;

    public static final String ERR_KEY_RESULT_COMMENT                               = "Result Comment";
    public static final String ERR_KEY_TESTER_COMMENT                               = "Tester Comment";
    public static final String ERR_RESULT_COMMENT_MUST_NOT_BE_BLANK                 = ERR_KEY_RESULT_COMMENT +
                                                                                      MUST_NOT_BE_BLANK;
    public static final String ERR_TESTER_COMMENT_MUST_NOT_BE_BLANK                 = ERR_KEY_TESTER_COMMENT +
            MUST_NOT_BE_BLANK;
    public static final String ERR_KEY_SET_OF_DEVICE                                = "Set of device";
    public static final String ERR_SET_OF_DEVICE_IS_NOT_EXIST                       = ERR_KEY_SET_OF_DEVICE +
                                                                                      " is not exist. ";
    public static final String ERR_TEST_CASE_STATUS_MUST_BE_WAITING_TO_TEST
                                                                                    = "Test case status must be WAITING TO TEST. ";
    public static final String ERROR_UPDATE_TEST_RESULT                             = "An error occurred. ";
    public static final String STRING_TO_JOIN                                       = ", ";
    public static final String SRING_ONE_SPACE                                      = " ";
    public static final String CUSTOM_FIELD_WRONG_TIME_FOR_QNA_SUBMISSION           = "Time For Qn ASubmission";
    public static final String CELL_HEADER_START                                    = "テストケース番号";

    public static final String REQUEST_PARAMETER_PROJECT_ID  = "projectId";
    public static final String REQUEST_PARAMETER_ASSIGNEE_ID = "assigneeId";
    public static final String REQUEST_PARAMETER_USERNAMES   = "usernames";
    public static final String REQUEST_PARAMETER_START_DATE  = "startDate";
    public static final String REQUEST_PARAMETER_END_DATE    = "endDate";
    public static final String DATE_FORMATTER_D_MM_YYYY      = "d/MM/yyyy";

    public static final String TEST_CASE_TYPE_FUNCTION                      = "Function";
    public static final String TEST_CASE_TYPE_FUNCTION_LOWER                = "function";
    public static final String TEST_CASE_TYPE_FUNCTION_TITLE                = TEST_CASE_TYPE_FUNCTION +
                                                                              " Testing Execution Productivity";
    public static final String TEST_CASE_TYPE_FUNCTION_DEFINITION_SCOPE     = "Scope: " +
                                                                              TEST_CASE_TYPE_FUNCTION_LOWER +
                                                                              " testing";
    public static final String TEST_CASE_TYPE_FUNCTION_DEFINITION_WORK_SIZE = "Work size: OK and NG result";
    public static final String TEST_CASE_TYPE_FUNCTION_DEFINITION_EFFORT
                                                                            = "Effort: execution, Q&A handling, defect handling";

    public static final int MINUTES_IN_AN_HOUR = 60;

    public static final String ERR_MESSAGE_ASSIGNEE_NOT_EXIST                  = "Assignee does not exist.";
    public static final int    SCHEDULE_JOB_IMPORT_START_HOUR                  = 0;
    public static final int    SCHEDULE_JOB_IMPORT_START_MINUTE                = 0;
    public static final int    SCHEDULE_JOB_IMPORT_START_SECOND                = 30;
    public static final int    SCHEDULE_JOB_IMPORT_PERIOD_TIME_SAFE_TO_DELETED = 120;

    public static final int    DEFAULT_INTERVAL_IN_SECONDS         = 900;
    public static final String MESSAGE_UPLOAD_BIG_FILE             = MESSAGE_UPLOAD_SUCCESS +
                                                                     " The file is a big file, it's going to queue to perform import by schedule.";
    public static final String MESSAGE_UPLOAD_NORMAL_FILE          = MESSAGE_UPLOAD_SUCCESS +
                                                                     " The file is a normal file, it's going to queue to perform import.";
    public static final String MESSAGE_REMOVE_FILE_IMPORTING_ERROR = "Can not delete file, it is importing";
    public static final String MESSAGE_REMOVE_FILE_SUCCESS         = "Delete file success, file name: ";
    public static final String MESSAGE_REMOVE_FILE_ERROR
                                                                   = "Can not delete file, it's going to perform import";
    public static final String MESSAGE_IMPORT_SUCCESS              = "Import successfully";
    public static final long   WAITING_THREAD_DONE_TIME            = 30000;
    public static final int    WAITING_THREAD_DONE_COUNT           = 10;

    public static final String TEST_PLAN_HEADER_FIRST_COLUMN   = "Test case No";
    public static final String TEST_RESULT_HEADER_FIRST_COLUMN = "Test case No";

    public static final List<String> SET_OF_DEVICE_ATTRIBUTES                = Arrays.asList(CUSTOM_FIELD_HU_DEVICE,
                                                                                             CUSTOM_FIELD_SCREEN_DEVICE,
                                                                                             BUILD_NUMBER_ATTRIBUTE,
                                                                                             CUSTOM_FIELD_VEHICLE_PARAMETER);
    public static final String       QUERY_CUSTOM_FIELD_PREFIX               = "cf[";
    public static final String       QUERY_CUSTOM_FIELD_SUFFIX               = "]";
    public static final String       ERR_SET_OF_DEVICE_ALREADY_EXISTS_PREFIX
                                                                             = "Set of device with same already exists. Please visit ";
    public static final String       ERR_SET_OF_DEVICE_ALREADY_EXISTS_SUFFIX = "/browse/";

    public static final String EXCEL_XLSX_CONTENT_TYPE
                                                   = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String CELL_NOT_APPLICABLE = "N/A";

    //Model management
    public static final String MODEL_ID             = "modelId";
    public static final String MODEL_IS_NOT_EXISTED = "Model is not existed";
    public static final String MODEL_NAME           = "modelName";
    public static final String MODEL_IS_REQUIRED    = "Model is required";
    public static final String MODEL_IS_EXISTED     = "Model is existed";
    public static final String MODEL_MORE_THAN_100  = "Model should not more than 100 characters";
    public static final int    NUMBER_ISSUE_TYPE    = 6;
    public static final String PAGE_VIEW_LIST       = "success";
    public static final String PAGE_NOT_EXIST       = "page-not-exist";
    public static final int    MAX_CHARACTER        = 100;

    public static final String ROLE_ERROR_MESSAGE               = "Permission error!";
    public static final String ROLE_REPORT_MESSAGE              = "Please check your role in project again!";
    public static final String BLOCKING_TICKET_ID_COLUMN        = "Blocking Ticket Id";
    public static final String BLOCKING_TICKET_TYPE_COLUMN      = "Blocking Ticket Type";
    public static final String BLOCKING_TICKET_STATUS_COLUMN    = "Blocking Ticket Status";
    public static final String SYNC_TEST_RESULT                 = "Error sync test result: ";
    public static final String SYNC_TEST_RESULT_VALIDATE        = "Error validate sync test result: ";
    public static final int    DEFAULT_INTERVAL_SYNC_IN_SECONDS = 3600;
    public static final String PATH_FILE_CSV                    = System.getProperty("user.home") + File.separator +
                                                                  "sync_test_result";
    public static final String FILE_NAME_CSV                    = "data_sync_test_result.csv";

    public static final String LIST_MEMBER                   = "lstMember";
    public static final String QUOTATION_MARKS               = "";
    public static final String APOSTROPHE                    = "'";
    public static final String FORMATTER_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String PATH_BACKUP_FILE_CSV          = PATH_FILE_CSV + File.separator + "backup";
    public static final String TIMESTAMP_FORMAT              = "yyyyMMddhhmmss";
    public static final String TIMESTAMP_MILISECOND_FORMAT   = "yyyyMMdd.hhmmssSSS";
    public static final String CSV_EXTENSION                 = "csv";

    public static final String OK                            = "OK";
    public static final String NG                            = "NG";
    public static final String NT                            = "NT";
    public static final String PN                            = "PN";
    public static final String ER                            = "ER";
    public static final String DG                            = "DG";
    public static final String DL                            = "DL";
}
