package io.github.kilmajster.keycloak.slack;

import io.github.kilmajster.keycloak.slack.message.SlackMessageSender;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.models.KeycloakContext;

public class SlackEventListenerTransaction extends EventListenerTransaction {

    public SlackEventListenerTransaction(final KeycloakContext context, final SlackMessageSender slackMessageSender) {
        super(
                (event, includeRepresentation) -> slackMessageSender.sendAdminEventMessage(context, event),
                (event) -> slackMessageSender.sendEventMessage(context, event)
        );
    }
}
