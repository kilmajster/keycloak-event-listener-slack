package io.github.kilmajster.keycloak.slack;

import io.github.kilmajster.keycloak.slack.config.SlackConfiguration;
import io.github.kilmajster.keycloak.slack.message.SlackMessageSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlackEventListenerProviderTest {

    @Mock
    private KeycloakSession session;

    @Mock
    private SlackConfiguration slackConfiguration;

    @Mock
    private SlackMessageSender slackMessageSender;

    @InjectMocks
    private SlackEventListenerProvider slackEventListenerProvider;

    @Test
    void onEvent_should_send_message_when_message_type_is_included_in_supported_events() {
        // given
        final EventType eventType = EventType.LOGIN;
        final Event event = mockEventForType(eventType);
        mockSupportedEventType(eventType);
        final KeycloakContext context = mockContextForSession();

        // when
        slackEventListenerProvider.onEvent(event);

        // then
        verify(slackMessageSender).sendEventMessage(
                eq(context),
                eq(event)
        );
    }

    private KeycloakContext mockContextForSession() {
        final KeycloakContext context = mock(KeycloakContext.class);
        when(session.getContext()).thenReturn(context);

        return context;
    }

    private Event mockEventForType(EventType type) {
        final Event event = mock(Event.class);
        when(event.getType()).thenReturn(type);

        return event;
    }

    private void mockSupportedEventType(EventType eventType) {
        when(slackConfiguration.getSupportedEvents())
                .thenReturn(Collections.singletonList(eventType));
    }

    @Test
    void onEvent_should_do_not_send_message_when_message_type_is_not_included_in_supported_events() {
        // given
        final Event event = mockEventForType(EventType.LOGIN);
        mockSupportedEventType(EventType.LOGOUT);

        // when
        slackEventListenerProvider.onEvent(event);

        // then
        verifyNoInteractions(slackMessageSender);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void onEvent_should_send_AdminEvent_message_when_message_type_is_included_in_supported_events(boolean includeRepresentation) {
        // given
        final OperationType operationType = OperationType.CREATE;
        final AdminEvent event = mockEventForType(operationType);
        mockSupportedAdminEventType(operationType);
        final KeycloakContext context = mockContextForSession();

        // when
        slackEventListenerProvider.onEvent(event, includeRepresentation);

        // then
        verify(slackMessageSender).sendAdminEventMessage(
                eq(context),
                eq(event)
        );
    }

    private AdminEvent mockEventForType(OperationType type) {
        final AdminEvent event = mock(AdminEvent.class);
        when(event.getOperationType()).thenReturn(type);

        return event;
    }

    private void mockSupportedAdminEventType(OperationType type) {
        when(slackConfiguration.getSupportedAdminEvents())
                .thenReturn(Collections.singletonList(type));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void onEvent_should_do_not_send_AdminEvent_message_when_message_type_is_not_included_in_supported_events(boolean includeRepresentation) {
        // given
        final AdminEvent event = mockEventForType(OperationType.CREATE);
        mockSupportedAdminEventType(OperationType.DELETE);

        // when
        slackEventListenerProvider.onEvent(event, includeRepresentation);

        // then
        verifyNoInteractions(slackMessageSender);
    }
}
