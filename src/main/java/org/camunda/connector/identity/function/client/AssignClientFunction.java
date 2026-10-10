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
 * Assign a client (an OAuth2/OIDC client application registered in the identity provider) to a
 * role, a group and/or a tenant. roleId/groupId/tenantId are all optional - whichever are provided
 * are assigned, any left empty are skipped.
 */
public class AssignClientFunction implements IdentitySubFunction {

    public static final String ERROR_ASSIGN_CLIENT = "ASSIGN_CLIENT";
    public static final String ERROR_ASSIGN_CLIENT_LABEL = "Assigning the client failed";

    private final Logger logger = LoggerFactory.getLogger(AssignClientFunction.class.getName());

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
        logger.info("--- AssignClient : START ClientId[{}] roleId[{}] groupId[{}] tenantId[{}] ignoreIfAlreadyDone[{}]",
                clientId, roleId, groupId, tenantId, ignoreIfAlreadyDone);
        try {
            if (containsInformation(roleId))
                assignRoleToClient(camundaClient, clientId, roleId, ignoreIfAlreadyDone);
            if (containsInformation(groupId))
                assignClientToGroup(camundaClient, clientId, groupId, ignoreIfAlreadyDone);
            if (containsInformation(tenantId))
                assignClientToTenant(camundaClient, clientId, tenantId, ignoreIfAlreadyDone);

            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            logger.info("--- AssignClient : END ClientId[{}] roleId[{}] groupId[{}] tenantId[{}] status[{}]",
                    clientId, roleId, groupId, tenantId, identityOutput.status);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("--- AssignClient : ERROR ClientId[{}] : {}", clientId, e.getMessage());
            throw new ConnectorException(ERROR_ASSIGN_CLIENT, "Error during assign-client " + e.getMessage());
        }
    }

    private void assignRoleToClient(CamundaClient camundaClient, String clientId, String roleId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newAssignRoleToClientCommand().roleId(roleId).clientId(clientId).execute();
            logger.info("ClientId[{}] assigned to roleId[{}]", clientId, roleId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("ClientId[{}] already assigned to roleId[{}], ignored", clientId, roleId);
                return;
            }
            logger.error("Error assigning clientId[{}] to roleId[{}] : ", clientId, roleId, e);
            throw new ConnectorException(ERROR_ASSIGN_CLIENT, "clientId[" + clientId + "] roleId[" + roleId + "] : " + e.getMessage());
        }
    }

    private void assignClientToGroup(CamundaClient camundaClient, String clientId, String groupId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newAssignClientToGroupCommand().clientId(clientId).groupId(groupId).execute();
            logger.info("ClientId[{}] assigned to groupId[{}]", clientId, groupId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("ClientId[{}] already assigned to groupId[{}], ignored", clientId, groupId);
                return;
            }
            logger.error("Error assigning clientId[{}] to groupId[{}] : ", clientId, groupId, e);
            throw new ConnectorException(ERROR_ASSIGN_CLIENT, "clientId[" + clientId + "] groupId[" + groupId + "] : " + e.getMessage());
        }
    }

    private void assignClientToTenant(CamundaClient camundaClient, String clientId, String tenantId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newAssignClientToTenantCommand().clientId(clientId).tenantId(tenantId).execute();
            logger.info("ClientId[{}] assigned to tenantId[{}]", clientId, tenantId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("ClientId[{}] already assigned to tenantId[{}], ignored", clientId, tenantId);
                return;
            }
            logger.error("Error assigning clientId[{}] to tenantId[{}] : ", clientId, tenantId, e);
            throw new ConnectorException(ERROR_ASSIGN_CLIENT, "clientId[" + clientId + "] tenantId[" + tenantId + "] : " + e.getMessage());
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
                ERROR_ASSIGN_CLIENT, ERROR_ASSIGN_CLIENT_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Assign client";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Assign a client to a role, a group and/or a tenant";
    }

    @Override
    public String getSubFunctionType() {
        return "assign-client";
    }
}
