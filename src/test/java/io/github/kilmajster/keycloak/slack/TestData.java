package io.github.kilmajster.keycloak.slack;

import java.util.UUID;

public interface TestData {

    String TEST_HOST = "http://localhost:8080";
    String TEST_REALM_NAME = "realm-name";
    String TEST_MESSAGE_TITLE = "message-title";
    String TEST_SLACK_TOKEN = "slack-token";

    String SLACK_LISTENER_ID = "slack";
    int DOCKER_KEYCLOAK_PORT = 8080;
    String DOCKER_KEYCLOAK_URL = "http://localhost:" + DOCKER_KEYCLOAK_PORT;
    String DOCKER_COMPOSE_FILENAME = "docker-compose.yml";
    String DOCKER_KEYCLOAK_SERVICE = "keycloak";
    String DOCKER_KEYCLOAK_REALM = "master";
    String DOCKER_KEYCLOAK_USERNAME = "admin";
    String DOCKER_KEYCLOAK_PASSWORD = "admin";
    String DOCKER_KEYCLOAK_ADMIN_CLIENT_ID = "admin-cli";

    String TEST_USERNAME = "username" + System.currentTimeMillis();
    String TEST_PASSWORD = UUID.randomUUID().toString();
}
