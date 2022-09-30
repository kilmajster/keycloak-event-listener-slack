package io.github.kilmajster.keycloak.slack.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.slack.api.model.block.ContextBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.util.JsonSerialization;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public final class SlackEventMessage {

    public static String eventTitle(String host) {
        return "New event has just occurred in Keycloak at " + host;
    }

    public static String adminEventTitle(String host) {
        return "New admin event has just occurred in Keycloak at " + host;
    }

    public static List<LayoutBlock> getEventMessageBlocks(Event event, String realmName, String host, boolean includeEventRepresentationEnabled) throws JsonProcessingException {
        if (includeEventRepresentationEnabled) {
            return List.of(
                    headerBlock(eventTitle(host)),
                    eventTypeAndTimeBlock(event.getType().name(), event.getTime()),
                    realmAndClientBlock(realmName, event.getClientId()),
                    representationBlock(event)
            );
        }
        return List.of(
                headerBlock(eventTitle(host)),
                eventTypeAndTimeBlock(event.getType().name(), event.getTime()),
                realmAndClientBlock(realmName, event.getClientId())
        );

    }

    public static List<LayoutBlock> getEventMessageBlocks(AdminEvent adminEvent, String realmName, String host, boolean includeAdminEventRepresentationEnabled) throws JsonProcessingException {
        if (includeAdminEventRepresentationEnabled) {
            return List.of(
                    headerBlock(adminEventTitle(host)),
                    eventTypeAndTimeBlock(adminEvent.getOperationType().name(), adminEvent.getTime()),
                    realmAndResourceBlock(realmName, adminEvent.getResourceTypeAsString()),
                    representationBlock(adminEvent)
            );
        }
        return List.of(
                headerBlock(adminEventTitle(host)),
                eventTypeAndTimeBlock(adminEvent.getOperationType().name(), adminEvent.getTime()),
                realmAndClientBlock(realmName, adminEvent.getResourceTypeAsString())
        );

    }

    private static SectionBlock headerBlock(final String title) {
        return SectionBlock.builder()
                .text(MarkdownTextObject.builder()
                        .text(title)
                        .build()
                ).build();
    }

    private static SectionBlock eventTypeAndTimeBlock(final String eventType, final long eventTime) {
        return SectionBlock.builder()
                .fields(List.of(
                        MarkdownTextObject.builder()
                                .text("*Event type:*\n" + eventType)
                                .build(),
                        MarkdownTextObject.builder()
                                .text("*When:*\n" + Instant.ofEpochMilli(eventTime).atZone(ZoneOffset.UTC))
                                .build()
                )).build();
    }

    private static SectionBlock realmAndClientBlock(final String realmName, final String clientId) {
        return SectionBlock.builder()
                .fields(List.of(
                        MarkdownTextObject.builder()
                                .text("*Realm:*\n" + realmName)
                                .build(),
                        MarkdownTextObject.builder()
                                .text("*Client:*\n" + clientId)
                                .build()
                )).build();
    }

    private static SectionBlock realmAndResourceBlock(final String realmName, final String resource) {
        return SectionBlock.builder()
                .fields(List.of(
                        MarkdownTextObject.builder()
                                .text("*Realm:*\n" + realmName)
                                .build(),
                        MarkdownTextObject.builder()
                                .text("*Resource:*\n" + resource)
                                .build()
                )).build();
    }

    private static ContextBlock representationBlock(Event event) throws JsonProcessingException {
        return ContextBlock.builder()
                .elements(List.of(
                        MarkdownTextObject.builder()
                                .text("```"
                                        + JsonSerialization.prettyMapper
                                        .writeValueAsString(ModelToRepresentation.toRepresentation(event))
                                        + "```"
                                ).build()
                )).build();
    }

    private static ContextBlock representationBlock(AdminEvent event) throws JsonProcessingException {
        return ContextBlock.builder()
                .elements(List.of(
                        MarkdownTextObject.builder()
                                .text("```"
                                        + JsonSerialization.prettyMapper
                                        .writeValueAsString(ModelToRepresentation.toRepresentation(event))
                                        + "```"
                                ).build()
                )).build();
    }
}
