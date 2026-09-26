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
