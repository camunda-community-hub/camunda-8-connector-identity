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

    /* search parameters */
    public static final String INPUT_SEARCH_BY_USER_NAME = "searchByUserName";
    public static final String INPUT_SEARCH_BY_USER_NAME_EXPLANATION = "Search by the username";
    public static final String INPUT_SEARCH_BY_USER_DISPLAY_NAME = "searchByUserDisplayName";
    public static final String INPUT_SEARCH_BY_USER_DISPLAY_NAME_EXPLANATION = "Search by the name of the user";
    public static final String INPUT_SEARCH_BY_USER_EMAIL = "searchByUserEmail";
    public static final String INPUT_SEARCH_BY_USER_EMAIL_EXPLANATION = "Search by Email of the user";
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

    /* add-in-role / delete-in-role parameter: one object listing the members per category */
    public static final String INPUT_ROLE_MEMBERS = "roleMembers";
    public static final String INPUT_ROLE_MEMBERS_EXPLANATION =
            "Members to add/remove, per category: {\"users\": [\"<userName>\"...], \"groups\": [\"<groupId>\"...], "
                    + "\"client\": [\"<clientId>\"...], \"mappingRule\": [\"<mappingRuleId>\"...]}";

    /* search-in-role parameter: one object selecting which categories to return */
    public static final String INPUT_ROLE_CONTENT_FILTER = "roleContentFilter";
    public static final String INPUT_ROLE_CONTENT_FILTER_EXPLANATION =
            "Categories to return: {\"users\": true|false, \"groups\": true|false, \"client\": true|false, \"mappingRule\": true|false}";

    public static final List<RunnerParameter> allParameters = ParameterToolbox.getInputParametersRaw();

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

    private final GroupMembers groupMembers = new GroupMembers();
    private final GroupContentFilter groupContentFilter = new GroupContentFilter();

    private final String roleId = "";
    private final String roleName = "";
    private final String roleDescription = "";

    private final RoleMembers roleMembers = new RoleMembers();
    private final RoleContentFilter roleContentFilter = new RoleContentFilter();

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

    public RoleMembers getRoleMembers() {
        return roleMembers;
    }

    public RoleContentFilter getRoleContentFilter() {
        return roleContentFilter;
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
