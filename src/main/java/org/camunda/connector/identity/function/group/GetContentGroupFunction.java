package org.camunda.connector.identity.function.group;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.search.response.Group;
import io.camunda.client.api.search.response.GroupUser;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import org.camunda.connector.identity.IdentityFunction;
import org.camunda.connector.identity.IdentityInput;
import org.camunda.connector.identity.IdentityOutput;
import org.camunda.connector.identity.toolbox.GroupContentFilter;
import org.camunda.connector.identity.toolbox.IdentitySubFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Return the content of a group. groupContentFilter selects which categories to return
 * (users/roles/mappingRule/client) - every category set to true is populated in the matching
 * output (as the real Camunda 8 client objects, not maps).
 */
public class GetContentGroupFunction implements IdentitySubFunction {

    public static final String ERROR_GET_CONTENT_GROUP = "GET_CONTENT_GROUP";
    public static final String ERROR_GET_CONTENT_GROUP_LABEL = "During the read of the group content";

    public static final String ERROR_GROUP_NOT_FOUND = "GROUP_NOT_FOUND";
    public static final String ERROR_GROUP_NOT_FOUND_LABEL = "No group found with the id provided";

    private final Logger logger = LoggerFactory.getLogger(GetContentGroupFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();
        String groupId = identityInput.getGroupId();
        GroupContentFilter filter = identityInput.getGroupContentFilter();
        logger.info("--- GetContentGroup : START GroupId[{}] filter[users={},roles={},mappingRule={},client={}]",
                groupId,
                filter == null || filter.isUsers(),
                filter == null || filter.isRoles(),
                filter == null || filter.isMappingRule(),
                filter == null || filter.isClient());
        try {
            IdentityOutput identityOutput = new IdentityOutput();

            List<Group> groupItems = camundaClient.newGroupsSearchRequest()
                    .filter(f -> f.groupId(groupId))
                    .execute().items();
            if (groupItems == null || groupItems.size() != 1) {
                logger.error("--- AddInGroup : ERROR GroupId[{}] not found, find {} items", groupId, groupItems == null ? 0 : groupItems.size());
                throw new ConnectorException(ERROR_GROUP_NOT_FOUND, "No group (or too many group) found with groupId[" + groupId + "] : find " + (groupItems == null ? 0 : groupItems.size()));
            }
            identityOutput.group = groupItems.getFirst();

            if (filter == null || filter.isUsers()) {
                List<GroupUser> items = camundaClient.newUsersByGroupSearchRequest(groupId).execute().items();
                identityOutput.listUsers = new ArrayList<>();
                for (GroupUser groupUser : items) {
                    identityOutput.listUsers.addAll(camundaClient.newUsersSearchRequest()
                            .filter(f -> f.username(groupUser.getUsername()))
                            .execute().items());
                }
            }
            if (filter == null || filter.isRoles())
                identityOutput.listRoles = camundaClient.newRolesByGroupSearchRequest(groupId).execute().items();
            if (filter == null || filter.isMappingRule())
                identityOutput.listMappingRules = camundaClient.newMappingRulesByGroupSearchRequest(groupId).execute().items();
            if (filter == null || filter.isClient())
                identityOutput.listClients = camundaClient.newClientsByGroupSearchRequest(groupId).execute().items();

            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            identityOutput.dateOperation = new Date();
            logger.info(
                    "--- GetContentGroup : END GroupId[{}] group[{}]  filter[users={},roles={},mappingRule={},client={}] listUsers[{}] listRoles[{}] listMappingRules[{}] listClientId[{}] status[{}] in {} ms",
                    groupId,
                    identityOutput.group,
                    filter == null || filter.isUsers(),
                    filter == null || filter.isRoles(),
                    filter == null || filter.isMappingRule(),
                    filter == null || filter.isClient(),
                    identityOutput.listUsers,
                    identityOutput.listRoles,
                    identityOutput.listMappingRules,
                    identityOutput.listClients,
                    identityOutput.status,
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("--- GetContentGroup : ERROR GroupId[{}] : {}", groupId, e.getMessage());
            throw new ConnectorException(ERROR_GET_CONTENT_GROUP, "groupId[" + groupId + "] : " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityInput.INPUT_GROUP_ID, //
                        "Group Id", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_GROUP_ID_EXPLANATION),//

                RunnerParameter.getInstance(IdentityInput.INPUT_GROUP_CONTENT_FILTER, //
                        "Content filter", //
                        GroupContentFilter.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_GROUP_CONTENT_FILTER_EXPLANATION));//
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityOutput.OUTPUT_STATUS, //
                        "Status", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_STATUS_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_GROUP, //
                        "Group", //
                        Group.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_GROUP_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_USERS, //
                        "List Users", //
                        List.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_USERS_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_ROLES, //
                        "List Roles", //
                        List.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_ROLES_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_MAPPING_RULES, //
                        "List Mapping Rules", //
                        List.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_MAPPING_RULES_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_CLIENT_ID, //
                        "List Client Id", //
                        List.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_CLIENT_ID_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_DATE_OPERATION, //
                        "Date of operation", //
                        Date.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_DATE_OPERATION_EXPLANATION));
    }

    @Override
    public Map<String, String> getSubFunctionListBpmnErrors() {
        return Map.of(IdentityFunction.ERROR_IDENTITY_CONNECTION, IdentityFunction.ERROR_IDENTITY_CONNECTION_LABEL, //
                IdentityFunction.ERROR_UNKNOWN_FUNCTION, IdentityFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
                ERROR_GROUP_NOT_FOUND, ERROR_GROUP_NOT_FOUND_LABEL, //
                ERROR_GET_CONTENT_GROUP, ERROR_GET_CONTENT_GROUP_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Get content group";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Get the content of a group (users, roles, mapping rules and/or clients)";
    }

    @Override
    public String getSubFunctionType() {
        return "get-content-group";
    }
}
