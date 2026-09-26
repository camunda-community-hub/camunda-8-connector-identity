package org.camunda.identity;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.CreateUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// How to run? See README.md on the project
// This class is a main and not a list of test because it's not possible to run it at each package: we need
// a running Camunda 8 cluster (with the Identity component enabled), reachable with the standard
// CamundaClient environment configuration (CAMUNDA_CLIENT_* / ZEEBE_* environment variables)

public class IdentityDirectTest {

    private final static Logger logger = LoggerFactory.getLogger(IdentityDirectTest.class.getName());

    /**
     * This class is a main and not a list of test because it's not possible to run it at each package: we need
     * a Camunda 8 cluster active, with the Identity component reachable.
     *
     * @param args args of application
     */
    public static void main(String[] args) {
        try (CamundaClient camundaClient = CamundaClient.newClientBuilder().build()) {
            playUserIdentityOperation(camundaClient);
        }
        logger.info("That's all folks");
    }

    /**
     * Play with user: create, search, update, delete
     */
    public static void playUserIdentityOperation(CamundaClient camundaClient) {
        CreateUserResponse created = camundaClient.newCreateUserCommand()
                .username("walter.bates")
                .name("Walter Bates")
                .email("walter.bates@camunda.com")
                .password("bpm")
                .execute();
        logger.info("User created: {}", created.getUsername());

        camundaClient.newUpdateUserCommand("walter.bates")
                .name("Walter Bates Updated")
                .email("walter.bates2@camunda.com")
                .execute();

        camundaClient.newUsersSearchRequest()
                .filter(f -> f.username("walter.bates"))
                .execute()
                .items()
                .forEach(user -> logger.info("Found user: {}", user.getUsername()));

        camundaClient.newDeleteUserCommand("walter.bates").execute();
    }
}
