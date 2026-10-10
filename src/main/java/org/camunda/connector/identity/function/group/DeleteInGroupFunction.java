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
 * Remove members from a group. groupMembers lists, per category, the users/roles/mapping rules/clients to remove.
 */
public class DeleteInGroupFunction implements IdentitySubFunction {

    public static final String ERROR_DELETE_IN_GROUP = "DELETE_IN_GROUP";
    public static final String ERROR_DELETE_IN_GROUP_LABEL = "Removing a member from the group failed";

    public static final String ERROR_GROUP_NOT_FOUND = "GROUP_NOT_FOUND";
    public static final String ERROR_GROUP_NOT_FOUND_LABEL = "No group found with the id provided";

    private final Logger logger = LoggerFactory.getLogger(DeleteInGroupFunction.class.getName());

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
        logger.info("--- DeleteInGroup : START GroupId[{}] users[{}] roles[{}] mappingRule[{}] client[{}] ignoreIfAlreadyDone[{}]",
                groupId,
                groupMembers.getUsers(),
                groupMembers.getRoles(),
                groupMembers.getMappingRule(),
                groupMembers.getClient(),
                ignoreIfAlreadyDone);
        try {
            IdentityOutput identityOutput = new IdentityOutput();
            List<Group> items = camundaClient.newGroupsSearchRequest()
                    .filter(f -> f.groupId(groupId))
                    .execute().items();
            if (items == null || items.size() != 1) {
                logger.error("--- DeleteInGroup : ERROR GroupId[{}] not found, find {} items", groupId, items == null ? 0 : items.size());
                throw new ConnectorException(ERROR_GROUP_NOT_FOUND, "No group (or too many group) found with groupId[" + groupId + "] : find " + (items == null ? 0 : items.size()));
            }
            identityOutput.group = items.getFirst();

            for (String userName : safeList(groupMembers.getUsers()))
                removeUserFromGroup(camundaClient, userName, groupId, ignoreIfAlreadyDone);
            for (String roleId : safeList(groupMembers.getRoles()))
                removeRoleFromGroup(camundaClient, roleId, groupId, ignoreIfAlreadyDone);
            for (String mappingRuleId : safeList(groupMembers.getMappingRule()))
                removeMappingRuleFromGroup(camundaClient, mappingRuleId, groupId, ignoreIfAlreadyDone);
            for (String clientId : safeList(groupMembers.getClient()))
                removeClientFromGroup(camundaClient, clientId, groupId, ignoreIfAlreadyDone);

            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            identityOutput.dateOperation = new Date();
            logger.info("--- DeleteInGroup : END GroupId[{}] users[{}] roles[{}] mappingRule[{}] client[{}] status[{}] in {} ms",
                    groupId,
                    groupMembers.getUsers(),
                    groupMembers.getRoles(),
                    groupMembers.getMappingRule(),
                    groupMembers.getClient(),
                    identityOutput.status,
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("--- DeleteInGroup : ERROR GroupId[{}] : {}", groupId, e.getMessage());
            throw new ConnectorException(ERROR_DELETE_IN_GROUP, "Error during delete-in-group " + e.getMessage());
        }
    }

    private void removeUserFromGroup(CamundaClient camundaClient, String userName, String groupId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newUnassignUserFromGroupCommand().username(userName).groupId(groupId).execute();
            logger.info("UserName[{}] removed from groupId[{}]", userName, groupId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("UserName[{}] not in groupId[{}], ignored", userName, groupId);
                return;
            }
            logger.error("Error removing userName[{}] from groupId[{}] : ", userName, groupId, e);
            throw new ConnectorException(ERROR_DELETE_IN_GROUP, "userName[" + userName + "] groupId[" + groupId + "] : " + e.getMessage());
        }
    }

    private void removeRoleFromGroup(CamundaClient camundaClient, String roleId, String groupId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newUnassignRoleFromGroupCommand().roleId(roleId).groupId(groupId).execute();
            logger.info("RoleId[{}] removed from groupId[{}]", roleId, groupId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("RoleId[{}] not in groupId[{}], ignored", roleId, groupId);
                return;
            }
            logger.error("Error removing roleId[{}] from groupId[{}] : ", roleId, groupId, e);
            throw new ConnectorException(ERROR_DELETE_IN_GROUP, "roleId[" + roleId + "] groupId[" + groupId + "] : " + e.getMessage());
        }
    }

    private void removeMappingRuleFromGroup(CamundaClient camundaClient, String mappingRuleId, String groupId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newUnassignMappingRuleFromGroupCommand().mappingRuleId(mappingRuleId).groupId(groupId).execute();
            logger.info("MappingRuleId[{}] removed from groupId[{}]", mappingRuleId, groupId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("MappingRuleId[{}] not in groupId[{}], ignored", mappingRuleId, groupId);
                return;
            }
            logger.error("Error removing mappingRuleId[{}] from groupId[{}] : ", mappingRuleId, groupId, e);
            throw new ConnectorException(ERROR_DELETE_IN_GROUP,
                    "mappingRuleId[" + mappingRuleId + "] groupId[" + groupId + "] : " + e.getMessage());
        }
    }

    private void removeClientFromGroup(CamundaClient camundaClient, String clientId, String groupId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newUnassignClientFromGroupCommand().clientId(clientId).groupId(groupId).execute();
            logger.info("ClientId[{}] removed from groupId[{}]", clientId, groupId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("ClientId[{}] not in groupId[{}], ignored", clientId, groupId);
                return;
            }
            logger.error("Error removing clientId[{}] from groupId[{}] : ", clientId, groupId, e);
            throw new ConnectorException(ERROR_DELETE_IN_GROUP, "clientId[" + clientId + "] groupId[" + groupId + "] : " + e.getMessage());
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

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_GROUP, //
                        "Group", //
                        Group.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_GROUP_EXPLANATION),

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
                ERROR_GROUP_NOT_FOUND, ERROR_GROUP_NOT_FOUND_LABEL, //
                ERROR_DELETE_IN_GROUP, ERROR_DELETE_IN_GROUP_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Delete in group";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Remove users, roles, mapping rules or clients from a group";
    }

    @Override
    public String getSubFunctionType() {
        return "delete-in-group";
    }
}
