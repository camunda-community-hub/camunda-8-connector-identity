package org.camunda.connector.identity.function.group;

import io.camunda.client.CamundaClient;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Return the content of a group. groupContentFilter selects which categories to return
 * (users/roles/mappingRule/client) - every category set to true is populated in the matching
 * output (as the real Camunda 8 client objects, not maps).
 */
public class SearchInGroupFunction implements IdentitySubFunction {

    public static final String ERROR_SEARCH_IN_GROUP = "SEARCH_IN_GROUP";
    public static final String ERROR_SEARCH_IN_GROUP_LABEL = "During the read of the group content";

    private final Logger logger = LoggerFactory.getLogger(SearchInGroupFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();
        String groupId = identityInput.getGroupId();
        GroupContentFilter filter = identityInput.getGroupContentFilter();
        try {
            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.groupId = groupId;

            if (filter == null || filter.isUsers())
                identityOutput.listUsers = camundaClient.newUsersByGroupSearchRequest(groupId).execute().items();
            if (filter == null || filter.isRoles())
                identityOutput.listRoles = camundaClient.newRolesByGroupSearchRequest(groupId).execute().items();
            if (filter == null || filter.isMappingRule())
                identityOutput.listMappingRules = camundaClient.newMappingRulesByGroupSearchRequest(groupId).execute().items();
            if (filter == null || filter.isClient())
                identityOutput.listClientId = camundaClient.newClientsByGroupSearchRequest(groupId).execute().items();

            identityOutput.status = "SUCCESS";
            identityOutput.dateOperation = new Date();
            logger.info(
                    "SearchInGroup GroupId[{}] filter[users={},roles={},mappingRule={},client={}] listUsers[{}] listRoles[{}] listMappingRules[{}] listClientId[{}] status[SUCCESS] in {} ms",
                    groupId,
                    filter == null || filter.isUsers(),
                    filter == null || filter.isRoles(),
                    filter == null || filter.isMappingRule(),
                    filter == null || filter.isClient(),
                    identityOutput.listUsers,
                    identityOutput.listRoles,
                    identityOutput.listMappingRules,
                    identityOutput.listClientId,
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("Error during SearchInGroup groupId[{}] : {}", groupId, e.getMessage());
            throw new ConnectorException(ERROR_SEARCH_IN_GROUP, "groupId[" + groupId + "] : " + e.getMessage());
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
        return Arrays.asList(RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_USERS, //
                        "List Users", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_USERS_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_ROLES, //
                        "List Roles", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_ROLES_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_MAPPING_RULES, //
                        "List Mapping Rules", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_MAPPING_RULES_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_CLIENT_ID, //
                        "List Client Id", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_CLIENT_ID_EXPLANATION));
    }

    @Override
    public Map<String, String> getSubFunctionListBpmnErrors() {
        return Map.of(IdentityFunction.ERROR_IDENTITY_CONNECTION, IdentityFunction.ERROR_IDENTITY_CONNECTION_LABEL, //
                IdentityFunction.ERROR_UNKNOWN_FUNCTION, IdentityFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
                ERROR_SEARCH_IN_GROUP, ERROR_SEARCH_IN_GROUP_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "SearchInGroup";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Get the content of a group (users, roles, mapping rules and/or clients)";
    }

    @Override
    public String getSubFunctionType() {
        return "search-in-group";
    }
}
