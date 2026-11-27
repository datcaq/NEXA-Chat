package com.nexa.chat.feature.messaging.model;

/** Receipt progress, independent of the sender's local outbox state.
 * NONE means no receipt evidence, not evidence of non-delivery.
 * GROUP receipt aggregation is intentionally not defined here.
 */
public enum MessageStatus {
    NONE, DELIVERED, READ
}
