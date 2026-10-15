package org.camunda.connector.identity.function.authorization;

import io.camunda.client.api.search.enums.OwnerType;
import io.camunda.client.api.search.enums.ResourceType;
import io.camunda.connector.cherrytemplate.RunnerParameter;

/**
 * Shared element-template dropdown choices for the authorization sub-functions. The owner type is
 * restricted to USER/ROLE/GROUP (CLIENT and MAPPING_RULE can also own an authorization in Camunda 8,
 * but are out of scope for this connector). The resource type offers every value of the Camunda 8
 * client's ResourceType enum.
 */
class AuthorizationChoices {

    private AuthorizationChoices() {
    }

    static RunnerParameter addOwnerTypeChoices(RunnerParameter parameter) {
        parameter.addChoice(OwnerType.USER.name(), "User");
        parameter.addChoice(OwnerType.ROLE.name(), "Role");
        parameter.addChoice(OwnerType.GROUP.name(), "Group");
        return parameter;
    }

    static RunnerParameter addResourceTypeChoices(RunnerParameter parameter) {
        for (ResourceType resourceType : ResourceType.values()) {
            if (resourceType != ResourceType.UNKNOWN_ENUM_VALUE) {
                parameter.addChoice(resourceType.name(), resourceType.name());
            }
        }
        return parameter;
    }
}
