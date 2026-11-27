package com.nexa.chat.feature.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.nexa.chat.feature.messaging.model.Conversation;
import com.nexa.chat.feature.messaging.model.Message;
import com.nexa.chat.feature.messaging.model.MessageType;
import com.nexa.chat.feature.messaging.model.OutboxEntry;
import com.nexa.chat.feature.messaging.model.OutboxState;
import com.nexa.chat.feature.messaging.model.ServerMessage;
import com.nexa.chat.feature.messaging.repository.FakeConversationRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/** Fresh fake per test: fixed account, peer and payloads; no network, clock or random-ID assertions. */
final class MessagingTestFixtures {
    static final UUID USER = UUID.fromString("11111111-1111-4111-8111-111111111111");
    static final UUID EPOCH = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");
    static final String PEER = "22222222-2222-4222-8222-222222222222";
    static final String HISTORY = "IMP02A fixture history";
    static final String COMPOSED = "IMP02A sent from composer";
    static final String HISTORY_ROW = HISTORY + " · SENT";
    static final String COMPOSED_ROW = COMPOSED + " · SENT";

    final FakeConversationRepository repository;
    final Conversation conversation;

    private MessagingTestFixtures(FakeConversationRepository repository, Conversation conversation) {
        this.repository = repository;
        this.conversation = conversation;
    }

    static MessagingTestFixtures create() throws Exception {
        FakeConversationRepository repository = new FakeConversationRepository(USER.toString());
        // Only public fake APIs seed history. Generated server IDs/timestamps are intentionally opaque.
        Conversation conversation = await(repository.openDirect(PEER));
        Message seed = new Message("33333333-3333-4333-8333-333333333333",
                conversation.getId(), USER.toString(), MessageType.TEXT, HISTORY);
        assertEquals(OutboxState.SENT, await(repository.send(seed)).getState());
        assertEquals(PEER, conversation.getTitle());
        return new MessagingTestFixtures(repository, conversation);
    }

    /** Supplement the visible once-only assertion with matching fake server and outbox evidence. */
    void assertSingleReconciledSend() throws Exception {
        List<ServerMessage> history = await(repository.listMessages(conversation.getId()));
        List<OutboxEntry> outbox = await(repository.listOutbox(conversation.getId()));
        assertEquals("One seed plus one UI send in server history", 2, history.size());
        assertEquals("One seed plus one UI send in fake outbox", 2, outbox.size());
        ServerMessage sent = null;
        int historyMatches = 0;
        for (ServerMessage row : history) {
            if (COMPOSED.equals(row.getMessage().getText())) {
                sent = row;
                historyMatches++;
            }
        }
        assertEquals("UI send persisted exactly once", 1, historyMatches);
        assertNotNull(sent);
        int outboxMatches = 0;
        for (OutboxEntry row : outbox) {
            if (COMPOSED.equals(row.getMessage().getText())) {
                outboxMatches++;
                assertEquals(OutboxState.SENT, row.getState());
                assertEquals(USER.toString(), row.getMessage().getSenderId());
                assertEquals(sent.getMessage().getClientMessageId(),
                        row.getMessage().getClientMessageId());
                assertNotNull(row.getAcknowledgement());
                assertEquals(sent.getAcknowledgement().getServerMessageId(),
                        row.getAcknowledgement().getServerMessageId());
                assertEquals(sent.getServerSequence(),
                        row.getAcknowledgement().getServerSequence());
            }
        }
        assertEquals("UI send acknowledged exactly once", 1, outboxMatches);
    }

    private static <T> T await(CompletionStage<T> stage) throws Exception {
        return stage.toCompletableFuture().get(5, TimeUnit.SECONDS);
    }
}
