package io.github.kilmajster.keycloak.slack.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.OperationType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Value
@AllArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
public class SlackConfiguration {

    String token;
    String channel;
    List<EventType> supportedEvents;
    boolean includeEventRepresentationEnabled;
    List<OperationType> supportedAdminEvents;
    boolean includeAdminEventRepresentationEnabled;

    public static SlackConfiguration getConfig() {
        return SlackConfiguration.of(
                EnvironmentVariableConfigReader.getToken(),
                EnvironmentVariableConfigReader.getChannel(),
                EnvironmentVariableConfigReader.getSupportedEvents(),
                EnvironmentVariableConfigReader.isIncludeEventRepresentationEnabled(),
                EnvironmentVariableConfigReader.getSupportedAdminEvents(),
                EnvironmentVariableConfigReader.isIncludeAdminEventRepresentationEnabled()
        );
    }

    private static final class EnvironmentVariableConfigReader {

        private static final boolean DEFAULT_SLACK_INCLUDE_REPRESENTATION = true;
        private static final String EVENT_LIST_SEPARATOR = ",";

        // token stuff
        private static final String ENV_SLACK_TOKEN = "SLACK_TOKEN";

        public static String getToken() {
            return System.getenv(ENV_SLACK_TOKEN);
        }

        // channel stuff
        private final static String EVN_SLACK_CHANNEL = "SLACK_CHANNEL";

        public static String getChannel() {
            return System.getenv(EVN_SLACK_CHANNEL);
        }

        // user events & error events stuff
        private static final String ENV_SLACK_INCLUDE_EVENTS = "SLACK_INCLUDE_EVENTS";
        private static final String ENV_SLACK_INCLUDE_ALL_EVENTS = "SLACK_INCLUDE_ALL_EVENTS";
        private static final String ENV_SLACK_INCLUDE_ALL_EVENTS_EXCEPT = "SLACK_INCLUDE_ALL_EVENTS_EXCEPT";
        private static final String ENV_SLACK_INCLUDE_ALL_ERRORS = "SLACK_INCLUDE_ALL_ERRORS";
        private static final String ENV_SLACK_INCLUDE_ALL_ERRORS_EXCEPT = "SLACK_INCLUDE_ALL_ERRORS_EXCEPT";

        private static List<EventType> getSupportedEvents() {
            final boolean includeAllEvents = Boolean.parseBoolean(System.getenv(ENV_SLACK_INCLUDE_ALL_EVENTS));
            if (includeAllEvents) {
                return List.of(EventType.values());
            }

            return ListUtils.sum(
                    getAllSupportedEvents(),
                    getAllSupportedErrors()
            );
        }

        private static List<EventType> getAllSupportedEvents() {
            final List<EventType> includeEvents = loadEventListFromEnvVariable(ENV_SLACK_INCLUDE_EVENTS, EventType.class);
            if (CollectionUtils.isNotEmpty(includeEvents)) {
                return includeEvents;
            }

            final List<EventType> excludedEvents = loadEventListFromEnvVariable(ENV_SLACK_INCLUDE_ALL_EVENTS_EXCEPT, EventType.class);
            if (CollectionUtils.isNotEmpty(excludedEvents)) {
                return Stream.of(EventType.values())
                        .filter(eventType -> !excludedEvents.contains(eventType))
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();
        }

        private static List<EventType> getAllSupportedErrors() {
            final boolean includeAllErrors = Boolean.parseBoolean(System.getenv(ENV_SLACK_INCLUDE_ALL_ERRORS));
            if (includeAllErrors) {
                return getAllErrorEvents();
            }

            final List<EventType> excludedErrors = loadEventListFromEnvVariable(ENV_SLACK_INCLUDE_ALL_ERRORS_EXCEPT, EventType.class);
            if (CollectionUtils.isNotEmpty(excludedErrors)) {
                return getAllErrorEvents().stream()
                        .filter(eventType -> !excludedErrors.contains(eventType))
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();
        }

        private final static String EVN_SLACK_INCLUDE_EVENT_REPRESENTATION = "SLACK_INCLUDE_EVENT_REPRESENTATION";

        public static boolean isIncludeEventRepresentationEnabled() {
            final String includeEventRepresentation = System.getenv(EVN_SLACK_INCLUDE_EVENT_REPRESENTATION);

            return Objects.isNull(includeEventRepresentation)
                    ? DEFAULT_SLACK_INCLUDE_REPRESENTATION
                    : Boolean.parseBoolean(includeEventRepresentation);
        }

        // admin events stuff
        private static final String ENV_SLACK_INCLUDE_ADMIN_EVENTS = "SLACK_INCLUDE_ADMIN_EVENTS";
        private static final String ENV_SLACK_INCLUDE_ALL_ADMIN_EVENTS = "SLACK_INCLUDE_ALL_ADMIN_EVENTS";

        private static List<OperationType> getSupportedAdminEvents() {
            final boolean includeAllAdminEvents = Boolean.parseBoolean(System.getenv(ENV_SLACK_INCLUDE_ALL_ADMIN_EVENTS));
            if (includeAllAdminEvents) {
                return List.of(OperationType.values());
            }

            final List<OperationType> includeAdminEvents = loadEventListFromEnvVariable(ENV_SLACK_INCLUDE_ADMIN_EVENTS, OperationType.class);
            if (CollectionUtils.isNotEmpty(includeAdminEvents)) {
                return includeAdminEvents;
            }

            return Collections.emptyList();
        }

        private final static String EVN_SLACK_INCLUDE_ADMIN_EVENT_REPRESENTATION = "SLACK_INCLUDE_ADMIN_EVENT_REPRESENTATION";

        public static boolean isIncludeAdminEventRepresentationEnabled() {
            final String includeAdminEventRepresentation = System.getenv(EVN_SLACK_INCLUDE_ADMIN_EVENT_REPRESENTATION);

            return Objects.isNull(includeAdminEventRepresentation)
                    ? DEFAULT_SLACK_INCLUDE_REPRESENTATION
                    : Boolean.parseBoolean(includeAdminEventRepresentation);
        }

        // helper methods
        private static <E extends Enum<E>> List<E> loadEventListFromEnvVariable(final String envVariableName, Class<E> eventClass) {
            final String[] array = StringUtils.split(
                    System.getenv(envVariableName),
                    EVENT_LIST_SEPARATOR
            );

            return ArrayUtils.isEmpty(array)
                    ? Collections.emptyList()
                    : Stream.of(array)
                    .map(enumValue -> {
                        try {
                            return Enum.valueOf(eventClass, StringUtils.deleteWhitespace(enumValue));
                        } catch (Exception e) {
                            log.warn("Not recognized event type in Slack configuration [{}]", enumValue);
                        }
                        return null;
                    }).filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        private static List<EventType> getAllErrorEvents() {
            return Stream.of(EventType.values())
                    .filter(eventType -> eventType.name().contains("_ERROR"))
                    .collect(Collectors.toList());
        }
    }
}
