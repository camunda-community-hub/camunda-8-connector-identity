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
        Long waitExport = identityInput.getWaitExport();
        logger.info("--- DeleteGroup : START GroupId[{}] ignoreIfAlreadyDone[{}] waitExport[{}]", groupId, ignoreIfAlreadyDone, waitExport);
        try {


            camundaClient.newDeleteGroupCommand(groupId).execute();

            IdentityFunction.waitExport(waitExport, logger, "DeleteGroup groupId[" + groupId + "]");

            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            identityOutput.dateOperation = new Date();
            identityOutput.groupIdDeleted = groupId;
            logger.info("--- DeleteGroup : END GroupId[{}] status[{}] in {} ms",
                    groupId,
                    identityOutput.status,
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                IdentityOutput identityOutput = new IdentityOutput();
                identityOutput.status = IdentityOutput.STATUS_ALREADY_DELETED;
                identityOutput.dateOperation = new Date();
                identityOutput.groupIdDeleted = groupId;
                logger.info("--- DeleteGroup : END GroupId[{}] already deleted, ignored, status[{}] in {} ms", groupId,
                        identityOutput.status,
                        System.currentTimeMillis() - markerTime);
                return identityOutput;
            }
            logger.error("--- DeleteGroup : ERROR GroupId[{}] : {}", groupId, e.getMessage());
            throw new ConnectorException(ERROR_DELETE_GROUP, "During delete group[" + groupId + "] : " + e.getMessage());
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

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_DATE_OPERATION, //
                        IdentityOutput.OUTPUT_DATE_OPERATION_LABEL, //
                        Date.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_DATE_OPERATION_EXPLANATION), //

                RunnerParameter.getInstance(IdentityOutput.OUTPUT_GROUP_ID_DELETED, //
                        IdentityOutput.OUTPUT_GROUP_ID_DELETED_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        IdentityOutput.OUTPUT_GROUP_ID_DELETED_EXPLANATION));
    }

    @Override
    public Map<String, String> getSubFunctionListBpmnErrors() {
        return Map.of(IdentityFunction.ERROR_IDENTITY_CONNECTION, IdentityFunction.ERROR_IDENTITY_CONNECTION_LABEL, //
                IdentityFunction.ERROR_UNKNOWN_FUNCTION, IdentityFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
                ERROR_DELETE_GROUP, ERROR_DELETE_GROUP_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Delete group";
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
