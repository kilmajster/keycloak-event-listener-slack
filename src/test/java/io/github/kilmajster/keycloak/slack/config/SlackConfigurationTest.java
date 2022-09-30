package io.github.kilmajster.keycloak.slack.config;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.OperationType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;

class SlackConfigurationTest {

    @Test
    void getConfig_should_return_config_with_token_when_SLACK_TOKEN_env_is_present() throws Exception {
        final String token = "slack-token";

        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_TOKEN",
                token
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.getToken()
        ).isEqualTo(token);
    }

    @Test
    void getConfig_should_return_config_with_null_token_when_SLACK_TOKEN_env_is_not_present() {
        final SlackConfiguration slackConfiguration = SlackConfiguration.getConfig();

        assertThat(
                slackConfiguration.getToken()
        ).isNull();
    }

    @Test
    void getConfig_should_return_config_with_channel_when_SLACK_CHANNEL_env_is_present() throws Exception {
        final String channel = "slack-channel";

        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_CHANNEL",
                channel
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.getChannel()
        ).isEqualTo(channel);
    }

    @Test
    void getConfig_should_return_config_with_null_channel_when_SLACK_CHANNEL_env_is_not_present() {
        final SlackConfiguration slackConfiguration = SlackConfiguration.getConfig();

        assertThat(
                slackConfiguration.getChannel()
        ).isNull();
    }

    @Test
    void getConfig_should_return_events_list_from_SLACK_INCLUDE_EVENTS_env() throws Exception {
        final String slackIncludeEvents = "LOGIN, LOGIN_ERROR,CODE_TO_TOKEN,REFRESH_TOKEN,    RESET_PASSWORD, SEND_IDENTITY_PROVIDER_LINK";

        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_INCLUDE_EVENTS",
                slackIncludeEvents
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.getSupportedEvents()
        ).containsExactlyInAnyOrder(
                EventType.LOGIN,
                EventType.LOGIN_ERROR,
                EventType.CODE_TO_TOKEN,
                EventType.REFRESH_TOKEN,
                EventType.RESET_PASSWORD,
                EventType.SEND_IDENTITY_PROVIDER_LINK
        );
    }

    @Test
    void getConfig_should_return_events_list_from_SLACK_INCLUDE_EVENTS_env_when_single_event_type_is_provided() throws Exception {
        final String slackIncludeEvents = "LOGIN";

        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_INCLUDE_EVENTS",
                slackIncludeEvents
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.getSupportedEvents()
        ).containsExactlyInAnyOrder(
                EventType.LOGIN
        );
    }

    @Test
    void getConfig_should_return_events_list_from_SLACK_INCLUDE_EVENTS_env_when_one_of_events_is_invalid() throws Exception {
        final String slackIncludeEvents = "LOGIN, not-existing-event-type :(   , LOGIN_ERROR";

        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_INCLUDE_EVENTS",
                slackIncludeEvents
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.getSupportedEvents()
        ).containsExactlyInAnyOrder(
                EventType.LOGIN,
                EventType.LOGIN_ERROR
        );
    }

    @Test
    void getConfig_should_return_all_possible_event_types_when_SLACK_INCLUDE_ALL_EVENTS_env_is_true() throws Exception {
        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_INCLUDE_ALL_EVENTS",
                "true"
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.getSupportedEvents()
        ).containsExactlyInAnyOrder(
                EventType.values()
        );
    }

    @Test
    void getConfig_should_return_empty_event_types_list_when_SLACK_INCLUDE_ALL_EVENTS_env_boolean_value_is_invalid() throws Exception {
        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_INCLUDE_ALL_EVENTS",
                ",,,for sure not boolean value :("
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.getSupportedEvents()
        ).isEmpty();
    }

    @Test
    void getConfig_should_return_all_possible_event_types_without_exceptions_when_SLACK_SLACK_INCLUDE_ALL_EVENTS_EXCEPT_env_is_present() throws Exception {
        final String slackEventsExceptions = "LOGIN, LOGIN_ERROR,CODE_TO_TOKEN,REFRESH_TOKEN,    RESET_PASSWORD, SEND_IDENTITY_PROVIDER_LINK";

        final Collection<EventType> expectedSupportedEvents = CollectionUtils.removeAll(
                List.of(EventType.values()),
                List.of(EventType.LOGIN,
                        EventType.LOGIN_ERROR,
                        EventType.CODE_TO_TOKEN,
                        EventType.REFRESH_TOKEN,
                        EventType.RESET_PASSWORD,
                        EventType.SEND_IDENTITY_PROVIDER_LINK
                )
        );

        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_INCLUDE_ALL_EVENTS_EXCEPT",
                slackEventsExceptions
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.getSupportedEvents()
        ).containsExactlyInAnyOrderElementsOf(
                expectedSupportedEvents
        );
    }

    @Test
    void getConfig_should_return_all_possible_error_events_when_SLACK_INCLUDE_ALL_ERRORS_env_is_true() throws Exception {
        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_INCLUDE_ALL_ERRORS",
                "true"
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.getSupportedEvents()
        ).containsExactlyInAnyOrderElementsOf(
                Arrays.stream(EventType.values())
                        .filter(eventType -> eventType.name().contains("_ERROR"))
                        .collect(Collectors.toList())
        );
    }

    @Test
    void getConfig_should_return_error_events_without_exceptions_when_SLACK_INCLUDE_ALL_ERRORS_EXCEPT_env_is_present() throws Exception {
        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_INCLUDE_ALL_ERRORS_EXCEPT",
                "LOGIN_ERROR, UPDATE_PASSWORD_ERROR"
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.getSupportedEvents()
        ).containsExactlyInAnyOrderElementsOf(
                Arrays.stream(EventType.values())
                        .filter(eventType -> eventType.name().contains("_ERROR"))
                        .filter(other -> !EventType.LOGIN_ERROR.equals(other))
                        .filter(other -> !EventType.UPDATE_PASSWORD_ERROR.equals(other))
                        .collect(Collectors.toList())
        );
    }

    @Test
    void getConfig_should_return_error_events_without_exceptions_when_SLACK_INCLUDE_ALL_ERRORS_EXCEPT_env_is_present_and_list_contains_invalid_types() throws Exception {
        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_INCLUDE_ALL_ERRORS_EXCEPT",
                "LOGIN_ERROR, UPDATE_PASSWORD_ERROR ,, ,,.,.,.,.,.,,    blah-blah 123 :/"
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.getSupportedEvents()
        ).containsExactlyInAnyOrderElementsOf(
                Arrays.stream(EventType.values())
                        .filter(eventType -> eventType.name().contains("_ERROR"))
                        .filter(other -> !EventType.LOGIN_ERROR.equals(other))
                        .filter(other -> !EventType.UPDATE_PASSWORD_ERROR.equals(other))
                        .collect(Collectors.toList())
        );
    }

    @Test
    void getConfig_should_return_include_event_representation_true_by_default() {
        final SlackConfiguration slackConfiguration = SlackConfiguration.getConfig();

        assertThat(
                slackConfiguration.isIncludeEventRepresentationEnabled()
        ).isTrue();
    }

    @Test
    void getConfig_should_return_include_event_representation_false_when_SLACK_INCLUDE_EVENT_REPRESENTATION_env_is_false() throws Exception {
        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_INCLUDE_EVENT_REPRESENTATION",
                "false"
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.isIncludeEventRepresentationEnabled()
        ).isFalse();
    }

    @Test
    void getConfig_should_return_config_with_supported_admin_events_when_SLACK_INCLUDE_ADMIN_EVENTS_env_is_present() throws Exception {
        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_INCLUDE_ADMIN_EVENTS",
                "CREATE, UPDATE"
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.getSupportedAdminEvents()
        ).containsExactlyInAnyOrder(
                OperationType.CREATE,
                OperationType.UPDATE
        );
    }

    @Test
    void getConfig_should_return_config_with_supported_admin_events_when_SLACK_INCLUDE_ADMIN_EVENTS_env_contains_invalid_values() throws Exception {
        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_INCLUDE_ADMIN_EVENTS",
                "CREATE, ACTION  ,  ,, ././,,, invalid admin acti0n"
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.getSupportedAdminEvents()
        ).containsExactlyInAnyOrder(
                OperationType.CREATE,
                OperationType.ACTION
        );
    }

    @Test
    void getConfig_should_return_include_admin_event_representation_true_by_default() {
        final SlackConfiguration slackConfiguration = SlackConfiguration.getConfig();

        assertThat(
                slackConfiguration.isIncludeAdminEventRepresentationEnabled()
        ).isTrue();
    }

    @Test
    void getConfig_should_return_include_event_representation_false_when_SLACK_INCLUDE_ADMIN_EVENT_REPRESENTATION_env_is_false() throws Exception {
        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_INCLUDE_ADMIN_EVENT_REPRESENTATION",
                "false"
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.isIncludeAdminEventRepresentationEnabled()
        ).isFalse();
    }

    @Test
    void getConfig_should_return_include_all_admin_events_when_SLACK_INCLUDE_ALL_ADMIN_EVENTS_env_is_true() throws Exception {
        final SlackConfiguration slackConfiguration = withEnvironmentVariable(
                "SLACK_INCLUDE_ALL_ADMIN_EVENTS",
                "true"
        ).execute(SlackConfiguration::getConfig);

        assertThat(
                slackConfiguration.getSupportedAdminEvents()
        ).containsExactlyInAnyOrder(
                OperationType.values()
        );
    }
}
