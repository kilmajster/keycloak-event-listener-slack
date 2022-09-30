package io.github.kilmajster.keycloak.slack;

import io.github.kilmajster.keycloak.slack.config.SlackConfiguration;
import io.github.kilmajster.keycloak.slack.message.SlackMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

@Slf4j
@RequiredArgsConstructor
public class SlackEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;
    private final SlackConfiguration slackConfiguration;
    private final SlackMessageSender slackMessageSender;

    @Override
    public void onEvent(Event event) {
        final boolean shouldSendEvent = slackConfiguration.getSupportedEvents().contains(event.getType());

        if (shouldSendEvent) {
            slackMessageSender.sendEventMessage(session.getContext(), event);
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        final boolean shouldSendAdminEvent = slackConfiguration.getSupportedAdminEvents().contains(event.getOperationType());

        if (shouldSendAdminEvent) {
            slackMessageSender.sendAdminEventMessage(session.getContext(), event);
        }
    }

    @Override
    public void close() {
    }
}
