package org.camunda.connector.identity.toolbox;

/**
 * Selects which categories of a group's content to return. Used as a single input for
 * search-in-group: {"users": true|false, "roles": true|false, "client": true|false, "mappingRule": true|false}
 */
public class GroupContentFilter {

    private boolean users = false;
    private boolean roles = false;
    private boolean client = false;
    private boolean mappingRule = false;

    public boolean isUsers() {
        return users;
    }

    public void setUsers(boolean users) {
        this.users = users;
    }

    public boolean isRoles() {
        return roles;
    }

    public void setRoles(boolean roles) {
        this.roles = roles;
    }

    public boolean isClient() {
        return client;
    }

    public void setClient(boolean client) {
        this.client = client;
    }

    public boolean isMappingRule() {
        return mappingRule;
    }

    public void setMappingRule(boolean mappingRule) {
        this.mappingRule = mappingRule;
    }
}
