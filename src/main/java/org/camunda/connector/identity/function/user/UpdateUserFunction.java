package org.camunda.connector.identity.function.user;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.command.UpdateUserCommandStep1;
import io.camunda.client.api.response.UpdateUserResponse;
import io.camunda.client.api.search.response.User;
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

public class UpdateUserFunction implements IdentitySubFunction {

    public static final String ERROR_UPDATE_USER = "UPDATE_USER";
    public static final String ERROR_UPDATE_USER_LABEL = "During update user in Camunda 8 Identity";

    private final Logger logger = LoggerFactory.getLogger(UpdateUserFunction.class.getName());

    private static boolean containsInformation(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();
        String userName = identityInput.getUserName();
        Long waitExport = identityInput.getWaitExport();
        logger.info("--- UpdateUser : START UserName[{}] displayName[{}] email[{}] waitExport[{}]",
                userName, identityInput.getUserDisplayName(), identityInput.getUserEmail(), waitExport);
        IdentityOutput identityOutput = new IdentityOutput();
        try {
            UpdateUserCommandStep1 command = camundaClient.newUpdateUserCommand(userName);
            if (containsInformation(identityInput.getUserDisplayName()))
                command = command.name(identityInput.getUserDisplayName());
            if (containsInformation(identityInput.getUserEmail()))
                command = command.email(identityInput.getUserEmail());
            if (containsInformation(identityInput.getUserPassword()))
                command = command.password(identityInput.getUserPassword());
            UpdateUserResponse response = command.execute();


            String respondUserName = response.getUsername();
            List<User> items = camundaClient.newUsersSearchRequest()
                    .filter(f -> f.username(respondUserName))
                    .execute()
                    .items();
            identityOutput.user = IdentityFunction.extractSingleResult(items, logger, "UpdateUser userName[" + respondUserName + "]");

            IdentityFunction.waitExport(waitExport, logger, "UpdateUser userName[" + respondUserName + "]");
            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            identityOutput.dateOperation = new Date();
            logger.info("--- UpdateUser : END UserName[{}] name[{}] email[{}] status[{}] UserUpdated[{}] in {} ms",
                    identityInput.getUserName(),
                    identityInput.getUserDisplayName(),
                    identityInput.getUserEmail(),
                    identityOutput.status,
                    identityOutput.user,
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("--- UpdateUser : ERROR UserName[{}] : {}", userName, e.getMessage());
            throw new ConnectorException(ERROR_UPDATE_USER, "During update user[" + userName + "] : " + e.getMessage());
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

                RunnerParameter.getInstance(IdentityInput.INPUT_USER_DISPLAY_NAME, //
                        IdentityInput.INPUT_USER_DISPLAY_NAME_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_USER_DISPLAY_NAME_EXPLANATION).setGroup(IdentitySubFunction.USER_GROUP),//

                RunnerParameter.getInstance(IdentityInput.INPUT_USER_EMAIL, //
                        IdentityInput.INPUT_USER_EMAIL_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_USER_EMAIL_EXPLANATION).setGroup(IdentitySubFunction.USER_GROUP),//

                RunnerParameter.getInstance(IdentityInput.INPUT_USER_PASSWORD, //
                        IdentityInput.INPUT_USER_PASSWORD_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_USER_PASSWORD_EXPLANATION).setGroup(IdentitySubFunction.USER_GROUP),//

                IdentityInput.getWaitExportParameter()//
        );

    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityOutput.OUTPUT_STATUS, //
                        IdentityOutput.OUTPUT_STATUS_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_STATUS_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_USER, //
                        IdentityOutput.OUTPUT_USER_LABEL, //
                        User.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_USER_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_DATE_OPERATION, //
                        IdentityOutput.OUTPUT_DATE_OPERATION_LABEL, //
                        Date.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_DATE_OPERATION_EXPLANATION)); //
    }

    @Override
    public Map<String, String> getSubFunctionListBpmnErrors() {
        return Map.of(IdentityFunction.ERROR_IDENTITY_CONNECTION, IdentityFunction.ERROR_IDENTITY_CONNECTION_LABEL, //
                IdentityFunction.ERROR_UNKNOWN_FUNCTION, IdentityFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
                ERROR_UPDATE_USER, ERROR_UPDATE_USER_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Update user";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Update a user in Camunda 8 Identity";
    }

    @Override
    public String getSubFunctionType() {
        return "update-user";
    }
}
