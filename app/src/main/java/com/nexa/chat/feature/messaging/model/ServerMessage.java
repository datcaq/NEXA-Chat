package com.nexa.chat.feature.messaging.model;

import java.util.Objects;

/** Confirmed history projection with authoritative ordering, independent of outbox. */
public final class ServerMessage {
    private final Message message;
    private final ServerAcknowledgement acknowledgement;
    private final MessageStatus receiptStatus;

    public ServerMessage(Message message, ServerAcknowledgement acknowledgement,
                         MessageStatus receiptStatus) {
        this.message = Objects.requireNonNull(message, "message");
        this.acknowledgement = Objects.requireNonNull(acknowledgement, "acknowledgement");
        this.receiptStatus = Objects.requireNonNull(receiptStatus, "receiptStatus");
        if (!message.getClientMessageId().equals(acknowledgement.getClientMessageId())) {
            throw new IllegalArgumentException("Server metadata belongs to another message");
        }
    }

    public Message getMessage() { return message; }
    public ServerAcknowledgement getAcknowledgement() { return acknowledgement; }
    public long getServerSequence() { return acknowledgement.getServerSequence(); }
    public MessageStatus getReceiptStatus() { return receiptStatus; }
}
