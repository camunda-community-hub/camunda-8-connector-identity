package org.camunda.connector.identity.function.group;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.search.response.Group;
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

    public static final String ERROR_GROUP_NOT_FOUND = "GROUP_NOT_FOUND";
    public static final String ERROR_GROUP_NOT_FOUND_LABEL = "No group found with the id provided";

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
        Long waitExport = identityInput.getWaitExport();
        logger.info("--- AddInGroup : START GroupId[{}] users[{}] roles[{}] mappingRule[{}] client[{}] ignoreIfAlreadyDone[{}] waitExport[{}]",
                groupId,
                groupMembers.getUsers(),
                groupMembers.getRoles(),
                groupMembers.getMappingRule(),
                groupMembers.getClient(),
                ignoreIfAlreadyDone,
                waitExport);
        try {
            IdentityOutput identityOutput = new IdentityOutput();
            List<Group> items = IdentityFunction.retrySearchById(
                    () -> camundaClient.newGroupsSearchRequest().filter(f -> f.groupId(groupId)).execute().items(),
                    logger, "AddInGroup groupId[" + groupId + "]");
            if (items == null || items.size() != 1) {
                logger.error("--- AddInGroup : ERROR GroupId[{}] not found, find {} items", groupId, items == null ? "null" : items.size());
                throw new ConnectorException(ERROR_GROUP_NOT_FOUND, "No group (or too many group) found with groupId[" + groupId + "] : find " + (items == null ? 0 : items.size()));
            }
            identityOutput.group = items.getFirst();


            for (String userName : safeList(groupMembers.getUsers()))
                addUserInGroup(camundaClient, userName, groupId, ignoreIfAlreadyDone);
            for (String roleId : safeList(groupMembers.getRoles()))
                addRoleInGroup(camundaClient, roleId, groupId, ignoreIfAlreadyDone);
            for (String mappingRuleId : safeList(groupMembers.getMappingRule()))
                addMappingRuleInGroup(camundaClient, mappingRuleId, groupId, ignoreIfAlreadyDone);
            for (String clientId : safeList(groupMembers.getClient()))
                addClientInGroup(camundaClient, clientId, groupId, ignoreIfAlreadyDone);

            IdentityFunction.waitExport(waitExport, logger, "AddInGroup groupId[" + groupId + "]");

            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            identityOutput.dateOperation = new Date();
            logger.info("--- AddInGroup : END GroupId[{}] name[{}] Description [{}] status[{}] in {} ms",
                    identityInput.getGroupId(),
                    identityInput.getGroupName(),
                    identityInput.getGroupDescription(),
                    identityOutput.status,
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("--- AddInGroup : ERROR GroupId[{}] : {}", groupId, e.getMessage());
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
            logger.error("Error adding userName[{}] in groupId[{}] : ", userName, groupId, e);
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
            logger.error("Error adding roleId[{}] in groupId[{}] : ", roleId, groupId, e);
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
            logger.error("Error adding mappingRuleId[{}] in groupId[{}] : ", mappingRuleId, groupId, e);
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
            logger.error("Error adding clientId[{}] in groupId[{}] : ", clientId, groupId, e);
            throw new ConnectorException(ERROR_ADD_IN_GROUP, "clientId[" + clientId + "] groupId[" + groupId + "] : " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityInput.INPUT_GROUP_ID, //
                        IdentityInput.INPUT_GROUP_ID_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_GROUP_ID_EXPLANATION).setGroup(IdentitySubFunction.GROUP_GROUP),//

                RunnerParameter.getInstance(IdentityInput.INPUT_GROUP_MEMBERS, //
                        IdentityInput.INPUT_GROUP_MEMBERS_LABEL, //
                        GroupMembers.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_GROUP_MEMBERS_EXPLANATION).setGroup(IdentitySubFunction.GROUP_GROUP),//

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
                RunnerParameter.getInstance(IdentityOutput.OUTPUT_GROUP, //
                        IdentityOutput.OUTPUT_GROUP_LABEL, //
                        Group.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_GROUP_EXPLANATION),
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
                ERROR_GROUP_NOT_FOUND, ERROR_GROUP_NOT_FOUND_LABEL,
                ERROR_ADD_IN_GROUP, ERROR_ADD_IN_GROUP_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Add in group";
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
