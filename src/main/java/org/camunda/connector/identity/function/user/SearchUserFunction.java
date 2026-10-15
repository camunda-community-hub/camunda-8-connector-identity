package org.camunda.connector.identity.function.user;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.search.response.GroupUser;
import io.camunda.client.api.search.response.RoleUser;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import org.camunda.connector.identity.IdentityFunction;
import org.camunda.connector.identity.IdentityInput;
import org.camunda.connector.identity.IdentityOutput;
import org.camunda.connector.identity.toolbox.IdentitySubFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SearchUserFunction implements IdentitySubFunction {

    public static final String ERROR_SEARCH_USER = "SEARCH_USER";
    public static final String ERROR_SEARCH_USER_LABEL = "During search user(s) in Camunda 8 Identity";

    private final Logger logger = LoggerFactory.getLogger(SearchUserFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();

        logger.info(
                "--- searchUser : START searchByGroupId[{}] searchByRoleId[{}] searchByUserName[{}] searchByUserDisplayName[{}] searchByUserEmail[{}]",
                identityInput.getSearchByGroupId(), identityInput.getSearchByRoleId(), identityInput.getSearchByUserName(),
                identityInput.getSearchByUserDisplayName(), identityInput.getSearchByUserEmail());

        IdentityOutput identityOutput;

        if (containsInformation(identityInput.getSearchByGroupId())) {
            identityOutput = searchByGroup(camundaClient, identityInput);
        } else if (containsInformation(identityInput.getSearchByRoleId())) {
            identityOutput = searchByRole(camundaClient, identityInput);
        } else {
            identityOutput = searchByCriteria(camundaClient, identityInput);
        }
        identityOutput.status = IdentityOutput.STATUS_SUCCESS;
        identityOutput.dateOperation = new Date();
        logger.info(
                "--- searchUser : END searchByGroupId[{}] searchByRoleId[{}] searchByUserName[{}] searchByUserDisplayName[{}] searchByUserEmail[{}] listUsers[{}] status[{}] in {} ms",
                identityInput.getSearchByGroupId(), identityInput.getSearchByRoleId(), identityInput.getSearchByUserName(),
                identityInput.getSearchByUserDisplayName(), identityInput.getSearchByUserEmail(),
                identityOutput.listUsers, identityOutput.status, System.currentTimeMillis() - markerTime);
        return identityOutput;
    }

    /**
     * Search all users assigned to a group
     */
    private IdentityOutput searchByGroup(CamundaClient camundaClient, IdentityInput identityInput)
            throws ConnectorException {
        String groupId = identityInput.getSearchByGroupId();
        IdentityOutput identityOutput = new IdentityOutput();
        try {
            List<GroupUser> groupsList = camundaClient.newUsersByGroupSearchRequest(groupId).execute().items();
            identityOutput.listUsers = new ArrayList<>();
            for (GroupUser groupUser : groupsList) {
                identityOutput.listUsers.addAll(camundaClient.newUsersSearchRequest()
                        .filter(f -> f.username(groupUser.getUsername()))
                        .execute().items());
            }
            logger.info("SearchByGroupId[{}] Found {} users", groupId, identityOutput.listUsers.size());
            return identityOutput;
        } catch (Exception e) {
            logger.error("--- searchUser : ERROR groupId[{}] : {}", groupId, e.getMessage());
            throw new ConnectorException(ERROR_SEARCH_USER, "groupId[" + groupId + "] : " + e.getMessage());
        }
    }

    /**
     * Search all users assigned to a role
     */
    private IdentityOutput searchByRole(CamundaClient camundaClient, IdentityInput identityInput)
            throws ConnectorException {
        String roleId = identityInput.getSearchByRoleId();
        IdentityOutput identityOutput = new IdentityOutput();
        try {
            List<RoleUser> roleUsersList = camundaClient.newUsersByRoleSearchRequest(roleId).execute().items();
            identityOutput.listUsers = new ArrayList<>();
            for (RoleUser roleUsers : roleUsersList) {
                identityOutput.listUsers.addAll(camundaClient.newUsersSearchRequest()
                        .filter(f -> f.username(roleUsers.getUsername()))
                        .execute().items());
            }
            logger.info("Search ByRoleId[{}] Found {} users", roleId, identityOutput.listUsers.size());
            return identityOutput;
        } catch (Exception e) {
            logger.error("--- searchUser : ERROR roleId[{}] : {}", roleId, e.getMessage());
            throw new ConnectorException(ERROR_SEARCH_USER, "roleId[" + roleId + "] : " + e.getMessage());
        }
    }

    /**
     * Search by username/name/email criteria
     *
     * @throws ConnectorException ERROR_SEARCH_USER
     */
    private IdentityOutput searchByCriteria(CamundaClient camundaClient, IdentityInput identityInput)
            throws ConnectorException {
        String userName = getSearchCriteria(identityInput.getSearchByUserName());
        String name = getSearchCriteria(identityInput.getSearchByUserDisplayName());
        String email = getSearchCriteria(identityInput.getSearchByUserEmail());
        String searchUserSignature = "userName[" + userName + "] name[" + name + "] email[" + email + "]";
        try {
            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.listUsers = camundaClient.newUsersSearchRequest()
                    .filter(f -> {
                        if (containsInformation(userName))
                            f.username(u -> u.like(userName));
                        if (containsInformation(name))
                            f.name(n -> n.like(name));
                        if (containsInformation(email))
                            f.email(e -> e.like(email));
                    })
                    .execute()
                    .items();

            logger.info("Search by {} Found {} users", searchUserSignature, identityOutput.listUsers.size());
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("--- searchUser : ERROR {} : {}", searchUserSignature, e.getMessage());
            throw new ConnectorException(ERROR_SEARCH_USER, "Error during search-users " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityInput.INPUT_SEARCH_BY_USER_NAME, //
                                IdentityInput.INPUT_SEARCH_BY_USER_NAME_LABEL, //
                                String.class, //
                                "", //
                                RunnerParameter.Level.OPTIONAL, //
                                IdentityInput.INPUT_SEARCH_BY_USER_NAME_EXPLANATION)
                        .setVisibleInTemplate().setGroup(IdentitySubFunction.USER_GROUP),//

                RunnerParameter.getInstance(IdentityInput.INPUT_SEARCH_BY_USER_DISPLAY_NAME, //
                                IdentityInput.INPUT_SEARCH_BY_USER_DISPLAY_NAME_LABEL, //
                                String.class, //
                                "", //
                                RunnerParameter.Level.OPTIONAL, //
                                IdentityInput.INPUT_SEARCH_BY_USER_DISPLAY_NAME_EXPLANATION)
                        .setVisibleInTemplate().setGroup(IdentitySubFunction.USER_GROUP),//

                RunnerParameter.getInstance(IdentityInput.INPUT_SEARCH_BY_USER_EMAIL, //
                                IdentityInput.INPUT_SEARCH_BY_USER_EMAIL_LABEL, //
                                String.class, //
                                "", //
                                RunnerParameter.Level.OPTIONAL, //
                                IdentityInput.INPUT_SEARCH_BY_USER_EMAIL_EXPLANATION)
                        .setVisibleInTemplate().setGroup(IdentitySubFunction.USER_GROUP),//

                RunnerParameter.getInstance(IdentityInput.INPUT_SEARCH_BY_GROUP_ID, //
                                IdentityInput.INPUT_SEARCH_BY_GROUP_ID_LABEL, //
                                String.class, //
                                "", //
                                RunnerParameter.Level.OPTIONAL, //
                                IdentityInput.INPUT_SEARCH_BY_GROUP_ID_EXPLANATION)
                        .setVisibleInTemplate().setGroup(IdentitySubFunction.USER_GROUP),//

                RunnerParameter.getInstance(IdentityInput.INPUT_SEARCH_BY_ROLE_ID, //
                                IdentityInput.INPUT_SEARCH_BY_ROLE_ID_LABEL, //
                                String.class, //
                                "", //
                                RunnerParameter.Level.OPTIONAL, //
                                IdentityInput.INPUT_SEARCH_BY_ROLE_ID_EXPLANATION)
                        .setVisibleInTemplate().setGroup(IdentitySubFunction.USER_GROUP)
        );//

    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(IdentityOutput.OUTPUT_STATUS, //
                        IdentityOutput.OUTPUT_STATUS_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_STATUS_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_LIST_USERS, //
                        IdentityOutput.OUTPUT_LIST_USERS_LABEL, //
                        List.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_LIST_USERS_EXPLANATION), //

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
                ERROR_SEARCH_USER, ERROR_SEARCH_USER_LABEL); //
    }

    @Override
    public String getSubFunctionName() {
        return "Search user";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Search users in Camunda 8 Identity by username/name/email, or by groupId, or by roleId";
    }

    @Override
    public String getSubFunctionType() {
        return "search-users";
    }

    private boolean containsInformation(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String getSearchCriteria(String criteriaContent) {
        if (!containsInformation(criteriaContent))
            return null;
        return criteriaContent;
    }
}
