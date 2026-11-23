package com.nexa.chat.feature.messaging.model;

import java.util.Objects;

/**
 * Immutable snapshot of ONE logical message. A retry reuses this message and ID.
 * Persistence, scheduling, concurrent ACK/retry serialization and server
 * deduplication belong to a future repository implementation.
 */
public final class OutboxEntry {
    private final Message message;
    private final OutboxState state;
    private final ServerAcknowledgement acknowledgement;
    private final MessageStatus receiptStatus;

    private OutboxEntry(Message message, OutboxState state,
                        ServerAcknowledgement acknowledgement, MessageStatus receiptStatus) {
        this.message = Objects.requireNonNull(message, "message");
        this.state = state;
        this.acknowledgement = acknowledgement;
        this.receiptStatus = receiptStatus;
    }

    public static OutboxEntry draft(Message message) {
        return new OutboxEntry(message, OutboxState.DRAFT, null, MessageStatus.NONE);
    }

    public OutboxEntry transitionTo(OutboxState next) {
        Objects.requireNonNull(next, "next");
        if (!state.canTransitionTo(next)) {
            throw new IllegalStateException("Invalid outbox transition: " + state + " -> " + next);
        }
        if (next == OutboxState.SENT) {
            throw new IllegalStateException("SENT requires server acknowledgement");
        }
        return new OutboxEntry(message, next, acknowledgement, receiptStatus);
    }

    /** Timeout denotes unknown server outcome. It never constructs another message. */
    public OutboxEntry onTimeout() {
        return transitionTo(OutboxState.FAILED);
    }

    public OutboxEntry retry() {
        if (state != OutboxState.FAILED) {
            throw new IllegalStateException("Only FAILED can retry");
        }
        return transitionTo(OutboxState.PENDING);
    }

    /**
     * Reconciliation of persistence evidence, separate from normal send transitions.
     * Accepts a late ACK while FAILED or requeued PENDING, and identical repeated ACKs.
     */
    public OutboxEntry acknowledge(ServerAcknowledgement ack) {
        Objects.requireNonNull(ack, "ack");
        if (!message.getClientMessageId().equals(ack.getClientMessageId())) {
            throw new IllegalArgumentException("ACK belongs to another logical message");
        }
        if (state == OutboxState.DRAFT) {
            throw new IllegalStateException("Unsubmitted draft cannot be acknowledged");
        }
        if (acknowledgement != null) {
            if (!acknowledgement.getServerMessageId().equals(ack.getServerMessageId())
                    || acknowledgement.getServerSequence() != ack.getServerSequence()) {
                throw new IllegalArgumentException("Conflicting server acknowledgement");
            }
            return this;
        }
        return new OutboxEntry(message, OutboxState.SENT, ack, receiptStatus);
    }

    /** Caller must validate receipt identity/authority before applying it. */
    public OutboxEntry recordReceipt(MessageStatus receipt) {
        Objects.requireNonNull(receipt, "receipt");
        if (state != OutboxState.SENT) {
            throw new IllegalStateException("Receipt requires an acknowledged message");
        }
        if (receipt.ordinal() < receiptStatus.ordinal()) {
            return this; // Ignore stale/out-of-order receipt events.
        }
        return new OutboxEntry(message, state, acknowledgement, receipt);
    }

    public Message getMessage() { return message; }
    public OutboxState getState() { return state; }
    /** Null until server persistence is confirmed; never synthesize sequence locally. */
    public ServerAcknowledgement getAcknowledgement() { return acknowledgement; }
    public MessageStatus getReceiptStatus() { return receiptStatus; }
}
