package org.camunda.connector.identity.function.user;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.command.UpdateUserCommandStep1;
import io.camunda.client.api.response.UpdateUserResponse;
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

            identityOutput.userName = response.getUsername();
            identityOutput.status = "SUCCESS";
            identityOutput.dateOperation = new Date();
            logger.info("UpdateUser UserName[{}] name[{}] email[{}] status[SUCCESS] in {} ms",
                    identityOutput.userName,
                    identityInput.getUserDisplayName(),
                    identityInput.getUserEmail(),
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("Error during UpdateUser on {}: {}", identityInput.getUserSignature(), e.getMessage());
            throw new ConnectorException(ERROR_UPDATE_USER, "During update user[" + userName + "] : " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityInput.INPUT_USER_NAME, //
                        "User Name", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_USER_NAME_EXPLANATION),//

                RunnerParameter.getInstance(IdentityInput.INPUT_USER_DISPLAY_NAME, //
                        "Name", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_USER_DISPLAY_NAME_EXPLANATION),//

                RunnerParameter.getInstance(IdentityInput.INPUT_USER_EMAIL, //
                        "Email", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_USER_EMAIL_EXPLANATION),//

                RunnerParameter.getInstance(IdentityInput.INPUT_USER_PASSWORD, //
                        "Password", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_USER_PASSWORD_EXPLANATION)//
        );

    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityOutput.OUTPUT_STATUS, //
                        "Status", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_STATUS_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_DATE_OPERATION, //
                        "Date of operation", //
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
        return "updateUser";
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
