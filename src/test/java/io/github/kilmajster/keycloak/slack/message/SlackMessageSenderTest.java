package io.github.kilmajster.keycloak.slack.message;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.LayoutBlock;
import io.github.kilmajster.keycloak.slack.config.SlackConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.models.RealmModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static io.github.kilmajster.keycloak.slack.TestData.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlackMessageSenderTest {

    @Mock
    private Slack slack;

    @Mock
    private SlackConfiguration slackConfiguration;

    @InjectMocks
    private SlackMessageSender slackMessageSender;

    @Test
    void sendEventMessage_should_send_event_to_slack() throws SlackApiException, IOException {
        // given
        final KeycloakContext context = mock(KeycloakContext.class);
        mockContextForHostFinding(context);
        mockContextForRealmNameFinding(context);

        final Event event = mock(Event.class);

        try (MockedStatic<SlackEventMessage> slackEventMessageMockedStatic = mockStatic(SlackEventMessage.class)) {
            final List<?> messageBlocks = mock(List.class);
            slackEventMessageMockedStatic.when(
                    () -> SlackEventMessage.getEventMessageBlocks(
                            eq(event),
                            eq(TEST_REALM_NAME),
                            eq(TEST_HOST),
                            anyBoolean()
                    )
            ).thenReturn(messageBlocks);

            slackEventMessageMockedStatic.when(
                    () -> SlackEventMessage.eventTitle(
                            eq(TEST_HOST)
                    )
            ).thenReturn(TEST_MESSAGE_TITLE);

            when(slackConfiguration.getToken()).thenReturn(TEST_SLACK_TOKEN);

            final MethodsClient methodsClient = mock(MethodsClient.class);

            when(slack.methods(
                    eq(TEST_SLACK_TOKEN)
            )).thenReturn(methodsClient);

            final ChatPostMessageResponse chatPostMessageResponse = mock(ChatPostMessageResponse.class);

            when(methodsClient.chatPostMessage(
                    any(ChatPostMessageRequest.class)
            )).thenReturn(chatPostMessageResponse);

            when(chatPostMessageResponse.isOk())
                    .thenReturn(true);

            // when
            slackMessageSender.sendEventMessage(context, event);

            // then
            verify(chatPostMessageResponse).isOk();
        }
    }

    @Test
    void sendAdminEventMessage_should_send_admin_event_to_slack() throws SlackApiException, IOException {
        // given
        final KeycloakContext context = mock(KeycloakContext.class);
        mockContextForHostFinding(context);
        mockContextForRealmNameFinding(context);

        final AdminEvent adminEvent = mock(AdminEvent.class);

        try (MockedStatic<SlackEventMessage> slackEventMessageMockedStatic = mockStatic(SlackEventMessage.class)) {
            final List<?> messageBlocks = mock(List.class);
            slackEventMessageMockedStatic.when(
                    () -> SlackEventMessage.getEventMessageBlocks(
                            eq(adminEvent),
                            eq(TEST_REALM_NAME),
                            eq(TEST_HOST),
                            anyBoolean()
                    )
            ).thenReturn(messageBlocks);

            slackEventMessageMockedStatic.when(
                    () -> SlackEventMessage.eventTitle(
                            eq(TEST_HOST)
                    )
            ).thenReturn(TEST_MESSAGE_TITLE);

            when(slackConfiguration.getToken()).thenReturn(TEST_SLACK_TOKEN);

            final MethodsClient methodsClient = mock(MethodsClient.class);

            when(slack.methods(
                    eq(TEST_SLACK_TOKEN)
            )).thenReturn(methodsClient);

            final ChatPostMessageResponse chatPostMessageResponse = mock(ChatPostMessageResponse.class);

            when(methodsClient.chatPostMessage(
                    any(ChatPostMessageRequest.class)
            )).thenReturn(chatPostMessageResponse);

            when(chatPostMessageResponse.isOk())
                    .thenReturn(true);

            // when
            slackMessageSender.sendAdminEventMessage(context, adminEvent);

            // then
            verify(chatPostMessageResponse).isOk();
        }
    }

    private void mockContextForRealmNameFinding(KeycloakContext context) {
        final RealmModel realm = mock(RealmModel.class);

        when(realm.getName()).thenReturn(TEST_REALM_NAME);
        when(context.getRealm()).thenReturn(realm);
    }

    private void mockContextForHostFinding(KeycloakContext context) {
        final KeycloakUriInfo keycloakUriInfo = mock(KeycloakUriInfo.class);
        final URI baseUri = mock(URI.class);

        when(keycloakUriInfo.getBaseUri()).thenReturn(baseUri);
        when(baseUri.toString()).thenReturn(TEST_HOST);
        when(context.getUri()).thenReturn(keycloakUriInfo);
    }
}
