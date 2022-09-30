package io.github.kilmajster.keycloak.slack.message;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.LayoutBlock;
import io.github.kilmajster.keycloak.slack.config.SlackConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakContext;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SlackMessageSender {

    private final Slack slack;
    private final SlackConfiguration slackConfiguration;

    public void sendEventMessage(KeycloakContext context, Event event) {
        try {
            final String host = getKeycloakHost(context);

            final List<LayoutBlock> messageBlocks = SlackEventMessage.getEventMessageBlocks(
                    event,
                    context.getRealm().getName(),
                    host,
                    slackConfiguration.isIncludeEventRepresentationEnabled()
            );

            sendSlackMessage(SlackEventMessage.eventTitle(host), messageBlocks);
        } catch (SlackApiException | IOException e) {
            log.error("An error occurred while sending event message to Slack!", e);
        }
    }


    public void sendAdminEventMessage(KeycloakContext context, AdminEvent event) {
        try {
            final String host = getKeycloakHost(context);

            final List<LayoutBlock> messageBlocks = SlackEventMessage.getEventMessageBlocks(
                    event,
                    context.getRealm().getName(),
                    host,
                    slackConfiguration.isIncludeEventRepresentationEnabled()
            );

            sendSlackMessage(SlackEventMessage.adminEventTitle(host), messageBlocks);
        } catch (SlackApiException | IOException e) {
            log.error("An error occurred while sending admin event message to Slack!", e);
        }
    }

    private void sendSlackMessage(final String title, final List<LayoutBlock> messageBlocks) throws SlackApiException, IOException {
        final ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                .channel(slackConfiguration.getChannel())
                .text(title)
                .blocks(messageBlocks)
                .build();

        final ChatPostMessageResponse response = slack.methods(slackConfiguration.getToken())
                .chatPostMessage(request);

        if (!response.isOk()) {
            log.warn("Failed to send message to Slack! Error = {}", response.getError());
        }
    }

    private String getKeycloakHost(final KeycloakContext context) {
        return context.getUri().getBaseUri().toString();
    }
}
