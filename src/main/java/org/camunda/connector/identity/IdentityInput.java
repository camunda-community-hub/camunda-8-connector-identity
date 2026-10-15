package org.camunda.connector.identity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import org.camunda.connector.identity.toolbox.*;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class IdentityInput implements CherryInput {

    public static final String INPUT_IDENTITY_FUNCTION = "identityFunction";

    /* user parameters */
    public static final String INPUT_USER_NAME = "userName";
    public static final String INPUT_USER_NAME_LABEL = "User Name";
    public static final String INPUT_USER_NAME_EXPLANATION = "The username must be unique in Camunda 8 Identity";

    public static final String INPUT_USER_DISPLAY_NAME = "userDisplayName";
    public static final String INPUT_USER_DISPLAY_NAME_LABEL = "Name";
    public static final String INPUT_USER_DISPLAY_NAME_EXPLANATION = "Display name of the user";

    public static final String INPUT_USER_EMAIL = "userEmail";
    public static final String INPUT_USER_EMAIL_LABEL = "Email";
    public static final String INPUT_USER_EMAIL_EXPLANATION = "Email of the user";

    public static final String INPUT_USER_PASSWORD = "userPassword";
    public static final String INPUT_USER_PASSWORD_LABEL = "Password";
    public static final String INPUT_USER_PASSWORD_EXPLANATION = "Password of the user";

    /* shared by every creation and deletion sub-function */
    public static final String INPUT_IGNORE_IF_ALREADY_DONE = "ignoreIfAlreadyDone";
    public static final String INPUT_IGNORE_IF_ALREADY_DONE_LABEL = "Ignore if already done";
    public static final String INPUT_IGNORE_IF_ALREADY_DONE_EXPLANATION =
            "If true (default), do not throw a BPMN error when the operation is already done "
                    + "(the entity already exists on creation, or does not exist anymore on deletion)";

    /* shared by every add-in-group/add-in-role sub-function */
    public static final String INPUT_WAIT_EXPORT = "waitExport";
    public static final String INPUT_WAIT_EXPORT_LABEL = "Wait export";
    public static final String INPUT_WAIT_EXPORT_EXPLANATION =
            "Wait for Identity to export the artefact created, in millisecond";

    /* search parameters (like-match) */
    public static final String INPUT_SEARCH_BY_USER_NAME = "searchByUserName";
    public static final String INPUT_SEARCH_BY_USER_NAME_LABEL = "Search by User Name";
    public static final String INPUT_SEARCH_BY_USER_NAME_EXPLANATION =
            "Search by the username (like-match): use * for zero/one/multiple characters and ? for a single "
                    + "character, e.g. \"*bates\" matches \"walter.bates\". Without a wildcard, only an exact "
                    + "match is returned. The *, ? wildcard syntax is only valid on a Camunda 8.10 or later "
                    + "cluster - against an older broker (8.9 and before) it fails with a 400 error.";
    public static final String INPUT_SEARCH_BY_USER_DISPLAY_NAME = "searchByUserDisplayName";
    public static final String INPUT_SEARCH_BY_USER_DISPLAY_NAME_LABEL = "Search by Name";
    public static final String INPUT_SEARCH_BY_USER_DISPLAY_NAME_EXPLANATION =
            "Search by the name of the user (like-match): use * for zero/one/multiple characters and ? for a "
                    + "single character, e.g. \"*Bates\" matches \"Walter Bates\". Without a wildcard, only an "
                    + "exact match is returned. The *, ? wildcard syntax is only valid on a Camunda 8.10 or "
                    + "later cluster - against an older broker (8.9 and before) it fails with a 400 error.";
    public static final String INPUT_SEARCH_BY_USER_EMAIL = "searchByUserEmail";
    public static final String INPUT_SEARCH_BY_USER_EMAIL_LABEL = "Search by Email";
    public static final String INPUT_SEARCH_BY_USER_EMAIL_EXPLANATION =
            "Search by Email of the user (like-match): use * for zero/one/multiple characters and ? for a "
                    + "single character. Without a wildcard, only an exact match is returned. The *, ? wildcard "
                    + "syntax is only valid on a Camunda 8.10 or later cluster - against an older broker (8.9 "
                    + "and before) it fails with a 400 error.";
    public static final String INPUT_SEARCH_BY_GROUP_ID = "searchByGroupId";
    public static final String INPUT_SEARCH_BY_GROUP_ID_LABEL = "Search by Group Id";
    public static final String INPUT_SEARCH_BY_GROUP_ID_EXPLANATION = "Search all users assigned to the given group";
    public static final String INPUT_SEARCH_BY_ROLE_ID = "searchByRoleId";
    public static final String INPUT_SEARCH_BY_ROLE_ID_LABEL = "Search by Role Id";
    public static final String INPUT_SEARCH_BY_ROLE_ID_EXPLANATION = "Search all users assigned to the given role";

    /* group parameters */
    public static final String INPUT_GROUP_ID = "groupId";
    public static final String INPUT_GROUP_ID_LABEL = "Group Id";
    public static final String INPUT_GROUP_ID_EXPLANATION = "The group id, unique in Camunda 8 Identity";
    public static final String INPUT_GROUP_NAME = "groupName";
    public static final String INPUT_GROUP_NAME_LABEL = "Group Name";
    public static final String INPUT_GROUP_NAME_EXPLANATION = "Name of the group";
    public static final String INPUT_GROUP_DESCRIPTION = "groupDescription";
    public static final String INPUT_GROUP_DESCRIPTION_LABEL = "Group Description";
    public static final String INPUT_GROUP_DESCRIPTION_EXPLANATION = "Description of the group";

    /* search-group parameter: name to search (like-match) */
    public static final String INPUT_GROUP_NAME_SEARCH = "groupNameSearch";
    public static final String INPUT_GROUP_NAME_SEARCH_LABEL = "Group Name Filter";
    public static final String INPUT_GROUP_NAME_SEARCH_EXPLANATION =
            "Name to search (like-match): use * for zero/one/multiple characters and ? for a single "
                    + "character, e.g. \"super*\" matches \"supervisor\". Without a wildcard, only an exact "
                    + "match is returned. The *, ? wildcard syntax is only valid on a Camunda 8.10 or later "
                    + "cluster - against an older broker (8.9 and before) it fails with a 400 error.";

    /* add-in-group / delete-in-group parameter: one object listing the members per category */
    public static final String INPUT_GROUP_MEMBERS = "groupMembers";
    public static final String INPUT_GROUP_MEMBERS_LABEL = "Members";
    public static final String INPUT_GROUP_MEMBERS_EXPLANATION =
            "Members to add/remove, per category: {\"users\": [\"<userName>\"...], \"roles\": [\"<roleId>\"...], "
                    + "\"client\": [\"<clientId>\"...], \"mappingRule\": [\"<mappingRuleId>\"...]}";

    /* search-in-group parameter: one object selecting which categories to return */
    public static final String INPUT_GROUP_CONTENT_FILTER = "groupContentFilter";
    public static final String INPUT_GROUP_CONTENT_FILTER_LABEL = "Content filter";
    public static final String INPUT_GROUP_CONTENT_FILTER_EXPLANATION =
            "Categories to return: {\"users\": true|false, \"roles\": true|false, \"client\": true|false, \"mappingRule\": true|false}";

    /* role parameters */
    public static final String INPUT_ROLE_ID = "roleId";
    public static final String INPUT_ROLE_ID_LABEL = "Role Id";
    public static final String INPUT_ROLE_ID_EXPLANATION = "The role id, unique in Camunda 8 Identity";
    public static final String INPUT_ROLE_NAME = "roleName";
    public static final String INPUT_ROLE_NAME_LABEL = "Role Name";
    public static final String INPUT_ROLE_NAME_EXPLANATION = "Name of the role";
    public static final String INPUT_ROLE_DESCRIPTION = "roleDescription";
    public static final String INPUT_ROLE_DESCRIPTION_LABEL = "Role Description";
    public static final String INPUT_ROLE_DESCRIPTION_EXPLANATION = "Description of the role";

    /* search-role parameter: name to search (like-match) */
    public static final String INPUT_ROLE_NAME_SEARCH = "roleNameSearch";
    public static final String INPUT_ROLE_NAME_SEARCH_LABEL = "Role Name filter";
    public static final String INPUT_ROLE_NAME_SEARCH_EXPLANATION =
            "Name to search (like-match): use * for zero/one/multiple characters and ? for a single "
                    + "character, e.g. \"super*\" matches \"supervisor\". Without a wildcard, only an exact "
                    + "match is returned. The *, ? wildcard syntax is only valid on a Camunda 8.10 or later "
                    + "cluster - against an older broker (8.9 and before) it fails with a 400 error.";

    /* add-in-role / delete-in-role parameter: one object listing the members per category */
    public static final String INPUT_ROLE_MEMBERS = "roleMembers";
    public static final String INPUT_ROLE_MEMBERS_LABEL = "Members";
    public static final String INPUT_ROLE_MEMBERS_EXPLANATION =
            "Members to add/remove, per category: {\"users\": [\"<userName>\"...], \"groups\": [\"<groupId>\"...], "
                    + "\"client\": [\"<clientId>\"...], \"mappingRule\": [\"<mappingRuleId>\"...]}";

    /* search-in-role parameter: one object selecting which categories to return */
    public static final String INPUT_ROLE_CONTENT_FILTER = "roleContentFilter";
    public static final String INPUT_ROLE_CONTENT_FILTER_LABEL = "Content filter";
    public static final String INPUT_ROLE_CONTENT_FILTER_EXPLANATION =
            "Categories to return: {\"users\": true|false, \"groups\": true|false, \"client\": true|false, \"mappingRule\": true|false}";

    /* client parameters */
    public static final String INPUT_CLIENT_ID = "clientId";
    public static final String INPUT_CLIENT_ID_LABEL = "Client Id";
    public static final String INPUT_CLIENT_ID_EXPLANATION = "The client id, as registered in the identity provider";
    public static final String INPUT_TENANT_ID = "tenantId";
    public static final String INPUT_TENANT_ID_LABEL = "Tenant Id";
    public static final String INPUT_TENANT_ID_EXPLANATION = "The tenant id, unique in Camunda 8 Identity";

    /* create-authorization parameters */
    public static final String INPUT_AUTHORIZATION_OWNER_TYPE = "authorizationOwnerType";
    public static final String INPUT_AUTHORIZATION_OWNER_TYPE_LABEL = "Owner Type";
    public static final String INPUT_AUTHORIZATION_OWNER_TYPE_EXPLANATION =
            "Type of the authorization owner. Accepted values: USER, ROLE, GROUP";
    public static final String INPUT_AUTHORIZATION_OWNER = "authorizationOwner";
    public static final String INPUT_AUTHORIZATION_OWNER_LABEL = "Owner";
    public static final String INPUT_AUTHORIZATION_OWNER_EXPLANATION =
            "Id of the owner: a username, a roleId or a groupId, depending on authorizationOwnerType";
    public static final String INPUT_AUTHORIZATION_RESOURCE_TYPE = "authorizationResourceType";
    public static final String INPUT_AUTHORIZATION_RESOURCE_TYPE_LABEL = "Resource Type";
    public static final String INPUT_AUTHORIZATION_RESOURCE_TYPE_EXPLANATION =
            "Type of resource to grant access to. Accepted values: AUDIT_LOG, AUTHORIZATION, BACKUP, BATCH, "
                    + "CLUSTER_VARIABLE, COMPONENT, DECISION_DEFINITION, DECISION_REQUIREMENTS_DEFINITION, DOCUMENT, "
                    + "EXPORTER, EXPRESSION, GLOBAL_LISTENER, GROUP, MAPPING_RULE, MESSAGE, PROCESS_DEFINITION, "
                    + "RESOURCE, ROLE, SECRET, SYSTEM, TENANT, USER, USER_TASK. For COMPONENT, the resourceId is "
                    + "one of \"*\", \"operate\", \"tasklist\", \"admin\".";
    public static final String INPUT_AUTHORIZATION_RESOURCE_ID = "authorizationResourceId";
    public static final String INPUT_AUTHORIZATION_RESOURCE_ID_LABEL = "Resource Id";
    public static final String INPUT_AUTHORIZATION_RESOURCE_ID_EXPLANATION =
            "Id of the resource, e.g. a process definition id, or \"*\"/\"operate\"/\"tasklist\"/\"admin\" when "
                    + "authorizationResourceType is COMPONENT";
    public static final String INPUT_AUTHORIZATION_PERMISSION = "authorizationPermission";
    public static final String INPUT_AUTHORIZATION_PERMISSION_LABEL = "Permission";
    public static final String INPUT_AUTHORIZATION_PERMISSION_EXPLANATION =
            "One or more permissions to grant: either a list of strings (e.g. [\"CLAIM_USER_TASK\", "
                    + "\"COMPLETE_USER_TASK\"]) or a single comma-separated string (e.g. "
                    + "\"CLAIM_USER_TASK,COMPLETE_USER_TASK\"). Accepted values: ACCESS, CANCEL_PROCESS_INSTANCE, "
                    + "CLAIM, CLAIM_USER_TASK, COMPLETE, COMPLETE_USER_TASK, CREATE, "
                    + "CREATE_BATCH_OPERATION_CANCEL_PROCESS_INSTANCE, CREATE_BATCH_OPERATION_DELETE_DECISION_DEFINITION, "
                    + "CREATE_BATCH_OPERATION_DELETE_DECISION_INSTANCE, CREATE_BATCH_OPERATION_DELETE_PROCESS_DEFINITION, "
                    + "CREATE_BATCH_OPERATION_DELETE_PROCESS_INSTANCE, CREATE_BATCH_OPERATION_MIGRATE_PROCESS_INSTANCE, "
                    + "CREATE_BATCH_OPERATION_MODIFY_PROCESS_INSTANCE, CREATE_BATCH_OPERATION_RESOLVE_INCIDENT, "
                    + "CREATE_BATCH_OPERATION_SUSPEND_PROCESS_INSTANCE, CREATE_BATCH_OPERATION_UPDATE_JOB, "
                    + "CREATE_DECISION_INSTANCE, CREATE_PROCESS_INSTANCE, CREATE_TASK_LISTENER, DELETE, "
                    + "DELETE_DECISION_INSTANCE, DELETE_DRD, DELETE_FORM, DELETE_PROCESS, DELETE_PROCESS_INSTANCE, "
                    + "DELETE_RESOURCE, DELETE_TASK_LISTENER, EVALUATE, MODIFY_PROCESS_INSTANCE, PAUSE, READ, "
                    + "READ_DECISION_DEFINITION, READ_DECISION_INSTANCE, READ_JOB_METRIC, READ_PROCESS_DEFINITION, "
                    + "READ_PROCESS_INSTANCE, READ_USAGE_METRIC, READ_USER_TASK, READ_TASK_LISTENER, RESTORE, REVEAL, "
                    + "SUSPEND_PROCESS_INSTANCE, UPDATE, UPDATE_PROCESS_INSTANCE, UPDATE_USER_TASK, UPDATE_TASK_LISTENER. "
                    + "An unrecognized value throws the UNKNOWN_PERMISSION BPMN error.";

    /* search-authorization parameters: any combination may be set, all are optional */
    public static final String INPUT_AUTHORIZATION_SEARCH_OWNER_ID = "authorizationSearchOwnerId";
    public static final String INPUT_AUTHORIZATION_SEARCH_OWNER_ID_LABEL = "Owner Id";
    public static final String INPUT_AUTHORIZATION_SEARCH_OWNER_ID_EXPLANATION = "Filter by the owner id (exact match)";
    public static final String INPUT_AUTHORIZATION_SEARCH_OWNER_TYPE = "authorizationSearchOwnerType";
    public static final String INPUT_AUTHORIZATION_SEARCH_OWNER_TYPE_LABEL = "Owner Type";
    public static final String INPUT_AUTHORIZATION_SEARCH_OWNER_TYPE_EXPLANATION =
            "Filter by the owner type. Accepted values: USER, ROLE, GROUP";
    public static final String INPUT_AUTHORIZATION_SEARCH_RESOURCE_TYPE = "authorizationSearchResourceType";
    public static final String INPUT_AUTHORIZATION_SEARCH_RESOURCE_TYPE_LABEL = "Resource Type";
    public static final String INPUT_AUTHORIZATION_SEARCH_RESOURCE_TYPE_EXPLANATION =
            "Filter by resource type - a list of strings, or a single comma-separated string (e.g. "
                    + "[\"PROCESS_DEFINITION\", \"DECISION_DEFINITION\"] or \"PROCESS_DEFINITION,DECISION_DEFINITION\"). "
                    + "Same accepted values as authorizationResourceType on create-authorization. When several values "
                    + "are given, one search is run per resource type and the results are merged.";
    public static final String INPUT_AUTHORIZATION_SEARCH_RESOURCE_ID = "authorizationSearchResourceId";
    public static final String INPUT_AUTHORIZATION_SEARCH_RESOURCE_ID_LABEL = "Resource Id";
    public static final String INPUT_AUTHORIZATION_SEARCH_RESOURCE_ID_EXPLANATION =
            "Filter by resource id(s) - a list of strings, or a single comma-separated string";
    public static final String INPUT_AUTHORIZATION_SEARCH_RESOURCE_PROPERTY_NAME = "authorizationSearchResourcePropertyName";
    public static final String INPUT_AUTHORIZATION_SEARCH_RESOURCE_PROPERTY_NAME_LABEL = "Resource Property Name";
    public static final String INPUT_AUTHORIZATION_SEARCH_RESOURCE_PROPERTY_NAME_EXPLANATION =
            "Filter by resource property name(s) - a list of strings, or a single comma-separated string";

    /* delete-authorization parameter */
    public static final String INPUT_AUTHORIZATION_KEY = "authorizationKey";
    public static final String INPUT_AUTHORIZATION_KEY_LABEL = "Authorization Key";
    public static final String INPUT_AUTHORIZATION_KEY_EXPLANATION =
            "The authorizationKey of the authorization to delete, as returned by create-authorization "
                    + "in the authorizationKey output";

    /* shared by every paginated search sub-function (search-role, search-group) */
    public static final String INPUT_PAGE_NUMBER = "pageNumber";
    public static final String INPUT_PAGE_NUMBER_LABEL = "Page number";
    public static final String INPUT_PAGE_NUMBER_EXPLANATION =
            "Page number to retrieve, starting at 1. Combined with pageSize to paginate search results.";
    public static final String INPUT_PAGE_SIZE = "pageSize";
    public static final String INPUT_PAGE_SIZE_LABEL = "Page size";
    public static final String INPUT_PAGE_SIZE_EXPLANATION = "Maximum number of results to return for one page.";
    private final String identityFunction = "";
    private final String userName = "";
    private final String userDisplayName = "";
    private final String userEmail = "";
    private final String userPassword = "";
    private final Boolean ignoreIfAlreadyDone = Boolean.TRUE;
    private final String searchByUserName = "";
    private final String searchByUserDisplayName = "";
    private final String searchByUserEmail = "";
    private final String searchByGroupId = "";
    private final String searchByRoleId = "";
    private final String groupId = "";
    private final String groupName = "";
    private final String groupDescription = "";
    private final String groupNameSearch = "";
    private final GroupMembers groupMembers = new GroupMembers();
    private final GroupContentFilter groupContentFilter = new GroupContentFilter();
    private final String roleId = "";
    private final String roleName = "";
    private final String roleDescription = "";
    private final String roleNameSearch = "";
    private final RoleMembers roleMembers = new RoleMembers();
    private final RoleContentFilter roleContentFilter = new RoleContentFilter();
    private final Integer pageNumber = 1;
    private final Integer pageSize = 50;
    private final String clientId = "";
    private final String tenantId = "";
    private final String authorizationOwnerType = "";
    private final String authorizationOwner = "";
    private final String authorizationResourceType = "";
    private final String authorizationResourceId = "";
    private final Object authorizationPermission = null;
    private final String authorizationSearchOwnerId = "";
    private final String authorizationSearchOwnerType = "";
    private final Object authorizationSearchResourceType = null;
    private final Object authorizationSearchResourceId = null;
    private final Object authorizationSearchResourcePropertyName = null;
    private final String authorizationKey = "";
    private final Long waitExport = 0L;

    /**
     * search-group and search-role each declare pageNumber/pageSize independently (a fresh instance
     * per call, not a shared static field) so each can carry its own element-template group (Group vs
     * Role) without the two search functions fighting over a single shared object.
     */
    public static RunnerParameter getPageNumberParameter() {
        return RunnerParameter.getInstance(INPUT_PAGE_NUMBER, //
                        INPUT_PAGE_NUMBER_LABEL, //
                        Integer.class, //
                        1, //
                        RunnerParameter.Level.REQUIRED, //
                        INPUT_PAGE_NUMBER_EXPLANATION)
                .setDefaultValue(0);
    }

    public static RunnerParameter getPageSizeParameter() {
        return RunnerParameter.getInstance(INPUT_PAGE_SIZE, //
                        INPUT_PAGE_SIZE_LABEL, //
                        Integer.class, //
                        50, //
                        RunnerParameter.Level.REQUIRED, //
                        INPUT_PAGE_SIZE_EXPLANATION)
                .setDefaultValue(100);
    }

    /**
     * Shared input for every creation/deletion sub-function (create-user, delete-user, create-group,
     * delete-group, create-role, delete-role, add-in-group, delete-in-group, add-in-role, delete-in-role).
     * A high priority forces it at the end of the parameter list, whatever sub-function registers it first.
     */
    public static RunnerParameter getIgnoreIfAlreadyDoneParameter() {
        RunnerParameter parameter = RunnerParameter.getInstance(INPUT_IGNORE_IF_ALREADY_DONE, //
                        INPUT_IGNORE_IF_ALREADY_DONE_LABEL, //
                        Boolean.class, //
                        Boolean.TRUE, //
                        RunnerParameter.Level.REQUIRED, //
                        INPUT_IGNORE_IF_ALREADY_DONE_EXPLANATION)
                .setGroup("Resilience");
        parameter.setAttribute("priority", 100);
        return parameter;
    }

    /**
     * Shared input for every add-in-group/add-in-role sub-function. If > 0, the function sleeps this
     * many milliseconds before searching for the group/role by id, to absorb the read-after-write
     * delay inherent to Camunda 8's eventually-consistent secondary storage (see retrySearchById in
     * IdentityFunction for the automatic retry; this is a manual, process-modeler-controlled knob on
     * top of it).
     */
    public static RunnerParameter getWaitExportParameter() {
        RunnerParameter parameter = RunnerParameter.getInstance(INPUT_WAIT_EXPORT, //
                        INPUT_WAIT_EXPORT_LABEL, //
                        Long.class, //
                        0L, //
                        RunnerParameter.Level.OPTIONAL, //
                        INPUT_WAIT_EXPORT_EXPLANATION)
                .setVisibleInTemplate()
                .setGroup("Resilience");
        parameter.setAttribute("priority", 95);

        return parameter;
    }

    public String getIdentityFunction() {
        return identityFunction;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public Boolean getIgnoreIfAlreadyDone() {
        return ignoreIfAlreadyDone;
    }

    public String getSearchByUserName() {
        return searchByUserName;
    }

    public String getSearchByUserDisplayName() {
        return searchByUserDisplayName;
    }

    public String getSearchByUserEmail() {
        return searchByUserEmail;
    }

    public String getSearchByGroupId() {
        return searchByGroupId;
    }

    public String getSearchByRoleId() {
        return searchByRoleId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public String getGroupNameSearch() {
        return groupNameSearch;
    }

    public GroupMembers getGroupMembers() {
        return groupMembers;
    }

    public GroupContentFilter getGroupContentFilter() {
        return groupContentFilter;
    }

    public String getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public String getRoleNameSearch() {
        return roleNameSearch;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public RoleMembers getRoleMembers() {
        return roleMembers;
    }

    public RoleContentFilter getRoleContentFilter() {
        return roleContentFilter;
    }

    public String getClientId() {
        return clientId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getAuthorizationOwnerType() {
        return authorizationOwnerType;
    }

    public String getAuthorizationOwner() {
        return authorizationOwner;
    }

    public String getAuthorizationResourceType() {
        return authorizationResourceType;
    }

    public String getAuthorizationResourceId() {
        return authorizationResourceId;
    }

    public Object getAuthorizationPermission() {
        return authorizationPermission;
    }

    public String getAuthorizationSearchOwnerId() {
        return authorizationSearchOwnerId;
    }

    public String getAuthorizationSearchOwnerType() {
        return authorizationSearchOwnerType;
    }

    public Object getAuthorizationSearchResourceType() {
        return authorizationSearchResourceType;
    }

    public Object getAuthorizationSearchResourceId() {
        return authorizationSearchResourceId;
    }

    public Object getAuthorizationSearchResourcePropertyName() {
        return authorizationSearchResourcePropertyName;
    }

    public String getAuthorizationKey() {
        return authorizationKey;
    }

    public Long getWaitExport() {
        return waitExport;
    }

    public String getUserSignature() {
        return "UserName[" + getUserName() + "] displayName[" + getUserDisplayName() + "] Email[" + getUserEmail() + "]";
    }

    @JsonIgnore
    @Override
    public List<Map<String, Object>> getInputParameters() {
        return ParameterToolbox.getInputParameters();
    }
}
