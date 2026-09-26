package org.camunda.connector.identity.toolbox;

/**
 * Selects which categories of a role's content to return. Used as a single input for
 * search-in-role: {"users": true|false, "groups": true|false, "client": true|false, "mappingRule": true|false}
 */
public class RoleContentFilter {

    private boolean users = false;
    private boolean groups = false;
    private boolean client = false;
    private boolean mappingRule = false;

    public boolean isUsers() {
        return users;
    }

    public void setUsers(boolean users) {
        this.users = users;
    }

    public boolean isGroups() {
        return groups;
    }

    public void setGroups(boolean groups) {
        this.groups = groups;
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
