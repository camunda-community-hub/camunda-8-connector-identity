package org.camunda.connector.identity.function.authorization;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.search.enums.OwnerType;
import io.camunda.client.api.search.enums.ResourceType;
import io.camunda.client.api.search.response.Authorization;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import org.camunda.connector.identity.IdentityFunction;
import org.camunda.connector.identity.IdentityInput;
import org.camunda.connector.identity.IdentityOutput;
import org.camunda.connector.identity.toolbox.IdentitySubFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Search authorizations, filtered by any combination of owner id, owner type, resource type(s),
 * resource id(s) and resource property name(s). Unlike every other filter here, resourceType accepts
 * a list: the Camunda 8 client's AuthorizationFilter only accepts one resourceType per search, so
 * when several are given, one search is issued per resourceType and the results are merged.
 */
public class SearchAuthorizationFunction implements IdentitySubFunction {

    public static final String ERROR_SEARCH_AUTHORIZATION = "SEARCH_AUTHORIZATION";
    public static final String ERROR_SEARCH_AUTHORIZATION_LABEL = "During search authorization(s) in Camunda 8 Identity";

    private final Logger logger = LoggerFactory.getLogger(SearchAuthorizationFunction.class.getName());

    private static boolean containsInformation(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        String ownerId = identityInput.getAuthorizationSearchOwnerId();
        String ownerType = identityInput.getAuthorizationSearchOwnerType();
        List<String> resourceTypeNames = IdentityFunction.parseStringList(identityInput.getAuthorizationSearchResourceType());
        List<String> resourceIds = IdentityFunction.parseStringList(identityInput.getAuthorizationSearchResourceId());
        List<String> resourcePropertyNames = IdentityFunction.parseStringList(identityInput.getAuthorizationSearchResourcePropertyName());

        logger.info("--- SearchAuthorization : START ownerId[{}] ownerType[{}] resourceType{} resourceId{} resourcePropertyName{}",
                ownerId, ownerType, resourceTypeNames, resourceIds, resourcePropertyNames);
        try {
            IdentityOutput identityOutput = new IdentityOutput();
            List<Authorization> result = new ArrayList<>();
            if (resourceTypeNames.isEmpty()) {
                result.addAll(search(camundaClient, ownerId, ownerType, null, resourceIds, resourcePropertyNames));
            } else {
                for (String resourceTypeName : resourceTypeNames) {
                    result.addAll(search(camundaClient, ownerId, ownerType, ResourceType.valueOf(resourceTypeName), resourceIds,
                            resourcePropertyNames));
                }
            }
            identityOutput.listAuthorizations = result;
            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            logger.info("--- SearchAuthorization : END listAuthorizations[{}] status[{}]", result.size(), identityOutput.status);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("--- SearchAuthorization : ERROR ownerId[{}] ownerType[{}] : {}", ownerId, ownerType, e.getMessage());
            throw new ConnectorException(ERROR_SEARCH_AUTHORIZATION, "Error during search-authorization " + e.getMessage());
        }
    }

    private List<Authorization> search(CamundaClient camundaClient,
                                       String ownerId,
                                       String ownerType,
                                       ResourceType resourceType,
                                       List<String> resourceIds,
                                       List<String> resourcePropertyNames) {
        return camundaClient.newAuthorizationSearchRequest()
                .filter(f -> {
                    if (containsInformation(ownerId))
                        f.ownerId(ownerId);
                    if (containsInformation(ownerType))
                        f.ownerType(OwnerType.valueOf(ownerType));
                    if (resourceType != null)
                        f.resourceType(resourceType);
                    if (!resourceIds.isEmpty())
                        f.resourceIds(resourceIds);
                    if (!resourcePropertyNames.isEmpty())
                        f.resourcePropertyNames(resourcePropertyNames);
                })
                .execute()
                .items();
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(
                RunnerParameter.getInstance(IdentityInput.INPUT_AUTHORIZATION_SEARCH_OWNER_ID, //
                        IdentityInput.INPUT_AUTHORIZATION_SEARCH_OWNER_ID_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_AUTHORIZATION_SEARCH_OWNER_ID_EXPLANATION).setGroup(IdentitySubFunction.AUTHORIZATION_GROUP),//

                AuthorizationChoices.addOwnerTypeChoices(RunnerParameter.getInstance(IdentityInput.INPUT_AUTHORIZATION_SEARCH_OWNER_TYPE, //
                        IdentityInput.INPUT_AUTHORIZATION_SEARCH_OWNER_TYPE_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_AUTHORIZATION_SEARCH_OWNER_TYPE_EXPLANATION).setGroup(IdentitySubFunction.AUTHORIZATION_GROUP)),//

                AuthorizationChoices.addResourceTypeChoices(RunnerParameter.getInstance(IdentityInput.INPUT_AUTHORIZATION_SEARCH_RESOURCE_TYPE, //
                        IdentityInput.INPUT_AUTHORIZATION_SEARCH_RESOURCE_TYPE_LABEL, //
                        Object.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_AUTHORIZATION_SEARCH_RESOURCE_TYPE_EXPLANATION).setGroup(IdentitySubFunction.AUTHORIZATION_GROUP)),//

                RunnerParameter.getInstance(IdentityInput.INPUT_AUTHORIZATION_SEARCH_RESOURCE_ID, //
                        IdentityInput.INPUT_AUTHORIZATION_SEARCH_RESOURCE_ID_LABEL, //
                        Object.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_AUTHORIZATION_SEARCH_RESOURCE_ID_EXPLANATION).setGroup(IdentitySubFunction.AUTHORIZATION_GROUP),//

                RunnerParameter.getInstance(IdentityInput.INPUT_AUTHORIZATION_SEARCH_RESOURCE_PROPERTY_NAME, //
                        IdentityInput.INPUT_AUTHORIZATION_SEARCH_RESOURCE_PROPERTY_NAME_LABEL, //
                        Object.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_AUTHORIZATION_SEARCH_RESOURCE_PROPERTY_NAME_EXPLANATION).setGroup(IdentitySubFunction.AUTHORIZATION_GROUP));//
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityOutput.OUTPUT_STATUS, //
                        IdentityOutput.OUTPUT_STATUS_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_STATUS_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_AUTHORIZATIONS, //
                        IdentityOutput.OUTPUT_LIST_AUTHORIZATIONS_LABEL, //
                        List.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_AUTHORIZATIONS_EXPLANATION));
    }

    @Override
    public Map<String, String> getSubFunctionListBpmnErrors() {
        return Map.of(IdentityFunction.ERROR_IDENTITY_CONNECTION, IdentityFunction.ERROR_IDENTITY_CONNECTION_LABEL, //
                IdentityFunction.ERROR_UNKNOWN_FUNCTION, IdentityFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
                ERROR_SEARCH_AUTHORIZATION, ERROR_SEARCH_AUTHORIZATION_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Search authorization";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Search authorizations in Camunda 8 Identity, filtered by owner and/or resource";
    }

    @Override
    public String getSubFunctionType() {
        return "search-authorization";
    }
}
