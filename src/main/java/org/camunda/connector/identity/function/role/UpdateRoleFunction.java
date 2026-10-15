package org.camunda.connector.identity.function.role;

import io.camunda.client.CamundaClient;
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

public class UpdateRoleFunction implements IdentitySubFunction {

    public static final String ERROR_UPDATE_ROLE = "UPDATE_ROLE";
    public static final String ERROR_UPDATE_ROLE_LABEL = "During update role in Camunda 8 Identity";

    private final Logger logger = LoggerFactory.getLogger(UpdateRoleFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();
        String roleId = identityInput.getRoleId();
        String roleName = identityInput.getRoleName();
        String roleDescription = identityInput.getRoleDescription();
        Long waitExport = identityInput.getWaitExport();
        logger.info("--- UpdateRole : START RoleId[{}] name[{}] Description[{}] waitExport[{}]", roleId, roleName, roleDescription, waitExport);
        try {
            camundaClient.newUpdateRoleCommand(roleId)
                    .name(roleName)
                    .description(roleDescription)
                    .execute();

            IdentityFunction.waitExport(waitExport, logger, "UpdateRole roleId[" + roleId + "]");

            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.role = new RoleImpl(roleId, roleName, roleDescription);
            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            identityOutput.dateOperation = new Date();
            logger.info("--- UpdateRole : END RoleId[{}] name[{}] status[{}] in {} ms",
                    roleId, roleName, identityOutput.status, System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("--- UpdateRole : ERROR RoleId[{}] : {}", roleId, e.getMessage());
            throw new ConnectorException(ERROR_UPDATE_ROLE, "During update role[" + roleId + "] : " + e.getMessage());
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
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_ROLE_NAME_EXPLANATION).setGroup(IdentitySubFunction.ROLE_GROUP),
                RunnerParameter.getInstance(IdentityInput.INPUT_ROLE_DESCRIPTION, //
                        IdentityInput.INPUT_ROLE_DESCRIPTION_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_ROLE_DESCRIPTION_EXPLANATION).setGroup(IdentitySubFunction.ROLE_GROUP),//

                IdentityInput.getWaitExportParameter());//
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
                ERROR_UPDATE_ROLE, ERROR_UPDATE_ROLE_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Update role";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Update a role's name in Camunda 8 Identity";
    }

    @Override
    public String getSubFunctionType() {
        return "update-role";
    }
}
