package io.github.kilmajster.keycloak.slack;

import com.slack.api.Slack;
import io.github.kilmajster.keycloak.slack.config.SlackConfiguration;
import io.github.kilmajster.keycloak.slack.message.SlackMessageSender;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class SlackEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final String PROVIDER_ID = "slack";

    private final Slack slack = Slack.getInstance();
    private final SlackConfiguration slackConfiguration = SlackConfiguration.getConfig();

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        final SlackMessageSender slackMessageSender = new SlackMessageSender(
                slack,
                slackConfiguration
        );

        final SlackEventListenerTransaction slackEventListenerTransaction = new SlackEventListenerTransaction(
                session.getContext(),
                slackMessageSender
        );

        session.getTransactionManager().enlistAfterCompletion(slackEventListenerTransaction);

        return new SlackEventListenerProvider(slackConfiguration, slackEventListenerTransaction);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
