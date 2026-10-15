package org.camunda.connector.identity.function.role;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.search.response.Role;
import io.camunda.client.api.search.response.RoleGroup;
import io.camunda.client.api.search.response.RoleUser;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import org.camunda.connector.identity.IdentityFunction;
import org.camunda.connector.identity.IdentityInput;
import org.camunda.connector.identity.IdentityOutput;
import org.camunda.connector.identity.toolbox.IdentitySubFunction;
import org.camunda.connector.identity.toolbox.RoleContentFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Return the content of a role. roleContentFilter selects which categories to return
 * (users/groups/mappingRule/client) - every category set to true is populated in the matching
 * output (as the real Camunda 8 client objects, not maps).
 */
public class GetContentRoleFunction implements IdentitySubFunction {

    public static final String ERROR_GET_CONTENT_ROLE = "GET_CONTENT_ROLE";
    public static final String ERROR_GET_CONTENT_ROLE_LABEL = "During the read of the role content";

    public static final String ERROR_ROLE_NOT_FOUND = "ROLE_NOT_FOUND";
    public static final String ERROR_ROLE_NOT_FOUND_LABEL = "No role found with the id provided";

    private final Logger logger = LoggerFactory.getLogger(GetContentRoleFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();
        String roleId = identityInput.getRoleId();
        RoleContentFilter filter = identityInput.getRoleContentFilter();
        logger.info("--- GetContentRole : START RoleId[{}] filter[users={},groups={},mappingRule={},client={}]",
                roleId,
                filter == null || filter.isUsers(),
                filter == null || filter.isGroups(),
                filter == null || filter.isMappingRule(),
                filter == null || filter.isClient());
        try {
            IdentityOutput identityOutput = new IdentityOutput();

            List<Role> roleItems = IdentityFunction.retrySearchById(
                    () -> camundaClient.newRolesSearchRequest().filter(f -> f.roleId(roleId)).execute().items(),
                    logger, "GetContentRole roleId[" + roleId + "]");
            if (roleItems == null || roleItems.size() != 1) {
                logger.error("--- GetContentRole : ERROR RoleId[{}] not found: find {} items", roleId, roleItems == null ? "null" : roleItems.size());
                throw new ConnectorException(ERROR_ROLE_NOT_FOUND, "No group (or too many group) found with roleId[" + roleId + "] : find " + (roleItems == null ? 0 : roleItems.size()));
            }
            identityOutput.role = roleItems.getFirst();


            if (filter == null || filter.isUsers()) {
                List<RoleUser> items = camundaClient.newUsersByRoleSearchRequest(roleId).execute().items();
                identityOutput.listUsers = new ArrayList<>();
                for (RoleUser roleUser : items) {
                    identityOutput.listUsers.addAll(camundaClient.newUsersSearchRequest()
                            .filter(f -> f.username(roleUser.getUsername()))
                            .execute().items());
                }

            }
            if (filter == null || filter.isGroups()) {
                List<RoleGroup> items = camundaClient.newGroupsByRoleSearchRequest(roleId).execute().items();
                identityOutput.listGroups = new ArrayList<>();
                for (RoleGroup roleGroup : items) {
                    identityOutput.listGroups.addAll(camundaClient.newGroupsSearchRequest()
                            .filter(f -> f.groupId(roleGroup.getGroupId()))
                            .execute().items());
                }
            }
            if (filter == null || filter.isMappingRule())
                identityOutput.listMappingRules = camundaClient.newMappingRulesByRoleSearchRequest(roleId).execute().items();
            if (filter == null || filter.isClient())
                identityOutput.listClients = camundaClient.newClientsByRoleSearchRequest(roleId).execute().items();

            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            identityOutput.dateOperation = new Date();
            logger.info(
                    "--- GetContentRole : END RoleId[{}] role[{}] filter[users={},groups={},mappingRule={},client={}] listUsers[{}] listGroups[{}] listMappingRules[{}] listClientId[{}] status[{}] in {} ms",
                    roleId,
                    identityOutput.role,
                    filter == null || filter.isUsers(),
                    filter == null || filter.isGroups(),
                    filter == null || filter.isMappingRule(),
                    filter == null || filter.isClient(),
                    identityOutput.listUsers,
                    identityOutput.listGroups,
                    identityOutput.listMappingRules,
                    identityOutput.listClients,
                    identityOutput.status,
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("--- GetContentRole : ERROR RoleId[{}] : {}", roleId, e.getMessage());
            throw new ConnectorException(ERROR_GET_CONTENT_ROLE, "roleId[" + roleId + "] : " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityInput.INPUT_ROLE_ID, //
                        IdentityInput.INPUT_ROLE_ID_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_ROLE_ID_EXPLANATION).setGroup(IdentitySubFunction.ROLE_GROUP),//

                RunnerParameter.getInstance(IdentityInput.INPUT_ROLE_CONTENT_FILTER, //
                        IdentityInput.INPUT_ROLE_CONTENT_FILTER_LABEL, //
                        RoleContentFilter.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_ROLE_CONTENT_FILTER_EXPLANATION).setGroup(IdentitySubFunction.ROLE_GROUP));//
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityOutput.OUTPUT_STATUS, //
                        IdentityOutput.OUTPUT_STATUS_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_STATUS_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_ROLE, //
                        IdentityOutput.OUTPUT_ROLE_LABEL, //
                        Role.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_ROLE_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_USERS, //
                        IdentityOutput.OUTPUT_LIST_USERS_LABEL, //
                        List.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_USERS_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_GROUPS, //
                        IdentityOutput.OUTPUT_LIST_GROUPS_LABEL, //
                        List.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_GROUPS_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_MAPPING_RULES, //
                        IdentityOutput.OUTPUT_LIST_MAPPING_RULES_LABEL, //
                        List.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_MAPPING_RULES_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_CLIENT_ID, //
                        IdentityOutput.OUTPUT_LIST_CLIENT_ID_LABEL, //
                        List.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_CLIENT_ID_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_DATE_OPERATION, //
                        IdentityOutput.OUTPUT_DATE_OPERATION_LABEL, //
                        Date.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_DATE_OPERATION_EXPLANATION));
    }

    @Override
    public Map<String, String> getSubFunctionListBpmnErrors() {
        return Map.of(IdentityFunction.ERROR_IDENTITY_CONNECTION, IdentityFunction.ERROR_IDENTITY_CONNECTION_LABEL, //
                IdentityFunction.ERROR_UNKNOWN_FUNCTION, IdentityFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
                ERROR_ROLE_NOT_FOUND, ERROR_ROLE_NOT_FOUND_LABEL, //
                ERROR_GET_CONTENT_ROLE, ERROR_GET_CONTENT_ROLE_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Get content role";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Get the content of a role (users, groups, mapping rules and/or clients)";
    }

    @Override
    public String getSubFunctionType() {
        return "get-content-role";
    }
}
