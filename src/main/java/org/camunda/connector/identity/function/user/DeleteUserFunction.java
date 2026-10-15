package org.camunda.connector.identity.function.user;

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

public class DeleteUserFunction implements IdentitySubFunction {

    public static final String ERROR_DELETE_USER = "DELETE_USER";
    public static final String ERROR_DELETE_USER_LABEL = "During delete user in Camunda 8 Identity";

    private final Logger logger = LoggerFactory.getLogger(DeleteUserFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();
        String userName = identityInput.getUserName();
        boolean ignoreIfAlreadyDone = Boolean.TRUE.equals(identityInput.getIgnoreIfAlreadyDone());
        Long waitExport = identityInput.getWaitExport();
        logger.info("--- DeleteUser : START UserName[{}] ignoreIfAlreadyDone[{}] waitExport[{}]", userName, ignoreIfAlreadyDone, waitExport);
        try {
            camundaClient.newDeleteUserCommand(userName).execute();

            IdentityFunction.waitExport(waitExport, logger, "DeleteUser userName[" + userName + "]");

            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            identityOutput.dateOperation = new Date();
            identityOutput.userNameDeleted = userName;
            logger.info("--- DeleteUser : END UserName[{}] status[{}] in {} ms",
                    userName,
                    identityOutput.status,
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                IdentityOutput identityOutput = new IdentityOutput();
                identityOutput.status = IdentityOutput.STATUS_ALREADY_DELETED;
                identityOutput.dateOperation = new Date();
                identityOutput.userNameDeleted = userName;
                logger.info("--- DeleteUser : END UserName[{}] already deleted, ignored, status[{}] in {} ms", userName,
                        identityOutput.status,
                        System.currentTimeMillis() - markerTime);
                return identityOutput;
            }
            logger.error("--- DeleteUser : ERROR UserName[{}] : {}", userName, e.getMessage());
            throw new ConnectorException(ERROR_DELETE_USER, "During delete user[" + userName + "] : " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityInput.INPUT_USER_NAME, //
                        IdentityInput.INPUT_USER_NAME_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_USER_NAME_EXPLANATION).setGroup(IdentitySubFunction.USER_GROUP),//

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

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_DATE_OPERATION, //
                        IdentityOutput.OUTPUT_DATE_OPERATION_LABEL, //
                        Date.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_DATE_OPERATION_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_USER_NAME_DELETED, //
                        IdentityOutput.OUTPUT_USER_NAME_DELETED_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_USER_NAME_DELETED_EXPLANATION));
    }

    @Override
    public Map<String, String> getSubFunctionListBpmnErrors() {
        return Map.of(IdentityFunction.ERROR_IDENTITY_CONNECTION, IdentityFunction.ERROR_IDENTITY_CONNECTION_LABEL, //
                IdentityFunction.ERROR_UNKNOWN_FUNCTION, IdentityFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
                ERROR_DELETE_USER, ERROR_DELETE_USER_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Delete user";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Delete a user from its username";
    }

    @Override
    public String getSubFunctionType() {
        return "delete-user";
    }
}
