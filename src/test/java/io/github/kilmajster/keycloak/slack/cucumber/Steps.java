package io.github.kilmajster.keycloak.slack.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.slack.api.Slack;
import com.slack.api.model.Conversation;
import com.slack.api.model.Message;
import com.slack.api.model.block.ContextBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.events.EventType;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmEventsConfigRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.openqa.selenium.By;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.ws.rs.core.Response;
import java.io.File;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static io.github.kilmajster.keycloak.slack.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class Steps {

    private static final String SLACK_TOKEN = System.getProperty("slack.token");
    private static final String SLACK_CHANNEL = System.getProperty("slack.channel");

    private static final DockerComposeContainer<?> composeContainer = new DockerComposeContainer<>(
            new File(DOCKER_COMPOSE_FILENAME))
            .withEnv("SLACK_TOKEN", SLACK_TOKEN)
            .withEnv("SLACK_CHANNEL", SLACK_CHANNEL)
            .withExposedService(DOCKER_KEYCLOAK_SERVICE, DOCKER_KEYCLOAK_PORT, Wait.forHttp("/"))
            .withLogConsumer(DOCKER_KEYCLOAK_SERVICE, new Slf4jLogConsumer(log));

    private final Keycloak keycloak = Keycloak.getInstance(
            DOCKER_KEYCLOAK_URL,
            DOCKER_KEYCLOAK_REALM,
            DOCKER_KEYCLOAK_USERNAME,
            DOCKER_KEYCLOAK_PASSWORD,
            DOCKER_KEYCLOAK_ADMIN_CLIENT_ID
    );

    @BeforeAll
    public static void startDocker() {
        if (!isContainerRunning()) {
            composeContainer.start();
        }
        log.info("Docker started!");
    }

    @AfterAll
    public static void stopDocker() {
        if (isContainerRunning()) {
            composeContainer.stop();
        }
        log.info("Docker stopped!");
    }

    @Given("Keycloak is up and running")
    public void keycloak_is_up_and_running() {
        keycloak.serverInfo();

        log.info("Keycloak is up and running!");
    }

    private static boolean isContainerRunning() {
        Optional<?> keycloak = composeContainer.getContainerByServiceName(DOCKER_KEYCLOAK_SERVICE);
        return keycloak.isPresent() && ((ContainerState) keycloak.get()).isRunning();
    }

    @Given("Slack event listener is enabled")
    public void slack_event_listener_is_enabled() {
        List<String> enabledEventListeners = keycloak.realm(DOCKER_KEYCLOAK_REALM).getRealmEventsConfig().getEventsListeners();

        if (!enabledEventListeners.contains(SLACK_LISTENER_ID)) {
            enabledEventListeners.add(SLACK_LISTENER_ID);

            RealmEventsConfigRepresentation eventsConfig = new RealmEventsConfigRepresentation();
            eventsConfig.setEventsListeners(enabledEventListeners);

            keycloak.realm(DOCKER_KEYCLOAK_REALM).updateRealmEventsConfig(eventsConfig);
            log.info("Slack event listener enabled!");
        }
    }

    @Given("fresh test user is generated")
    public void fresh_test_user_id_generated() {
        CredentialRepresentation creds = new CredentialRepresentation();
        creds.setTemporary(false);
        creds.setType(CredentialRepresentation.PASSWORD);
        creds.setValue(TEST_PASSWORD);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(TEST_USERNAME);
        user.setEnabled(true);
        user.setCredentials(Collections.singletonList(creds));

        Response response = keycloak.realm(DOCKER_KEYCLOAK_REALM)
                .users()
                .create(user);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CREATED);
    }

    @When("user navigates to account console page")
    public void user_navigates_to_account_console_page() {
        open(DOCKER_KEYCLOAK_URL + "/realms/" + DOCKER_KEYCLOAK_REALM + "/account");
    }

    @When("user clicks a sign in button")
    public void click_sign_in_button() {
        $(By.id("landingSignInButton")).click();
    }

    @Then("login form should be displayed")
    public void login_form_should_be_displayed() {
        assertThat(
                $(By.id("kc-form-login")).isDisplayed()
        ).isTrue();
    }

    @When("user log into account console with valid credentials")
    public void log_into_account_console() {
        $(By.id("username")).val(TEST_USERNAME);
        $(By.id("password")).val(TEST_PASSWORD);
        $(By.id("kc-login")).click();
    }

    @Then("user should be logged into account console")
    public void verify_that_user_is_logged_in() {
        $(By.id("landingLoggedInUser")).shouldHave(text(TEST_USERNAME));
    }

    @Then("message with LOGIN event was delivered to Slack")
    @SneakyThrows
    public void slack_message_was_sent() {
        String channelId = findChannelId();
        log.info("Found channel id = {}", channelId);

        Message latestSlackMessage = findLatestSlackMessage(channelId);
        log.info("Fetched latest Slack message = {}", latestSlackMessage);

        ObjectNode details = extractEventDetails(latestSlackMessage);
        long time = details.get("time").asLong();
        String type = details.get("type").asText();

        assertThat(new Timestamp(time)).isEqualToIgnoringSeconds(new Date());
        assertThat(type).isEqualTo(EventType.LOGIN.name());
        assertThat(latestSlackMessage.toString()).contains(TEST_USERNAME);
    }

    @SneakyThrows
    private ObjectNode extractEventDetails(Message message) {
        String markdownDetailsText = ((MarkdownTextObject) ((ContextBlock) message
                .getBlocks()
                .stream()
                .filter(layoutBlock -> layoutBlock.getClass().equals(ContextBlock.class))
                .findAny()
                .orElseThrow())
                .getElements()
                .get(0))
                .getText();

        String unmarkdownedDetails = StringUtils.remove(markdownDetailsText, "```");
        return new ObjectMapper().readValue(unmarkdownedDetails, ObjectNode.class);
    }

    @SneakyThrows
    private Message findLatestSlackMessage(String id) {
        return Slack.getInstance()
                .methods()
                .conversationsHistory(r -> r
                        .token(SLACK_TOKEN)
                        .channel(id)
                        .limit(1)
                ).getMessages().get(0);
    }

    @SneakyThrows
    private String findChannelId() {
        return Slack.getInstance()
                .methods()
                .conversationsList(r -> r.token(SLACK_TOKEN))
                .getChannels()
                .stream()
                .filter(channel -> channel.getName().equals(SLACK_CHANNEL))
                .map(Conversation::getId)
                .findAny()
                .orElseThrow();
    }
}
