package org.camunda.connector.identity.function.role;

import io.camunda.client.CamundaClient;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import org.camunda.connector.identity.IdentityFunction;
import org.camunda.connector.identity.IdentityInput;
import org.camunda.connector.identity.IdentityOutput;
import org.camunda.connector.identity.toolbox.IdentitySubFunction;
import org.camunda.connector.identity.toolbox.RoleMembers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Add members in a role. roleMembers lists, per category, the users/groups/mapping rules/clients to add.
 */
public class AddInRoleFunction implements IdentitySubFunction {

    public static final String ERROR_ADD_IN_ROLE = "ADD_IN_ROLE";
    public static final String ERROR_ADD_IN_ROLE_LABEL = "Adding a member to the role failed";

    private final Logger logger = LoggerFactory.getLogger(AddInRoleFunction.class.getName());

    private static List<String> safeList(List<String> list) {
        return list == null ? List.of() : list;
    }

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();
        String roleId = identityInput.getRoleId();
        RoleMembers roleMembers = identityInput.getRoleMembers();
        boolean ignoreIfAlreadyDone = Boolean.TRUE.equals(identityInput.getIgnoreIfAlreadyDone());
        try {
            for (String userName : safeList(roleMembers.getUsers()))
                addUserInRole(camundaClient, userName, roleId, ignoreIfAlreadyDone);
            for (String groupId : safeList(roleMembers.getGroups()))
                addGroupInRole(camundaClient, groupId, roleId, ignoreIfAlreadyDone);
            for (String mappingRuleId : safeList(roleMembers.getMappingRule()))
                addMappingRuleInRole(camundaClient, mappingRuleId, roleId, ignoreIfAlreadyDone);
            for (String clientId : safeList(roleMembers.getClient()))
                addClientInRole(camundaClient, clientId, roleId, ignoreIfAlreadyDone);

            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.roleId = roleId;
            identityOutput.status = "SUCCESS";
            identityOutput.dateOperation = new Date();
            logger.info("AddInRole RoleId[{}] users[{}] groups[{}] mappingRule[{}] client[{}] status[SUCCESS] in {} ms",
                    roleId,
                    roleMembers.getUsers(),
                    roleMembers.getGroups(),
                    roleMembers.getMappingRule(),
                    roleMembers.getClient(),
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("Error during AddInRole roleId[{}] : {}", roleId, e.getMessage());
            throw new ConnectorException(ERROR_ADD_IN_ROLE, "Error during add-in-role " + e.getMessage());
        }
    }

    private void addUserInRole(CamundaClient camundaClient, String userName, String roleId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newAssignRoleToUserCommand().roleId(roleId).username(userName).execute();
            logger.info("UserName[{}] added in roleId[{}]", userName, roleId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("UserName[{}] already in roleId[{}], ignored", userName, roleId);
                return;
            }
            logger.error("Error adding userName[{}] in roleId[{}] : {}", userName, roleId, e);
            throw new ConnectorException(ERROR_ADD_IN_ROLE, "userName[" + userName + "] roleId[" + roleId + "] : " + e.getMessage());
        }
    }

    private void addGroupInRole(CamundaClient camundaClient, String groupId, String roleId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newAssignRoleToGroupCommand().roleId(roleId).groupId(groupId).execute();
            logger.info("GroupId[{}] added in roleId[{}]", groupId, roleId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("GroupId[{}] already in roleId[{}], ignored", groupId, roleId);
                return;
            }
            logger.error("Error adding groupId[{}] in roleId[{}] : {}", groupId, roleId, e);
            throw new ConnectorException(ERROR_ADD_IN_ROLE, "groupId[" + groupId + "] roleId[" + roleId + "] : " + e.getMessage());
        }
    }

    private void addMappingRuleInRole(CamundaClient camundaClient, String mappingRuleId, String roleId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newAssignRoleToMappingRuleCommand().roleId(roleId).mappingRuleId(mappingRuleId).execute();
            logger.info("MappingRuleId[{}] added in roleId[{}]", mappingRuleId, roleId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("MappingRuleId[{}] already in roleId[{}], ignored", mappingRuleId, roleId);
                return;
            }
            logger.error("Error adding mappingRuleId[{}] in roleId[{}] : {}", mappingRuleId, roleId, e);
            throw new ConnectorException(ERROR_ADD_IN_ROLE,
                    "mappingRuleId[" + mappingRuleId + "] roleId[" + roleId + "] : " + e.getMessage());
        }
    }

    private void addClientInRole(CamundaClient camundaClient, String clientId, String roleId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newAssignRoleToClientCommand().roleId(roleId).clientId(clientId).execute();
            logger.info("ClientId[{}] added in roleId[{}]", clientId, roleId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("ClientId[{}] already in roleId[{}], ignored", clientId, roleId);
                return;
            }
            logger.error("Error adding clientId[{}] in roleId[{}] : {}", clientId, roleId, e);
            throw new ConnectorException(ERROR_ADD_IN_ROLE, "clientId[" + clientId + "] roleId[" + roleId + "] : " + e.getMessage());
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

                RunnerParameter.getInstance(IdentityInput.INPUT_ROLE_MEMBERS, //
                        "Members", //
                        RoleMembers.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_ROLE_MEMBERS_EXPLANATION),//

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
                ERROR_ADD_IN_ROLE, ERROR_ADD_IN_ROLE_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "AddInRole";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Add users, groups, mapping rules or clients in a role";
    }

    @Override
    public String getSubFunctionType() {
        return "add-in-role";
    }
}
