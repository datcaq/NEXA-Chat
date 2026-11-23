package com.nexa.chat.feature.messaging;

/** TV2 feature-local proposal. TV1 supplies the host adapter; no global navigation edits. */
public interface ChatNavigator {
    void openConversation(String conversationId);
    void back();
}
