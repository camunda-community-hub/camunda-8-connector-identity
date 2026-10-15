package org.camunda.connector.identity;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.command.ClientHttpException;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.cherrytemplate.CherryConnector;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import org.camunda.connector.identity.function.authorization.CreateAuthorizationFunction;
import org.camunda.connector.identity.function.authorization.DeleteAuthorizationFunction;
import org.camunda.connector.identity.function.authorization.SearchAuthorizationFunction;
import org.camunda.connector.identity.function.client.AssignClientFunction;
import org.camunda.connector.identity.function.client.UnassignClientFunction;
import org.camunda.connector.identity.function.group.*;
import org.camunda.connector.identity.function.role.*;
import org.camunda.connector.identity.function.user.CreateUserFunction;
import org.camunda.connector.identity.function.user.DeleteUserFunction;
import org.camunda.connector.identity.function.user.SearchUserFunction;
import org.camunda.connector.identity.function.user.UpdateUserFunction;
import org.camunda.connector.identity.toolbox.IdentitySubFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Supplier;

@OutboundConnector(name = "IdentityFunction", inputVariables = {IdentityInput.INPUT_IDENTITY_FUNCTION,

        IdentityInput.INPUT_USER_NAME, // createUser/updateUser/deleteUser
        IdentityInput.INPUT_USER_DISPLAY_NAME, // createUser/updateUser
        IdentityInput.INPUT_USER_EMAIL, // createUser/updateUser
        IdentityInput.INPUT_USER_PASSWORD, // createUser/updateUser

        IdentityInput.INPUT_SEARCH_BY_USER_NAME, // searchUser
        IdentityInput.INPUT_SEARCH_BY_USER_DISPLAY_NAME, // searchUser
        IdentityInput.INPUT_SEARCH_BY_USER_EMAIL, // searchUser
        IdentityInput.INPUT_SEARCH_BY_GROUP_ID, // searchUser
        IdentityInput.INPUT_SEARCH_BY_ROLE_ID, // searchUser

        IdentityInput.INPUT_GROUP_ID, // createGroup/updateGroup/addInGroup/searchInGroup/deleteInGroup/deleteGroup
        IdentityInput.INPUT_GROUP_NAME, // createGroup/updateGroup
        IdentityInput.INPUT_GROUP_DESCRIPTION, // createGroup
        IdentityInput.INPUT_GROUP_NAME_SEARCH, // searchGroup

        IdentityInput.INPUT_GROUP_MEMBERS, // addInGroup/deleteInGroup

        IdentityInput.INPUT_GROUP_CONTENT_FILTER, // searchInGroup

        IdentityInput.INPUT_ROLE_ID, // createRole/updateRole/addInRole/searchInRole/deleteInRole/deleteRole
        IdentityInput.INPUT_ROLE_NAME, // createRole/updateRole
        IdentityInput.INPUT_ROLE_DESCRIPTION, // createRole
        IdentityInput.INPUT_ROLE_NAME_SEARCH, // searchRole

        IdentityInput.INPUT_ROLE_MEMBERS, // addInRole/deleteInRole

        IdentityInput.INPUT_ROLE_CONTENT_FILTER, // searchInRole

        IdentityInput.INPUT_PAGE_NUMBER, // searchRole/searchGroup
        IdentityInput.INPUT_PAGE_SIZE, // searchRole/searchGroup

        IdentityInput.INPUT_CLIENT_ID, // assignClient/unassignClient
        IdentityInput.INPUT_TENANT_ID, // assignClient/unassignClient

        IdentityInput.INPUT_AUTHORIZATION_OWNER_TYPE, // createAuthorization
        IdentityInput.INPUT_AUTHORIZATION_OWNER, // createAuthorization
        IdentityInput.INPUT_AUTHORIZATION_RESOURCE_TYPE, // createAuthorization
        IdentityInput.INPUT_AUTHORIZATION_RESOURCE_ID, // createAuthorization
        IdentityInput.INPUT_AUTHORIZATION_PERMISSION, // createAuthorization

        IdentityInput.INPUT_AUTHORIZATION_SEARCH_OWNER_ID, // searchAuthorization
        IdentityInput.INPUT_AUTHORIZATION_SEARCH_OWNER_TYPE, // searchAuthorization
        IdentityInput.INPUT_AUTHORIZATION_SEARCH_RESOURCE_TYPE, // searchAuthorization
        IdentityInput.INPUT_AUTHORIZATION_SEARCH_RESOURCE_ID, // searchAuthorization
        IdentityInput.INPUT_AUTHORIZATION_SEARCH_RESOURCE_PROPERTY_NAME, // searchAuthorization

        IdentityInput.INPUT_AUTHORIZATION_KEY, // deleteAuthorization

        IdentityInput.INPUT_WAIT_EXPORT, // addInGroup/addInRole

        IdentityInput.INPUT_IGNORE_IF_ALREADY_DONE}, type = "c-identity-function")
// createUser/deleteUser/createGroup/deleteGroup/createRole/deleteRole

public class IdentityFunction implements OutboundConnectorFunction, CherryConnector {
    public static final String ERROR_UNKNOWN_FUNCTION = "UNKNOWN_FUNCTION";
    public static final String ERROR_UNKNOWN_FUNCTION_LABEL = "The function is unknown. There is a limited number of operation";
    public static final String ERROR_IDENTITY_CONNECTION = "IDENTITY_CONNECTION";
    public static final String ERROR_IDENTITY_CONNECTION_LABEL = "Error arrived during the connection to the Camunda 8 cluster";
    public static final List<Class<?>> allFunctions = Arrays.asList(CreateUserFunction.class, // add User
            DeleteUserFunction.class, // removeUser
            SearchUserFunction.class, // Search User
            UpdateUserFunction.class, // Update user
            CreateGroupFunction.class, // create group
            SearchGroupFunction.class, // search groups by name
            UpdateGroupFunction.class, // update a group's name
            AddInGroupFunction.class, // add users/roles/mappingRules/clients in a group
            GetContentGroupFunction.class, // read the content of a group
            DeleteInGroupFunction.class, // remove users/roles/mappingRules/clients from a group
            DeleteGroupFunction.class, // delete group
            CreateRoleFunction.class, // create role
            SearchRoleFunction.class, // search roles by name
            UpdateRoleFunction.class, // update a role's name
            AddInRoleFunction.class, // add users/groups/mappingRules/clients in a role
            GetContentRoleFunction.class, // read the content of a role
            DeleteInRoleFunction.class, // remove users/groups/mappingRules/clients from a role
            DeleteRoleFunction.class, // delete role
            AssignClientFunction.class, // assign a client to a role/group/tenant
            UnassignClientFunction.class, // unassign a client from a role/group/tenant
            CreateAuthorizationFunction.class, // create an authorization
            SearchAuthorizationFunction.class, // search authorizations
            DeleteAuthorizationFunction.class); // delete an authorization
    /**
     * Total attempts (including the first) made by {@link #retrySearchById} before giving up.
     */
    public static final int SEARCH_RETRY_MAX_ATTEMPTS = 6;
    /**
     * Delay, in milliseconds, between two attempts of {@link #retrySearchById}.
     */
    public static final long SEARCH_RETRY_DELAY_MILLIS = 400;
    private static final String WORKER_LOGO = "data:image/png;base64,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";
    private final Logger logger = LoggerFactory.getLogger(IdentityFunction.class.getName());

    /**
     * Return the common parameters to all sub functions.
     * There is no connection parameter here: the connector gets the CamundaClient from the runtime
     * environment (same as any job worker running in the same cluster), so nothing needs to be
     * configured on the BPMN task.
     *
     * @return list of common parameters
     */
    public static List<RunnerParameter> getInputCommonParameters() {
        return Collections.emptyList();
    }

    /**
     * Detect a "already done" error from the Camunda 8 API: the entity already exists (409, on
     * creation/assignment) or does not exist anymore (404, on deletion/unassignment). Any other error
     * (validation, connection, unexpected status) is a real failure and must not be ignored.
     *
     * @param e the exception caught from a CamundaClient command execution
     * @return true if the error is an "already done" conflict/not-found response
     */
    public static boolean isAlreadyDoneError(Throwable e) {
        if (e instanceof ClientHttpException httpException) {
            int code = httpException.code();
            return code == 409 || code == 404;
        }
        return false;
    }

    /**
     * Extract the single expected result from a search-by-id request performed right after a
     * create/update command. Camunda 8's Search/Get APIs are eventually consistent (the exporter
     * needs to index the record), so a search immediately following a write may legitimately return
     * zero items yet, or in rare cases more than one. Either case is logged as an error, but the
     * function continues rather than throwing (a hard failure here would turn a cosmetic/transient
     * read-after-write gap into a BPMN incident for an operation that actually succeeded).
     *
     * @param items       the items returned by the search
     * @param logger      the caller's logger
     * @param description context to identify the search in the log (e.g. "CreateRole roleId[abc]")
     * @return the single item, the first of several if more than one was found, or null if none was found
     */
    public static <T> T extractSingleResult(List<T> items, Logger logger, String description) {
        if (items == null || items.isEmpty()) {
            logger.error("{} : search returned no result (possible read-after-write delay in Camunda 8 secondary storage)",
                    description);
            return null;
        }
        if (items.size() > 1) {
            logger.error("{} : search returned {} results, expected exactly one", description, items.size());
        }
        return items.getFirst();
    }

    /**
     * Sleep for the given number of milliseconds if positive - a manual, process-modeler-controlled
     * wait for Identity to export the artefact just created (complementing the automatic
     * {@link #retrySearchById} retry) before the function searches for it by id. A null or
     * non-positive value is a no-op.
     *
     * @param waitExportMillis milliseconds to wait, or null/0/negative to skip
     * @param logger           the caller's logger
     * @param description      context to identify the caller in the log (e.g. "AddInGroup groupId[abc]")
     */
    public static void waitExport(Long waitExportMillis, Logger logger, String description) {
        if (waitExportMillis == null || waitExportMillis <= 0) {
            return;
        }
        logger.info("{} : waitExport[{}] ms after operation", description, waitExportMillis);
        try {
            Thread.sleep(waitExportMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Retry a search-by-id a few times with a short delay, to absorb the read-after-write delay
     * inherent to Camunda 8's eventually-consistent secondary storage: a create/update command
     * returns success as soon as the broker has processed it, but the exporter still needs to index
     * the record before any Search/Get API call can see it - typically milliseconds, occasionally a
     * couple of seconds. Without this, a function that validates an entity exists (e.g. add-in-group
     * checking the group by id) can wrongly throw a "not found" error for an entity that was in fact
     * just created successfully, one task earlier in the same process instance.
     *
     * @param search      performs one search attempt; called again with a fresh request each retry
     * @param logger      the caller's logger, for a debug trace on retries (silent on first-try success)
     * @param description context to identify the search in the log (e.g. "AddInGroup groupId[abc]")
     * @return the first non-empty result, or the last (possibly empty) result if every attempt was empty
     */
    public static <T> List<T> retrySearchById(Supplier<List<T>> search, Logger logger, String description) {
        List<T> items = List.of();
        for (int attempt = 1; attempt <= SEARCH_RETRY_MAX_ATTEMPTS; attempt++) {
            items = search.get();
            if (items != null && !items.isEmpty()) {
                if (attempt > 1) {
                    logger.info("{} : found on attempt {}/{} (read-after-write delay)", description, attempt,
                            SEARCH_RETRY_MAX_ATTEMPTS);
                }
                return items;
            }
            if (attempt < SEARCH_RETRY_MAX_ATTEMPTS) {
                logger.info("{} : search returned no result on attempt {}/{}, retrying in {} ms", description, attempt,
                        SEARCH_RETRY_MAX_ATTEMPTS, SEARCH_RETRY_DELAY_MILLIS);
                try {
                    Thread.sleep(SEARCH_RETRY_DELAY_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return items;
                }
            }
        }
        return items;
    }

    /**
     * Parse an input that may be given as a list of strings or as a single comma-separated string
     * (used by the authorization permission/resourceType/resourceId/resourcePropertyName filters,
     * where the Camunda 8 client API is varargs/Collection-based but a FEEL caller may find it more
     * convenient to pass one string).
     *
     * @param raw the raw input value: null, a String, a List, or any other object (toString()-ed)
     * @return a list of trimmed, non-empty strings - empty if raw is null or blank
     */
    public static List<String> parseStringList(Object raw) {
        if (raw == null) {
            return List.of();
        }
        if (raw instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        String str = raw.toString();
        if (str.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    public IdentityOutput execute(OutboundConnectorContext outboundConnectorContext) throws Exception {

        IdentityInput identityInput = outboundConnectorContext.bindVariables(IdentityInput.class);
        // search the sub-function referenced
        String function = identityInput.getIdentityFunction();
        long beginTime = System.currentTimeMillis();

        // Get the CamundaClient, auto-configured from the runtime environment, and work from it
        try (CamundaClient camundaClient = CamundaClient.newClientBuilder().build()) {

            for (IdentitySubFunction inputSubFunction : getListSubFunctions()) {
                if (inputSubFunction.getSubFunctionType().equals(function)) {
                    IdentityOutput identityOutput = inputSubFunction.executeSubFunction(camundaClient, identityInput,
                            outboundConnectorContext);
                    logger.info("IdentityFunction End function [{}] in {} ms", function, System.currentTimeMillis() - beginTime);
                    return identityOutput;
                }
            }
            throw new ConnectorException(ERROR_UNKNOWN_FUNCTION, "Identity Function Unknown [" + function + "]");
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            throw new ConnectorException(ERROR_IDENTITY_CONNECTION,
                    "Error while getting the CamundaClient : " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Camunda 8 Identity operation (create/search/update/delete user)";
    }

    @Override
    public String getLogo() {
        return WORKER_LOGO;
    }

    @Override
    public String getCollectionName() {
        return "UserManagement";
    }

    @Override
    public Map<String, String> getListBpmnErrors() {
        Map<String, String> allErrors = new HashMap<>();
        allErrors.put(ERROR_UNKNOWN_FUNCTION, ERROR_UNKNOWN_FUNCTION_LABEL);

        for (IdentitySubFunction subFunction : getListSubFunctions()) {
            allErrors.putAll(subFunction.getSubFunctionListBpmnErrors());
        }

        return allErrors;
    }

    @Override
    public Class<?> getInputParameterClass() {
        return IdentityInput.class;
    }

    @Override
    public Class<?> getOutputParameterClass() {
        return IdentityOutput.class;
    }

    @Override
    public List<String> getAppliesTo() {
        return null; // Library decide
    }

    public List<IdentitySubFunction> getListSubFunctions() {
        List<IdentitySubFunction> listSubFunction = new ArrayList<>();

        for (Class<?> classFunction : allFunctions) {
            try {
                Constructor<?> constructor = classFunction.getConstructor();
                IdentitySubFunction inputSubFunction = (IdentitySubFunction) constructor.newInstance();
                listSubFunction.add(inputSubFunction);
            } catch (Exception e) {
                logger.error("Can't call a constructor on {} : {}", classFunction.getName(), e.toString());
            }

        }
        return listSubFunction;
    }
}
