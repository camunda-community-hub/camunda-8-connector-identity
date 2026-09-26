package org.camunda.connector.identity.function.role;

import io.camunda.client.CamundaClient;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Return the content of a role. roleContentFilter selects which categories to return
 * (users/groups/mappingRule/client) - every category set to true is populated in the matching
 * output (as the real Camunda 8 client objects, not maps).
 */
public class SearchInRoleFunction implements IdentitySubFunction {

    public static final String ERROR_SEARCH_IN_ROLE = "SEARCH_IN_ROLE";
    public static final String ERROR_SEARCH_IN_ROLE_LABEL = "During the read of the role content";

    private final Logger logger = LoggerFactory.getLogger(SearchInRoleFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();
        String roleId = identityInput.getRoleId();
        RoleContentFilter filter = identityInput.getRoleContentFilter();
        try {
            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.roleId = roleId;

            if (filter == null || filter.isUsers())
                identityOutput.listUsers = camundaClient.newUsersByRoleSearchRequest(roleId).execute().items();
            if (filter == null || filter.isGroups())
                identityOutput.listGroups = camundaClient.newGroupsByRoleSearchRequest(roleId).execute().items();
            if (filter == null || filter.isMappingRule())
                identityOutput.listMappingRules = camundaClient.newMappingRulesByRoleSearchRequest(roleId).execute().items();
            if (filter == null || filter.isClient())
                identityOutput.listClientId = camundaClient.newClientsByRoleSearchRequest(roleId).execute().items();

            identityOutput.status = "SUCCESS";
            identityOutput.dateOperation = new Date();
            logger.info(
                    "SearchInRole RoleId[{}] filter[users={},groups={},mappingRule={},client={}] listUsers[{}] listGroups[{}] listMappingRules[{}] listClientId[{}] status[SUCCESS] in {} ms",
                    roleId,
                    filter == null || filter.isUsers(),
                    filter == null || filter.isGroups(),
                    filter == null || filter.isMappingRule(),
                    filter == null || filter.isClient(),
                    identityOutput.listUsers,
                    identityOutput.listGroups,
                    identityOutput.listMappingRules,
                    identityOutput.listClientId,
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("Error during SearchInRole roleId[{}] : {}", roleId, e.getMessage());
            throw new ConnectorException(ERROR_SEARCH_IN_ROLE, "roleId[" + roleId + "] : " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityInput.INPUT_ROLE_ID, //
                        "Role Id", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_ROLE_ID_EXPLANATION),//

                RunnerParameter.getInstance(IdentityInput.INPUT_ROLE_CONTENT_FILTER, //
                        "Content filter", //
                        RoleContentFilter.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_ROLE_CONTENT_FILTER_EXPLANATION));//
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_USERS, //
                        "List Users", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_USERS_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_GROUPS, //
                        "List Groups", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_GROUPS_EXPLANATION), //

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
                ERROR_SEARCH_IN_ROLE, ERROR_SEARCH_IN_ROLE_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "SearchInRole";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Get the content of a role (users, groups, mapping rules and/or clients)";
    }

    @Override
    public String getSubFunctionType() {
        return "search-in-role";
    }
}
