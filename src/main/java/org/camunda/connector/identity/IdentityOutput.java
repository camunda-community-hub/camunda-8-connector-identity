package org.camunda.connector.identity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.client.api.search.response.*;
import io.camunda.connector.cherrytemplate.CherryOutput;
import org.camunda.connector.identity.toolbox.ParameterToolbox;

import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class IdentityOutput implements CherryOutput {

    public static final String OUTPUT_STATUS = "status";
    public static final String OUTPUT_STATUS_LABEL = "Status";
    public static final String OUTPUT_STATUS_EXPLANATION = "Status of operation";
    public static final String OUTPUT_DATE_OPERATION = "dateOperation";
    public static final String OUTPUT_DATE_OPERATION_LABEL = "Date of operation";
    public static final String OUTPUT_DATE_OPERATION_EXPLANATION = "Date when the operation is done in Camunda 8 Identity";
    public static final String OUTPUT_LIST_USERS = "listUsers";
    public static final String OUTPUT_LIST_USERS_LABEL = "List Users";
    public static final String OUTPUT_LIST_USERS_EXPLANATION = "List of users found by the search, or members of a group (raw Camunda 8 client objects, not maps)";
    public static final String OUTPUT_LIST_ROLES = "listRoles";
    public static final String OUTPUT_LIST_ROLES_LABEL = "List Roles";
    public static final String OUTPUT_LIST_ROLES_EXPLANATION = "List of roles assigned to the group";
    public static final String OUTPUT_LIST_MAPPING_RULES = "listMappingRules";
    public static final String OUTPUT_LIST_MAPPING_RULES_LABEL = "List Mapping Rules";
    public static final String OUTPUT_LIST_MAPPING_RULES_EXPLANATION = "List of mapping rules assigned to the group";
    public static final String OUTPUT_LIST_CLIENT_ID = "listClientId";
    public static final String OUTPUT_LIST_CLIENT_ID_LABEL = "List Client Id";
    public static final String OUTPUT_LIST_CLIENT_ID_EXPLANATION = "List of clients assigned to the group/role";
    public static final String OUTPUT_LIST_GROUPS = "listGroups";
    public static final String OUTPUT_LIST_GROUPS_LABEL = "List Groups";
    public static final String OUTPUT_LIST_GROUPS_EXPLANATION = "List of groups assigned to the role";
    public static final String OUTPUT_USER = "user";
    public static final String OUTPUT_USER_LABEL = "User";
    public static final String OUTPUT_USER_EXPLANATION = "The user created/updated (raw Camunda 8 client object, not a map)";
    public static final String OUTPUT_ROLE = "role";
    public static final String OUTPUT_ROLE_LABEL = "Role";
    public static final String OUTPUT_ROLE_EXPLANATION = "The role created/updated (raw Camunda 8 client object, not a map)";
    public static final String OUTPUT_GROUP = "group";
    public static final String OUTPUT_GROUP_LABEL = "Group";
    public static final String OUTPUT_GROUP_EXPLANATION = "The group created/updated (raw Camunda 8 client object, not a map)";
    public static final String OUTPUT_AUTHORIZATION_KEY = "authorizationKey";
    public static final String OUTPUT_AUTHORIZATION_KEY_LABEL = "Authorization Key";
    public static final String OUTPUT_AUTHORIZATION_KEY_EXPLANATION = "The authorizationKey of the authorization created";
    public static final String OUTPUT_LIST_AUTHORIZATIONS = "listAuthorizations";
    public static final String OUTPUT_LIST_AUTHORIZATIONS_LABEL = "List Authorizations";
    public static final String OUTPUT_LIST_AUTHORIZATIONS_EXPLANATION = "List of authorizations found by the search (raw Camunda 8 client objects, not maps)";

    /* per-entity deletion markers: set to the id of the entity deleted, on every delete-xxx sub-function */
    public static final String OUTPUT_USER_NAME_DELETED = "userNameDeleted";
    public static final String OUTPUT_USER_NAME_DELETED_LABEL = "User Name Deleted";
    public static final String OUTPUT_USER_NAME_DELETED_EXPLANATION = "The username of the user deleted";
    public static final String OUTPUT_GROUP_ID_DELETED = "groupIdDeleted";
    public static final String OUTPUT_GROUP_ID_DELETED_LABEL = "Group Id Deleted";
    public static final String OUTPUT_GROUP_ID_DELETED_EXPLANATION = "The groupId of the group deleted";
    public static final String OUTPUT_ROLE_ID_DELETED = "roleIdDeleted";
    public static final String OUTPUT_ROLE_ID_DELETED_LABEL = "Role Id Deleted";
    public static final String OUTPUT_ROLE_ID_DELETED_EXPLANATION = "The roleId of the role deleted";
    public static final String OUTPUT_AUTHORIZATION_KEY_DELETED = "authorizationKeyDeleted";
    public static final String OUTPUT_AUTHORIZATION_KEY_DELETED_LABEL = "Authorization Key Deleted";
    public static final String OUTPUT_AUTHORIZATION_KEY_DELETED_EXPLANATION = "The authorizationKey of the authorization deleted";

    public final static String STATUS_SUCCESS = "SUCCESS";
    public final static String STATUS_CREATED = "CREATED";
    public final static String STATUS_ALREADY_EXIST = "ALREADY_EXIST";
    public final static String STATUS_ALREADY_DELETED = "ALREADY_DELETED";
    public String status;
    public Date dateOperation = null;
    /**
     * List of users found by a search, or members of a group/role. Holds the real Camunda 8 client
     * objects (User, or GroupUser/RoleUser when the search is by group/role) - never a Map.
     */
    public List<User> listUsers;
    public List<Role> listRoles;
    public List<MappingRule> listMappingRules;
    public List<Client> listClients;
    public List<Group> listGroups;


    public User user;
    public Role role;
    public Group group;
    public Long authorizationKey;
    public List<Authorization> listAuthorizations;
    public String userNameDeleted;
    public String groupIdDeleted;
    public String roleIdDeleted;
    public Long authorizationKeyDeleted;

    @JsonIgnore
    @Override
    public List<Map<String, Object>> getOutputParameters() {
        return ParameterToolbox.getOutputParameters();
    }

}
