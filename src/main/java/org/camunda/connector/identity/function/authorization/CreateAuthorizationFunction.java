package org.camunda.connector.identity.function.authorization;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.CreateAuthorizationResponse;
import io.camunda.client.api.search.enums.OwnerType;
import io.camunda.client.api.search.enums.PermissionType;
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

import java.util.*;

/**
 * Create an authorization, granting one or more permissions on a resource to a user, a role or a
 * group. See https://docs.camunda.io/docs/components/concepts/access-control/authorizations/ .
 */
public class CreateAuthorizationFunction implements IdentitySubFunction {

    public static final String ERROR_CREATE_AUTHORIZATION = "CREATE_AUTHORIZATION";
    public static final String ERROR_CREATE_AUTHORIZATION_LABEL = "Create authorization failed";

    public static final String ERROR_UNKNOWN_PERMISSION = "UNKNOWN_PERMISSION";
    public static final String ERROR_UNKNOWN_PERMISSION_LABEL = "One of the given permissions is not a valid Camunda 8 permission";

    private final Logger logger = LoggerFactory.getLogger(CreateAuthorizationFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();
        String ownerType = identityInput.getAuthorizationOwnerType();
        String owner = identityInput.getAuthorizationOwner();
        String resourceType = identityInput.getAuthorizationResourceType();
        String resourceId = identityInput.getAuthorizationResourceId();
        List<String> permissionNames = IdentityFunction.parseStringList(identityInput.getAuthorizationPermission());
        boolean ignoreIfAlreadyDone = Boolean.TRUE.equals(identityInput.getIgnoreIfAlreadyDone());
        Long waitExport = identityInput.getWaitExport();

        logger.info("--- CreateAuthorization : START ownerType[{}] owner[{}] resourceType[{}] resourceId[{}] permission{} ignoreIfAlreadyDone[{}] waitExport[{}]",
                ownerType, owner, resourceType, resourceId, permissionNames, ignoreIfAlreadyDone, waitExport);

        List<PermissionType> permissionTypes = new ArrayList<>();
        for (String permissionName : permissionNames) {
            try {
                permissionTypes.add(PermissionType.valueOf(permissionName));
            } catch (IllegalArgumentException e) {
                logger.error("--- CreateAuthorization : ERROR unknown permission[{}]", permissionName);
                throw new ConnectorException(ERROR_UNKNOWN_PERMISSION, "Unknown permission[" + permissionName + "]");
            }
        }

        OwnerType ownerTypeValue = OwnerType.valueOf(ownerType);
        ResourceType resourceTypeValue = ResourceType.valueOf(resourceType);

        try {
            CreateAuthorizationResponse response = camundaClient.newCreateAuthorizationCommand()
                    .ownerId(owner)
                    .ownerType(ownerTypeValue)
                    .resourceId(resourceId)
                    .resourceType(resourceTypeValue)
                    .permissionTypes(permissionTypes.toArray(new PermissionType[0]))
                    .execute();

            IdentityFunction.waitExport(waitExport, logger, "CreateAuthorization owner[" + owner + "]");

            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.authorizationKey = response.getAuthorizationKey();
            identityOutput.status = IdentityOutput.STATUS_CREATED;
            identityOutput.dateOperation = new Date();
            logger.info("--- CreateAuthorization : END authorizationKey[{}] status[{}] in {} ms",
                    identityOutput.authorizationKey, identityOutput.status, System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                IdentityOutput identityOutput = new IdentityOutput();
                List<Authorization> items = camundaClient.newAuthorizationSearchRequest()
                        .filter(f -> f.ownerId(owner).ownerType(ownerTypeValue).resourceType(resourceTypeValue).resourceIds(resourceId))
                        .execute().items();
                Authorization existing = IdentityFunction.extractSingleResult(items, logger,
                        "CreateAuthorization owner[" + owner + "] resourceType[" + resourceType + "] resourceId[" + resourceId + "]");
                identityOutput.authorizationKey = existing == null ? null : Long.valueOf(existing.getAuthorizationKey());
                identityOutput.status = IdentityOutput.STATUS_ALREADY_EXIST;
                identityOutput.dateOperation = new Date();
                logger.info("--- CreateAuthorization : END already exists, ignored, authorizationKey[{}] status[{}] in {} ms",
                        identityOutput.authorizationKey, identityOutput.status, System.currentTimeMillis() - markerTime);
                return identityOutput;
            }
            logger.error("--- CreateAuthorization : ERROR ownerType[{}] owner[{}] resourceType[{}] resourceId[{}] : {}",
                    ownerType, owner, resourceType, resourceId, e.getMessage());
            throw new ConnectorException(ERROR_CREATE_AUTHORIZATION, "Error during create-authorization " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(
                AuthorizationChoices.addOwnerTypeChoices(RunnerParameter.getInstance(IdentityInput.INPUT_AUTHORIZATION_OWNER_TYPE, //
                        IdentityInput.INPUT_AUTHORIZATION_OWNER_TYPE_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_AUTHORIZATION_OWNER_TYPE_EXPLANATION).setGroup(IdentitySubFunction.AUTHORIZATION_GROUP)),//

                RunnerParameter.getInstance(IdentityInput.INPUT_AUTHORIZATION_OWNER, //
                        IdentityInput.INPUT_AUTHORIZATION_OWNER_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_AUTHORIZATION_OWNER_EXPLANATION).setGroup(IdentitySubFunction.AUTHORIZATION_GROUP),//

                AuthorizationChoices.addResourceTypeChoices(RunnerParameter.getInstance(IdentityInput.INPUT_AUTHORIZATION_RESOURCE_TYPE, //
                        IdentityInput.INPUT_AUTHORIZATION_RESOURCE_TYPE_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_AUTHORIZATION_RESOURCE_TYPE_EXPLANATION).setGroup(IdentitySubFunction.AUTHORIZATION_GROUP)),//

                RunnerParameter.getInstance(IdentityInput.INPUT_AUTHORIZATION_RESOURCE_ID, //
                        IdentityInput.INPUT_AUTHORIZATION_RESOURCE_ID_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_AUTHORIZATION_RESOURCE_ID_EXPLANATION).setGroup(IdentitySubFunction.AUTHORIZATION_GROUP),//

                RunnerParameter.getInstance(IdentityInput.INPUT_AUTHORIZATION_PERMISSION, //
                        IdentityInput.INPUT_AUTHORIZATION_PERMISSION_LABEL, //
                        Object.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_AUTHORIZATION_PERMISSION_EXPLANATION).setGroup(IdentitySubFunction.AUTHORIZATION_GROUP),//

                IdentityInput.getWaitExportParameter(), //

                IdentityInput.getIgnoreIfAlreadyDoneParameter());//
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityOutput.OUTPUT_STATUS, //
                        IdentityOutput.OUTPUT_STATUS_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_STATUS_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_AUTHORIZATION_KEY, //
                        IdentityOutput.OUTPUT_AUTHORIZATION_KEY_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_AUTHORIZATION_KEY_EXPLANATION), //

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
                ERROR_UNKNOWN_PERMISSION, ERROR_UNKNOWN_PERMISSION_LABEL, //
                ERROR_CREATE_AUTHORIZATION, ERROR_CREATE_AUTHORIZATION_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Create authorization";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Create an authorization, granting permissions on a resource to a user, a role or a group";
    }

    @Override
    public String getSubFunctionType() {
        return "create-authorization";
    }
}
