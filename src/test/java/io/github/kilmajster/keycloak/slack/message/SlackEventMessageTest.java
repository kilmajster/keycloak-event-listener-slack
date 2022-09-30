package io.github.kilmajster.keycloak.slack.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.slack.api.model.block.LayoutBlock;
import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;

import java.util.List;

import static io.github.kilmajster.keycloak.slack.TestData.TEST_HOST;
import static io.github.kilmajster.keycloak.slack.TestData.TEST_REALM_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SlackEventMessageTest {

    @Test
    void eventTitle_should_build_event_title() {
        // when
        final String title = SlackEventMessage.eventTitle(TEST_HOST);

        // then
        assertThat(title).isEqualTo("New event has just occurred in Keycloak at " + TEST_HOST);
    }

    @Test
    void adminEventTitle_should_build_admin_event_title() {
        // when
        final String title = SlackEventMessage.adminEventTitle(TEST_HOST);

        // then
        assertThat(title).isEqualTo("New admin event has just occurred in Keycloak at " + TEST_HOST);
    }

    @Test
    void getEventMessageBlocks_should_build_event_message_blocks_when_include_representation_is_enabled() throws JsonProcessingException {
        // given
        final Event event = mockEventForTypeAndTime(
                EventType.LOGIN,
                System.currentTimeMillis()
        );

        // when
        final List<LayoutBlock> eventMessageBlocks = SlackEventMessage.getEventMessageBlocks(
                event,
                TEST_REALM_NAME,
                TEST_HOST,
                true
        );

        // then
        assertThat(eventMessageBlocks).hasSize(4);
    }

    @Test
    void getEventMessageBlocks_should_build_event_message_blocks_when_include_representation_is_disabled() throws JsonProcessingException {
        // given
        final Event event = mockEventForTypeAndTime(
                EventType.LOGOUT,
                System.currentTimeMillis()
        );

        // when
        final List<LayoutBlock> eventMessageBlocks = SlackEventMessage.getEventMessageBlocks(
                event,
                TEST_REALM_NAME,
                TEST_HOST,
                false
        );

        // then
        assertThat(eventMessageBlocks).hasSize(3);
    }


    @Test
    void getEventMessageBlocks_should_build_admin_event_message_blocks_when_include_representation_is_enabled() throws JsonProcessingException {
        // given
        final AdminEvent event = mockEventForTypeAndTime(
                OperationType.CREATE,
                System.currentTimeMillis()
        );

        // when
        final List<LayoutBlock> eventMessageBlocks = SlackEventMessage.getEventMessageBlocks(
                event,
                TEST_REALM_NAME,
                TEST_HOST,
                true
        );

        // then
        assertThat(eventMessageBlocks).hasSize(4);
    }

    @Test
    void getEventMessageBlocks_should_build_admin_event_message_blocks_when_include_representation_is_disabled() throws JsonProcessingException {
        // given
        final AdminEvent event = mockEventForTypeAndTime(
                OperationType.DELETE,
                System.currentTimeMillis()
        );

        // when
        final List<LayoutBlock> eventMessageBlocks = SlackEventMessage.getEventMessageBlocks(
                event,
                TEST_REALM_NAME,
                TEST_HOST,
                false
        );

        // then
        assertThat(eventMessageBlocks).hasSize(3);
    }

    private Event mockEventForTypeAndTime(EventType type, long time) {
        final Event event = mock(Event.class);

        when(event.getType()).thenReturn(type);
        when(event.getTime()).thenReturn(time);

        return event;
    }

    private AdminEvent mockEventForTypeAndTime(OperationType type, long time) {
        final AdminEvent event = mock(AdminEvent.class);

        when(event.getOperationType()).thenReturn(type);
        when(event.getTime()).thenReturn(time);

        return event;
    }
}