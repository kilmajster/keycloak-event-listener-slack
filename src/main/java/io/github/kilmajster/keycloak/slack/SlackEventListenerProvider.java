package io.github.kilmajster.keycloak.slack;

import io.github.kilmajster.keycloak.slack.config.SlackConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

@Slf4j
@RequiredArgsConstructor
public class SlackEventListenerProvider implements EventListenerProvider {

    private final SlackConfiguration slackConfiguration;
    private final SlackEventListenerTransaction slackEventListenerTransaction;

    @Override
    public void onEvent(Event event) {
        final boolean shouldSendEvent = slackConfiguration.getSupportedEvents().contains(event.getType());

        if (shouldSendEvent) {
            slackEventListenerTransaction.addEvent(event);
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        final boolean shouldSendAdminEvent = slackConfiguration.getSupportedAdminEvents().contains(event.getOperationType());

        if (shouldSendAdminEvent) {
            slackEventListenerTransaction.addAdminEvent(event, includeRepresentation);
        }
    }

    @Override
    public void close() {
    }
}
