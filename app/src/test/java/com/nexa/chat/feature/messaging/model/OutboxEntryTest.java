package com.nexa.chat.feature.messaging.model;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class OutboxEntryTest {
    private Message message() {
        return new Message("client-1", "conversation-1", "sender-1", MessageType.TEXT, "Hello");
    }

    private OutboxEntry sending() {
        return OutboxEntry.draft(message()).transitionTo(OutboxState.PENDING)
                .transitionTo(OutboxState.SENDING);
    }

    private ServerAcknowledgement ack() {
        return new ServerAcknowledgement("client-1", "server-1", 42);
    }

    @Test public void happyPathRequiresServerEvidence() {
        OutboxEntry draft = OutboxEntry.draft(message());
        OutboxEntry pending = draft.transitionTo(OutboxState.PENDING);
        OutboxEntry sending = pending.transitionTo(OutboxState.SENDING);
        OutboxEntry sent = sending.acknowledge(ack());
        assertEquals(OutboxState.DRAFT, draft.getState());
        assertEquals(OutboxState.PENDING, pending.getState());
        assertEquals(OutboxState.SENDING, sending.getState());
        assertEquals(OutboxState.SENT, sent.getState());
        assertEquals(42, sent.getAcknowledgement().getServerSequence());
        assertEquals(MessageStatus.NONE, sent.getReceiptStatus());
        assertThrows(IllegalStateException.class, () -> sending.transitionTo(OutboxState.SENT));
    }

    @Test public void transitionMatrixAllowsExactlyTheFiveSpecifiedEdges() {
        for (OutboxState from : OutboxState.values()) {
            for (OutboxState to : OutboxState.values()) {
                boolean expected = (from == OutboxState.DRAFT && to == OutboxState.PENDING)
                        || (from == OutboxState.PENDING && to == OutboxState.SENDING)
                        || (from == OutboxState.SENDING && to == OutboxState.SENT)
                        || (from == OutboxState.SENDING && to == OutboxState.FAILED)
                        || (from == OutboxState.FAILED && to == OutboxState.PENDING);
                assertEquals(from + " -> " + to, expected, from.canTransitionTo(to));
            }
        }
    }

    @Test public void rejectsEveryInvalidEntryTransition() {
        OutboxEntry inFlight = sending();
        OutboxEntry[] entries = {
                OutboxEntry.draft(message()),
                OutboxEntry.draft(message()).transitionTo(OutboxState.PENDING),
                inFlight, inFlight.acknowledge(ack()), inFlight.onTimeout()
        };
        for (OutboxEntry entry : entries) {
            for (OutboxState next : OutboxState.values()) {
                if (!entry.getState().canTransitionTo(next) || next == OutboxState.SENT) {
                    assertThrows(IllegalStateException.class, () -> entry.transitionTo(next));
                }
            }
        }
    }

    @Test public void retriesKeepTheSameMessageIdAndFrozenPayload() {
        OutboxEntry entry = sending();
        Message original = entry.getMessage();
        for (int attempt = 0; attempt < 5; attempt++) {
            entry = entry.onTimeout().retry();
            assertEquals(OutboxState.PENDING, entry.getState());
            assertSame(original, entry.getMessage());
            assertEquals("client-1", entry.getMessage().getClientMessageId());
            assertEquals("Hello", entry.getMessage().getContent());
            entry = entry.transitionTo(OutboxState.SENDING);
        }
    }

    @Test public void lostAckReusesLogicalMessageAndDeduplicatingServerRecord() {
        // Test double explicitly assumes server idempotency; no real backend is tested.
        Map<String, ServerAcknowledgement> serverRecords = new HashMap<>();
        OutboxEntry first = sending();
        ServerAcknowledgement persisted = serverRecords.computeIfAbsent(
                first.getMessage().getClientMessageId(), id -> ack());
        // Server persisted, but ACK is lost. Client cannot infer non-persistence.
        OutboxEntry retry = first.onTimeout().retry().transitionTo(OutboxState.SENDING);
        ServerAcknowledgement replay = serverRecords.computeIfAbsent(
                retry.getMessage().getClientMessageId(), id -> ack());
        OutboxEntry sent = retry.acknowledge(replay);
        assertSame(first.getMessage(), sent.getMessage());
        assertSame(persisted, replay);
        assertEquals(1, serverRecords.size());
        assertEquals(OutboxState.SENT, sent.getState());
    }

    @Test public void lateAckReconcilesFailureAndRequeuedMessage() {
        OutboxEntry failed = sending().onTimeout();
        assertEquals(OutboxState.SENT, failed.acknowledge(ack()).getState());
        assertEquals(OutboxState.SENT, failed.retry().acknowledge(ack()).getState());
    }

    @Test public void repeatedAckIsIdempotentAndConflictingAckIsRejected() {
        OutboxEntry sent = sending().acknowledge(ack());
        assertSame(sent, sent.acknowledge(ack()));
        assertThrows(IllegalArgumentException.class, () -> sent.acknowledge(
                new ServerAcknowledgement("client-1", "other-server", 42)));
        assertThrows(IllegalArgumentException.class, () -> sent.acknowledge(
                new ServerAcknowledgement("client-1", "server-1", 43)));
        assertThrows(IllegalArgumentException.class, () -> sending().acknowledge(
                new ServerAcknowledgement("other-client", "server-1", 42)));
        assertThrows(IllegalStateException.class, () -> OutboxEntry.draft(message()).acknowledge(ack()));
    }

    @Test public void receiptProgressIsIndependentAndNeverRegresses() {
        OutboxEntry sent = sending().acknowledge(ack());
        OutboxEntry delivered = sent.recordReceipt(MessageStatus.DELIVERED);
        OutboxEntry read = delivered.recordReceipt(MessageStatus.READ);
        assertEquals(OutboxState.SENT, read.getState());
        assertEquals(MessageStatus.READ, read.recordReceipt(MessageStatus.DELIVERED).getReceiptStatus());
        assertEquals(MessageStatus.READ, read.recordReceipt(MessageStatus.NONE).getReceiptStatus());
        assertEquals(MessageStatus.READ, sent.recordReceipt(MessageStatus.READ).getReceiptStatus());
        assertThrows(IllegalStateException.class,
                () -> sending().recordReceipt(MessageStatus.READ));
    }

    @Test public void onlyFailureCanRetryAndOnlySendingCanTimeout() {
        assertThrows(IllegalStateException.class, () -> sending().retry());
        assertThrows(IllegalStateException.class, () -> OutboxEntry.draft(message()).onTimeout());
        assertThrows(IllegalStateException.class, () -> sending().acknowledge(ack()).retry());
    }
}
