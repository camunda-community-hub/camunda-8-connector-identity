[![Community badge: Stable](https://img.shields.io/badge/Lifecycle-Stable-brightgreen)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#stable-)
[![Community extension badge](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)

# camunda-8-connector-identity

<img src="Identity.png" alt="Identity.png" width="100" height="100" />

A connector to manage users and groups in the Camunda 8 Identity component, via the official
`io.camunda:camunda-client-java` SDK.

# Principle

Camunda 8 Identity manages the users, groups, roles and mapping rules allowed to access Operate,
Tasklist, and Optimize. This connector exposes the user-, group-, role- and client-management
operations available on the Camunda 8 Java client as BPMN service tasks:

![IdentityOperations.png](doc/IdentityOperations.png)

On user:

* Create
* Search (by username/name/email - wildcard `like` match -, by group, or by role)
* Update
* Delete

On group:

* Create
* Add users/roles/mapping rules/clients in a group (one call, one `groupMembers` object)
* Get the content of a group (its users, roles, mapping rules and/or clients, one call, one `groupContentFilter` object)
* Search groups by name (wildcard `like` match, paginated)
* Delete users/roles/mapping rules/clients from a group (one call, one `groupMembers` object)
* Delete a group

on role:

* Create
* Add users/groups/mapping rules/clients in a role (one call, one `roleMembers` object)
* Get the content of a role (its users, groups, mapping rules and/or clients, one call, one `roleContentFilter` object)
* Search roles by name (wildcard `like` match, paginated)
* Delete users/groups/mapping rules/clients from a role (one call, one `roleMembers` object)
* Delete

On Client:

* Assign a client to a role, a group and/or a tenant
* Unassign a client from a role, a group and/or a tenant

> The `*`/`?` wildcard (`like`) search used by "Search users", "Search groups" and "Search roles"
> is only understood by a Camunda 8.10+ broker. Against an older broker (8.9 and before) it fails
> with `400 Request property [filter.name] cannot be parsed`.

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

| Name             | Description                                                                                                                                                                                                                                                                                      | Class            | Default | Level    |
|------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------|---------|----------|
| identityFunction | Function: `create-user`, `search-users`, `update-user`, `delete-user`, `create-group`, `add-in-group`, `get-content-group`, `search-group`, `delete-in-group`, `delete-group`, `create-role`, `add-in-role`, `get-content-role`, `search-role`, `delete-in-role`, `delete-role`, `assign-client`, `unassign-client` | java.lang.String |         | REQUIRED |

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

| Name          | Description                                                                 | Class                                      | Level    |
|---------------|------------------------------------------------------------------------------|---------------------------------------------|----------|
| status        | Status of operation (`CREATED`, or `ALREADY_EXIST` if `ignoreIfAlreadyDone`) | java.lang.String                           | OPTIONAL |
| user          | The user created (real Camunda 8 client object, not a map)                   | io.camunda.client.api.search.response.User | OPTIONAL |
| dateOperation | Date when the operation is performed                                         | java.util.Date                             | OPTIONAL |

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

| Name                    | Description                                                                                                        | Class            | Default | Level    |
|-------------------------|----------------------------------------------------------------------------------------------------------------------|------------------|---------|----------|
| searchByUserName        | Search by the username (wildcard `like` match: `*` zero/one/multiple chars, `?` a single char - e.g. `"*bates"`)      | java.lang.String |         | OPTIONAL |
| searchByUserDisplayName | Search by the name of the user (wildcard `like` match, same syntax)                                                  | java.lang.String |         | OPTIONAL |
| searchByUserEmail       | Search by the email of the user (wildcard `like` match, same syntax)                                                 | java.lang.String |         | OPTIONAL |
| searchByGroupId         | Search all users of the given group                                                                                  | java.lang.String |         | OPTIONAL |
| searchByRoleId          | Search all users of the given role                                                                                   | java.lang.String |         | OPTIONAL |

> Without a wildcard, `searchByUserName`/`searchByUserDisplayName`/`searchByUserEmail` only match
> exactly. The `*`/`?` wildcard syntax requires a Camunda 8.10+ broker - see the note at the top of
> this document.

## Output

| Name          | Description                                                                 | Class             | Level    |
|---------------|--------------------------------------------------------------------------------|-------------------|----------|
| status        | Status of operation                                                             | java.lang.String  | OPTIONAL |
| listUsers     | List of users found by the search (real Camunda 8 client objects, not maps)    | java.util.List    | OPTIONAL |
| dateOperation | Date when the operation is performed                                           | java.util.Date    | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| SEARCH_USER         | During search user(s)                                               |

# Update user

`identityFunction = update-user`. Calls `CamundaClient.newUpdateUserCommand(userName)`, chaining
`.name()`/`.email()`/`.password()` only for the inputs actually provided, then re-reads the user
to build the output.

## Inputs

| Name            | Description                     | Class            | Default | Level    |
|-----------------|------------------------------------|------------------|---------|----------|
| userName        | Username to update                 | java.lang.String |         | REQUIRED |
| userDisplayName | New display name                   | java.lang.String |         | OPTIONAL |
| userEmail       | New email                          | java.lang.String |         | OPTIONAL |
| userPassword    | New password                       | java.lang.String |         | OPTIONAL |

## Output

| Name          | Description                           | Class                                       | Level    |
|---------------|------------------------------------------|----------------------------------------------|----------|
| status        | Status of operation                      | java.lang.String                            | OPTIONAL |
| user          | The updated user (real Camunda 8 client object, not a map) | io.camunda.client.api.search.response.User | OPTIONAL |
| dateOperation | Date when the operation is performed     | java.util.Date                              | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| UPDATE_USER         | During the update of the user                                       |

# Delete user

`identityFunction = delete-user`. Calls `CamundaClient.newDeleteUserCommand(userName)`.

## Inputs

| Name                | Description                                                                                                                                                  | Class             | Default | Level    |
|---------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------|---------|----------|
| userName            | Username to delete                                                                                                                                              | java.lang.String  |         | REQUIRED |
| ignoreIfAlreadyDone | If true (default), do not throw a BPMN error when the user does not exist anymore - returns status `ALREADY_DELETED` instead                                   | java.lang.Boolean | true    | REQUIRED |

## Output

| Name          | Description                                                                 | Class            | Level    |
|---------------|--------------------------------------------------------------------------------|------------------|----------|
| status        | Status of operation (`SUCCESS`, or `ALREADY_DELETED` if `ignoreIfAlreadyDone`) | java.lang.String | OPTIONAL |
| dateOperation | Date when the operation is performed                                           | java.util.Date   | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| DELETE_USER         | During deletion of the user                                         |

# Create group

`identityFunction = create-group`. Calls `CamundaClient.newCreateGroupCommand()`.

## Inputs

| Name                | Description                                                                                                  | Class             | Default | Level    |
|---------------------|------------------------------------------------------------------------------------------------------------------|-------------------|---------|----------|
| groupId             | The group id, unique in the cluster                                                                               | java.lang.String  |         | REQUIRED |
| groupName           | Name of the group                                                                                                  | java.lang.String  |         | OPTIONAL |
| groupDescription    | Description of the group                                                                                          | java.lang.String  |         | OPTIONAL |
| ignoreIfAlreadyDone | If true (default), do not throw a BPMN error when the group already exists - returns status `ALREADY_EXIST` instead | java.lang.Boolean | true    | REQUIRED |

## Output

| Name          | Description                                                                 | Class                                        | Level    |
|---------------|--------------------------------------------------------------------------------|-----------------------------------------------|----------|
| status        | Status of operation (`CREATED`, or `ALREADY_EXIST` if `ignoreIfAlreadyDone`)   | java.lang.String                             | OPTIONAL |
| group         | The group created (real Camunda 8 client object, not a map)                    | io.camunda.client.api.search.response.Group | OPTIONAL |
| dateOperation | Date when the operation is performed                                            | java.util.Date                               | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| CREATE_GROUP        | Create group failed                                                 |

# Add in group

`identityFunction = add-in-group`. Adds members to a group in one call. `groupMembers` is a
single FEEL context object listing every member to add, per category - all non-empty lists are
processed, calling the matching `CamundaClient.newAssign...ToGroupCommand()` once per id. The
group is first looked up by `groupId`; if it does not exist (or more than one match is returned),
the function fails with `GROUP_NOT_FOUND` before any member is added:

```
={users: ["walter.bates"], roles: ["supervisor-role"], client: [], mappingRule: []}
```

## Inputs

| Name                | Description                                                                                                   | Class             | Default | Level    |
|---------------------|--------------------------------------------------------------------------------------------------------------------|-------------------|---------|----------|
| groupId             | The group to add the members to                                                                                      | java.lang.String  |         | REQUIRED |
| groupMembers        | `{users: [...], roles: [...], client: [...], mappingRule: [...]}` - usernames/role ids/client ids/mapping rule ids to add | `GroupMembers`    |         | REQUIRED |
| ignoreIfAlreadyDone | If true (default), do not throw a BPMN error when a member is already in the group - skips it instead               | java.lang.Boolean | true    | REQUIRED |

## Output

| Name          | Description                                                   | Class                                         | Level    |
|---------------|--------------------------------------------------------------------|-------------------------------------------------|----------|
| status        | Status of operation                                                 | java.lang.String                                | OPTIONAL |
| group         | The group the members were added to (real Camunda 8 client object) | io.camunda.client.api.search.response.Group    | OPTIONAL |
| dateOperation | Date when the operation is performed                                | java.util.Date                                  | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| GROUP_NOT_FOUND     | No group found with the id provided                                 |
| ADD_IN_GROUP        | Adding a member to the group failed                                 |

# Get content group

`identityFunction = get-content-group`. Returns the content of a group in one call. The group is
first looked up by `groupId` (fails with `GROUP_NOT_FOUND` if it does not exist, or more than one
match is returned). `groupContentFilter` is a single FEEL context object selecting which
categories to return - every category set to `true` is populated in the matching output:

```
={users: true, roles: true, client: false, mappingRule: false}
```

| Category      | Calls                                                        | Output populated |
|---------------|-----------------------------------------------------------------|-------------------|
| `users`       | `CamundaClient.newUsersByGroupSearchRequest(groupId)`, then resolves each username to a full `User` | `listUsers` (`List<User>`) |
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

| Name             | Description                                    | Class                                         | Level    |
|------------------|---------------------------------------------------|-------------------------------------------------|----------|
| status           | Status of operation                                | java.lang.String                               | OPTIONAL |
| group            | The group read (real Camunda 8 client object)      | io.camunda.client.api.search.response.Group   | OPTIONAL |
| listUsers        | Members of the group (`groupContentFilter.users`)  | java.util.List (`List<User>`)                  | OPTIONAL |
| listRoles        | Roles of the group (`groupContentFilter.roles`)    | java.util.List (`List<Role>`)                  | OPTIONAL |
| listMappingRules | Mapping rules of the group (`groupContentFilter.mappingRule`) | java.util.List (`List<MappingRule>`)   | OPTIONAL |
| listClientId     | Clients of the group (`groupContentFilter.client`) | java.util.List (`List<Client>`)                | OPTIONAL |
| dateOperation    | Date when the operation is performed               | java.util.Date                                 | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| GROUP_NOT_FOUND     | No group found with the id provided                                 |
| GET_CONTENT_GROUP   | During the read of the group content                                |

# Search group

`identityFunction = search-group`. Calls `CamundaClient.newGroupsSearchRequest()` filtered by a
wildcard (`like`) match on `groupNameSearch`, paginated by `pageNumber`/`pageSize`. The only
filterable fields on a group are `groupId` and `name` - there is no date-based filter, and no
"find all groups a given username belongs to" reverse search (only the forward
`newUsersByGroupSearchRequest` from a known `groupId` exists, see Get content group above).

## Inputs

| Name            | Description                                                                                      | Class             | Default | Level    |
|-----------------|-------------------------------------------------------------------------------------------------------|-------------------|---------|----------|
| groupNameSearch | Wildcard `like` match on the group name: `*` zero/one/multiple chars, `?` a single char                | java.lang.String  |         | REQUIRED |
| pageNumber      | Page number (1-based)                                                                                   | java.lang.Integer |         | REQUIRED |
| pageSize        | Page size                                                                                                | java.lang.Integer |         | REQUIRED |

## Output

| Name       | Description                                                                 | Class          | Level    |
|------------|----------------------------------------------------------------------------|----------------|----------|
| status     | Status of operation                                                         | java.lang.String | OPTIONAL |
| listGroups | List of groups found by the search (real Camunda 8 client objects)         | java.util.List | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| SEARCH_GROUP        | During search group(s) in Camunda 8 Identity                        |

# Delete in group

`identityFunction = delete-in-group`. Removes members from a group in one call, using the same
`groupMembers` object as Add in group - all non-empty lists are processed, calling the matching
`CamundaClient.newUnassign...FromGroupCommand()` once per id. The group is first looked up by
`groupId`; if it does not exist (or more than one match is returned), the function fails with
`GROUP_NOT_FOUND` before any member is removed.

## Inputs

| Name                | Description                                                                                                                   | Class             | Default | Level    |
|---------------------|------------------------------------------------------------------------------------------------------------------------------------|-------------------|---------|----------|
| groupId             | The group to remove the members from                                                                                                | java.lang.String  |         | REQUIRED |
| groupMembers        | `{users: [...], roles: [...], client: [...], mappingRule: [...]}` - usernames/role ids/client ids/mapping rule ids to remove        | `GroupMembers`    |         | REQUIRED |
| ignoreIfAlreadyDone | If true (default), do not throw a BPMN error when a member is already not in the group - skips it instead                          | java.lang.Boolean | true    | REQUIRED |

## Output

| Name          | Description                                                        | Class                                        | Level    |
|---------------|-------------------------------------------------------------------------|-------------------------------------------------|----------|
| status        | Status of operation                                                      | java.lang.String                                | OPTIONAL |
| group         | The group the members were removed from (real Camunda 8 client object)  | io.camunda.client.api.search.response.Group    | OPTIONAL |
| dateOperation | Date when the operation is performed                                     | java.util.Date                                  | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| GROUP_NOT_FOUND     | No group found with the id provided                                 |
| DELETE_IN_GROUP     | Removing a member from the group failed                             |

# Delete group

`identityFunction = delete-group`. Calls `CamundaClient.newDeleteGroupCommand(groupId)`.

## Inputs

| Name                | Description                                                                                                       | Class             | Default | Level    |
|---------------------|------------------------------------------------------------------------------------------------------------------------|-------------------|---------|----------|
| groupId             | Group id to delete                                                                                                       | java.lang.String  |         | REQUIRED |
| ignoreIfAlreadyDone | If true (default), do not throw a BPMN error when the group does not exist anymore - returns status `ALREADY_DELETED` instead | java.lang.Boolean | true    | REQUIRED |

## Output

| Name          | Description                                                                 | Class            | Level    |
|---------------|--------------------------------------------------------------------------------|------------------|----------|
| status        | Status of operation (`SUCCESS`, or `ALREADY_DELETED` if `ignoreIfAlreadyDone`) | java.lang.String | OPTIONAL |
| dateOperation | Date when the operation is performed                                           | java.util.Date   | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| DELETE_GROUP        | During delete group in Camunda 8 Identity                           |

# Create role

`identityFunction = create-role`. Calls `CamundaClient.newCreateRoleCommand()`.

## Inputs

| Name                | Description                                                                                                 | Class             | Default | Level    |
|---------------------|------------------------------------------------------------------------------------------------------------------|-------------------|---------|----------|
| roleId              | The role id, unique in the cluster                                                                                | java.lang.String  |         | REQUIRED |
| roleName            | Name of the role                                                                                                   | java.lang.String  |         | OPTIONAL |
| roleDescription     | Description of the role                                                                                           | java.lang.String  |         | OPTIONAL |
| ignoreIfAlreadyDone | If true (default), do not throw a BPMN error when the role already exists - returns status `ALREADY_EXIST` instead | java.lang.Boolean | true    | REQUIRED |

## Output

| Name          | Description                                                                 | Class                                       | Level    |
|---------------|--------------------------------------------------------------------------------|-----------------------------------------------|----------|
| status        | Status of operation (`CREATED`, or `ALREADY_EXIST` if `ignoreIfAlreadyDone`)   | java.lang.String                             | OPTIONAL |
| role          | The role created (real Camunda 8 client object, not a map)                     | io.camunda.client.api.search.response.Role  | OPTIONAL |
| dateOperation | Date when the operation is performed                                            | java.util.Date                               | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| CREATE_ROLE         | Create role failed                                                  |

# Add in role

`identityFunction = add-in-role`. Adds members to a role in one call. `roleMembers` is a
single FEEL context object listing every member to add, per category - all non-empty lists are
processed, calling the matching `CamundaClient.newAssignRoleTo...Command()` once per id. The role
is first looked up by `roleId`; if it does not exist (or more than one match is returned), the
function fails with `ROLE_NOT_FOUND` before any member is added:

```
={users: ["walter.bates"], groups: ["supervisor"], client: [], mappingRule: []}
```

## Inputs

| Name                | Description                                                                                                       | Class             | Default | Level    |
|---------------------|-------------------------------------------------------------------------------------------------------------------------|-------------------|---------|----------|
| roleId              | The role to add the members to                                                                                           | java.lang.String  |         | REQUIRED |
| roleMembers         | `{users: [...], groups: [...], client: [...], mappingRule: [...]}` - usernames/group ids/client ids/mapping rule ids to add | `RoleMembers`     |         | REQUIRED |
| ignoreIfAlreadyDone | If true (default), do not throw a BPMN error when a member is already in the role - skips it instead                    | java.lang.Boolean | true    | REQUIRED |

## Output

| Name          | Description                                                   | Class                                        | Level    |
|---------------|--------------------------------------------------------------------|-------------------------------------------------|----------|
| status        | Status of operation                                                 | java.lang.String                                | OPTIONAL |
| role          | The role the members were added to (real Camunda 8 client object)  | io.camunda.client.api.search.response.Role     | OPTIONAL |
| dateOperation | Date when the operation is performed                                | java.util.Date                                  | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| ROLE_NOT_FOUND      | No role found with the id provided                                   |
| ADD_IN_ROLE         | Adding a member to the role failed                                  |

# Get content role

`identityFunction = get-content-role`. Returns the content of a role in one call. The role is
first looked up by `roleId` (fails with `ROLE_NOT_FOUND` if it does not exist, or more than one
match is returned). `roleContentFilter` is a single FEEL context object selecting which
categories to return - every category set to `true` is populated in the matching output:

```
={users: true, groups: true, client: false, mappingRule: false}
```

| Category      | Calls                                                        | Output populated |
|---------------|-----------------------------------------------------------------|-------------------|
| `users`       | `CamundaClient.newUsersByRoleSearchRequest(roleId)`, then resolves each username to a full `User` | `listUsers` (`List<User>`) |
| `groups`      | `CamundaClient.newGroupsByRoleSearchRequest(roleId)`, then resolves each groupId to a full `Group` | `listGroups` (`List<Group>`) |
| `mappingRule` | `CamundaClient.newMappingRulesByRoleSearchRequest(roleId)`       | `listMappingRules` (`List<MappingRule>`) |
| `client`      | `CamundaClient.newClientsByRoleSearchRequest(roleId)`            | `listClientId` (`List<Client>`) |

All lists hold the real Camunda 8 client response objects (not maps).

## Inputs

| Name              | Description                                                                        | Class               | Default | Level    |
|-------------------|-------------------------------------------------------------------------------------------------------------------------------|---------------------|---------|----------|
| roleId            | The role to read                                                                   | java.lang.String    |         | REQUIRED |
| roleContentFilter | `{users: true\|false, groups: true\|false, client: true\|false, mappingRule: true\|false}` | `RoleContentFilter` |         | REQUIRED |

## Output

| Name             | Description                                    | Class                                        | Level    |
|------------------|---------------------------------------------------|-------------------------------------------------|----------|
| status           | Status of operation                                | java.lang.String                               | OPTIONAL |
| role             | The role read (real Camunda 8 client object)       | io.camunda.client.api.search.response.Role    | OPTIONAL |
| listUsers        | Members of the role (`roleContentFilter.users`)    | java.util.List (`List<User>`)                  | OPTIONAL |
| listGroups       | Groups of the role (`roleContentFilter.groups`)    | java.util.List (`List<Group>`)                 | OPTIONAL |
| listMappingRules | Mapping rules of the role (`roleContentFilter.mappingRule`) | java.util.List (`List<MappingRule>`)  | OPTIONAL |
| listClientId     | Clients of the role (`roleContentFilter.client`)   | java.util.List (`List<Client>`)                | OPTIONAL |
| dateOperation    | Date when the operation is performed               | java.util.Date                                 | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| ROLE_NOT_FOUND      | No role found with the id provided                                   |
| GET_CONTENT_ROLE    | During the read of the role content                                  |

# Search role

`identityFunction = search-role`. Calls `CamundaClient.newRolesSearchRequest()` filtered by a
wildcard (`like`) match on `roleNameSearch`, paginated by `pageNumber`/`pageSize`. The only
filterable fields on a role are `roleId` and `name` - there is no date-based filter, and no
"find all roles a given username belongs to" reverse search (only the forward
`newUsersByRoleSearchRequest` from a known `roleId` exists, see Get content role above).

## Inputs

| Name           | Description                                                                                      | Class             | Default | Level    |
|----------------|-------------------------------------------------------------------------------------------------------|-------------------|---------|----------|
| roleNameSearch | Wildcard `like` match on the role name: `*` zero/one/multiple chars, `?` a single char                  | java.lang.String  |         | REQUIRED |
| pageNumber     | Page number (1-based)                                                                                    | java.lang.Integer |         | REQUIRED |
| pageSize       | Page size                                                                                                 | java.lang.Integer |         | REQUIRED |

## Output

| Name      | Description                                                         | Class            | Level    |
|-----------|--------------------------------------------------------------------------|------------------|----------|
| status    | Status of operation                                                       | java.lang.String | OPTIONAL |
| listRoles | List of roles found by the search (real Camunda 8 client objects)        | java.util.List   | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| SEARCH_ROLE         | During search role(s) in Camunda 8 Identity                         |

# Delete in role

`identityFunction = delete-in-role`. Removes members from a role in one call, using the same
`roleMembers` object as Add in role - all non-empty lists are processed, calling the matching
`CamundaClient.newUnassignRoleFrom...Command()` once per id. The role is first looked up by
`roleId`; if it does not exist (or more than one match is returned), the function fails with
`ROLE_NOT_FOUND` before any member is removed.

## Inputs

| Name                | Description                                                                                                                      | Class             | Default | Level    |
|---------------------|-----------------------------------------------------------------------------------------------------------------------------------------|-------------------|---------|----------|
| roleId              | The role to remove the members from                                                                                                       | java.lang.String  |         | REQUIRED |
| roleMembers         | `{users: [...], groups: [...], client: [...], mappingRule: [...]}` - usernames/group ids/client ids/mapping rule ids to remove            | `RoleMembers`     |         | REQUIRED |
| ignoreIfAlreadyDone | If true (default), do not throw a BPMN error when a member is already not in the role - skips it instead                                 | java.lang.Boolean | true    | REQUIRED |

## Output

| Name          | Description                                                       | Class                                        | Level    |
|---------------|-------------------------------------------------------------------------|-------------------------------------------------|----------|
| status        | Status of operation                                                      | java.lang.String                                | OPTIONAL |
| role          | The role the members were removed from (real Camunda 8 client object)   | io.camunda.client.api.search.response.Role     | OPTIONAL |
| dateOperation | Date when the operation is performed                                     | java.util.Date                                  | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| ROLE_NOT_FOUND      | No role found with the id provided                                   |
| DELETE_IN_ROLE      | Removing a member from the role failed                               |

# Delete role

`identityFunction = delete-role`. Calls `CamundaClient.newDeleteRoleCommand(roleId)`.

## Inputs

| Name                | Description                                                                                                                    | Class             | Default | Level    |
|---------------------|------------------------------------------------------------------------------------------------------------------------------------|-------------------|---------|----------|
| roleId              | Role id to delete                                                                                                                     | java.lang.String  |         | REQUIRED |
| ignoreIfAlreadyDone | If true (default), do not throw a BPMN error when the role does not exist anymore - returns status `ALREADY_DELETED` instead         | java.lang.Boolean | true    | REQUIRED |

## Output

| Name          | Description                                                                 | Class            | Level    |
|---------------|--------------------------------------------------------------------------------|------------------|----------|
| status        | Status of operation (`SUCCESS`, or `ALREADY_DELETED` if `ignoreIfAlreadyDone`) | java.lang.String | OPTIONAL |
| dateOperation | Date when the operation is performed                                           | java.util.Date   | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| DELETE_ROLE         | During delete role in Camunda 8 Identity                             |

# Assign client

`identityFunction = assign-client`. Assigns a client to a role, a group and/or a tenant in one
call - each of `roleId`/`groupId`/`tenantId` that is set triggers its own assignment:
`CamundaClient.newAssignRoleToClientCommand()`, `newAssignClientToGroupCommand()`,
`newAssignClientToTenantCommand()`.

## Inputs

| Name                | Description                                                                                             | Class             | Default | Level    |
|---------------------|---------------------------------------------------------------------------------------------------------------|-------------------|---------|----------|
| clientId            | The client to assign                                                                                            | java.lang.String  |         | REQUIRED |
| roleId              | Role to assign the client to                                                                                    | java.lang.String  |         | OPTIONAL |
| groupId             | Group to assign the client to                                                                                   | java.lang.String  |         | OPTIONAL |
| tenantId            | Tenant to assign the client to                                                                                  | java.lang.String  |         | OPTIONAL |
| ignoreIfAlreadyDone | If true (default), do not throw a BPMN error when the client is already assigned - skips it instead             | java.lang.Boolean | true    | REQUIRED |

## Output

| Name   | Description          | Class            | Level    |
|--------|------------------------|------------------|----------|
| status | Status of operation    | java.lang.String | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| ASSIGN_CLIENT       | Assigning the client failed                                         |

# Unassign client

`identityFunction = unassign-client`. Unassigns a client from a role, a group and/or a tenant in
one call - each of `roleId`/`groupId`/`tenantId` that is set triggers its own unassignment:
`CamundaClient.newUnassignRoleFromClientCommand()`, `newUnassignClientFromGroupCommand()`,
`newUnassignClientFromTenantCommand()`.

## Inputs

| Name                | Description                                                                                               | Class             | Default | Level    |
|---------------------|-----------------------------------------------------------------------------------------------------------------|-------------------|---------|----------|
| clientId            | The client to unassign                                                                                           | java.lang.String  |         | REQUIRED |
| roleId              | Role to unassign the client from                                                                                 | java.lang.String  |         | OPTIONAL |
| groupId             | Group to unassign the client from                                                                                | java.lang.String  |         | OPTIONAL |
| tenantId            | Tenant to unassign the client from                                                                               | java.lang.String  |         | OPTIONAL |
| ignoreIfAlreadyDone | If true (default), do not throw a BPMN error when the client is already not assigned - skips it instead          | java.lang.Boolean | true    | REQUIRED |

## Output

| Name   | Description          | Class            | Level    |
|--------|------------------------|------------------|----------|
| status | Status of operation    | java.lang.String | OPTIONAL |

## BPMN Errors

| Name                | Explanation                                                       |
|---------------------|--------------------------------------------------------------------|
| IDENTITY_CONNECTION | Error arrived during the connection to the Camunda 8 cluster       |
| UNKNOWN_FUNCTION    | The function is unknown. There is a limited number of operations   |
| UNASSIGN_CLIENT     | Unassigning the client failed                                       |

# Build

```bash
mvn clean package
```

Two jars are produced. The jar with all dependencies can be uploaded in the [Cherry Framework](https://github.com/camunda-community-hub/zeebe-cherry-framework)

## Element Template

The element template can be found in the [element-templates](/element-templates/identity-function.json) directory.
