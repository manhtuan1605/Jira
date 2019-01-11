package com.cmcglobal.plugins.api;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.group.GroupService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.cmcglobal.plugins.entity.TestCase;
import com.cmcglobal.plugins.jira.scheduler.ImportBigFileSalJobs;
import com.cmcglobal.plugins.jira.scheduler.SyncTestResultSalJobs;
import com.cmcglobal.plugins.service.IssueHelperService;
import com.cmcglobal.plugins.service.TestCaseService;
import com.cmcglobal.plugins.utils.Constants;
import com.cmcglobal.plugins.utils.Utilities;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.cmcglobal.plugins.utils.Constants.DEFAULT_INTERVAL_IN_SECONDS;
import static com.cmcglobal.plugins.utils.Constants.DEFAULT_INTERVAL_SYNC_IN_SECONDS;

@Component
public class JiraCustomPluginListener implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(JiraCustomPluginListener.class);

    @JiraImport
    private final EventPublisher eventPublisher;

    @JiraImport
    private IssueTypeManager issueTypeManager;

    @JiraImport
    private GroupManager groupManager;

    @JiraImport
    private GroupService groupService;

    @JiraImport
    private final CustomFieldManager customFieldManager;

    @JiraImport
    private final AvatarManager avatarManager;

    private static List<CustomField> customFields = new ArrayList<>();

    @Autowired
    private IssueHelperService issueHelperService;

    @Autowired
    private TestCaseService testCaseService;

    @Autowired
    private ImportBigFileSalJobs importBigFileSalJobs;

    @Autowired
    private SyncTestResultSalJobs syncTestResultSalJobs;

    @Autowired
    public JiraCustomPluginListener(EventPublisher eventPublisher, IssueTypeManager issueTypeManager,
                                    GroupManager groupManager, GroupService groupService,
                                    CustomFieldManager customFieldManager, AvatarManager avatarManager) {
        this.eventPublisher = eventPublisher;
        this.issueTypeManager = issueTypeManager;
        this.groupManager = groupManager;
        this.groupService = groupService;
        this.customFieldManager = customFieldManager;
        this.avatarManager = avatarManager;
    }

    /**
     * Called when the plugin has been enabled.
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Enabling plugin");
        //do some stuffs
        //EDIT COMMENT FIELD'S DESCRIPTION
        EditableDefaultFieldLayout editableFieldLayout = ComponentAccessor.getFieldLayoutManager()
                                                                          .getEditableDefaultFieldLayout();
        List<FieldLayoutItem> fieldLayoutItems = editableFieldLayout.getFieldLayoutItems();
        for (FieldLayoutItem fieldLayoutItem : fieldLayoutItems) {
            if (fieldLayoutItem.getOrderableField().getName().equalsIgnoreCase("Comment")) {
                editableFieldLayout.setDescription(fieldLayoutItem, checkAssignee());
                ComponentAccessor.getFieldLayoutManager().storeEditableDefaultFieldLayout(editableFieldLayout);
                break;
            }
        }

        //CREATE GROUP
        if (!groupManager.groupExists(Constants.GROUP_LEADER)) {
            groupManager.createGroup(Constants.GROUP_LEADER);
        }
        if (!groupManager.groupExists(Constants.GROUP_TESTER)) {
            groupManager.createGroup(Constants.GROUP_TESTER);
        }

        //CREATE ROLE
        ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager.class);
        if (projectRoleManager.getProjectRole(Constants.ROLE_PM) == null) {
            projectRoleManager.createRole(new ProjectRoleImpl(Constants.ROLE_PM, ""));
        }
        if (projectRoleManager.getProjectRole(Constants.ROLE_QC_LEADER) == null) {
            projectRoleManager.createRole(new ProjectRoleImpl(Constants.ROLE_QC_LEADER, ""));
        }
        if (projectRoleManager.getProjectRole(Constants.PROJECT_ROLE_QC) == null) {
            projectRoleManager.createRole(new ProjectRoleImpl(Constants.PROJECT_ROLE_QC, ""));
        }

        //ENABLE EXPORT EXCEL
        PropertySet propertySet = ComponentAccessor.getComponent(PropertiesManager.class).getPropertySet();
        if (!propertySet.getBoolean(Constants.PROPERTY_EXPORT_EXCEL)) {
            propertySet.setBoolean(Constants.PROPERTY_EXPORT_EXCEL, true);
        }

        //GET ISSUE TYPE
        IssueType issueTypeTestcase = getIssueType(Constants.ISSUE_TYPE_TESTCASE_NAME,
                                                   Constants.ISSUE_TYPE_TESTCASE_DESCRIPTION, 10303L);
        IssueType issueTypeTask = getIssueType(Constants.ISSUE_TYPE_TASK_NAME, Constants.ISSUE_TYPE_TASK_DESCRIPTION,
                                               10318L);
        IssueType issueTypeQnA = getIssueType(Constants.ISSUE_TYPE_QNA_NAME, Constants.ISSUE_TYPE_QNA_DESCRIPTION,
                                              10320L);
        IssueType issueTypeDefect = getIssueType(Constants.ISSUE_TYPE_DEFECT_NAME,
                                                 Constants.ISSUE_TYPE_DEFECT_DESCRIPTION, 10304L);
        IssueType issueTypeHuDevice = getIssueType(Constants.HU_DEVICE, Constants.HU_DEVICE_DESCRIPTION, 10322L);
        IssueType issueTypeScreenDevice = getIssueType(Constants.SCREEN_DEVICE, Constants.SCREEN_DEVICE_DESCRIPTION,
                                                       10321L);
        IssueType issueTypeHuTypeVehicle = getIssueType(Constants.HU_TYPE_VEHICLE,
                                                        Constants.HU_TYPE_VEHICLE_DESCRIPTION, 10311L);
        IssueType issueTypeSiSysInfo = getIssueType(Constants.SI_SYS_INFO, Constants.SI_SYS_INFO_DESCRIPTION, 10315L);
        IssueType issueTypeSiAmigoInfo = getIssueType(Constants.SI_AMIGO_INFO, Constants.SI_AMIGO_INFO_DESCRIPTION,
                                                      10310L);
        IssueType issueTypeSetOfDevice = getIssueType(Constants.SET_OF_DEVICE, Constants.SET_OF_DEVICE_DESCRIPTION,
                                                      10307L);
        IssueType issueTypeAutomotiveDevice = getIssueType(Constants.AUTOMOTIVE_DEVICE,
                                                           Constants.AUTOMOTIVE_DEVICE_DESCRIPTION, 10308L);
        IssueType issueTypePeripheralDevice = getIssueType(Constants.PERIPHERAL_DEVICE,
                                                           Constants.PERIPHERAL_DEVICE_DESCRIPTION, 10323L);
        IssueType issueTypeExternalDevice = getIssueType(Constants.ISSUE_TYPE_EXTERNAL_DEVICE,
                                                         Constants.EXTERNAL_DEVICE_DESCRIPTION, 10313L);

        //CREATE CUSTOM FIELD
        List<JiraContextNode> contexts = new ArrayList<>();
        contexts.add(GlobalIssueContext.getInstance());
        CustomFieldType fieldTypeText = this.customFieldManager.getCustomFieldType(Constants.CUSTOM_FIELD_TYPE_TEXT);
        CustomFieldType fieldTypeSelect = this.customFieldManager.getCustomFieldType(
                Constants.CUSTOM_FIELD_TYPE_SELECT);
        CustomFieldType fieldTypeTextarea = this.customFieldManager.getCustomFieldType(
                Constants.CUSTOM_FIELD_TYPE_TEXTAREA);
        CustomFieldType fieldTypeDate = this.customFieldManager.getCustomFieldType(
                Constants.CUSTOM_FIELD_TYPE_DATE_PICKER);
        CustomFieldType fieldTypeMultiSelect = this.customFieldManager.getCustomFieldType(
                Constants.CUSTOM_FIELD_TYPE_MULTI_SELECT);
        CustomFieldType fieldTypeNumber = this.customFieldManager.getCustomFieldType(
                Constants.CUSTOM_FIELD_TYPE_NUMBER);
        CustomFieldSearcher fieldTypeTextSearcher = this.customFieldManager.getCustomFieldSearcher(
                Constants.CUSTOM_FIELD_TYPE_TEXT_SEARCHER);
        CustomFieldSearcher fieldTypeDateSearcher = this.customFieldManager.getCustomFieldSearcher(
                Constants.CUSTOM_FIELD_TYPE_DATE_PICKER_SEARCHER);
        CustomFieldSearcher fieldTypeMultiSelectSearcher = this.customFieldManager.getCustomFieldSearcher(
                Constants.CUSTOM_FIELD_TYPE_MULTI_SELECT_SEARCHER);
        CustomFieldSearcher fieldTypeNumberSearcher = this.customFieldManager.getCustomFieldSearcher(
                Constants.CUSTOM_FIELD_TYPE_NUMBER_SEARCHER);
        CustomField customField;

        // CREATE CUSTOM FIELD BASE ON ISSUE TYPE
        for (String attribute : Utilities.listTestcaseAttribute()) {
            if (!this.customFieldManager.getCustomFieldObjectsByName(attribute).isEmpty()) {
                continue;
            }

            if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_PHASE) ||
                attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_TESTCASE_TYPE)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeSelect,
                                                                        fieldTypeMultiSelectSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTestcase));
            } else if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_START_DATE) ||
                       attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_END_DATE) ||
                       attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_UPDATE_DATE)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeDate,
                                                                        fieldTypeDateSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTestcase));
            } else if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_MIRROR_LINK) ||
                       attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_ANDROID_AUTO)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Arrays.asList(issueTypeTestcase,
                                                                                      issueTypePeripheralDevice));
            } else if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_TESTCASE_ID)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeNumber,
                                                                        fieldTypeNumberSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTestcase));
            } else {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeTextarea,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTestcase));
                //SET CUSTOM FIELD'S ROW TO 1
                this.customFieldManager.updateCustomField(customField.getIdAsLong(), attribute,
                                                          setCustomFieldRow(customField), fieldTypeTextSearcher);
            }
            customFields.add(customField);
        }

        // CREATE CUSTOM FIELD BASE ON ISSUE TYPE
        boolean flagReadOnly = false;
        boolean flagNumber = false;
        boolean flagDateOfImplementation = false;
        int order = 0;
        for (String attribute : Utilities.listTestResultAttribute()) {
            if (!this.customFieldManager.getCustomFieldObjectsByName(attribute).isEmpty()) {
                order++;
                continue;
            }

            if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_LATEST_RESULT)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeSelect,
                                                                        fieldTypeMultiSelectSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTestcase));

                List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();
                if (!schemes.isEmpty()) {
                    FieldConfigScheme scheme = schemes.get(0);
                    Map configs = scheme.getConfigsByConfig();
                    if (configs != null && !configs.isEmpty()) {
                        FieldConfig config = (FieldConfig) configs.keySet().iterator().next();
                        OptionsManager optionsManager = ComponentManager.getComponentInstanceOfType(
                                OptionsManager.class);
                        long numberAdded = 100;
                        optionsManager.createOptions(config, null, numberAdded, Constants.RESULT_TYPES);
                    }
                }
            } else if (attribute.equalsIgnoreCase(Constants.HU_ID_ATTRIBUTE)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Arrays.asList(issueTypeTestcase,
                                                                                      issueTypeHuDevice));
                flagReadOnly = true;
            } else if (attribute.equalsIgnoreCase(Constants.VEHICLE_ATTRIBUTE)) {
                customFields.add(this.customFieldManager.createCustomField(attribute, null, fieldTypeSelect,
                                                                           fieldTypeMultiSelectSearcher, contexts,
                                                                           Arrays.asList(issueTypeHuTypeVehicle,
                                                                                         issueTypeSetOfDevice)));
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTestcase));
                flagReadOnly = true;
            } else if (attribute.equalsIgnoreCase(Constants.AMP_TYPE_ATTRIBUTE)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTestcase));
                CustomField customfieldSelect = this.customFieldManager.createCustomField(attribute, null,
                                                                                          fieldTypeSelect,
                                                                                          fieldTypeMultiSelectSearcher,
                                                                                          contexts, Arrays.asList(
                                issueTypeHuDevice, issueTypeHuTypeVehicle, issueTypeSiAmigoInfo, issueTypeSetOfDevice));
                customFields.add(customfieldSelect);

                List<FieldConfigScheme> schemes = customfieldSelect.getConfigurationSchemes();
                if (!schemes.isEmpty()) {
                    FieldConfigScheme scheme = schemes.get(0);
                    Map configs = scheme.getConfigsByConfig();
                    if (configs != null && !configs.isEmpty()) {
                        FieldConfig config = (FieldConfig) configs.keySet().iterator().next();
                        OptionsManager optionsManager = ComponentManager.getComponentInstanceOfType(
                                OptionsManager.class);
                        long numberAdded = 100;
                        optionsManager.createOptions(config, null, numberAdded, Constants.AMP_TYPES);
                    }
                }
                flagReadOnly = true;
            } else if (attribute.equalsIgnoreCase(Constants.HU_INDEX) ||
                       attribute.equalsIgnoreCase(Constants.SI_INDEX)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Arrays.asList(issueTypeTestcase,
                                                                                      issueTypeSetOfDevice));
                flagReadOnly = true;
            } else if (attribute.equals(Constants.CUSTOM_FIELD_EXTERNAL_TICKET_ID)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Arrays.asList(issueTypeTestcase, issueTypeQnA,
                                                                                      issueTypeDefect,
                                                                                      issueTypeHuDevice,
                                                                                      issueTypeScreenDevice,
                                                                                      issueTypeHuTypeVehicle,
                                                                                      issueTypeSiSysInfo,
                                                                                      issueTypeSiAmigoInfo,
                                                                                      issueTypeSetOfDevice));
            } else if (attribute.equals(Constants.CUSTOM_FIELD_PENDING_TYPE)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeSelect,
                                                                        fieldTypeMultiSelectSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTestcase));

                List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();
                if (!schemes.isEmpty()) {
                    FieldConfigScheme scheme = schemes.get(0);
                    Map configs = scheme.getConfigsByConfig();
                    if (configs != null && !configs.isEmpty()) {
                        FieldConfig config = (FieldConfig) configs.keySet().iterator().next();
                        OptionsManager optionsManager = ComponentManager.getComponentInstanceOfType(
                                OptionsManager.class);
                        long numberAdded = 100;
                        optionsManager.createOptions(config, null, numberAdded, Constants.PENDING_TYPES);
                    }
                }
                //update description to this custom field.
                this.customFieldManager.updateCustomField(customField.getIdAsLong(), attribute, addDescriptionInPendingTypeScreen(customField), fieldTypeMultiSelectSearcher);
            } else if (attribute.equals(Constants.CUSTOM_FIELD_RESULT_COMMENT)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeTextarea,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTestcase));
            } else if (attribute.equals(Constants.CUSTOM_FIELD_HU_MARKET)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTestcase));
                flagReadOnly = true;
            } else if (attribute.equals(Constants.CUSTOM_FIELD_EXTERNAL_TEST_DEVICE_ID)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeNumber,
                                                                        fieldTypeNumberSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTestcase));
            } else if (attribute.equals(Constants.CUSTOM_FIELD_TEST_DURATION)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeNumber,
                                                                        fieldTypeNumberSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTestcase));
                flagNumber = true;
            } else if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_DATE_OF_IMPLEMENTATION)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeDate,
                                                                        fieldTypeDateSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTestcase));
                List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();
                if (!schemes.isEmpty()) {
                    FieldConfigScheme scheme = schemes.get(0);
                    Map configs = scheme.getConfigsByConfig();
                    if (configs != null && !configs.isEmpty()) {
                        FieldConfig config = (FieldConfig) configs.keySet().iterator().next();
                        customField.getCustomFieldType().setDefaultValue(config, new Date());
                    }
                }
                flagDateOfImplementation = true;
            } else if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_TESTER_COMMENTS)){
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeTextarea,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTestcase));
            } else {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTestcase));
            }
            customFields.add(customField);
            //HIDE CUSTOM FIELD VALUE WHEN OPEN DIALOG
            if (order > 3) {
                this.customFieldManager.updateCustomField(customField.getIdAsLong(), attribute,
                                                          hideCustomFieldValue(customField, flagReadOnly, flagNumber,
                                                                               flagDateOfImplementation),
                                                          (attribute.equalsIgnoreCase(
                                                                  Constants.CUSTOM_FIELD_PENDING_TYPE)) ?
                                                          fieldTypeMultiSelectSearcher :
                                                          fieldTypeTextSearcher);
            }
            flagReadOnly = false;
            flagNumber = false;
            flagDateOfImplementation = false;
            order++;
        }

        // CREATE CUSTOM FIELD BASE ON ISSUE TYPE
        for (String attribute : Utilities.listQnAAttribute()) {
            if (this.customFieldManager.getCustomFieldObjectsByName(attribute).isEmpty()) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(issueTypeQnA));
                customFields.add(customField);
            }
        }

        // CREATE CUSTOM FIELD BASE ON ISSUE TYPE
        for (String attribute : Utilities.listDefectAttribute()) {
            if (this.customFieldManager.getCustomFieldObjectsByName(attribute).isEmpty()) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(issueTypeDefect));
                customFields.add(customField);
            }
        }

        //Environment Project
        // CREATE CUSTOM FIELD BASE ON ISSUE TYPE
        for (String attribute : Utilities.listHuDeviceAttribute()) {
            if (!this.customFieldManager.getCustomFieldObjectsByName(attribute).isEmpty()) {
                continue;
            }

            if (attribute.equalsIgnoreCase(Constants.DEVICE_TYPE_ATTRIBUTE) ||
                attribute.equalsIgnoreCase(Constants.DEVICE_SUB_TYPE_ATTRIBUTE)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Arrays.asList(issueTypeHuDevice,
                                                                                      issueTypeScreenDevice,
                                                                                      issueTypeHuTypeVehicle,
                                                                                      issueTypeSiAmigoInfo,
                                                                                      issueTypeSetOfDevice));
            } else if (attribute.equalsIgnoreCase(Constants.TEST_HU_MARKET_ATTRIBUTE)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeMultiSelect,
                                                                        fieldTypeMultiSelectSearcher, contexts,
                                                                        Arrays.asList(issueTypeHuTypeVehicle,
                                                                                      issueTypeHuDevice,
                                                                                      issueTypeSetOfDevice));
                //SET CUSTOM FIELD'S SEARCHABLE
                this.customFieldManager.updateCustomField(customField.getIdAsLong(), attribute,
                                                          searchableSelect(customField), fieldTypeMultiSelectSearcher);
            } else {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(issueTypeHuDevice));
            }
            customFields.add(customField);
        }

        // CREATE CUSTOM FIELD BASE ON ISSUE TYPE
        for (String attribute : Utilities.listScreenDeviceAttribute()) {
            if (!this.customFieldManager.getCustomFieldObjectsByName(attribute).isEmpty()) {
                continue;
            }
            if (attribute.equalsIgnoreCase(Constants.SYSTEM_DEVICE_NAME_ATTRIBUTE) ||
                attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_DEVICE_ID) ||
                attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_SCREEN_DEVICE_TYPE)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Arrays.asList(issueTypeScreenDevice,
                                                                                      issueTypeSetOfDevice));
            } else if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_DEVICE_NAME)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Arrays.asList(issueTypeScreenDevice,
                                                                                      issueTypeSetOfDevice,
                                                                                      issueTypePeripheralDevice));
            } else {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(
                                                                                issueTypeScreenDevice));
            }
            customFields.add(customField);
        }

        // CREATE CUSTOM FIELD BASE ON ISSUE TYPE
        for (String attribute : Utilities.listHuTypeVehicleAttribute()) {
            if (!this.customFieldManager.getCustomFieldObjectsByName(attribute).isEmpty()) {
                continue;
            }
            if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_SYS_DATA_PREFIX)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Arrays.asList(issueTypeHuTypeVehicle,
                                                                                      issueTypeSetOfDevice));
            } else {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(
                                                                                issueTypeHuTypeVehicle));
            }
            customFields.add(customField);
        }

        // CREATE CUSTOM FIELD BASE ON ISSUE TYPE
        for (String attribute : Utilities.listSiSysInfoAttribute()) {
            if (!this.customFieldManager.getCustomFieldObjectsByName(attribute).isEmpty()) {
                continue;
            }

            if (attribute.equalsIgnoreCase(Constants.RELEASE_DATE_ATTRIBUTE)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeDate,
                                                                        fieldTypeDateSearcher, contexts,
                                                                        Arrays.asList(issueTypeSiSysInfo,
                                                                                      issueTypeSiAmigoInfo));
            } else if (attribute.equalsIgnoreCase(Constants.BUILD_NUMBER_ATTRIBUTE)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Arrays.asList(issueTypeSiSysInfo,
                                                                                      issueTypeSiAmigoInfo,
                                                                                      issueTypeSetOfDevice));
            } else if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_SYS_SET_ID_VERSION) ||
                       attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_SYS_DATA_VERSION) ||
                       attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_SYS_SOFT)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Arrays.asList(issueTypeSiSysInfo,
                                                                                      issueTypeSetOfDevice));
            } else {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(issueTypeSiSysInfo));
            }
            customFields.add(customField);
        }

        for (String attribute : Utilities.listSiAmigoInfoAttribute()) {
            if (this.customFieldManager.getCustomFieldObjectsByName(attribute).isEmpty()) {
                if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_AMIGO_PG_VERSION) ||
                    attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_AMIGO_DATA_VERSION)) {
                    customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                            fieldTypeTextSearcher, contexts,
                                                                            Arrays.asList(issueTypeSiAmigoInfo,
                                                                                          issueTypeSetOfDevice));
                } else {
                    customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                            fieldTypeTextSearcher, contexts,
                                                                            Collections.singletonList(
                                                                                    issueTypeSiAmigoInfo));
                }
                customFields.add(customField);
            }
        }

        for (String attribute : Utilities.listSetOfDeviceAttribute()) {
            if (!this.customFieldManager.getCustomFieldObjectsByName(attribute).isEmpty()) {
                continue;
            }
            if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_MODEL)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeSelect,
                                                                        fieldTypeMultiSelectSearcher, contexts,
                                                                        Collections.singletonList(
                                                                                issueTypeSetOfDevice));
            } else {
                //            if (this.customFieldManager.getCustomFieldObjectsByName(attribute).isEmpty()) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(
                                                                                issueTypeSetOfDevice));
            }
            if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_HU_DEVICE)) {
                this.customFieldManager.updateCustomField(customField.getIdAsLong(), attribute,
                                                          fillDataHuDevice(customField,
                                                                           this.customFieldManager.getCustomFieldObjectByName(
                                                                                   Constants.CUSTOM_FIELD_HU_ID)),
                                                          fieldTypeTextSearcher);
            } else if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_SCREEN_DEVICE)) {
                this.customFieldManager.updateCustomField(customField.getIdAsLong(), attribute,
                                                          fillDataScreenDevice(customField,
                                                                               this.customFieldManager.getCustomFieldObjectByName(
                                                                                       Constants.CUSTOM_FIELD_DEVICE_ID)),
                                                          fieldTypeTextSearcher);
            }
            //            }
            customFields.add(customField);
        }
        //  Devices Project
        for (String attribute : Utilities.listPeripheralDevice()) {
            if (!this.customFieldManager.getCustomFieldObjectsByName(attribute).isEmpty()) {
                continue;
            }

            if (attribute.equalsIgnoreCase(Constants.NOTE) ||
                attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_CMC_CODE)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Arrays.asList(issueTypeAutomotiveDevice,
                                                                                      issueTypePeripheralDevice));
            } else if (attribute.equalsIgnoreCase(Constants.CUSTOM_FIELD_EXTERNAL_DEVICE_TYPE)) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeSelect,
                                                                        fieldTypeMultiSelectSearcher, contexts,
                                                                        Arrays.asList(issueTypeAutomotiveDevice,
                                                                                      issueTypePeripheralDevice));
            } else {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(
                                                                                issueTypePeripheralDevice));
            }
            customFields.add(customField);
        }

        for (String attribute : Utilities.listAutomotiveDevice()) {
            if (this.customFieldManager.getCustomFieldObjectsByName(attribute).isEmpty()) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(
                                                                                issueTypeAutomotiveDevice));
                customFields.add(customField);
            }
        }

        for (String attribute : Utilities.listTaskAttribute()) {
            if (this.customFieldManager.getCustomFieldObjectsByName(attribute).isEmpty()) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeSelect,
                                                                        fieldTypeMultiSelectSearcher, contexts,
                                                                        Collections.singletonList(issueTypeTask));
                List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();
                if (!schemes.isEmpty()) {
                    FieldConfigScheme scheme = schemes.get(0);
                    Map configs = scheme.getConfigsByConfig();
                    if (configs != null && !configs.isEmpty()) {
                        FieldConfig config = (FieldConfig) configs.keySet().iterator().next();
                        OptionsManager optionsManager = ComponentManager.getComponentInstanceOfType(
                                OptionsManager.class);
                        long numberAdded = 100;
                        optionsManager.createOptions(config, null, numberAdded, Constants.TYPE_OF_WORKS);
                    }
                }
                customFields.add(customField);
            }
        }

        // Create custom field base on external device
        boolean isAddFunction = true;
        for (String attribute : Utilities.listExternalDevice()) {
            if (this.customFieldManager.getCustomFieldObjectsByName(attribute).isEmpty()) {
                customField = this.customFieldManager.createCustomField(attribute, null, fieldTypeText,
                                                                        fieldTypeTextSearcher, contexts,
                                                                        Collections.singletonList(
                                                                                issueTypeExternalDevice));
                if (!attribute.equalsIgnoreCase(Constants.EXTERNAL_DEVICE)) {
                    //SET CUSTOM FIELD'S SEARCHABLE
                    this.customFieldManager.updateCustomField(customField.getIdAsLong(), attribute,
                                                              fillExternalDeviceScreen(customField, isAddFunction),
                                                              fieldTypeTextSearcher);
                    isAddFunction = false;
                }
                customFields.add(customField);
            }
        }

        this.customFieldManager.updateCustomField(
                this.customFieldManager.getCustomFieldObjectByName(Constants.CUSTOM_FIELD_HU_ID_HU_INDEX).getIdAsLong(),
                Constants.CUSTOM_FIELD_HU_ID_HU_INDEX, fillData(), fieldTypeTextSearcher);

        //CREATE CUSTOM SCHEDULER
        importBigFileSalJobs.reschedule(DEFAULT_INTERVAL_IN_SECONDS);
        syncTestResultSalJobs.reschedule(DEFAULT_INTERVAL_SYNC_IN_SECONDS);

        eventPublisher.register(this);
    }

    /**
     * Called when the plugin is being disabled or removed.
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        log.info("Disabling plugin");
        //DELETE GROUP
        JiraServiceContextImpl jiraServiceContextImpl = new JiraServiceContextImpl(
                ComponentAccessor.getUserManager().getUserByKey(Constants.USER_ADMIN_KEY));
        groupService.delete(jiraServiceContextImpl, Constants.GROUP_LEADER, null);
        groupService.delete(jiraServiceContextImpl, Constants.GROUP_TESTER, null);

        //DISABLE EXPORT EXCEL
        PropertySet propertySet = ComponentAccessor.getComponent(PropertiesManager.class).getPropertySet();
        if (propertySet.getBoolean(Constants.PROPERTY_EXPORT_EXCEL)) {
            propertySet.setBoolean(Constants.PROPERTY_EXPORT_EXCEL, false);
        }

        //DELETE CUSTOM FIELD
        /*for (CustomField customField : customFields) {
            this.customFieldManager.removeCustomField(customField);
        }*/

        //UNSCHEDULER
        importBigFileSalJobs.unschedule();
        syncTestResultSalJobs.unschedule();

        //DELETE ROLE
        /*ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager.class);
        if (projectRoleManager.getProjectRole(Constants.ROLE_PM) != null) {
            projectRoleManager.deleteRole(projectRoleManager.getProjectRole(Constants.ROLE_PM));
        }
        if (projectRoleManager.getProjectRole(Constants.ROLE_QC_LEADER) != null) {
            projectRoleManager.deleteRole(projectRoleManager.getProjectRole(Constants.ROLE_QC_LEADER));
        }
        if (projectRoleManager.getProjectRole(Constants.PROJECT_ROLE_QC) != null) {
            projectRoleManager.deleteRole(projectRoleManager.getProjectRole(Constants.PROJECT_ROLE_QC));
        }*/

        eventPublisher.unregister(this);
    }

    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {
        Long eventTypeId = issueEvent.getEventTypeId();
        Issue issue = issueEvent.getIssue();

        if (eventTypeId.equals(EventType.ISSUE_CREATED_ID)) {
            log.info("Issue {} has been created at {}.", issue.getKey(), issue.getCreated());
        } else if (eventTypeId.equals(EventType.ISSUE_RESOLVED_ID)) {
            log.info("Issue {} has been resolved at {}.", issue.getKey(), issue.getResolutionDate());
        } else if (eventTypeId.equals(EventType.ISSUE_CLOSED_ID)) {
            log.info("Issue {} has been closed at {}.", issue.getKey(), issue.getUpdated());
        } else if (eventTypeId.equals(EventType.ISSUE_UPDATED_ID)) {
            log.info("Issue {} has been update at {}.", issue.getKey(), issue.getUpdated());
            final boolean isSuccess = issueHelperService.createOrUpdateIssue((MutableIssue) issue, false);
            if (!isSuccess) {
                log.error("Update issue error");
            }
        } else if (eventTypeId.equals(EventType.ISSUE_DELETED_ID)) {
            boolean isSuccess = testCaseService.deleteTestcase(issue.getId());
            if (isSuccess) {
                log.info("Delete testcase success");
            } else {
                log.error("Delete testcase error");
            }
        } else if (eventTypeId.equals(EventType.ISSUE_MOVED_ID)) {
            final Object phaseObj = issue.getCustomFieldValue(
                    customFieldManager.getCustomFieldObjectByName(Constants.CUSTOM_FIELD_PHASE));
            final Object testcaseTypeObj = issue.getCustomFieldValue(
                    customFieldManager.getCustomFieldObjectByName(Constants.CUSTOM_FIELD_TESTCASE_TYPE));

            if (phaseObj == null || testcaseTypeObj == null) {
                // Rollback issue
                log.error("Phase and Testcase Type must not be null");
                final TestCase testCase = testCaseService.findByIssueId(issue.getId());
                if (testCase != null) {
                    final boolean isSuccess = issueHelperService.updateIssueOnEventMove(
                            ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(), issue.getId(),
                            testCase.getPhase(), testCase.getTestcaseType());
                    if (isSuccess) {
                        log.info("Rollback issue when moved is success");
                    } else {
                        throw new RuntimeException("Error rollback issue when moved");
                    }
                }
                throw new RuntimeException("Phase and Testcase Type must not be null");
            }

            List<Option> optionsPhase = getOptions(
                    customFieldManager.getCustomFieldObjectByName(Constants.CUSTOM_FIELD_PHASE), issue.getProjectId());
            String phaseId = null;
            for (final Option option : optionsPhase) {
                if (StringUtils.equalsIgnoreCase(option.getValue(), phaseObj.toString())) {
                    phaseId = option.getOptionId().toString();
                }
            }
            List<Option> optionsTestcaseType = getOptions(
                    customFieldManager.getCustomFieldObjectByName(Constants.CUSTOM_FIELD_TESTCASE_TYPE),
                    issue.getProjectId());
            String testcaseTypeId = null;
            for (final Option option : optionsTestcaseType) {
                if (StringUtils.equalsIgnoreCase(option.getValue(), testcaseTypeObj.toString())) {
                    testcaseTypeId = option.getOptionId().toString();
                }
            }
            final boolean isSuccess = testCaseService.updateWhenIssueMoved(issue.getId(), phaseId, testcaseTypeId);
            if (isSuccess) {
                log.info("Move testcase success");
            } else {
                log.error("Move testcase error");
            }
        }
    }

    public IssueType getIssueType(String issueTypeName, String issueTypeDescription, Long avatarId) {
        IssueType issueType = null;
        boolean issueTypeExist = false;
        Collection<IssueType> listIssueType = this.issueTypeManager.getIssueTypes();
        for (IssueType issue : listIssueType) {
            if (issue.getName().equals(issueTypeName)) {
                issueType = issue;
                issueTypeExist = true;
            }
        }
        if (!issueTypeExist) {
            issueType = this.issueTypeManager.createIssueType(issueTypeName, issueTypeDescription,
                                                              this.avatarManager.getById(avatarId) != null ?
                                                              avatarId :
                                                              0L);
        }

        return issueType;
    }

    private String fillData() {
        CustomField customFieldHuIdHuIndex = this.customFieldManager.getCustomFieldObjectByName(
                Constants.CUSTOM_FIELD_HU_ID_HU_INDEX);
        CustomField customFieldHuId = this.customFieldManager.getCustomFieldObjectByName(Constants.CUSTOM_FIELD_HU_ID);
        CustomField customFieldHuIndex = this.customFieldManager.getCustomFieldObjectByName(
                Constants.CUSTOM_FIELD_HU_INDEX);
        CustomField customFieldHuDevice = this.customFieldManager.getCustomFieldObjectByName(
                Constants.CUSTOM_FIELD_HU_DEVICE);
        CustomField customFieldHuMarket = this.customFieldManager.getCustomFieldObjectByName(
                Constants.CUSTOM_FIELD_HU_MARKET);
        CustomField customFieldTestHuMarket = this.customFieldManager.getCustomFieldObjectByName(
                Constants.CUSTOM_FIELD_TEST_HU_MARKET);
        CustomField customFieldVehicleTestcase = null;
        CustomField customFieldVehicleEnvironment = null;
        Collection<CustomField> customFieldVehicles = this.customFieldManager.getCustomFieldObjectsByName(
                Constants.CUSTOM_FIELD_VEHICLE_PARAMETER);
        for (CustomField customField : customFieldVehicles) {
            if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_SELECT)) {
                customFieldVehicleEnvironment = customField;
            } else if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_TEXT)) {
                customFieldVehicleTestcase = customField;
            }
        }
        CustomField customFieldAmpTypeTestcase = null;
        CustomField customFieldAmpTypeEnvironment = null;
        Collection<CustomField> customFieldAmpTypes = customFieldManager.getCustomFieldObjectsByName(
                Constants.AMP_TYPE_ATTRIBUTE);
        for (CustomField customField : customFieldAmpTypes) {
            if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_SELECT)) {
                customFieldAmpTypeEnvironment = customField;
            } else if (customField.getCustomFieldType().getKey().equals(Constants.CUSTOM_FIELD_TYPE_TEXT)) {
                customFieldAmpTypeTestcase = customField;
            }
        }
        CustomField customFieldSiIndex = this.customFieldManager.getCustomFieldObjectByName(
                Constants.CUSTOM_FIELD_SI_INDEX);

        //SEARCH HU ID AND HU INDEX ON EVENT
        StringBuilder builder = new StringBuilder();
        builder.append("<script type=\"text/javascript\">\n");
        builder.append("var arrIssues;\n");
        builder.append("function onInput() {\n");
        builder.append("\tvar opts = $('#huid').find('*');\n");
        builder.append("\tvar arr;\n");
        builder.append("\tvar val = $('#");
        builder.append(customFieldHuIdHuIndex.getId());
        builder.append("').val();\n");
        builder.append("\tif (~val.indexOf(' ')) {\n");
        builder.append("\t\tarr = val.split(' ');\n");
        builder.append("\t} else {\n");
        builder.append("\t\tarr = [val, ''];\n");
        builder.append("\t}\n");
        builder.append("\topts.each(function(index) {\n");
        builder.append("\t\tif ($(this).val() === val) {\n");
        builder.append("\t\t\t$('#");
        builder.append(customFieldHuId.getId());
        builder.append("').val(arr[0]);\n");
        builder.append("\t\t\t$('#");
        builder.append(customFieldHuIndex.getId());
        builder.append("').val(arr[1]);\n");
        builder.append("\t\t\tif ($('#");
        builder.append(customFieldHuIndex.getId());
        builder.append("').val() != '') {\n");
        builder.append("\t\t\t\t//FILL DATA\n");
        builder.append("\t\t\t\tvar options = arrIssues[index].fields.");
        builder.append(customFieldTestHuMarket.getId());
        builder.append(";\n");
        builder.append("\t\t\t\tvar testHuMarket = \"\";\n");
        builder.append("\t\t\t\tfor (var i = 0; i < options.length; i++) {\n");
        builder.append("\t\t\t\t\ttestHuMarket += options[i].value + (i == options.length - 1 ? \"\" : \", \");\n");
        builder.append("\t\t\t\t}\n");
        builder.append("\t\t\t\t$('#");
        builder.append(customFieldHuMarket.getId());
        builder.append("').val(testHuMarket);\n");
        builder.append("\t\t\t\t$('#");
        builder.append(customFieldVehicleTestcase.getId());
        builder.append("').val(arrIssues[index].fields.");
        builder.append(customFieldVehicleEnvironment.getId());
        builder.append(".value);\n");
        builder.append("\t\t\t\t$('#");
        builder.append(customFieldAmpTypeTestcase.getId());
        builder.append("').val(arrIssues[index].fields.");
        builder.append(customFieldAmpTypeEnvironment.getId());
        builder.append(".value);\n");
        builder.append("\t\t\t\t$('#");
        builder.append(customFieldSiIndex.getId());
        builder.append("').val(arrIssues[index].fields.");
        builder.append(customFieldSiIndex.getId());
        builder.append(");\n");
        builder.append("\t\t\t}\n");
        builder.append("\t\t}\n");
        builder.append("\t});\n");
        builder.append("}\n\n");
        builder.append("function SortByHuIndex(a, b){\n");
        builder.append("\tvar aHuIndex = parseInt(a.fields.");
        builder.append(customFieldHuIndex.getId());
        builder.append(");\n");
        builder.append("\tvar bHuIndex = parseInt(b.fields.");
        builder.append(customFieldHuIndex.getId());
        builder.append(");\n");
        builder.append("\treturn ((aHuIndex < bHuIndex) ? 1 : ((aHuIndex > bHuIndex) ? -1 : 0));\n");
        builder.append("}\n\n");
        builder.append("if (!$('.error').length) {\n");
        builder.append("\t$('#");
        builder.append(customFieldHuIdHuIndex.getId());
        builder.append("').val('');\n");
        builder.append("}\n");
        builder.append("$('#");
        builder.append(customFieldHuIdHuIndex.getId());
        builder.append(
                "').attr({'list':'huid', 'oninput':'onInput()', 'autocomplete':'off'}).after('<datalist id=\"huid\"></datalist>').keyup(function(event) {\n");
        builder.append("\tvar keyCode = event.which || event.keyCode;\n");
        builder.append("\tvar arr;\n");
        builder.append("\tvar flag = true;\n");
        builder.append("\tvar val = $(this).val();\n");
        builder.append("\tvar issueOpens = [];\n");
        builder.append("\tvar issueNotOpens = [];\n");
        builder.append("\tif (~val.indexOf(' ')) {\n");
        builder.append("\t\tflag  = false;\n");
        builder.append("\t\tarr = val.split(' ');\n");
        builder.append("\t} else {\n");
        builder.append("\t\tarr = [val, ''];\n");
        builder.append("\t}\n");
        builder.append("\tif (keyCode > 47 && keyCode < 106 || keyCode == 32 || keyCode == 8 || keyCode == 189) {\n");
        builder.append(
                "\t\t$.getJSON(AJS.params.baseURL + \"/rest/api/2/search?jql='Hu Device'~'\"+arr[0]+((flag || arr[1] == '') ? \"*'\" : \"*' AND 'Hu Index'~'\"+arr[1]+\"*'\"), function(data) {\n");
        builder.append("\t\t\tdata.issues.sort(SortByHuIndex);\n");
        builder.append("\t\t\tfor(var issue of data.issues){\n");
        builder.append("\t\t\t\tif(issue.fields.status.name == 'Open'){\n");
        builder.append("\t\t\t\t\tissueOpens.push(issue);\n");
        builder.append("\t\t\t\t} else {\n");
        builder.append("\t\t\t\t\tissueNotOpens.push(issue);\n");
        builder.append("\t\t\t\t}\n");
        builder.append("\t\t\t}\n");
        builder.append("\t\t\tarrIssues = issueOpens.concat(issueNotOpens);\n");
        builder.append("\t\t\tvar str = '';\n");
        builder.append("\t\t\tif (arrIssues.length != 0) {\n");
        builder.append("\t\t\t\t$(\"#nomatches\").remove();\n");
        builder.append("\t\t\t\tfor (var i = 0; i < arrIssues.length; i++) {\n");
        builder.append("\t\t\t\t\tstr += '<option>' + arrIssues[i].fields.");
        builder.append(customFieldHuDevice.getId());
        builder.append(" + (flag ? '</option>' : ' ' + arrIssues[i].fields.");
        builder.append(customFieldHuIndex.getId());
        builder.append("+ '</option>');\n");
        builder.append("\t\t\t\t}\n");
        builder.append("\t\t\t} else {\n");
        builder.append("\t\t\t\tif (!$('#nomatches').length) {\n");
        builder.append("\t\t\t\t\t$('#");
        builder.append(customFieldHuIdHuIndex.getId());
        builder.append("').after('<div id=\"nomatches\" class=\"error\">No Matches</div>');\n");
        builder.append("\t\t\t\t}\n");
        builder.append("\t\t\t\t$('#");
        builder.append(customFieldHuId.getId());
        builder.append("').val('');\n");
        builder.append("\t\t\t\t$('#");
        builder.append(customFieldHuIndex.getId());
        builder.append("').val('');\n");
        builder.append("\t\t\t\t$('#");
        builder.append(customFieldHuMarket.getId());
        builder.append("').val('');\n");
        builder.append("\t\t\t\t$('#");
        builder.append(customFieldVehicleTestcase.getId());
        builder.append("').val('');\n");
        builder.append("\t\t\t\t$('#");
        builder.append(customFieldAmpTypeTestcase.getId());
        builder.append("').val('');\n");
        builder.append("\t\t\t\t$('#");
        builder.append(customFieldSiIndex.getId());
        builder.append("').val('');\n");
        builder.append("\t\t\t}\n");
        builder.append("\t\t\t$('#huid').html(str);\n");
        builder.append("\t\t});\n");
        builder.append("\t}\n");
        builder.append("});\n");
        builder.append("</script>");

        return builder.toString();
    }

    private String addDescriptionInPendingTypeScreen(CustomField customFieldPendingType){
        final String id = customFieldPendingType.getId();
        StringBuilder builder = new StringBuilder();
        builder.append("<script type=\"text/javascript\">\n" +
                       "        targetCategory = document.getElementById('"+id+"');\n" +
                       "        currentScreen = document.getElementById(\"issue-workflow-transition-submit\").value\n" +
                       "        tempVar = 0;\n" + "        if(currentScreen==\"Pending\") {\n" +
                       "            while (targetCategory.length > tempVar) {\n" +
                       "                item = targetCategory.options[tempVar].text\n" +
                       "                if (item == \"ER\" || item == \"NT\" || item == \"DG\" || item == \"DL\") {\n" +
                       "                    targetCategory.remove(tempVar);\n" + "                } else {\n" +
                       "                    tempVar++;\n" + "                }\n" + "            }\n" +
                       "        } else {\n" + "            while (targetCategory.length > tempVar) {\n" +
                       "                item = targetCategory.options[tempVar].text\n" +
                       "                if (item == \"PN(QA)\" || item == \"PN(Bug)\" || item == \"PN(Equipment)\" || item == \"PN(Internal)\") {\n" +
                       "                    targetCategory.remove(tempVar);\n" + "                } else {\n" +
                       "                    tempVar++;\n" + "                }\n" + "            }\n" + "        }\n" +
                       "    </script>");
        return builder.toString();
    }

    private String fillDataHuDevice(CustomField customFieldHuDevice, CustomField customFieldHuId) {
        final String id = customFieldHuDevice.getId();
        StringBuilder builder = new StringBuilder();
        builder.append("<script type=\"text/javascript\">\n");
        builder.append("\tAJS.toInit(function() {\n");
        builder.append("\t\tif (!$('#huid1').length) {\n");
        builder.append("\t\t\t$('#");
        builder.append(id);
        builder.append(
                "').attr({ 'list':'huid1', 'autocomplete':'off'}).after('<datalist id=\"huid1\"></datalist>').keyup(function(event){huDeviceKeyUp(event)});\n");
        builder.append("\t\t}\n");
        builder.append("\t});\n\n");
        builder.append("\tJIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e,context) {\n");
        builder.append("\t\tif (!$('#huid1').length) {\n");
        builder.append("\t\t\t$('#");
        builder.append(id);
        builder.append(
                "').attr({ 'list':'huid1', 'autocomplete':'off'}).after('<datalist id=\"huid1\"></datalist>').keyup(function(event){huDeviceKeyUp(event)});\n");
        builder.append("\t\t}\n");
        builder.append("\t});\n\n");
        builder.append("\tfunction huDeviceKeyUp(event) {\n");
        builder.append("\t\tvar keyCode = event.which || event.keyCode;\n");
        builder.append("\t\tvar arrIssues;\n");
        builder.append("\t\tvar val = $('#");
        builder.append(id);
        builder.append("').val();\n");
        builder.append("\t\tvar issueOpens = [];\n");
        builder.append("\t\tvar issueNotOpens = [];\n");
        builder.append("\t\tif (keyCode > 47 && keyCode < 106 || keyCode == 32 || keyCode == 8 || keyCode == 189) {\n");
        builder.append(
                "\t\t\t$.getJSON(AJS.params.baseURL + \"/rest/api/2/search?jql='project'='\" + getProjectKey($('#project').val()) + \"' AND 'Hu Id'~'\" + val + \"*'\", function(data) {\n");
        builder.append("\t\t\t\tfor (var issue of data.issues) {\n");
        builder.append("\t\t\t\t\tif (issue.fields.status.name == 'Open') {\n");
        builder.append("\t\t\t\t\t\tissueOpens.push(issue);\n");
        builder.append("\t\t\t\t\t} else {\n");
        builder.append("\t\t\t\t\t\tissueNotOpens.push(issue);\n");
        builder.append("\t\t\t\t\t}\n");
        builder.append("\t\t\t\t}\n");
        builder.append("\t\t\t\tarrIssues = issueOpens.concat(issueNotOpens);\n");
        builder.append("\t\t\t\tvar str = '';\n");
        builder.append("\t\t\t\tfor (var i = 0; i < arrIssues.length; i++) {\n");
        builder.append("\t\t\t\t\tstr += '<option>' + arrIssues[i].fields.");
        builder.append(customFieldHuId.getId());
        builder.append(" + '</option>';\n");
        builder.append("\t\t\t\t}\n");
        builder.append("\t\t\t\t$('#huid1').html(str);\n");
        builder.append("\t\t\t});\n");
        builder.append("\t\t}\n");
        builder.append("\t};\n\n");
        builder.append("\tfunction getProjectKey(pid) {\n");
        builder.append("\t\tvar url = AJS.params.baseURL + \"/rest/api/2/project/\" + pid;\n");
        builder.append("\t\tvar projectKey;\n");
        builder.append("\t\t$.ajax({\n");
        builder.append("\t\t\turl:url,\n");
        builder.append("\t\t\ttype:'get',\n");
        builder.append("\t\t\tdataType:'json',\n");
        builder.append("\t\t\tasync:false,\n");
        builder.append("\t\t\tsuccess:function(data) {\n");
        builder.append("\t\t\t\tprojectKey = data.key;\n");
        builder.append("\t\t\t}\n");
        builder.append("\t\t});\n");
        builder.append("\t\treturn projectKey;\n");
        builder.append("\t}\n");
        builder.append("</script>");

        return builder.toString();
    }

    private String fillDataScreenDevice(CustomField customFieldScreenDevice, CustomField customFieldDeviceId) {
        final String id = customFieldScreenDevice.getId();
        StringBuilder builder = new StringBuilder();
        builder.append("<script type=\"text/javascript\">\n");
        builder.append("\tAJS.toInit(function() {\n");
        builder.append("\t\tif (!$('#huid2').length) {\n");
        builder.append("\t\t\t$('#");
        builder.append(id);
        builder.append(
                "').attr({ 'list':'huid2', 'autocomplete':'off'}).after('<datalist id=\"huid2\"></datalist>').keyup(function(event){screenDeviceKeyUp(event)});\n");
        builder.append("\t\t}\n");
        builder.append("\t});\n\n");
        builder.append("\tJIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e,context) {\n");
        builder.append("\t\tif (!$('#huid2').length) {\n");
        builder.append("\t\t\t$('#");
        builder.append(id);
        builder.append(
                "').attr({ 'list':'huid2', 'autocomplete':'off'}).after('<datalist id=\"huid2\"></datalist>').keyup(function(event){screenDeviceKeyUp(event)});\n");
        builder.append("\t\t}\n");
        builder.append("\t});\n\n");
        builder.append("\tfunction screenDeviceKeyUp(event) {\n");
        builder.append("\t\tvar keyCode = event.which || event.keyCode;\n");
        builder.append("\t\tvar arrIssues;\n");
        builder.append("\t\tvar val = $('#");
        builder.append(id);
        builder.append("').val();\n");
        builder.append("\t\tvar issueOpens = [];\n");
        builder.append("\t\tvar issueNotOpens = [];\n");
        builder.append("\t\tif (keyCode > 47 && keyCode < 106 || keyCode == 32 || keyCode == 8 || keyCode == 189) {\n");
        builder.append(
                "\t\t\t$.getJSON(AJS.params.baseURL + \"/rest/api/2/search?jql='project'='\" + getProjectKey($('#project').val()) + \"' AND 'Device Id'~'\" + val + \"*'\", function(data) {\n");
        builder.append("\t\t\t\tfor (var issue of data.issues) {\n");
        builder.append("\t\t\t\t\tif (issue.fields.status.name == 'Open') {\n");
        builder.append("\t\t\t\t\t\tissueOpens.push(issue);\n");
        builder.append("\t\t\t\t\t} else {\n");
        builder.append("\t\t\t\t\t\tissueNotOpens.push(issue);\n");
        builder.append("\t\t\t\t\t}\n");
        builder.append("\t\t\t\t}\n");
        builder.append("\t\t\t\tarrIssues = issueOpens.concat(issueNotOpens);\n");
        builder.append("\t\t\t\tvar str = '';\n");
        builder.append("\t\t\t\tfor (var i = 0; i < arrIssues.length; i++) {\n");
        builder.append("\t\t\t\t\tstr += '<option>' + arrIssues[i].fields.");
        builder.append(customFieldDeviceId.getId());
        builder.append(" + '</option>';\n");
        builder.append("\t\t\t\t}\n");
        builder.append("\t\t\t\t$('#huid2').html(str);\n");
        builder.append("\t\t\t});\n");
        builder.append("\t\t}\n");
        builder.append("\t};\n");
        builder.append("</script>");

        return builder.toString();
    }

    private String hideCustomFieldValue(CustomField customField, boolean flagReadOnly, boolean flagNumber,
                                        boolean flagDateOfImplementation) {
        StringBuilder builder = new StringBuilder();
        if (!flagDateOfImplementation) {
            builder.append("<script type=\"text/javascript\">\n");
            builder.append("\tif ($('#");
            builder.append(
                    this.customFieldManager.getCustomFieldObjectByName(Constants.CUSTOM_FIELD_HU_ID_HU_INDEX).getId());
            builder.append("').length) {\n");
            builder.append("\t\tif (!$('.error').length) {\n");
            builder.append("\t\t\t$('#");
            builder.append(customField.getId());
            builder.append("').val('');\n");
            builder.append("\t\t}\n");
            if (flagReadOnly) {
                builder.append("\t\t$('#");
                builder.append(customField.getId());
                builder.append("').attr('readonly', 'readonly');\n");
            }
            builder.append("\t}\n");
            builder.append("</script>");
        }
        if (flagNumber) {
            builder.append("Please enter the number of minutes");
        }

        return builder.toString();
    }

    private String setCustomFieldRow(CustomField customField) {
        StringBuilder builder = new StringBuilder();
        builder.append("<script type=\"text/javascript\">\n");
        builder.append("\tAJS.toInit(function() {\n");
        builder.append("\t\t$('#");
        builder.append(customField.getId());
        builder.append("').attr('rows', '1');\n");
        builder.append("\t});\n");
        builder.append("\tJIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e,context) {\n");
        builder.append("\t\t$('#");
        builder.append(customField.getId());
        builder.append("').attr('rows', '1');\n");
        builder.append("\t});\n");
        builder.append("</script>");

        return builder.toString();
    }

    private String searchableSelect(CustomField customField) {
        StringBuilder builder = new StringBuilder();
        builder.append("<script type=\"text/javascript\">\n");
        builder.append("\t(function($) {\n");
        builder.append("\t\tAJS.$(\"#");
        builder.append(customField.getId());
        builder.append(" option[value='-1']\").remove();\n");
        builder.append("\t\tfunction convertMulti(id) {\n");
        builder.append("\t\t\tif (AJS.$('#'+id+'-textarea').length == 0) {\n");
        builder.append("\t\t\t\tnew AJS.MultiSelect({\n");
        builder.append("\t\t\t\t\telement: $('#'+id),\n");
        builder.append("\t\t\t\t\titemAttrDisplayed: 'label',\n");
        builder.append("\t\t\t\t\terrorMessage: AJS.params.multiselectComponentsError\n");
        builder.append("\t\t\t\t});\n");
        builder.append("\t\t\t}\n");
        builder.append("\t\t}\n");
        builder.append("\t\tAJS.toInit(function() {\n");
        builder.append("\t\t\tconvertMulti('");
        builder.append(customField.getId());
        builder.append("');\n");
        builder.append("\t\t});\n");
        builder.append("\t\tJIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context) {\n");
        builder.append("\t\t\tAJS.$(\"#");
        builder.append(customField.getId());
        builder.append(" option[value='-1']\").remove();\n");
        builder.append("\t\t\tconvertMulti('");
        builder.append(customField.getId());
        builder.append("');\n");
        builder.append("\t\t});\n");
        builder.append("\t})(AJS.$);\n");
        builder.append("</script>");

        return builder.toString();
    }

    private String checkAssignee() {
        StringBuilder builder = new StringBuilder();
        builder.append("<script type=\"text/javascript\">\n");
        builder.append("\t$('#assignee').change(function() {\n");
        builder.append("\t\tvar issueKey = $('.issue-link').attr('data-issue-key');\n");
        builder.append("\t\tvar projectKey = $('#comment').attr('data-projectkey');\n");
        builder.append("\t\tvar assigneeSelected = $('#assignee-group-suggested option:selected').val();\n");
        builder.append("\t\t$('#assignee-group-suggested option:selected').each(function() {\n");
        builder.append("\t\t\tassigneeSelected = $('#assignee-group-suggested option:selected').val();\n");
        builder.append("\t\t});\n");
        builder.append("\t\tjQuery.ajax({\n");
        builder.append("\t\t\ttype: 'post',\n");
        builder.append(
                "\t\t\turl: AJS.params.baseURL + '/secure/TeamsManagementAction!validateAssign.jspa?issueKey=' + issueKey + '&projectKey=' + projectKey + '&assignUser=' + assigneeSelected,\n");
        builder.append("\t\t\tsuccess: function(data) {\n");
        builder.append("\t\t\t\t$('#assign-issue-submit').removeAttr('disabled');\n");
        builder.append("\t\t\t\tif($('#assignee').next().hasClass('error')) {\n");
        builder.append("\t\t\t\t\t$('#assignee').next().remove();\n");
        builder.append("\t\t\t\t}\n");
        builder.append("\t\t\t},\n");
        builder.append("\t\t\terror: function(errors) {\n");
        builder.append("\t\t\t\tvar errors = JSON.parse(errors.responseText);\n");
        builder.append("\t\t\t\t$('#assignee').after('<div class=\"error\">'+ errors['error'] + '</div>');\n");
        builder.append("\t\t\t\t$('#assign-issue-submit').attr('disabled','disabled');\n");
        builder.append("\t\t\t}\n");
        builder.append("\t\t});\n");
        builder.append("\t}).trigger('change');\n");
        builder.append("AJS.$(document).ready(function() {\n" + "        if($('#assign-to-me').length > 0){\n" +
                       "            issueKey = $(\".issue-link\").attr(\"data-issue-key\");\n" +
                       "            projectKey = $(\"#comment\").attr(\"data-projectkey\");\n" +
                       "            linkHref = $('#assign-to-me').attr('href');\n" +
                       "            index = linkHref.indexOf(\"assignee\");\n" +
                       "            assignee = linkHref.substr(index+9);\n" + "             jQuery.ajax({\n" +
                       "                type: \"post\",\n" +
                       "                url: AJS.params.baseURL + \"/secure/TeamsManagementAction!validateAssign.jspa?issueKey=\" + issueKey + \"&projectKey=\" + projectKey + \"&assignUser=\" + assignee,\n" +
                       "                success: function (data) {\n" + "                },\n" +
                       "                error: function (errors) {\n" +
                       "                    $('#assign-to-me').hide(); \n" + "                }\n" +
                       "            });\n" + "        }\n" + "});");
        builder.append("</script>");

        return builder.toString();
    }

    private String fillExternalDeviceScreen(CustomField customField, boolean isAddFunction) {
        CustomField customFieldCmcCode = this.customFieldManager.getCustomFieldObjectByName(
                Constants.CUSTOM_FIELD_CMC_CODE);
        StringBuilder builder = new StringBuilder();
        final String id = customField.getId();
        builder.append("<script type=\"text/javascript\">\n");
        builder.append("\tAJS.toInit(function() {\n");
        builder.append("\t\tif (!$('#huid_");
        builder.append(id);
        builder.append("').length) {\n");
        builder.append("\t\t\t$('#");
        builder.append(id);
        builder.append("').attr({ 'list':'huid_");
        builder.append(id);
        builder.append("', 'autocomplete':'off'}).after('<datalist id=\"huid_");
        builder.append(id);
        builder.append("\"></datalist>').keyup(function(event){screenDeviceKeyUp(event,'");
        builder.append(id);
        builder.append("')});\n");
        builder.append("\t\t}\n");
        builder.append("\t});\n\n");
        builder.append("\tJIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e,context) {\n");
        builder.append("\t\tif (!$('#huid_");
        builder.append(id);
        builder.append("').length) {\n");
        builder.append("\t\t\t$('#");
        builder.append(id);
        builder.append("').attr({ 'list':'huid_");
        builder.append(id);
        builder.append("', 'autocomplete':'off'}).after('<datalist id=\"huid_");
        builder.append(id);
        builder.append("\"></datalist>').keyup(function(event){screenDeviceKeyUp(event,'");
        builder.append(id);
        builder.append("')});\n");
        builder.append("\t\t}\n");
        builder.append("\t});\n\n");
        if (isAddFunction) {
            builder.append("\tfunction screenDeviceKeyUp(event, id) {\n");
            builder.append("\t\tvar keyCode = event.which || event.keyCode;\n");
            builder.append("\t\tvar arrIssues = [];\n");
            builder.append("\t\tvar val = $('#'+id).val();\n");
            builder.append("\t\tvar issueOpens = [];\n");
            builder.append("\t\tvar issueNotOpens = [];\n");
            builder.append(
                    "\t\tif (keyCode > 47 && keyCode < 106 || keyCode == 32 || keyCode == 8 || keyCode == 189) {\n");
            builder.append(
                    "\t\t\t$.getJSON(AJS.params.baseURL + \"/rest/api/2/search?jql='project'='\" + getProjectKey($('#project').val()) + \"' AND 'issuetype' = 'Peripheral Device' AND 'CMC Code'~'\" + val + \"*'\", function(data) {\n");
            builder.append("\t\t\t\tfor (var issue of data.issues) {\n");
            builder.append("\t\t\t\t\tif (issue.fields.status.name == 'Open') {\n");
            builder.append("\t\t\t\t\t\tissueOpens.push(issue);\n");
            builder.append("\t\t\t\t\t} else {\n");
            builder.append("\t\t\t\t\t\tissueNotOpens.push(issue);\n");
            builder.append("\t\t\t\t\t}\n");
            builder.append("\t\t\t\t}\n");
            builder.append("\t\t\t\tarrIssues = issueOpens.concat(issueNotOpens);\n");
            builder.append("\t\t\t\tvar str = '';\n");
            builder.append("\t\t\t\tfor (var i = 0; i < arrIssues.length; i++) {\n");
            builder.append("\t\t\t\t\tstr += '<option>' + arrIssues[i].fields.");
            builder.append(customFieldCmcCode.getId());
            builder.append(" + '</option>';\n");
            builder.append("\t\t\t\t}\n");
            builder.append("\t\t\t\t$('#huid_' + id).html(str);\n");
            builder.append("\t\t\t});\n");
            builder.append("\t\t}\n");
            builder.append("\t}\n\n");
            builder.append("\tfunction getProjectKey(pid) {\n");
            builder.append("\t\tvar url = AJS.params.baseURL + \"/rest/api/2/project/\" + pid;\n");
            builder.append("\t\tvar projectKey;\n");
            builder.append("\t\t$.ajax({\n");
            builder.append("\t\t\turl:url,\n");
            builder.append("\t\t\ttype:'get',\n");
            builder.append("\t\t\tdataType:'json',\n");
            builder.append("\t\t\tasync:false,\n");
            builder.append("\t\t\tsuccess:function(data) {\n");
            builder.append("\t\t\t\tprojectKey = data.key;\n");
            builder.append("\t\t\t}\n");
            builder.append("\t\t});\n");
            builder.append("\t\treturn projectKey;\n");
            builder.append("\t}\n");
        }
        builder.append("</script>");

        return builder.toString();
    }

    public List<Option> getOptions(CustomField customField, final Long projectId) {
        List<Option> options = new ArrayList<>();
        final List<FieldConfigScheme> schemes = customField.getConfigurationSchemes();
        if (!schemes.isEmpty()) {
            FieldConfigScheme fieldConfigScheme = null;
            //            final Long projectId = Helper.getCurrentProject(jiraHelper).getId();
            for (FieldConfigScheme scheme : schemes) {
                if (scheme.getAssociatedProjectIds().contains(projectId)) {
                    fieldConfigScheme = scheme;
                    break;
                }
            }
            if (fieldConfigScheme != null) {
                Map configs = fieldConfigScheme.getConfigsByConfig();
                if (!CollectionUtils.isEmpty(configs)) {
                    FieldConfig config = (FieldConfig) configs.keySet().iterator().next();
                    OptionsManager optionsManager = ComponentManager.getComponentInstanceOfType(OptionsManager.class);
                    options = optionsManager.getOptions(config).getRootOptions();
                }
            }
        }
        return options;
    }

}


