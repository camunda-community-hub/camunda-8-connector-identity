package org.camunda.connector.identity.function.role;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.CreateRoleResponse;
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

public class CreateRoleFunction implements IdentitySubFunction {

    public static final String ERROR_CREATE_ROLE = "CREATE_ROLE";
    public static final String ERROR_CREATE_ROLE_LABEL = "Create role failed";

    private final Logger logger = LoggerFactory.getLogger(CreateRoleFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();
        String roleId = identityInput.getRoleId();
        boolean ignoreIfAlreadyDone = Boolean.TRUE.equals(identityInput.getIgnoreIfAlreadyDone());
        try {
            CreateRoleResponse response = camundaClient.newCreateRoleCommand()
                    .roleId(roleId)
                    .name(identityInput.getRoleName())
                    .description(identityInput.getRoleDescription())
                    .execute();

            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.roleId = response.getRoleId();
            identityOutput.status = "SUCCESS";
            identityOutput.dateOperation = new Date();
            logger.info("CreateRole RoleId[{}] name[{}] Description[{}] status[SUCCESS] in {} ms",
                    roleId,
                    identityInput.getRoleName(),
                    identityInput.getRoleDescription(),
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                IdentityOutput identityOutput = new IdentityOutput();
                identityOutput.roleId = roleId;
                identityOutput.status = "ALREADY_EXIST";
                identityOutput.dateOperation = new Date();
                logger.info("RoleId[{}] already exists, ignored in {} ms", roleId,
                        System.currentTimeMillis() - markerTime);
                return identityOutput;
            }
            logger.error("Error during CreateRole roleId[{}] : {}", roleId, e.getMessage());
            throw new ConnectorException(ERROR_CREATE_ROLE, "RoleId[" + roleId + "] failed " + e.getMessage());
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

                RunnerParameter.getInstance(IdentityInput.INPUT_ROLE_NAME, //
                        "Role Name", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_ROLE_NAME_EXPLANATION),//

                RunnerParameter.getInstance(IdentityInput.INPUT_ROLE_DESCRIPTION, //
                        "Role Description", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_ROLE_DESCRIPTION_EXPLANATION),//

                IdentityInput.getIgnoreIfAlreadyDoneParameter());//
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityOutput.OUTPUT_STATUS, //
                        "Status", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_STATUS_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_ROLE_ID, //
                        "Role Id", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_ROLE_ID_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_DATE_OPERATION, //
                        "Date of operation", //
                        Date.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_DATE_OPERATION_EXPLANATION));
    }

    @Override
    public Map<String, String> getSubFunctionListBpmnErrors() {
        return Map.of(IdentityFunction.ERROR_IDENTITY_CONNECTION, IdentityFunction.ERROR_IDENTITY_CONNECTION_LABEL, //
                IdentityFunction.ERROR_UNKNOWN_FUNCTION, IdentityFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
                ERROR_CREATE_ROLE, ERROR_CREATE_ROLE_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "CreateRole";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Create a role in Camunda 8 Identity";
    }

    @Override
    public String getSubFunctionType() {
        return "create-role";
    }
}
