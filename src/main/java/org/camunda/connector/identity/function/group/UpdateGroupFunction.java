package org.camunda.connector.identity.function.group;

import io.camunda.client.CamundaClient;
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

public class UpdateGroupFunction implements IdentitySubFunction {

    public static final String ERROR_UPDATE_GROUP = "UPDATE_GROUP";
    public static final String ERROR_UPDATE_GROUP_LABEL = "During update group in Camunda 8 Identity";

    private final Logger logger = LoggerFactory.getLogger(UpdateGroupFunction.class.getName());

    @Override
    public IdentityOutput executeSubFunction(CamundaClient camundaClient,
                                             IdentityInput identityInput,
                                             OutboundConnectorContext context) throws ConnectorException {
        long markerTime = System.currentTimeMillis();
        String groupId = identityInput.getGroupId();
        String groupName = identityInput.getGroupName();
        String description = identityInput.getGroupDescription();
        logger.info("--- UpdateGroup : START GroupId[{}] name[{}]", groupId, groupName);
        try {
            camundaClient.newUpdateGroupCommand(groupId)
                    .name(groupName)
                    .description(description)
                    .execute();

            IdentityOutput identityOutput = new IdentityOutput();

            identityOutput.group = new GroupImpl(groupId, groupName, description);

            identityOutput.status = IdentityOutput.STATUS_SUCCESS;
            identityOutput.dateOperation = new Date();
            logger.info("--- UpdateGroup : END GroupId[{}] name[{}] status[{}] in {} ms",
                    groupId, groupName, identityOutput.status, System.currentTimeMillis() - markerTime);
            return identityOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("--- UpdateGroup : ERROR GroupId[{}] : {}", groupId, e.getMessage());
            throw new ConnectorException(ERROR_UPDATE_GROUP, "During update group[" + groupId + "] : " + e.getMessage());
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
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_GROUP_NAME_EXPLANATION),
                RunnerParameter.getInstance(IdentityInput.INPUT_GROUP_DESCRIPTION, //
                        "Group Description", //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        IdentityInput.INPUT_GROUP_DESCRIPTION_EXPLANATION)
        );//
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
                        "Group updated", //
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
                ERROR_UPDATE_GROUP, ERROR_UPDATE_GROUP_LABEL);
    }

    @Override
    public String getSubFunctionName() {
        return "Update group";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Update a group's name in Camunda 8 Identity";
    }

    @Override
    public String getSubFunctionType() {
        return "update-group";
    }
}
