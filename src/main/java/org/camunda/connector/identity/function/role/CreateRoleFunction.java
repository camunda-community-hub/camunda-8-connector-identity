package org.camunda.connector.identity.function.role;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.CreateRoleResponse;
import io.camunda.client.api.search.response.Role;
import io.camunda.client.impl.search.response.RoleImpl;
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
        logger.info("--- CreateRole : START RoleId[{}] name[{}] description[{}] ignoreIfAlreadyDone[{}]",
                roleId, identityInput.getRoleName(), identityInput.getRoleDescription(), ignoreIfAlreadyDone);
        try {
            CreateRoleResponse response = camundaClient.newCreateRoleCommand()
                    .roleId(roleId)
                    .name(identityInput.getRoleName())
                    .description(identityInput.getRoleDescription() == null ? "" : identityInput.getRoleDescription())
                    .execute();


            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.role = new RoleImpl(response.getRoleId(), response.getName(), response.getDescription());
            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            identityOutput.dateOperation = new Date();
            logger.info("--- CreateRole : END RoleId[{}] name[{}] Description[{}] status[{}] in {} ms",
                    roleId,
                    identityInput.getRoleName(),
                    identityInput.getRoleDescription(),
                    identityOutput.status,
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                IdentityOutput identityOutput = new IdentityOutput();
                List<Role> items = camundaClient.newRolesSearchRequest()
                        .filter(f -> f.roleId(roleId))
                        .execute().items();
                identityOutput.role = IdentityFunction.extractSingleResult(items, logger, "CreateRole roleId[" + roleId + "]");
                identityOutput.status = IdentityOutput.STATUS_ALREADY_EXIST;
                identityOutput.dateOperation = new Date();
                logger.info("--- CreateRole : END RoleId[{}] already exists, ignored, status[{}] in {} ms", roleId,
                        identityOutput.status,
                        System.currentTimeMillis() - markerTime);
                return identityOutput;
            }
            logger.error("--- CreateRole : ERROR RoleId[{}] : ", roleId, e);
            throw new ConnectorException(ERROR_CREATE_ROLE, "RoleId[" + roleId + "] failed " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityInput.INPUT_ROLE_ID, //
                        IdentityInput.INPUT_ROLE_ID_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_ROLE_ID_EXPLANATION).setGroup(IdentitySubFunction.ROLE_GROUP),//

                RunnerParameter.getInstance(IdentityInput.INPUT_ROLE_NAME, //
                        IdentityInput.INPUT_ROLE_NAME_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_ROLE_NAME_EXPLANATION).setGroup(IdentitySubFunction.ROLE_GROUP),//

                RunnerParameter.getInstance(IdentityInput.INPUT_ROLE_DESCRIPTION, //
                        IdentityInput.INPUT_ROLE_DESCRIPTION_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_ROLE_DESCRIPTION_EXPLANATION).setGroup(IdentitySubFunction.ROLE_GROUP),//

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

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_ROLE, //
                        IdentityOutput.OUTPUT_ROLE_LABEL, //
                        Role.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_ROLE_EXPLANATION), //

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
                ERROR_CREATE_ROLE, ERROR_CREATE_ROLE_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Create role";
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
