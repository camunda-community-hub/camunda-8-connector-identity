package org.camunda.connector.identity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.client.api.search.response.Client;
import io.camunda.client.api.search.response.MappingRule;
import io.camunda.client.api.search.response.Role;
import io.camunda.client.api.search.response.RoleGroup;
import io.camunda.connector.cherrytemplate.CherryOutput;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import org.camunda.connector.identity.toolbox.ParameterToolbox;

import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class IdentityOutput implements CherryOutput {

    public static final String OUTPUT_STATUS = "status";
    public static final String OUTPUT_STATUS_EXPLANATION = "Status of operation";
    public static final String OUTPUT_DATE_OPERATION = "dateOperation";
    public static final String OUTPUT_DATE_OPERATION_EXPLANATION = "Date when the operation is done in Camunda 8 Identity";
    public static final String OUTPUT_USER_NAME = "userName";
    public static final String OUTPUT_USER_NAME_EXPLANATION = "Username of the user created (or updated). Camunda 8 Identity identifies a user by its username, there is no separate userId";
    public static final String OUTPUT_USER = "user";
    public static final String OUTPUT_USER_EXPLANATION = "Identity user";
    public static final String OUTPUT_LIST_USERS = "listUsers";
    public static final String OUTPUT_LIST_USERS_EXPLANATION = "List of users found by the search, or members of a group (raw Camunda 8 client objects, not maps)";
    public static final String OUTPUT_GROUP_ID = "groupId";
    public static final String OUTPUT_GROUP_ID_EXPLANATION = "Id of the group created/updated/deleted";
    public static final String OUTPUT_LIST_ROLES = "listRoles";
    public static final String OUTPUT_LIST_ROLES_EXPLANATION = "List of roles assigned to the group";
    public static final String OUTPUT_LIST_MAPPING_RULES = "listMappingRules";
    public static final String OUTPUT_LIST_MAPPING_RULES_EXPLANATION = "List of mapping rules assigned to the group";
    public static final String OUTPUT_LIST_CLIENT_ID = "listClientId";
    public static final String OUTPUT_LIST_CLIENT_ID_EXPLANATION = "List of clients assigned to the group/role";
    public static final String OUTPUT_ROLE_ID = "roleId";
    public static final String OUTPUT_ROLE_ID_EXPLANATION = "Id of the role created/updated/deleted";
    public static final String OUTPUT_LIST_GROUPS = "listGroups";
    public static final String OUTPUT_LIST_GROUPS_EXPLANATION = "List of groups assigned to the role";

    public static final List<RunnerParameter> allParameters = ParameterToolbox.getOutputParametersRaw();

    public String status;
    public Date dateOperation = null;
    public String userName;
    public Object user;
    /**
     * List of users found by a search, or members of a group/role. Holds the real Camunda 8 client
     * objects (User, or GroupUser/RoleUser when the search is by group/role) - never a Map.
     */
    public List<?> listUsers;
    public String groupId;
    public List<Role> listRoles;
    public List<MappingRule> listMappingRules;
    public List<Client> listClientId;
    public String roleId;
    public List<RoleGroup> listGroups;

    @JsonIgnore
    @Override
    public List<Map<String, Object>> getOutputParameters() {
        return ParameterToolbox.getOutputParameters();
    }

}
