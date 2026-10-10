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
    public static final String INPUT_USER_NAME_EXPLANATION = "The username must be unique in Camunda 8 Identity";
    public static final String INPUT_USER_DISPLAY_NAME = "userDisplayName";
    public static final String INPUT_USER_DISPLAY_NAME_EXPLANATION = "Display name of the user";
    public static final String INPUT_USER_EMAIL = "userEmail";
    public static final String INPUT_USER_EMAIL_EXPLANATION = "Email of the user";
    public static final String INPUT_USER_PASSWORD = "userPassword";
    public static final String INPUT_USER_PASSWORD_EXPLANATION = "Password of the user";

    /* shared by every creation and deletion sub-function */
    public static final String INPUT_IGNORE_IF_ALREADY_DONE = "ignoreIfAlreadyDone";
    public static final String INPUT_IGNORE_IF_ALREADY_DONE_EXPLANATION =
            "If true (default), do not throw a BPMN error when the operation is already done "
                    + "(the entity already exists on creation, or does not exist anymore on deletion)";

    /* search parameters (like-match) */
    public static final String INPUT_SEARCH_BY_USER_NAME = "searchByUserName";
    public static final String INPUT_SEARCH_BY_USER_NAME_EXPLANATION =
            "Search by the username (like-match): use * for zero/one/multiple characters and ? for a single "
                    + "character, e.g. \"*bates\" matches \"walter.bates\". Without a wildcard, only an exact "
                    + "match is returned. The *, ? wildcard syntax is only valid on a Camunda 8.10 or later "
                    + "cluster - against an older broker (8.9 and before) it fails with a 400 error.";
    public static final String INPUT_SEARCH_BY_USER_DISPLAY_NAME = "searchByUserDisplayName";
    public static final String INPUT_SEARCH_BY_USER_DISPLAY_NAME_EXPLANATION =
            "Search by the name of the user (like-match): use * for zero/one/multiple characters and ? for a "
                    + "single character, e.g. \"*Bates\" matches \"Walter Bates\". Without a wildcard, only an "
                    + "exact match is returned. The *, ? wildcard syntax is only valid on a Camunda 8.10 or "
                    + "later cluster - against an older broker (8.9 and before) it fails with a 400 error.";
    public static final String INPUT_SEARCH_BY_USER_EMAIL = "searchByUserEmail";
    public static final String INPUT_SEARCH_BY_USER_EMAIL_EXPLANATION =
            "Search by Email of the user (like-match): use * for zero/one/multiple characters and ? for a "
                    + "single character. Without a wildcard, only an exact match is returned. The *, ? wildcard "
                    + "syntax is only valid on a Camunda 8.10 or later cluster - against an older broker (8.9 "
                    + "and before) it fails with a 400 error.";
    public static final String INPUT_SEARCH_BY_GROUP_ID = "searchByGroupId";
    public static final String INPUT_SEARCH_BY_GROUP_ID_EXPLANATION = "Search all users assigned to the given group";
    public static final String INPUT_SEARCH_BY_ROLE_ID = "searchByRoleId";
    public static final String INPUT_SEARCH_BY_ROLE_ID_EXPLANATION = "Search all users assigned to the given role";

    /* group parameters */
    public static final String INPUT_GROUP_ID = "groupId";
    public static final String INPUT_GROUP_ID_EXPLANATION = "The group id, unique in Camunda 8 Identity";
    public static final String INPUT_GROUP_NAME = "groupName";
    public static final String INPUT_GROUP_NAME_EXPLANATION = "Name of the group";
    public static final String INPUT_GROUP_DESCRIPTION = "groupDescription";
    public static final String INPUT_GROUP_DESCRIPTION_EXPLANATION = "Description of the group";

    /* search-group parameter: name to search (like-match) */
    public static final String INPUT_GROUP_NAME_SEARCH = "groupNameSearch";
    public static final String INPUT_GROUP_NAME_SEARCH_EXPLANATION =
            "Name to search (like-match): use * for zero/one/multiple characters and ? for a single "
                    + "character, e.g. \"super*\" matches \"supervisor\". Without a wildcard, only an exact "
                    + "match is returned. The *, ? wildcard syntax is only valid on a Camunda 8.10 or later "
                    + "cluster - against an older broker (8.9 and before) it fails with a 400 error.";

    /* add-in-group / delete-in-group parameter: one object listing the members per category */
    public static final String INPUT_GROUP_MEMBERS = "groupMembers";
    public static final String INPUT_GROUP_MEMBERS_EXPLANATION =
            "Members to add/remove, per category: {\"users\": [\"<userName>\"...], \"roles\": [\"<roleId>\"...], "
                    + "\"client\": [\"<clientId>\"...], \"mappingRule\": [\"<mappingRuleId>\"...]}";

    /* search-in-group parameter: one object selecting which categories to return */
    public static final String INPUT_GROUP_CONTENT_FILTER = "groupContentFilter";
    public static final String INPUT_GROUP_CONTENT_FILTER_EXPLANATION =
            "Categories to return: {\"users\": true|false, \"roles\": true|false, \"client\": true|false, \"mappingRule\": true|false}";

    /* role parameters */
    public static final String INPUT_ROLE_ID = "roleId";
    public static final String INPUT_ROLE_ID_EXPLANATION = "The role id, unique in Camunda 8 Identity";
    public static final String INPUT_ROLE_NAME = "roleName";
    public static final String INPUT_ROLE_NAME_EXPLANATION = "Name of the role";
    public static final String INPUT_ROLE_DESCRIPTION = "roleDescription";
    public static final String INPUT_ROLE_DESCRIPTION_EXPLANATION = "Description of the role";

    /* search-role parameter: name to search (like-match) */
    public static final String INPUT_ROLE_NAME_SEARCH = "roleNameSearch";
    public static final String INPUT_ROLE_NAME_SEARCH_EXPLANATION =
            "Name to search (like-match): use * for zero/one/multiple characters and ? for a single "
                    + "character, e.g. \"super*\" matches \"supervisor\". Without a wildcard, only an exact "
                    + "match is returned. The *, ? wildcard syntax is only valid on a Camunda 8.10 or later "
                    + "cluster - against an older broker (8.9 and before) it fails with a 400 error.";

    /* add-in-role / delete-in-role parameter: one object listing the members per category */
    public static final String INPUT_ROLE_MEMBERS = "roleMembers";
    public static final String INPUT_ROLE_MEMBERS_EXPLANATION =
            "Members to add/remove, per category: {\"users\": [\"<userName>\"...], \"groups\": [\"<groupId>\"...], "
                    + "\"client\": [\"<clientId>\"...], \"mappingRule\": [\"<mappingRuleId>\"...]}";

    /* search-in-role parameter: one object selecting which categories to return */
    public static final String INPUT_ROLE_CONTENT_FILTER = "roleContentFilter";
    public static final String INPUT_ROLE_CONTENT_FILTER_EXPLANATION =
            "Categories to return: {\"users\": true|false, \"groups\": true|false, \"client\": true|false, \"mappingRule\": true|false}";

    /* client parameters */
    public static final String INPUT_CLIENT_ID = "clientId";
    public static final String INPUT_CLIENT_ID_EXPLANATION = "The client id, as registered in the identity provider";
    public static final String INPUT_TENANT_ID = "tenantId";
    public static final String INPUT_TENANT_ID_EXPLANATION = "The tenant id, unique in Camunda 8 Identity";

    /* shared by every paginated search sub-function (search-role, search-group) */
    public static final String INPUT_PAGE_NUMBER = "pageNumber";
    public static final String INPUT_PAGE_NUMBER_EXPLANATION =
            "Page number to retrieve, starting at 1. Combined with pageSize to paginate search results.";
    public static final String INPUT_PAGE_SIZE = "pageSize";
    public static final String INPUT_PAGE_SIZE_EXPLANATION = "Maximum number of results to return for one page.";
    /**
     * Shared input for every paginated search sub-function (search-role, search-group). A high
     * priority forces it at the end of the parameter list, whatever sub-function registers it first.
     */
    public static RunnerParameter pageNumberParameter = RunnerParameter.getInstance(INPUT_PAGE_NUMBER, //
                    "Page number", //
                    Integer.class, //
                    1, //
                    RunnerParameter.Level.REQUIRED, //
                    INPUT_PAGE_NUMBER_EXPLANATION)
            .setDefaultValue(0);
    public static RunnerParameter pageSizeParameter = RunnerParameter.getInstance(INPUT_PAGE_SIZE, //
                    "Page size", //
                    Integer.class, //
                    50, //
                    RunnerParameter.Level.REQUIRED, //
                    INPUT_PAGE_SIZE_EXPLANATION)
            .setDefaultValue(100);
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

    /**
     * Shared input for every creation/deletion sub-function (create-user, delete-user, create-group,
     * delete-group, create-role, delete-role, add-in-group, delete-in-group, add-in-role, delete-in-role).
     * A high priority forces it at the end of the parameter list, whatever sub-function registers it first.
     */
    public static RunnerParameter getIgnoreIfAlreadyDoneParameter() {
        RunnerParameter parameter = RunnerParameter.getInstance(INPUT_IGNORE_IF_ALREADY_DONE, //
                "Ignore if already done", //
                Boolean.class, //
                Boolean.TRUE, //
                RunnerParameter.Level.REQUIRED, //
                INPUT_IGNORE_IF_ALREADY_DONE_EXPLANATION);
        parameter.setAttribute("priority", 100);
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

    public String getUserSignature() {
        return "UserName[" + getUserName() + "] displayName[" + getUserDisplayName() + "] Email[" + getUserEmail() + "]";
    }

    @JsonIgnore
    @Override
    public List<Map<String, Object>> getInputParameters() {
        return ParameterToolbox.getInputParameters();
    }
}
