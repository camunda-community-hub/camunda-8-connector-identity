package org.camunda.connector.identity.function.role;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.search.response.Role;
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
 * Remove members from a role. roleMembers lists, per category, the users/groups/mapping rules/clients to remove.
 */
public class DeleteInRoleFunction implements IdentitySubFunction {

    public static final String ERROR_DELETE_IN_ROLE = "DELETE_IN_ROLE";
    public static final String ERROR_DELETE_IN_ROLE_LABEL = "Removing a member from the role failed";

    public static final String ERROR_ROLE_NOT_FOUND = "ROLE_NOT_FOUND";
    public static final String ERROR_ROLE_NOT_FOUND_LABEL = "No role found with the id provided";

    private final Logger logger = LoggerFactory.getLogger(DeleteInRoleFunction.class.getName());

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
        Long waitExport = identityInput.getWaitExport();
        logger.info("--- DeleteInRole : START RoleId[{}] users[{}] groups[{}] mappingRule[{}] client[{}] ignoreIfAlreadyDone[{}] waitExport[{}]",
                roleId,
                roleMembers.getUsers(),
                roleMembers.getGroups(),
                roleMembers.getMappingRule(),
                roleMembers.getClient(),
                ignoreIfAlreadyDone,
                waitExport);
        try {
            IdentityOutput identityOutput = new IdentityOutput();
            List<Role> items = IdentityFunction.retrySearchById(
                    () -> camundaClient.newRolesSearchRequest().filter(f -> f.roleId(roleId)).execute().items(),
                    logger, "DeleteInRole roleId[" + roleId + "]");
            if (items == null || items.size() != 1) {
                logger.error("--- DeleteInRole : ERROR RoleId[{}] not found, find {} items", roleId, items == null ? 0 : items.size());
                throw new ConnectorException(ERROR_ROLE_NOT_FOUND, "No role (or too many role) found with roleId[" + roleId + "] : find " + (items == null ? 0 : items.size()));
            }
            identityOutput.role = items.getFirst();

            for (String userName : safeList(roleMembers.getUsers()))
                removeUserFromRole(camundaClient, userName, roleId, ignoreIfAlreadyDone);
            for (String groupId : safeList(roleMembers.getGroups()))
                removeGroupFromRole(camundaClient, groupId, roleId, ignoreIfAlreadyDone);
            for (String mappingRuleId : safeList(roleMembers.getMappingRule()))
                removeMappingRuleFromRole(camundaClient, mappingRuleId, roleId, ignoreIfAlreadyDone);
            for (String clientId : safeList(roleMembers.getClient()))
                removeClientFromRole(camundaClient, clientId, roleId, ignoreIfAlreadyDone);

            IdentityFunction.waitExport(waitExport, logger, "DeleteInRole roleId[" + roleId + "]");

            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            identityOutput.dateOperation = new Date();
            logger.info("--- DeleteInRole : END RoleId[{}] users[{}] groups[{}] mappingRule[{}] client[{}] status[{}] in {} ms",
                    roleId,
                    roleMembers.getUsers(),
                    roleMembers.getGroups(),
                    roleMembers.getMappingRule(),
                    roleMembers.getClient(),
                    identityOutput.status,
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("--- DeleteInRole : ERROR RoleId[{}] : {}", roleId, e.getMessage());
            throw new ConnectorException(ERROR_DELETE_IN_ROLE, "Error during delete-in-role " + e.getMessage());
        }
    }

    private void removeUserFromRole(CamundaClient camundaClient, String userName, String roleId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newUnassignRoleFromUserCommand().roleId(roleId).username(userName).execute();
            logger.info("UserName[{}] removed from roleId[{}]", userName, roleId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("UserName[{}] not in roleId[{}], ignored", userName, roleId);
                return;
            }
            logger.error("Error removing userName[{}] from roleId[{}] : ", userName, roleId, e);
            throw new ConnectorException(ERROR_DELETE_IN_ROLE, "userName[" + userName + "] roleId[" + roleId + "] : " + e.getMessage());
        }
    }

    private void removeGroupFromRole(CamundaClient camundaClient, String groupId, String roleId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newUnassignRoleFromGroupCommand().roleId(roleId).groupId(groupId).execute();
            logger.info("GroupId[{}] removed from roleId[{}]", groupId, roleId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("GroupId[{}] not in roleId[{}], ignored", groupId, roleId);
                return;
            }
            logger.error("Error removing groupId[{}] from roleId[{}] : ", groupId, roleId, e);
            throw new ConnectorException(ERROR_DELETE_IN_ROLE, "groupId[" + groupId + "] roleId[" + roleId + "] : " + e.getMessage());
        }
    }

    private void removeMappingRuleFromRole(CamundaClient camundaClient, String mappingRuleId, String roleId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newUnassignRoleFromMappingRuleCommand().roleId(roleId).mappingRuleId(mappingRuleId).execute();
            logger.info("MappingRuleId[{}] removed from roleId[{}]", mappingRuleId, roleId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("MappingRuleId[{}] not in roleId[{}], ignored", mappingRuleId, roleId);
                return;
            }
            logger.error("Error removing mappingRuleId[{}] from roleId[{}] : ", mappingRuleId, roleId, e);
            throw new ConnectorException(ERROR_DELETE_IN_ROLE,
                    "mappingRuleId[" + mappingRuleId + "] roleId[" + roleId + "] : " + e.getMessage());
        }
    }

    private void removeClientFromRole(CamundaClient camundaClient, String clientId, String roleId, boolean ignoreIfAlreadyDone)
            throws ConnectorException {
        try {
            camundaClient.newUnassignRoleFromClientCommand().roleId(roleId).clientId(clientId).execute();
            logger.info("ClientId[{}] removed from roleId[{}]", clientId, roleId);
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                logger.info("ClientId[{}] not in roleId[{}], ignored", clientId, roleId);
                return;
            }
            logger.error("Error removing clientId[{}] from roleId[{}] : ", clientId, roleId, e);
            throw new ConnectorException(ERROR_DELETE_IN_ROLE, "clientId[" + clientId + "] roleId[" + roleId + "] : " + e.getMessage());
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

                RunnerParameter.getInstance(IdentityInput.INPUT_ROLE_MEMBERS, //
                        IdentityInput.INPUT_ROLE_MEMBERS_LABEL, //
                        RoleMembers.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_ROLE_MEMBERS_EXPLANATION).setGroup(IdentitySubFunction.ROLE_GROUP),//

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
                        "", //
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
                ERROR_ROLE_NOT_FOUND, ERROR_ROLE_NOT_FOUND_LABEL, //
                ERROR_DELETE_IN_ROLE, ERROR_DELETE_IN_ROLE_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Delete in role";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Remove users, groups, mapping rules or clients from a role";
    }

    @Override
    public String getSubFunctionType() {
        return "delete-in-role";
    }
}
