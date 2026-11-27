package com.nexa.chat.feature.messaging.model;

/** Local sending lifecycle. FAILED may still have been persisted by the server. */
public enum OutboxState {
    DRAFT, PENDING, SENDING, SENT, FAILED;

    public boolean canTransitionTo(OutboxState next) {
        switch (this) {
            case DRAFT: return next == PENDING;
            case PENDING: return next == SENDING;
            case SENDING: return next == SENT || next == FAILED;
            case FAILED: return next == PENDING;
            default: return false;
        }
    }
}
