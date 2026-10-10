package org.camunda.connector.identity.function.role;

import io.camunda.client.CamundaClient;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import org.camunda.connector.identity.IdentityFunction;
import org.camunda.connector.identity.IdentityInput;
import org.camunda.connector.identity.IdentityOutput;
import org.camunda.connector.identity.toolbox.IdentitySubFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Search roles by name, using a "like" (wildcard) match: * matches zero/one/multiple characters,
 * ? matches a single character (e.g. "super*" matches "supervisor"). The $like advanced filter on
 * RoleFilter.name is only understood by a Camunda 8.10+ broker - against an older broker (8.9 and
 * before) it fails with "400 Request property [filter.name] cannot be parsed", since that broker
 * version doesn't yet support the advanced-filter shape for this field. The Camunda 8 Identity
 * Role API only exposes roleId and name as filter criteria at all - there is no date-based filter
 * (no created/updated timestamp is exposed on Role) and no way to search roles by the content
 * they contain (e.g. "find all roles a given username belongs to"): the only join-style search
 * goes from a known roleId to its members (newUsersByRoleSearchRequest), never the reverse.
 */
public class SearchRoleFunction implements IdentitySubFunction {

    public static final String ERROR_SEARCH_ROLE = "SEARCH_ROLE";
    public static final String ERROR_SEARCH_ROLE_LABEL = "During search role(s) in Camunda 8 Identity";

    private final Logger logger = LoggerFactory.getLogger(SearchRoleFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        String roleNameSearch = identityInput.getRoleNameSearch();
        int pageNumber = identityInput.getPageNumber() == null ? 1 : identityInput.getPageNumber();
        int pageSize = identityInput.getPageSize() == null ? 50 : identityInput.getPageSize();
        int offset = (Math.max(pageNumber, 1) - 1) * pageSize;
        logger.info("--- SearchRole : START RoleNameSearch[{}] pageNumber[{}] pageSize[{}]", roleNameSearch, pageNumber, pageSize);
        try {
            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.listRoles = camundaClient.newRolesSearchRequest()
                    .filter(f -> f.name(n -> n.like(roleNameSearch)))
                    .page(p -> p.from(offset).limit(pageSize))
                    .execute().items();
            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            logger.info("--- SearchRole : END RoleNameSearch[{}] listRoles[{}] status[{}]",
                    roleNameSearch, identityOutput.listRoles, identityOutput.status);
            return identityOutput;
        } catch (Exception e) {
            logger.error("--- SearchRole : ERROR RoleNameSearch[{}] : {}", roleNameSearch, e.getMessage());
            throw new ConnectorException(ERROR_SEARCH_ROLE, "roleNameSearch[" + roleNameSearch + "] : " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityInput.INPUT_ROLE_NAME_SEARCH, //
                        "Role Name filter", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_ROLE_NAME_SEARCH_EXPLANATION),//

                IdentityInput.pageNumberParameter, //

                IdentityInput.pageSizeParameter);//
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityOutput.OUTPUT_STATUS, //
                        "Status", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_STATUS_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_ROLES, //
                        "List Roles", //
                        List.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_ROLES_EXPLANATION));
    }

    @Override
    public Map<String, String> getSubFunctionListBpmnErrors() {
        return Map.of(IdentityFunction.ERROR_IDENTITY_CONNECTION, IdentityFunction.ERROR_IDENTITY_CONNECTION_LABEL, //
                IdentityFunction.ERROR_UNKNOWN_FUNCTION, IdentityFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
                ERROR_SEARCH_ROLE, ERROR_SEARCH_ROLE_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Search role";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Search roles by name in Camunda 8 Identity";
    }

    @Override
    public String getSubFunctionType() {
        return "search-role";
    }
}
