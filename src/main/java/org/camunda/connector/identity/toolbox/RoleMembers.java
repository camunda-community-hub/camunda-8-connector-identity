package org.camunda.connector.identity.toolbox;

import java.util.ArrayList;
import java.util.List;

/**
 * Members of a role, per category. Used as a single input for add-in-role/delete-in-role:
 * {"users": ["<userName>"...], "groups": ["<groupId>"...], "client": ["<clientId>"...], "mappingRule": ["<mappingRuleId>"...]}
 */
public class RoleMembers {

    private List<String> users = new ArrayList<>();
    private List<String> groups = new ArrayList<>();
    private List<String> client = new ArrayList<>();
    private List<String> mappingRule = new ArrayList<>();

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String> getClient() {
        return client;
    }

    public void setClient(List<String> client) {
        this.client = client;
    }

    public List<String> getMappingRule() {
        return mappingRule;
    }

    public void setMappingRule(List<String> mappingRule) {
        this.mappingRule = mappingRule;
    }
}
