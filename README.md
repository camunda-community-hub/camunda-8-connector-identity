[![Community badge: Stable](https://img.shields.io/badge/Lifecycle-Stable-brightgreen)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#stable-)
[![Community extension badge](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)

# camunda-8-connector-identity

A connector to manage users and groups in the Camunda 8 Identity component, via the official
`io.camunda:camunda-client-java` SDK.

# Principle

Camunda 8 Identity manages the users, groups, roles and mapping rules allowed to access Operate,
Tasklist, and Optimize. This connector exposes the user- and group-management operations available
on the Camunda 8 Java client as BPMN service tasks:

* Create a user
* Search users (by username/name/email, by group, or by role)
* Update a user
* Delete a user
* Create a group
* Add users/roles/mapping rules/clients in a group (one call, one `groupMembers` object)
* Search in a group (its users, roles, mapping rules and/or clients, one call, one `groupContentFilter` object)
* Delete users/roles/mapping rules/clients from a group (one call, one `groupMembers` object)
* Delete a group
* Create a role
* Add users/groups/mapping rules/clients in a role (one call, one `roleMembers` object)
* Search in a role (its users, groups, mapping rules and/or clients, one call, one `roleContentFilter` object)
* Delete users/groups/mapping rules/clients from a role (one call, one `roleMembers` object)
* Delete a role

Unlike Keycloak (see the sibling `camunda-8-connector-keycloak` project, which this connector is
adapted from), Camunda 8 Identity does not expose a "userId" (an opaque, generated identifier).
Instead, a user is identified by its **username**, which is unique across the cluster. Everywhere
the Keycloak connector used a `userId`, this connector uses `userName` instead. Likewise, a group
is identified by its **groupId**, and a role by its **roleId** (neither is an opaque id).

There is also no concept of "realm" in Camunda 8 Identity, and no role-synchronization-on-create
for users.

## Connecting to the cluster

The connector gets a `CamundaClient` via `CamundaClient.newClientBuilder().build()` and works
from it directly - it does not expose any connection field of its own. The client is
auto-configured from the runtime environment (the standard `CAMUNDA_CLIENT_*`/`ZEEBE_*` variables),
exactly like any job worker running in the same Connector Runtime/cluster.

This parameter describes the function:

| Name             | Description                                                          | Class            | Default | Level    |
|------------------|-----------------------------------------------------------------------|------------------|---------|----------|
| identityFunction | Function: `create-user`, `search-users`, `update-user`, `delete-user` | java.lang.String |         | REQUIRED |

# Create user

`identityFunction = create-user`. Calls `CamundaClient.newCreateUserCommand()`.

## Inputs

| Name              | Description                                                                                          | Class             | Default | Level    |
|-------------------|--------------------------------------------------------------------------------------------------------|-------------------|---------|----------|
| userName          | The username, must be unique in the cluster                                                            | java.lang.String  |         | REQUIRED |
| userDisplayName   | Display name of the user                                                                                | java.lang.String  |         | OPTIONAL |
| userEmail         | Email of the user                                                                                       | java.lang.String  |         | OPTIONAL |
| userPassword      | Password of the user                                                                                    | java.lang.String  |         | OPTIONAL |
| ignoreIfAlreadyDone | If true (default), do not throw a BPMN error when the operation is already done (entity already exists on creation, or does not exist anymore on deletion) | java.lang.Boolean | true    | REQUIRED |

## Output

| Name          | Description                            | Class            | Level    |
|---------------|------------------------------------------|------------------|----------|
| userName      | Username of the user created (or updated)| java.lang.String | REQUIRED |
| status        | Status of operation (`CREATED`/`UPDATED`)| java.lang.String | REQUIRED |
| dateOperation | Date when the operation is performed     | java.util.Date   | REQUIRED |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| USER_ALREADY_EXIST  | The username already exists, and `ignoreIfAlreadyDone` is false     |
| CREATE_USER         | Create user failed                                                  |

# Search users

`identityFunction = search-users`. Three search modes, checked in this order:

1. If `searchByGroupId` is set: calls `CamundaClient.newUsersByGroupSearchRequest(groupId)`, returns users of that group (username only).
2. Else if `searchByRoleId` is set: calls `CamundaClient.newUsersByRoleSearchRequest(roleId)`, returns users of that role (username only).
3. Else: calls `CamundaClient.newUsersSearchRequest()` filtered by `searchByUserName`/`searchByUserDisplayName`/`searchByUserEmail`, returns full users (username/name/email).

## Inputs

| Name                    | Description                              | Class            | Default | Level    |
|-------------------------|--------------------------------------------|------------------|---------|----------|
| searchByUserName        | Search by the username                     | java.lang.String |         | OPTIONAL |
| searchByUserDisplayName | Search by the name of the user             | java.lang.String |         | OPTIONAL |
| searchByUserEmail       | Search by the email of the user            | java.lang.String |         | OPTIONAL |
| searchByGroupId         | Search all users of the given group        | java.lang.String |         | OPTIONAL |
| searchByRoleId          | Search all users of the given role         | java.lang.String |         | OPTIONAL |

## Output

| Name      | Description                        | Class          | Level    |
|-----------|---------------------------------------|----------------|----------|
| listUsers | List of users found by the search (real Camunda 8 client objects, not maps) | java.util.List | REQUIRED |
| userName  | Username of the first user of the list| java.lang.String | REQUIRED |
| user      | First user of the list (real Camunda 8 client object, not a map) | `User`/`GroupUser`/`RoleUser` | REQUIRED |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| SEARCH_USER         | During search user(s)                                               |

# Update user

`identityFunction = update-user`. Calls `CamundaClient.newUpdateUserCommand(userName)`.

## Inputs

| Name            | Description                     | Class            | Default | Level    |
|-----------------|------------------------------------|------------------|---------|----------|
| userName        | Username to update                 | java.lang.String |         | REQUIRED |
| userDisplayName | New display name                   | java.lang.String |         | OPTIONAL |
| userEmail       | New email                          | java.lang.String |         | OPTIONAL |
| userPassword    | New password                       | java.lang.String |         | OPTIONAL |

## Output

| Name          | Description                          | Class            | Level    |
|---------------|-----------------------------------------|------------------|----------|
| status        | Status of operation                     | java.lang.String | REQUIRED |
| dateOperation | Date when the operation is performed    | java.util.Date   | REQUIRED |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| UPDATE_USER         | During the update of the user                                       |

# Delete user

`identityFunction = delete-user`. Calls `CamundaClient.newDeleteUserCommand(userName)`.

## Inputs

| Name     | Description       | Class            | Default | Level    |
|----------|--------------------|------------------|---------|----------|
| userName | Username to delete | java.lang.String |         | REQUIRED |

## Output

| Name          | Description                          | Class            | Level    |
|---------------|-----------------------------------------|------------------|----------|
| status        | Status of operation                     | java.lang.String | REQUIRED |
| dateOperation | Date when the operation is performed    | java.util.Date   | REQUIRED |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| DELETE_USER         | During deletion of the user                                         |

# Create group

`identityFunction = create-group`. Calls `CamundaClient.newCreateGroupCommand()`.

## Inputs

| Name             | Description                | Class            | Default | Level    |
|------------------|-----------------------------|------------------|---------|----------|
| groupId          | The group id, unique in the cluster | java.lang.String |         | REQUIRED |
| groupName        | Name of the group           | java.lang.String |         | OPTIONAL |
| groupDescription | Description of the group    | java.lang.String |         | OPTIONAL |

## Output

| Name          | Description                          | Class            | Level    |
|---------------|-----------------------------------------|------------------|----------|
| groupId       | Id of the group created                 | java.lang.String | REQUIRED |
| status        | Status of operation                     | java.lang.String | REQUIRED |
| dateOperation | Date when the operation is performed    | java.util.Date   | REQUIRED |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| CREATE_GROUP        | Create group failed                                                 |

# Add in group

`identityFunction = add-in-group`. Adds members to a group in one call. `groupMembers` is a
single FEEL context object listing every member to add, per category - all non-empty lists are
processed, calling the matching `CamundaClient.newAssign...ToGroupCommand()` once per id:

```
={users: ["walter.bates"], roles: ["supervisor-role"], client: [], mappingRule: []}
```

## Inputs

| Name         | Description                                                                                                                            | Class            | Default | Level    |
|--------------|------------------------------------------------------------------------------------------------------------------------------------------|------------------|---------|----------|
| groupId      | The group to add the members to                                                                                                          | java.lang.String |         | REQUIRED |
| groupMembers | `{users: [...], roles: [...], client: [...], mappingRule: [...]}` - usernames/role ids/client ids/mapping rule ids to add                | `GroupMembers`   |         | REQUIRED |

## Output

| Name          | Description                          | Class            | Level    |
|---------------|-----------------------------------------|------------------|----------|
| groupId       | Id of the group                         | java.lang.String | REQUIRED |
| status        | Status of operation                     | java.lang.String | REQUIRED |
| dateOperation | Date when the operation is performed    | java.util.Date   | REQUIRED |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| ADD_IN_GROUP        | Adding a member to the group failed                                 |

# Search in group

`identityFunction = search-in-group`. Returns the content of a group in one call.
`groupContentFilter` is a single FEEL context object selecting which categories to return - every
category set to `true` is populated in the matching output:

```
={users: true, roles: true, client: false, mappingRule: false}
```

| Category      | Calls                                                        | Output populated |
|---------------|-----------------------------------------------------------------|-------------------|
| `users`       | `CamundaClient.newUsersByGroupSearchRequest(groupId)`            | `listUsers` (`List<GroupUser>`) |
| `roles`       | `CamundaClient.newRolesByGroupSearchRequest(groupId)`            | `listRoles` (`List<Role>`) |
| `mappingRule` | `CamundaClient.newMappingRulesByGroupSearchRequest(groupId)`     | `listMappingRules` (`List<MappingRule>`) |
| `client`      | `CamundaClient.newClientsByGroupSearchRequest(groupId)`          | `listClientId` (`List<Client>`) |

All lists hold the real Camunda 8 client response objects (not maps).

## Inputs

| Name               | Description                                                                          | Class                | Default | Level    |
|--------------------|-----------------------------------------------------------------------------------------|----------------------|---------|----------|
| groupId            | The group to read                                                                        | java.lang.String     |         | REQUIRED |
| groupContentFilter | `{users: true\|false, roles: true\|false, client: true\|false, mappingRule: true\|false}` | `GroupContentFilter` |         | REQUIRED |

## Output

| Name             | Description                                    | Class            | Level    |
|------------------|---------------------------------------------------|------------------|----------|
| groupId          | Id of the group read                               | java.lang.String | REQUIRED |
| listUsers        | Members of the group (`groupContentFilter.users`)  | `List<GroupUser>` | OPTIONAL |
| listRoles        | Roles of the group (`groupContentFilter.roles`)    | `List<Role>`      | OPTIONAL |
| listMappingRules | Mapping rules of the group (`groupContentFilter.mappingRule`) | `List<MappingRule>` | OPTIONAL |
| listClientId     | Clients of the group (`groupContentFilter.client`) | `List<Client>`    | OPTIONAL |
| status           | Status of operation                                | java.lang.String  | REQUIRED |
| dateOperation    | Date when the operation is performed               | java.util.Date    | REQUIRED |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| SEARCH_IN_GROUP     | During the read of the group content                                |

# Delete in group

`identityFunction = delete-in-group`. Removes members from a group in one call, using the same
`groupMembers` object as Add in group - all non-empty lists are processed, calling the matching
`CamundaClient.newUnassign...FromGroupCommand()` once per id.

## Inputs

| Name         | Description                                                                                                                | Class          | Default | Level    |
|--------------|---------------------------------------------------------------------------------------------------------------------------|----------------|---------|----------|
| groupId      | The group to remove the members from                                                                                       | java.lang.String |       | REQUIRED |
| groupMembers | `{users: [...], roles: [...], client: [...], mappingRule: [...]}` - usernames/role ids/client ids/mapping rule ids to remove | `GroupMembers` |         | REQUIRED |

## Output

| Name          | Description                          | Class            | Level    |
|---------------|-----------------------------------------|------------------|----------|
| groupId       | Id of the group                         | java.lang.String | REQUIRED |
| status        | Status of operation                     | java.lang.String | REQUIRED |
| dateOperation | Date when the operation is performed    | java.util.Date   | REQUIRED |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| DELETE_IN_GROUP     | Removing a member from the group failed                             |

# Delete group

`identityFunction = delete-group`. Calls `CamundaClient.newDeleteGroupCommand(groupId)`.

## Inputs

| Name    | Description       | Class            | Default | Level    |
|---------|--------------------|------------------|---------|----------|
| groupId | Group id to delete | java.lang.String |         | REQUIRED |

## Output

| Name          | Description                          | Class            | Level    |
|---------------|-----------------------------------------|------------------|----------|
| groupId       | Id of the group deleted                 | java.lang.String | REQUIRED |
| status        | Status of operation                     | java.lang.String | REQUIRED |
| dateOperation | Date when the operation is performed    | java.util.Date   | REQUIRED |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| DELETE_GROUP        | During delete group in Camunda 8 Identity                           |

# Create role

`identityFunction = create-role`. Calls `CamundaClient.newCreateRoleCommand()`.

## Inputs

| Name            | Description                | Class            | Default | Level    |
|-----------------|-----------------------------|------------------|---------|----------|
| roleId          | The role id, unique in the cluster | java.lang.String |         | REQUIRED |
| roleName        | Name of the role           | java.lang.String |         | OPTIONAL |
| roleDescription | Description of the role    | java.lang.String |         | OPTIONAL |

## Output

| Name          | Description                          | Class            | Level    |
|---------------|-----------------------------------------|------------------|----------|
| roleId        | Id of the role created                  | java.lang.String | REQUIRED |
| status        | Status of operation                     | java.lang.String | REQUIRED |
| dateOperation | Date when the operation is performed    | java.util.Date   | REQUIRED |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| CREATE_ROLE         | Create role failed                                                  |

# Add in role

`identityFunction = add-in-role`. Adds members to a role in one call. `roleMembers` is a
single FEEL context object listing every member to add, per category - all non-empty lists are
processed, calling the matching `CamundaClient.newAssignRoleTo...Command()` once per id:

```
={users: ["walter.bates"], groups: ["supervisor"], client: [], mappingRule: []}
```

## Inputs

| Name        | Description                                                                                                              | Class          | Default | Level    |
|-------------|----------------------------------------------------------------------------------------------------------------------------|----------------|---------|----------|
| roleId      | The role to add the members to                                                                                            | java.lang.String |       | REQUIRED |
| roleMembers | `{users: [...], groups: [...], client: [...], mappingRule: [...]}` - usernames/group ids/client ids/mapping rule ids to add | `RoleMembers`  |         | REQUIRED |

## Output

| Name          | Description                          | Class            | Level    |
|---------------|-----------------------------------------|------------------|----------|
| roleId        | Id of the role                          | java.lang.String | REQUIRED |
| status        | Status of operation                     | java.lang.String | REQUIRED |
| dateOperation | Date when the operation is performed    | java.util.Date   | REQUIRED |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| ADD_IN_ROLE         | Adding a member to the role failed                                  |

# Search in role

`identityFunction = search-in-role`. Returns the content of a role in one call.
`roleContentFilter` is a single FEEL context object selecting which categories to return - every
category set to `true` is populated in the matching output:

```
={users: true, groups: true, client: false, mappingRule: false}
```

| Category      | Calls                                                        | Output populated |
|---------------|-----------------------------------------------------------------|-------------------|
| `users`       | `CamundaClient.newUsersByRoleSearchRequest(roleId)`              | `listUsers` (`List<RoleUser>`) |
| `groups`      | `CamundaClient.newGroupsByRoleSearchRequest(roleId)`             | `listGroups` (`List<RoleGroup>`) |
| `mappingRule` | `CamundaClient.newMappingRulesByRoleSearchRequest(roleId)`       | `listMappingRules` (`List<MappingRule>`) |
| `client`      | `CamundaClient.newClientsByRoleSearchRequest(roleId)`            | `listClientId` (`List<Client>`) |

All lists hold the real Camunda 8 client response objects (not maps).

## Inputs

| Name              | Description                                                                        | Class               | Default | Level    |
|-------------------|-----------------------------------------------------------------------------------|---------------------|---------|----------|
| roleId            | The role to read                                                                   | java.lang.String    |         | REQUIRED |
| roleContentFilter | `{users: true\|false, groups: true\|false, client: true\|false, mappingRule: true\|false}` | `RoleContentFilter` |         | REQUIRED |

## Output

| Name             | Description                                    | Class            | Level    |
|------------------|---------------------------------------------------|------------------|----------|
| roleId           | Id of the role read                                | java.lang.String | REQUIRED |
| listUsers        | Members of the role (`roleContentFilter.users`)    | `List<RoleUser>`  | OPTIONAL |
| listGroups       | Groups of the role (`roleContentFilter.groups`)    | `List<RoleGroup>` | OPTIONAL |
| listMappingRules | Mapping rules of the role (`roleContentFilter.mappingRule`) | `List<MappingRule>` | OPTIONAL |
| listClientId     | Clients of the role (`roleContentFilter.client`)   | `List<Client>`    | OPTIONAL |
| status           | Status of operation                                | java.lang.String  | REQUIRED |
| dateOperation    | Date when the operation is performed               | java.util.Date    | REQUIRED |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| SEARCH_IN_ROLE      | During the read of the role content                                  |

# Delete in role

`identityFunction = delete-in-role`. Removes members from a role in one call, using the same
`roleMembers` object as Add in role - all non-empty lists are processed, calling the matching
`CamundaClient.newUnassignRoleFrom...Command()` once per id.

## Inputs

| Name        | Description                                                                                                                    | Class         | Default | Level    |
|-------------|-------------------------------------------------------------------------------------------------------------------------------|---------------|---------|----------|
| roleId      | The role to remove the members from                                                                                            | java.lang.String |      | REQUIRED |
| roleMembers | `{users: [...], groups: [...], client: [...], mappingRule: [...]}` - usernames/group ids/client ids/mapping rule ids to remove | `RoleMembers` |         | REQUIRED |

## Output

| Name          | Description                          | Class            | Level    |
|---------------|-----------------------------------------|------------------|----------|
| roleId        | Id of the role                          | java.lang.String | REQUIRED |
| status        | Status of operation                     | java.lang.String | REQUIRED |
| dateOperation | Date when the operation is performed    | java.util.Date   | REQUIRED |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| DELETE_IN_ROLE      | Removing a member from the role failed                               |

# Delete role

`identityFunction = delete-role`. Calls `CamundaClient.newDeleteRoleCommand(roleId)`.

## Inputs

| Name   | Description       | Class            | Default | Level    |
|--------|--------------------|------------------|---------|----------|
| roleId | Role id to delete  | java.lang.String |         | REQUIRED |

## Output

| Name          | Description                          | Class            | Level    |
|---------------|-----------------------------------------|------------------|----------|
| roleId        | Id of the role deleted                  | java.lang.String | REQUIRED |
| status        | Status of operation                     | java.lang.String | REQUIRED |
| dateOperation | Date when the operation is performed    | java.util.Date   | REQUIRED |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| DELETE_ROLE         | During delete role in Camunda 8 Identity                             |

# Build

```bash
mvn clean package
```

Two jars are produced. The jar with all dependencies can be uploaded in the [Cherry Framework](https://github.com/camunda-community-hub/zeebe-cherry-framework)

## Element Template

The element template can be found in the [element-templates](/element-templates/identity-function.json) directory.
