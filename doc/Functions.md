# Identity function

Camunda 8 Identity operation (create/search/update/delete user)


### Inputs
| Name                                    | Description            | Class                                                     | Level    |
|-----------------------------------------|------------------------|-----------------------------------------------------------|----------|
| identityFunction                        | Identity Function      | java.lang.String                                          | REQUIRED |
| userName                                | User Name              | java.lang.String                                          | REQUIRED |
| userDisplayName                         | Name                   | java.lang.String                                          | OPTIONAL |
| userEmail                               | Email                  | java.lang.String                                          | OPTIONAL |
| userPassword                            | Password               | java.lang.String                                          | OPTIONAL |
| searchByUserName                        | Search by User Name    | java.lang.String                                          | OPTIONAL |
| searchByUserDisplayName                 | Search by Name         | java.lang.String                                          | OPTIONAL |
| searchByUserEmail                       | Search by Email        | java.lang.String                                          | OPTIONAL |
| searchByGroupId                         | Search by Group Id     | java.lang.String                                          | OPTIONAL |
| searchByRoleId                          | Search by Role Id      | java.lang.String                                          | OPTIONAL |
| groupId                                 | Group Id               | java.lang.String                                          | REQUIRED |
| groupName                               | Group Name             | java.lang.String                                          | OPTIONAL |
| groupDescription                        | Group Description      | java.lang.String                                          | OPTIONAL |
| groupNameSearch                         | Group Name Filter      | java.lang.String                                          | REQUIRED |
| pageNumber                              | Page number            | java.lang.Integer                                         | REQUIRED |
| pageSize                                | Page size              | java.lang.Integer                                         | REQUIRED |
| groupMembers                            | Members                | org.camunda.connector.identity.toolbox.GroupMembers       | REQUIRED |
| groupContentFilter                      | Content filter         | org.camunda.connector.identity.toolbox.GroupContentFilter | REQUIRED |
| roleId                                  | Role Id                | java.lang.String                                          | REQUIRED |
| roleName                                | Role Name              | java.lang.String                                          | OPTIONAL |
| roleDescription                         | Role Description       | java.lang.String                                          | REQUIRED |
| roleNameSearch                          | Role Name filter       | java.lang.String                                          | REQUIRED |
| roleMembers                             | Members                | org.camunda.connector.identity.toolbox.RoleMembers        | REQUIRED |
| roleContentFilter                       | Content filter         | org.camunda.connector.identity.toolbox.RoleContentFilter  | REQUIRED |
| clientId                                | Client Id              | java.lang.String                                          | REQUIRED |
| tenantId                                | Tenant Id              | java.lang.String                                          | OPTIONAL |
| authorizationOwnerType                  | Owner Type             | java.lang.String                                          | REQUIRED |
| authorizationOwner                      | Owner                  | java.lang.String                                          | REQUIRED |
| authorizationResourceType               | Resource Type          | java.lang.String                                          | REQUIRED |
| authorizationResourceId                 | Resource Id            | java.lang.String                                          | REQUIRED |
| authorizationPermission                 | Permission             | java.lang.Object                                          | REQUIRED |
| authorizationSearchOwnerId              | Owner Id               | java.lang.String                                          | OPTIONAL |
| authorizationSearchOwnerType            | Owner Type             | java.lang.String                                          | OPTIONAL |
| authorizationSearchResourceType         | Resource Type          | java.lang.Object                                          | OPTIONAL |
| authorizationSearchResourceId           | Resource Id            | java.lang.Object                                          | OPTIONAL |
| authorizationSearchResourcePropertyName | Resource Property Name | java.lang.Object                                          | OPTIONAL |
| authorizationKey                        | Authorization Key      | java.lang.String                                          | REQUIRED |
| waitExport                              | Wait export            | java.lang.Long                                            | OPTIONAL |
| ignoreIfAlreadyDone                     | Ignore if already done | java.lang.Boolean                                         | REQUIRED |



### Outputs
| Name                    | Description               | Class                                       | Level    |
|-------------------------|---------------------------|---------------------------------------------|----------|
| status                  | Status                    | java.lang.String                            | OPTIONAL |
| user                    | User                      | io.camunda.client.api.search.response.User  | OPTIONAL |
| dateOperation           | Date of operation         | java.util.Date                              | OPTIONAL |
| userNameDeleted         | User Name Deleted         | java.lang.String                            | OPTIONAL |
| listUsers               | List Users                | java.util.List                              | OPTIONAL |
| group                   | Group                     | io.camunda.client.api.search.response.Group | OPTIONAL |
| listGroups              | List Groups               | java.util.List                              | OPTIONAL |
| listRoles               | List Roles                | java.util.List                              | OPTIONAL |
| listMappingRules        | List Mapping Rules        | java.util.List                              | OPTIONAL |
| listClientId            | List Client Id            | java.util.List                              | OPTIONAL |
| groupIdDeleted          | Group Id Deleted          | java.lang.String                            | OPTIONAL |
| role                    | Role                      | io.camunda.client.api.search.response.Role  | OPTIONAL |
| roleIdDeleted           | Role Id Deleted           | java.lang.String                            | OPTIONAL |
| authorizationKey        | Authorization Key         | java.lang.String                            | OPTIONAL |
| listAuthorizations      | List Authorizations       | java.util.List                              | OPTIONAL |
| authorizationKeyDeleted | Authorization Key Deleted | java.lang.String                            | OPTIONAL |



### Errors
| Name                 | Explanation                                                      |
|----------------------|------------------------------------------------------------------|
| DELETE_IN_GROUP      | Removing a member from the group failed                          |
| CREATE_AUTHORIZATION | Create authorization failed                                      |
| CREATE_USER          | Create user failed                                               |
| UNKNOWN_PERMISSION   | One of the given permissions is not a valid Camunda 8 permission |
| ADD_IN_GROUP         | Adding a member to the group failed                              |
| UPDATE_ROLE          | During update role in Camunda 8 Identity                         |
| GET_CONTENT_GROUP    | During the read of the group content                             |
| ROLE_NOT_FOUND       | No role found with the id provided                               |
| USER_ALREADY_EXIST   | The username already exists in Camunda 8 Identity                |
| CREATE_GROUP         | Create group failed                                              |
| UPDATE_GROUP         | During update group in Camunda 8 Identity                        |
| SEARCH_ROLE          | During search role(s) in Camunda 8 Identity                      |
| SEARCH_GROUP         | During search group(s) in Camunda 8 Identity                     |
| ADD_IN_ROLE          | Adding a member to the role failed                               |
| IDENTITY_CONNECTION  | Error arrived during the connection to the Camunda 8 cluster     |
| DELETE_GROUP         | During delete group in Camunda 8 Identity                        |
| DELETE_IN_ROLE       | Removing a member from the role failed                           |
| DELETE_USER          | During delete user in Camunda 8 Identity                         |
| UPDATE_USER          | During update user in Camunda 8 Identity                         |
| UNASSIGN_CLIENT      | Unassigning the client failed                                    |
| GROUP_NOT_FOUND      | No group found with the id provided                              |
| SEARCH_USER          | During search user(s) in Camunda 8 Identity                      |
| DELETE_ROLE          | During delete role in Camunda 8 Identity                         |
| DELETE_AUTHORIZATION | During delete authorization in Camunda 8 Identity                |
| CREATE_ROLE          | Create role failed                                               |
| SEARCH_AUTHORIZATION | During search authorization(s) in Camunda 8 Identity             |
| GET_CONTENT_ROLE     | During the read of the role content                              |
| UNKNOWN_FUNCTION     | The function is unknown. There is a limited number of operation  |
| ASSIGN_CLIENT        | Assigning the client failed                                      |

