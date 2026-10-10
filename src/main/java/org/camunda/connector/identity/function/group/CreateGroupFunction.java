package org.camunda.connector.identity.function.group;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.CreateGroupResponse;
import io.camunda.client.api.search.response.Group;
import io.camunda.client.impl.search.response.GroupImpl;
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

public class CreateGroupFunction implements IdentitySubFunction {

    public static final String ERROR_CREATE_GROUP = "CREATE_GROUP";
    public static final String ERROR_CREATE_GROUP_LABEL = "Create group failed";

    private final Logger logger = LoggerFactory.getLogger(CreateGroupFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = new Date().getTime();
        boolean ignoreIfAlreadyDone = Boolean.TRUE.equals(identityInput.getIgnoreIfAlreadyDone());
        logger.info("--- CreateGroup : START GroupId[{}] name[{}] description[{}] ignoreIfAlreadyDone[{}]",
                identityInput.getGroupId(), identityInput.getGroupName(), identityInput.getGroupDescription(), ignoreIfAlreadyDone);
        try {
            CreateGroupResponse response = camundaClient.newCreateGroupCommand()
                    .groupId(identityInput.getGroupId())
                    .name(identityInput.getGroupName())
                    .description(identityInput.getGroupDescription() == null ? "" : identityInput.getGroupDescription())
                    .execute();
            IdentityOutput identityOutput = new IdentityOutput();
            identityOutput.group = new GroupImpl(response.getGroupId(), response.getName(), response.getDescription());
            identityOutput.status = IdentityOutput.STATUS_CREATED;
            identityOutput.dateOperation = new Date();
            logger.info("--- CreateGroup : END GroupId[{}] name[{}] Description [{}] status[{}] in {} ms",
                    identityInput.getGroupId(),
                    identityInput.getGroupName(),
                    identityInput.getGroupDescription(),
                    identityOutput.status,
                    System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            if (IdentityFunction.isAlreadyDoneError(e) && ignoreIfAlreadyDone) {
                IdentityOutput identityOutput = new IdentityOutput();
                identityOutput.group = new GroupImpl(identityInput.getGroupId(), identityInput.getGroupName(), identityInput.getGroupDescription());

                identityOutput.status = IdentityOutput.STATUS_ALREADY_EXIST;
                identityOutput.dateOperation = new Date();
                logger.info("--- CreateGroup : END GroupId[{}] already exists, ignored, status[{}] in {} ms", identityInput.getGroupId(),
                        identityOutput.status,
                        System.currentTimeMillis() - markerTime);
                return identityOutput;
            }
            logger.error("--- CreateGroup : ERROR GroupId[{}] ", identityInput.getGroupId(), e);
            throw new ConnectorException(ERROR_CREATE_GROUP, "GroupId[" + identityInput.getGroupId() + "] failed " + e.getMessage());
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

                RunnerParameter.getInstance(IdentityInput.INPUT_GROUP_NAME, //
                                "Group Name", //
                                String.class, //
                                "", //
                                RunnerParameter.Level.OPTIONAL, //
                                IdentityInput.INPUT_GROUP_NAME_EXPLANATION)
                        .setVisibleInTemplate(),//

                RunnerParameter.getInstance(IdentityInput.INPUT_GROUP_DESCRIPTION, //
                                "Group Description", //
                                String.class, //
                                "", //
                                RunnerParameter.Level.OPTIONAL, //
                                IdentityInput.INPUT_GROUP_DESCRIPTION_EXPLANATION)
                        .setVisibleInTemplate(),//

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
                        IdentityOutput.OUTPUT_GROUP_EXPLANATION), //

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
                ERROR_CREATE_GROUP, ERROR_CREATE_GROUP_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Create group";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Create a group in Camunda 8 Identity";
    }

    @Override
    public String getSubFunctionType() {
        return "create-group";
    }
}
