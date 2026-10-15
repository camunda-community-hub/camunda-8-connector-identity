package org.camunda.connector.identity.function.group;

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
 * Search groups by name, using a "like" (wildcard) match: * matches zero/one/multiple characters,
 * ? matches a single character (e.g. "super*" matches "supervisor"). The $like advanced filter on
 * GroupFilter.name is only understood by a Camunda 8.10+ broker - against an older broker (8.9 and
 * before) it fails with "400 Request property [filter.name] cannot be parsed", since that broker
 * version doesn't yet support the advanced-filter shape for this field. The Camunda 8 Identity
 * Group API only exposes groupId and name as filter criteria at all - there is no date-based filter
 * (no created/updated timestamp is exposed on Group) and no way to search groups by the content
 * they contain (e.g. "find all groups a given username belongs to"): the only join-style search
 * goes from a known groupId to its members (newUsersByGroupSearchRequest), never the reverse.
 */
public class SearchGroupFunction implements IdentitySubFunction {

    public static final String ERROR_SEARCH_GROUP = "SEARCH_GROUP";
    public static final String ERROR_SEARCH_GROUP_LABEL = "During search group(s) in Camunda 8 Identity";

    private final Logger logger = LoggerFactory.getLogger(SearchGroupFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        String groupNameSearch = identityInput.getGroupNameSearch();
        int pageNumber = identityInput.getPageNumber() == null ? 1 : identityInput.getPageNumber();
        int pageSize = identityInput.getPageSize() == null ? 50 : identityInput.getPageSize();
        int offset = (Math.max(pageNumber, 1) - 1) * pageSize;
        logger.info("--- SearchGroup : START GroupNameSearch[{}] pageNumber[{}] pageSize[{}]", groupNameSearch, pageNumber, pageSize);
        try {
            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.listGroups = camundaClient.newGroupsSearchRequest()
                    .filter(f -> f.name(n -> n.like(groupNameSearch)))
                    .page(p -> p.from(offset).limit(pageSize))
                    .execute().items();
            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            logger.info("--- SearchGroup : END GroupNameSearch[{}] listGroups[{}] status[{}]",
                    groupNameSearch, identityOutput.listGroups, identityOutput.status);
            return identityOutput;
        } catch (Exception e) {
            logger.error("--- SearchGroup : ERROR GroupNameSearch[{}] : {}", groupNameSearch, e.getMessage());
            throw new ConnectorException(ERROR_SEARCH_GROUP, "groupNameSearch[" + groupNameSearch + "] : " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityInput.INPUT_GROUP_NAME_SEARCH, //
                        IdentityInput.INPUT_GROUP_NAME_SEARCH_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_GROUP_NAME_SEARCH_EXPLANATION).setGroup(IdentitySubFunction.GROUP_GROUP),//

                IdentityInput.getPageNumberParameter().setGroup(IdentitySubFunction.GROUP_GROUP), //

                IdentityInput.getPageSizeParameter().setGroup(IdentitySubFunction.GROUP_GROUP));//
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityOutput.OUTPUT_STATUS, //
                        IdentityOutput.OUTPUT_STATUS_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_STATUS_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_GROUPS, //
                        IdentityOutput.OUTPUT_LIST_GROUPS_LABEL, //
                        List.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_GROUPS_EXPLANATION));
    }

    @Override
    public Map<String, String> getSubFunctionListBpmnErrors() {
        return Map.of(IdentityFunction.ERROR_IDENTITY_CONNECTION, IdentityFunction.ERROR_IDENTITY_CONNECTION_LABEL, //
                IdentityFunction.ERROR_UNKNOWN_FUNCTION, IdentityFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
                ERROR_SEARCH_GROUP, ERROR_SEARCH_GROUP_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Search group";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Search groups by name in Camunda 8 Identity";
    }

    @Override
    public String getSubFunctionType() {
        return "search-group";
    }
}
