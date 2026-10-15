package org.camunda.connector.identity.toolbox;

import io.camunda.client.CamundaClient;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import org.camunda.connector.identity.IdentityInput;
import org.camunda.connector.identity.IdentityOutput;

import java.util.List;
import java.util.Map;

public interface IdentitySubFunction {

    /**
     * Element-template group label for every parameter specific to one business package (user,
     * group, role, client, authorization). Shared/cross-cutting parameters (ignoreIfAlreadyDone,
     * waitExport) have their own cross-cutting group instead - see IdentityInput.
     */
    String USER_GROUP = "User";
    String GROUP_GROUP = "Group";
    String ROLE_GROUP = "Role";
    String CLIENT_GROUP = "Client";
    String AUTHORIZATION_GROUP = "Authorization";

    IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                      IdentityInput identityInput,
                                      OutboundConnectorContext context) throws ConnectorException;

    List<RunnerParameter> getInputsParameter();

    List<RunnerParameter> getOutputsParameter();

    Map<String, String> getSubFunctionListBpmnErrors();

    String getSubFunctionName();

    String getSubFunctionDescription();

    String getSubFunctionType();

}
