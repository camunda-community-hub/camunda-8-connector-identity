package org.camunda.connector.identity.function.role;

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

public class DeleteRoleFunction implements IdentitySubFunction {

    public static final String ERROR_DELETE_ROLE = "DELETE_ROLE";
    public static final String ERROR_DELETE_ROLE_LABEL = "During delete role in Camunda 8 Identity";

    private final Logger logger = LoggerFactory.getLogger(DeleteRoleFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();
        String roleId = identityInput.getRoleId();
        boolean ignoreIfAlreadyDone = Boolean.TRUE.equals(identityInput.getIgnoreIfAlreadyDone());
        Long waitExport = identityInput.getWaitExport();
        logger.info("--- DeleteRole : START RoleId[{}] ignoreIfAlreadyDone[{}] waitExport[{}]", roleId, ignoreIfAlreadyDone, waitExport);
        try {
            camundaClient.newDeleteRoleCommand(roleId).execute();

            IdentityFunction.waitExport(waitExport, logger, "DeleteRole roleId[" + roleId + "]");

            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            identityOutput.dateOperation = new Date();
            identityOutput.roleIdDeleted = roleId;
            logger.info("--- DeleteRole : END RoleId[{}] status[{}] in {} ms",
                    roleId,
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
                identityOutput.roleIdDeleted = roleId;
                logger.info("--- DeleteRole : END RoleId[{}] already deleted, ignored, status[{}] in {} ms", roleId,
                        identityOutput.status,
                        System.currentTimeMillis() - markerTime);
                return identityOutput;
            }
            logger.error("--- DeleteRole : ERROR RoleId[{}] : {}", roleId, e.getMessage());
            throw new ConnectorException(ERROR_DELETE_ROLE, "During delete role[" + roleId + "] : " + e.getMessage());
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

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_ROLE_ID_DELETED, //
                        IdentityOutput.OUTPUT_ROLE_ID_DELETED_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_ROLE_ID_DELETED_EXPLANATION));
    }

    @Override
    public Map<String, String> getSubFunctionListBpmnErrors() {
        return Map.of(IdentityFunction.ERROR_IDENTITY_CONNECTION, IdentityFunction.ERROR_IDENTITY_CONNECTION_LABEL, //
                IdentityFunction.ERROR_UNKNOWN_FUNCTION, IdentityFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
                ERROR_DELETE_ROLE, ERROR_DELETE_ROLE_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Delete role";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Delete a role from its roleId";
    }

    @Override
    public String getSubFunctionType() {
        return "delete-role";
    }
}
