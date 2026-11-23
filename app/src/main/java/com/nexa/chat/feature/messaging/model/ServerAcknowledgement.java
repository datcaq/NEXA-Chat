package com.nexa.chat.feature.messaging.model;

/** Server persistence evidence. Sequence is scoped to a conversation.
 * Sequence base/range must be confirmed by the backend contract.
 */
public final class ServerAcknowledgement {
    private final String clientMessageId;
    private final String serverMessageId;
    private final long serverSequence;

    public ServerAcknowledgement(String clientMessageId, String serverMessageId,
                                 long serverSequence) {
        this.clientMessageId = Message.requireId(clientMessageId);
        this.serverMessageId = Message.requireId(serverMessageId);
        this.serverSequence = serverSequence;
    }

    public String getClientMessageId() { return clientMessageId; }
    public String getServerMessageId() { return serverMessageId; }
    public long getServerSequence() { return serverSequence; }
}
