# Test

## TestParameters

This JUnit test verifies parameters show up correctly so the Cherry Runtime can generate the
element-template correctly.

## Identity Direct Test

Run the class `io.camunda.identity.IdentityDirectTest` (it has a `main`, not JUnit tests, since it
needs a running Camunda 8 cluster with the Identity component enabled).

Edit the file and change the constants:

```java
public static final String CAMUNDA_REST_ADDRESS = "http://localhost:8080";
public static final String CAMUNDA_GRPC_ADDRESS = "http://localhost:26500";
```

* `CAMUNDA_REST_ADDRESS` / `CAMUNDA_GRPC_ADDRESS` are the addresses of the Camunda 8 cluster (Zeebe
  gateway) to connect to. This must be a cluster where the Identity component is enabled, and the
  user calling the API must have the permission to manage users.

Run the class. It will create a user, update it, search for it, and delete it.
