package org.camunda.connector.identity.function.group;

import io.camunda.client.CamundaClient;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import org.camunda.connector.identity.IdentityFunction;
import org.camunda.connector.identity.IdentityInput;
import org.camunda.connector.identity.IdentityOutput;
import org.camunda.connector.identity.toolbox.GroupMembers;
import org.camunda.connector.identity.toolbox.IdentitySubFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Add members in a group. groupMembers lists, per category, the users/roles/mapping rules/clients to add.
 */
public class AddInGroupFunction implements IdentitySubFunction {

    public static final String ERROR_ADD_IN_GROUP = "ADD_IN_GROUP";
    public static final String ERROR_ADD_IN_GROUP_LABEL = "Adding a member to the group failed";

    private final Logger logger = LoggerFactory.getLogger(AddInGroupFunction.class.getName());

    private static List<String> safeList(List<String> list) {
        return list == null ? List.of() : list;
    }

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();
        String groupId = identityInput.getGroupId();
        GroupMembers groupMembers = identityInput.getGroupMembers();
        boolean ignoreIfAlreadyDone = Boolean.TRUE.equals(identityInput.getIgnoreIfAlreadyDone());
        try {
            for (String userName : safeList(groupMembers.getUsers()))
                addUserInGroup(camundaClient, userName, groupId, ignoreIfAlreadyDone);
            for (String roleId : safeList(groupMembers.getRoles()))
                addRoleInGroup(camundaClient, roleId, groupId, ignoreIfAlreadyDone);
            for (String mappingRuleId : safeList(groupMembers.getMappingRule()))
                addMappingRuleInGroup(camundaClient, mappingRuleId, groupId, ignoreIfAlreadyDone);
            for (String clientId : safeList(groupMembers.getClient()))
                addClientInGroup(camundaClient, clientId, groupId, ignoreIfAlreadyDone);

            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.groupId = groupId;
            identityOutput.status = "SUCCESS";
            identityOutput.dateOperation = new Date();
            logger.info("AddInGroup GroupId[{}] name[{}] Description [{}]status[CREATED] in {} ms",
                    identityInput.getGroupId(),
                    identityInput.getGroupName(),
                    identityInput.getGroupDescription(),
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("Error during AddInGroup groupId[{}] : {}", groupId, e.getMessage());
            throw new ConnectorException(ERROR_ADD_IN_GROUP, "Error during add-in-group " + e.getMessage());
        }
    }

    private void addUserInGroup(CamundaClient camundaClient, String userName, String groupId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newAssignUserToGroupCommand().username(userName).groupId(groupId).execute();
            logger.info("UserName[{}] added in groupId[{}]", userName, groupId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("UserName[{}] already in groupId[{}], ignored", userName, groupId);
                return;
            }
            logger.error("Error adding userName[{}] in groupId[{}] : {}", userName, groupId, e);
            throw new ConnectorException(ERROR_ADD_IN_GROUP, "userName[" + userName + "] groupId[" + groupId + "] : " + e.getMessage());
        }
    }

    private void addRoleInGroup(CamundaClient camundaClient, String roleId, String groupId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newAssignRoleToGroupCommand().roleId(roleId).groupId(groupId).execute();
            logger.info("RoleId[{}] added in groupId[{}]", roleId, groupId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("RoleId[{}] already in groupId[{}], ignored", roleId, groupId);
                return;
            }
            logger.error("Error adding roleId[{}] in groupId[{}] : {}", roleId, groupId, e);
            throw new ConnectorException(ERROR_ADD_IN_GROUP, "roleId[" + roleId + "] groupId[" + groupId + "] : " + e.getMessage());
        }
    }

    private void addMappingRuleInGroup(CamundaClient camundaClient, String mappingRuleId, String groupId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newAssignMappingRuleToGroupCommand().mappingRuleId(mappingRuleId).groupId(groupId).execute();
            logger.info("MappingRuleId[{}] added in groupId[{}]", mappingRuleId, groupId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("MappingRuleId[{}] already in groupId[{}], ignored", mappingRuleId, groupId);
                return;
            }
            logger.error("Error adding mappingRuleId[{}] in groupId[{}] : {}", mappingRuleId, groupId, e);
            throw new ConnectorException(ERROR_ADD_IN_GROUP,
                    "mappingRuleId[" + mappingRuleId + "] groupId[" + groupId + "] : " + e.getMessage());
        }
    }

    private void addClientInGroup(CamundaClient camundaClient, String clientId, String groupId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newAssignClientToGroupCommand().clientId(clientId).groupId(groupId).execute();
            logger.info("ClientId[{}] added in groupId[{}]", clientId, groupId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("ClientId[{}] already in groupId[{}], ignored", clientId, groupId);
                return;
            }
            logger.error("Error adding clientId[{}] in groupId[{}] : {}", clientId, groupId, e);
            throw new ConnectorException(ERROR_ADD_IN_GROUP, "clientId[" + clientId + "] groupId[" + groupId + "] : " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityInput.INPUT_GROUP_ID, //
                        "Group Id", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_GROUP_ID_EXPLANATION),//

                RunnerParameter.getInstance(IdentityInput.INPUT_GROUP_MEMBERS, //
                        "Members", //
                        GroupMembers.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_GROUP_MEMBERS_EXPLANATION),//

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
                ERROR_ADD_IN_GROUP, ERROR_ADD_IN_GROUP_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "AddInGroup";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Add users, roles, mapping rules or clients in a group";
    }

    @Override
    public String getSubFunctionType() {
        return "add-in-group";
    }
}
