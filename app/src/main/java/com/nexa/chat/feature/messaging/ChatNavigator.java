package com.nexa.chat.feature.messaging;

/** TV2 internal navigation port, implemented by the account MessagingRouter.
 * App Shell delegates System Back and cross-module entry; Home signatures remain PROPOSED. */
public interface ChatNavigator {
    void openConversation(String conversationId);
    void back();
}
