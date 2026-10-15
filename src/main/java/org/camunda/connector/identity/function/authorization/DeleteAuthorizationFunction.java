package org.camunda.connector.identity.function.authorization;

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
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Delete an authorization from its authorizationKey (as returned by create-authorization).
 */
public class DeleteAuthorizationFunction implements IdentitySubFunction {

    public static final String ERROR_DELETE_AUTHORIZATION = "DELETE_AUTHORIZATION";
    public static final String ERROR_DELETE_AUTHORIZATION_LABEL = "During delete authorization in Camunda 8 Identity";

    private final Logger logger = LoggerFactory.getLogger(DeleteAuthorizationFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();
        String authorizationKeyInput = identityInput.getAuthorizationKey();
        Long waitExport = identityInput.getWaitExport();
        logger.info("--- DeleteAuthorization : START AuthorizationKey[{}] waitExport[{}]", authorizationKeyInput, waitExport);

        long authorizationKey;
        try {
            authorizationKey = Long.parseLong(authorizationKeyInput == null ? "" : authorizationKeyInput.trim());
        } catch (NumberFormatException e) {
            logger.error("--- DeleteAuthorization : ERROR AuthorizationKey[{}] is not a valid number", authorizationKeyInput);
            throw new ConnectorException(ERROR_DELETE_AUTHORIZATION, "authorizationKey[" + authorizationKeyInput + "] is not a valid number");
        }

        try {
            camundaClient.newDeleteAuthorizationCommand(authorizationKey).execute();

            IdentityFunction.waitExport(waitExport, logger, "DeleteAuthorization authorizationKey[" + authorizationKey + "]");

            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            identityOutput.dateOperation = new Date();
            identityOutput.authorizationKey = authorizationKey;
            identityOutput.authorizationKeyDeleted = authorizationKey;
            logger.info("--- DeleteAuthorization : END AuthorizationKey[{}] status[{}] in {} ms",
                    authorizationKeyInput, identityOutput.status, System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("--- DeleteAuthorization : ERROR AuthorizationKey[{}] : {}", authorizationKeyInput, e.getMessage());
            throw new ConnectorException(ERROR_DELETE_AUTHORIZATION, "authorizationKey[" + authorizationKeyInput + "] : " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityInput.INPUT_AUTHORIZATION_KEY, //
                        IdentityInput.INPUT_AUTHORIZATION_KEY_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_AUTHORIZATION_KEY_EXPLANATION).setGroup(IdentitySubFunction.AUTHORIZATION_GROUP),//

                IdentityInput.getWaitExportParameter());
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityOutput.OUTPUT_STATUS, //
                        IdentityOutput.OUTPUT_STATUS_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_STATUS_EXPLANATION), //
                RunnerParameter.getInstance(IdentityOutput.OUTPUT_DATE_OPERATION, //
                        IdentityOutput.OUTPUT_DATE_OPERATION_LABEL, //
                        Date.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_DATE_OPERATION_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_AUTHORIZATION_KEY_DELETED, //
                        IdentityOutput.OUTPUT_AUTHORIZATION_KEY_DELETED_LABEL, //
                        Long.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_AUTHORIZATION_KEY_DELETED_EXPLANATION));
    }

    @Override
    public Map<String, String> getSubFunctionListBpmnErrors() {
        return Map.of(IdentityFunction.ERROR_IDENTITY_CONNECTION, IdentityFunction.ERROR_IDENTITY_CONNECTION_LABEL, //
                IdentityFunction.ERROR_UNKNOWN_FUNCTION, IdentityFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
                ERROR_DELETE_AUTHORIZATION, ERROR_DELETE_AUTHORIZATION_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Delete authorization";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Delete an authorization from its authorizationKey";
    }

    @Override
    public String getSubFunctionType() {
        return "delete-authorization";
    }
}
