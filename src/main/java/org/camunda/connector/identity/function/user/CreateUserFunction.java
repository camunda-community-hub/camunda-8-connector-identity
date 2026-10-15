package org.camunda.connector.identity.function.user;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.CreateUserResponse;
import io.camunda.client.api.search.response.User;
import io.camunda.client.impl.search.response.UserImpl;
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

public class CreateUserFunction implements IdentitySubFunction {

    public static final String ERROR_CREATE_USER = "CREATE_USER";
    public static final String ERROR_CREATE_USER_LABEL = "Create user failed";
    public static final String ERROR_USER_ALREADY_EXIST = "USER_ALREADY_EXIST";
    public static final String ERROR_USER_ALREADY_EXIST_LABEL = "The username already exists in Camunda 8 Identity";

    private final Logger logger = LoggerFactory.getLogger(CreateUserFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {

        String userName = identityInput.getUserName();
        String displayName = identityInput.getUserDisplayName();
        String email = identityInput.getUserEmail();
        String password = identityInput.getUserPassword();
        boolean ignoreIfAlreadyDone = Boolean.TRUE.equals(identityInput.getIgnoreIfAlreadyDone());
        Long waitExport = identityInput.getWaitExport();

        logger.info("--- CreateUser : START UserName[{}] displayName[{}] email[{}] ignoreIfAlreadyDone[{}] waitExport[{}]",
                userName, displayName, email, ignoreIfAlreadyDone, waitExport);

        long markerTime = System.currentTimeMillis();

        IdentityOutput identityOutput = new IdentityOutput();
        try {
            CreateUserResponse response = camundaClient.newCreateUserCommand()
                    .username(userName)
                    .name(displayName == null ? "" : displayName)
                    .email(email == null ? "" : email)
                    .password(password == null ? "" : password)
                    .execute();

            // Create the user from the response
            IdentityFunction.waitExport(waitExport, logger, "CreateUser userName[" + userName + "]");

            identityOutput.user = new UserImpl(response.getUsername(), response.getName(), response.getEmail());
            identityOutput.status = IdentityOutput.STATUS_CREATED;
            identityOutput.dateOperation = new Date();
            logger.info("--- CreateUser : END {} status[{}] in {} ms",
                    identityInput.getUserSignature(),
                    identityOutput.status,
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e)) {
                if (ignoreIfAlreadyDone) {
                    List<User> items = camundaClient.newUsersSearchRequest()
                            .filter(f -> f.username(userName))
                            .execute()
                            .items();
                    identityOutput.user = IdentityFunction.extractSingleResult(items, logger, "CreateUser userName[" + userName + "]");
                    identityOutput.status = IdentityOutput.STATUS_ALREADY_EXIST;
                    identityOutput.dateOperation = new Date();
                    logger.info("--- CreateUser : END UserName[{}] already exists, ignored, status[{}] in {} ms", userName,
                            identityOutput.status,
                            System.currentTimeMillis() - markerTime);
                    return identityOutput;
                }
                logger.error("--- CreateUser : ERROR UserName[{}] already exists: {}", userName, e.getMessage());
                throw new ConnectorException(ERROR_USER_ALREADY_EXIST,
                        "User[" + userName + "] already exists:" + e.getMessage());
            }
            logger.error("--- CreateUser : ERROR UserName[{}] ", userName, e);
            throw new ConnectorException(ERROR_CREATE_USER, "UserName[" + userName + "] failed " + e.getMessage());
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
                                IdentityInput.INPUT_USER_DISPLAY_NAME_EXPLANATION)
                        .setVisibleInTemplate().setGroup(IdentitySubFunction.USER_GROUP),//

                RunnerParameter.getInstance(IdentityInput.INPUT_USER_EMAIL, //
                                IdentityInput.INPUT_USER_EMAIL_LABEL, //
                                String.class, //
                                "", //
                                RunnerParameter.Level.OPTIONAL, //
                                IdentityInput.INPUT_USER_EMAIL_EXPLANATION)
                        .setVisibleInTemplate().setGroup(IdentitySubFunction.USER_GROUP),//

                RunnerParameter.getInstance(IdentityInput.INPUT_USER_PASSWORD, //
                                IdentityInput.INPUT_USER_PASSWORD_LABEL, //
                                String.class, //
                                "", //
                                RunnerParameter.Level.OPTIONAL, //
                                IdentityInput.INPUT_USER_PASSWORD_EXPLANATION)
                        .setVisibleInTemplate().setGroup(IdentitySubFunction.USER_GROUP),//

                IdentityInput.getWaitExportParameter(), //

                IdentityInput.getIgnoreIfAlreadyDoneParameter() //

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
                ERROR_USER_ALREADY_EXIST, ERROR_USER_ALREADY_EXIST_LABEL, //
                ERROR_CREATE_USER, ERROR_CREATE_USER_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Create user";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Create a user in Camunda 8 Identity";
    }

    @Override
    public String getSubFunctionType() {
        return "create-user";
    }
}
