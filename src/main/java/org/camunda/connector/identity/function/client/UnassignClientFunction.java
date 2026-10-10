package org.camunda.connector.identity.function.client;

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
import java.util.List;
import java.util.Map;

/**
 * Unassign a client (an OAuth2/OIDC client application registered in the identity provider) from a
 * role, a group and/or a tenant. roleId/groupId/tenantId are all optional - whichever are provided
 * are unassigned, any left empty are skipped.
 */
public class UnassignClientFunction implements IdentitySubFunction {

    public static final String ERROR_UNASSIGN_CLIENT = "UNASSIGN_CLIENT";
    public static final String ERROR_UNASSIGN_CLIENT_LABEL = "Unassigning the client failed";

    private final Logger logger = LoggerFactory.getLogger(UnassignClientFunction.class.getName());

    private static boolean containsInformation(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        String clientId = identityInput.getClientId();
        String roleId = identityInput.getRoleId();
        String groupId = identityInput.getGroupId();
        String tenantId = identityInput.getTenantId();
        boolean ignoreIfAlreadyDone = Boolean.TRUE.equals(identityInput.getIgnoreIfAlreadyDone());
        logger.info("--- UnassignClient : START ClientId[{}] roleId[{}] groupId[{}] tenantId[{}] ignoreIfAlreadyDone[{}]",
                clientId, roleId, groupId, tenantId, ignoreIfAlreadyDone);
        try {
            if (containsInformation(roleId))
                unassignRoleFromClient(camundaClient, clientId, roleId, ignoreIfAlreadyDone);
            if (containsInformation(groupId))
                unassignClientFromGroup(camundaClient, clientId, groupId, ignoreIfAlreadyDone);
            if (containsInformation(tenantId))
                unassignClientFromTenant(camundaClient, clientId, tenantId, ignoreIfAlreadyDone);

            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            logger.info("--- UnassignClient : END ClientId[{}] roleId[{}] groupId[{}] tenantId[{}] status[{}]",
                    clientId, roleId, groupId, tenantId, identityOutput.status);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("--- UnassignClient : ERROR ClientId[{}] : {}", clientId, e.getMessage());
            throw new ConnectorException(ERROR_UNASSIGN_CLIENT, "Error during unassign-client " + e.getMessage());
        }
    }

    private void unassignRoleFromClient(CamundaClient camundaClient, String clientId, String roleId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newUnassignRoleFromClientCommand().roleId(roleId).clientId(clientId).execute();
            logger.info("ClientId[{}] unassigned from roleId[{}]", clientId, roleId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("ClientId[{}] not assigned to roleId[{}], ignored", clientId, roleId);
                return;
            }
            logger.error("Error unassigning clientId[{}] from roleId[{}] : ", clientId, roleId, e);
            throw new ConnectorException(ERROR_UNASSIGN_CLIENT, "clientId[" + clientId + "] roleId[" + roleId + "] : " + e.getMessage());
        }
    }

    private void unassignClientFromGroup(CamundaClient camundaClient, String clientId, String groupId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newUnassignClientFromGroupCommand().clientId(clientId).groupId(groupId).execute();
            logger.info("ClientId[{}] unassigned from groupId[{}]", clientId, groupId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("ClientId[{}] not assigned to groupId[{}], ignored", clientId, groupId);
                return;
            }
            logger.error("Error unassigning clientId[{}] from groupId[{}] : ", clientId, groupId, e);
            throw new ConnectorException(ERROR_UNASSIGN_CLIENT, "clientId[" + clientId + "] groupId[" + groupId + "] : " + e.getMessage());
        }
    }

    private void unassignClientFromTenant(CamundaClient camundaClient, String clientId, String tenantId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newUnassignClientFromTenantCommand().clientId(clientId).tenantId(tenantId).execute();
            logger.info("ClientId[{}] unassigned from tenantId[{}]", clientId, tenantId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("ClientId[{}] not assigned to tenantId[{}], ignored", clientId, tenantId);
                return;
            }
            logger.error("Error unassigning clientId[{}] from tenantId[{}] : ", clientId, tenantId, e);
            throw new ConnectorException(ERROR_UNASSIGN_CLIENT, "clientId[" + clientId + "] tenantId[" + tenantId + "] : " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityInput.INPUT_CLIENT_ID, //
                        "Client Id", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_CLIENT_ID_EXPLANATION),//

                RunnerParameter.getInstance(IdentityInput.INPUT_ROLE_ID, //
                        "Role Id", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_ROLE_ID_EXPLANATION),//

                RunnerParameter.getInstance(IdentityInput.INPUT_GROUP_ID, //
                        "Group Id", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_GROUP_ID_EXPLANATION),//

                RunnerParameter.getInstance(IdentityInput.INPUT_TENANT_ID, //
                        "Tenant Id", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityInput.INPUT_TENANT_ID_EXPLANATION),//

                IdentityInput.getIgnoreIfAlreadyDoneParameter());//
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return List.of(RunnerParameter.getInstance(IdentityOutput.OUTPUT_STATUS, //
                "Status", //
                String.class, //
                "", //
                RunnerParameter.Level.OPTIONAL, //
                IdentityOutput.OUTPUT_STATUS_EXPLANATION));
    }

    @Override
    public Map<String, String> getSubFunctionListBpmnErrors() {
        return Map.of(IdentityFunction.ERROR_IDENTITY_CONNECTION, IdentityFunction.ERROR_IDENTITY_CONNECTION_LABEL, //
                IdentityFunction.ERROR_UNKNOWN_FUNCTION, IdentityFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
                ERROR_UNASSIGN_CLIENT, ERROR_UNASSIGN_CLIENT_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Unassign client";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Unassign a client from a role, a group and/or a tenant";
    }

    @Override
    public String getSubFunctionType() {
        return "unassign-client";
    }
}
