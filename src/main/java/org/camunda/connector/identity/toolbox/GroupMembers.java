package org.camunda.connector.identity.toolbox;

import java.util.ArrayList;
import java.util.List;

/**
 * Members of a group, per category. Used as a single input for add-in-group/delete-in-group:
 * {"users": ["<userName>"...], "roles": ["<roleId>"...], "client": ["<clientId>"...], "mappingRule": ["<mappingRuleId>"...]}
 */
public class GroupMembers {

    private List<String> users = new ArrayList<>();
    private List<String> roles = new ArrayList<>();
    private List<String> client = new ArrayList<>();
    private List<String> mappingRule = new ArrayList<>();

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
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
