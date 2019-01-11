package com.cmcglobal.plugins.utils;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexingService;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.ImportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cmcglobal.plugins.utils.Constants.CLASS_HELPER;
import static com.cmcglobal.plugins.utils.Constants.FORMAT_DATE_IMPORT;

/**
 * author : nttha1
 */
public class Helper {

    private static final Logger logger = LoggerFactory.getLogger(Helper.class);

    private Helper() {
        throw new IllegalStateException(CLASS_HELPER);
    }

    public static Date getCurrentDate() {
        return Calendar.getInstance().getTime();
    }

    public static Date convertStringToLocalDate(String dateStr) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(FORMAT_DATE_IMPORT)
                                                                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                                                                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                                                                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                                                                    .toFormatter();
        LocalDate d = LocalDateTime.parse(dateStr, formatter).toLocalDate();

        return Date.from(d.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String convertDateToString(Date date) {
        DateFormat formatter = new SimpleDateFormat(FORMAT_DATE_IMPORT);

        return formatter.format(date);
    }

    public static Date convertStringToDate(String dateStr) {
        DateFormat formatter = new SimpleDateFormat(FORMAT_DATE_IMPORT);
        try {
            return formatter.parse(dateStr);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    public static <E> List<E> convertObj(List<?> lst, Class<E> cls) {
        List<E> result = new ArrayList<>();
        for (Object obj : lst) {
            if (cls.isInstance(obj)) {
                result.add(cls.cast(obj));
            }
        }

        return result;
    }

    public static boolean compareDate(Date date1, Date date2) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.PATTERM_DATE);
        String datea = sdf.format(date1);
        String dateb = sdf.format(date2);

        return Objects.equals(datea, dateb);
    }

    public static float roundTwoDecimals(float number) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Float.valueOf(twoDForm.format(number));
    }

    public static double roundDoubleTwoDecimal(double number) {
        DecimalFormat formater = new DecimalFormat("#.##");
        return Double.valueOf(formater.format(number));
    }

    public static String convertStringToHour(float number) {
        int hour = (int) (number * 100) / 100;
        int munite = ((int) (number * 100) - hour * 100) * 60 / 100;
        if (munite < 10) {
            return hour + " : 0" + munite;
        }
        return hour + " : " + munite;
    }

    public static Project getCurrentProject(JiraHelper jiraHelper) {
        ProjectManager projectManager = ComponentAccessor.getComponent(ProjectManager.class);
        Project currentProject = (jiraHelper.getProject());
        if (currentProject == null) {
            String target = jiraHelper.getRequest().getRequestURI();
            Pattern p = Pattern.compile(Constants.PATTERN);
            Matcher m = p.matcher(target);
            m.find();
            String parseResult = m.group();
            currentProject = projectManager.getProjectObjByKey(parseResult);
        }

        return currentProject;
    }

    public static Project getProjectByPid(final long pid) {
        final ProjectManager projectManager = ComponentManager.getComponentInstanceOfType(ProjectManager.class);

        return projectManager.getProjectObj(pid);
    }

    public static ProjectRole getProjectRoleByRoleName(final String theRoleName) {
        final ProjectRoleManager projectRoleManager = ComponentManager.getComponentInstanceOfType(
                ProjectRoleManager.class);

        return projectRoleManager.getProjectRole(theRoleName);
    }

    public static boolean isBetweenLocalDate(LocalDate date, LocalDate start, LocalDate end) {
        return (date.isAfter(start) || date.isEqual(start)) && (date.isBefore(end) || date.isEqual(end));
    }

    public static void addPermissionScheme(Project project, String permissionSchemeName) {
        PermissionSchemeManager permissionSchemeManager = ComponentAccessor.getPermissionSchemeManager();
        Scheme scheme = permissionSchemeManager.getSchemeObject(permissionSchemeName);
        if (scheme == null) {
            Scheme defaultSchemeObject = permissionSchemeManager.getDefaultSchemeObject();
            Iterator<SchemeEntity> schemeEntities = defaultSchemeObject.getEntities().iterator();
            Collection<SchemeEntity> newSchemeEntities = new ArrayList<>();
            ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager.class);
            ProjectRole projectRolePM = projectRoleManager.getProjectRole(Constants.ROLE_PM);
            ProjectRole projectRoleQCLeader = projectRoleManager.getProjectRole(Constants.ROLE_QC_LEADER);
            ProjectRole projectRoleQC = projectRoleManager.getProjectRole(Constants.PROJECT_ROLE_QC);
            while (schemeEntities.hasNext()) {
                SchemeEntity schemeEntity = schemeEntities.next();
                if (projectRolePM != null && projectRoleQCLeader != null && projectRoleQC != null) {
                    if (schemeEntity.getEntityTypeId().equals(ProjectPermissions.EDIT_ISSUES)) {
                        schemeEntity = new SchemeEntity(Constants.SCHEME_ENTITY_TYPE_PROJECT_ROLE,
                                                        String.valueOf(projectRolePM.getId()),
                                                        ProjectPermissions.EDIT_ISSUES);
                        newSchemeEntities.add(new SchemeEntity(Constants.SCHEME_ENTITY_TYPE_PROJECT_ROLE,
                                                               String.valueOf(projectRoleQCLeader.getId()),
                                                               ProjectPermissions.EDIT_ISSUES));
                    } else if (schemeEntity.getEntityTypeId().equals(ProjectPermissions.ASSIGN_ISSUES)) {
                        schemeEntity = new SchemeEntity(Constants.SCHEME_ENTITY_TYPE_PROJECT_ROLE,
                                                        String.valueOf(projectRolePM.getId()),
                                                        ProjectPermissions.ASSIGN_ISSUES);
                        newSchemeEntities.add(new SchemeEntity(Constants.SCHEME_ENTITY_TYPE_PROJECT_ROLE,
                                                               String.valueOf(projectRoleQCLeader.getId()),
                                                               ProjectPermissions.ASSIGN_ISSUES));
                    } else if (schemeEntity.getEntityTypeId().equals(ProjectPermissions.ASSIGNABLE_USER)) {
                        schemeEntity = new SchemeEntity(Constants.SCHEME_ENTITY_TYPE_PROJECT_ROLE,
                                                        String.valueOf(projectRoleQC.getId()),
                                                        ProjectPermissions.ASSIGNABLE_USER);
                        newSchemeEntities.add(new SchemeEntity(Constants.SCHEME_ENTITY_TYPE_PROJECT_ROLE,
                                                               String.valueOf(projectRolePM.getId()),
                                                               ProjectPermissions.ASSIGNABLE_USER));
                        newSchemeEntities.add(new SchemeEntity(Constants.SCHEME_ENTITY_TYPE_PROJECT_ROLE,
                                                               String.valueOf(projectRoleQCLeader.getId()),
                                                               ProjectPermissions.ASSIGNABLE_USER));
                    }
                }
                newSchemeEntities.add(schemeEntity);
            }
            scheme = new Scheme(1L, defaultSchemeObject.getType(), permissionSchemeName, newSchemeEntities);
            scheme = permissionSchemeManager.createSchemeAndEntities(scheme);
        }
        permissionSchemeManager.removeSchemesFromProject(project);
        permissionSchemeManager.addSchemeToProject(project, scheme);
    }

    public static void addConfigurationScheme(Project project, CustomField customField) {
        FieldConfigScheme fieldConfigScheme = new FieldConfigScheme.Builder().setName(
                "Configuration Scheme for " + project.getName())
                                                                             .setDescription(
                                                                                     "Configuration Scheme for " +
                                                                                     project.getName() +
                                                                                     " generated by CMCGlobal Test Management Plugin")
                                                                             .setFieldId(customField.getId())
                                                                             .toFieldConfigScheme();

        List<JiraContextNode> jiraContextNodes = CustomFieldUtils.buildJiraIssueContexts(false,
                                                                                         new Long[] { project.getId() },
                                                                                         ComponentAccessor.getProjectManager());
        List<IssueType> issueTypes = (List<IssueType>) project.getIssueTypes();
        ComponentAccessor.getFieldConfigSchemeManager()
                         .createFieldConfigScheme(fieldConfigScheme, jiraContextNodes, issueTypes, customField);
    }

    public static void reIndexIssue(Issue issue) {
        boolean wasIndexing = ImportUtils.isIndexIssues();
        ImportUtils.setIndexIssues(true);
        try {
            ComponentAccessor.getComponent(IssueIndexingService.class).reIndex(issue);
        } catch (IndexException e) {
            logger.error(e.getMessage());
        } finally {
            ImportUtils.setIndexIssues(wasIndexing);
        }
    }

}
