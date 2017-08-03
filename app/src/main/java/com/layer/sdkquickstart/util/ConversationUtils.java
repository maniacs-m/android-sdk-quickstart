package com.layer.sdkquickstart.util;

import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;

public class ConversationUtils {
    private static final String METADATA_KEY_CONVERSATION_TITLE = "conversationName";

    // TODO requires conversation support

    public static String getConversationTitle(Identity authenticatedUser, Conversation conversation) {
        String metadataTitle = getConversationMetadataTitle(conversation);
        if (metadataTitle != null) return metadataTitle.trim();

        StringBuilder sb = new StringBuilder();
        for (Identity participant : conversation.getParticipants()) {
            if (participant.equals(authenticatedUser)) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(IdentityUtils.getDisplayName(participant));
        }
        return sb.toString().trim();
    }

    public static String getConversationMetadataTitle(Conversation conversation) {
        String metadataTitle = (String) conversation.getMetadata().get(METADATA_KEY_CONVERSATION_TITLE);
        if (metadataTitle != null && !metadataTitle.trim().isEmpty()) return metadataTitle.trim();
        return null;
    }

    public static void setConversationMetadataTitle(Conversation conversation, String title) {
        if (title == null || title.trim().isEmpty()) {
            conversation.removeMetadataAtKeyPath(METADATA_KEY_CONVERSATION_TITLE);
        } else {
            conversation.putMetadataAtKeyPath(METADATA_KEY_CONVERSATION_TITLE, title.trim());
        }
    }
}
