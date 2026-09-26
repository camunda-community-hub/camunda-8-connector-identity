package org.camunda.connector.identity.function.group;

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
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DeleteGroupFunction implements IdentitySubFunction {

    public static final String ERROR_DELETE_GROUP = "DELETE_GROUP";
    public static final String ERROR_DELETE_GROUP_LABEL = "During delete group in Camunda 8 Identity";

    private final Logger logger = LoggerFactory.getLogger(DeleteGroupFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();
        String groupId = identityInput.getGroupId();
        boolean ignoreIfAlreadyDone = Boolean.TRUE.equals(identityInput.getIgnoreIfAlreadyDone());
        try {
            camundaClient.newDeleteGroupCommand(groupId).execute();

            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.groupId = groupId;
            identityOutput.status = "SUCCESS";
            identityOutput.dateOperation = new Date();
            logger.info("DeleteGroup GroupId[{}] status[SUCCESS] in {} ms",
                    groupId,
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                IdentityOutput identityOutput = new IdentityOutput();
                identityOutput.groupId = groupId;
                identityOutput.status = "ALREADY_DELETED";
                identityOutput.dateOperation = new Date();
                logger.info("DeleteGroup GroupId[{}] already deleted, ignored in {} ms", groupId,
                        System.currentTimeMillis() - markerTime);
                return identityOutput;
            }
            logger.error("Error during DeleteGroup groupId[{}] : {}", groupId, e.getMessage());
            throw new ConnectorException(ERROR_DELETE_GROUP, "During delete group[" + groupId + "] : " + e.getMessage());
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
                ERROR_DELETE_GROUP, ERROR_DELETE_GROUP_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "DeleteGroup";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Delete a group from its groupId";
    }

    @Override
    public String getSubFunctionType() {
        return "delete-group";
    }
}
