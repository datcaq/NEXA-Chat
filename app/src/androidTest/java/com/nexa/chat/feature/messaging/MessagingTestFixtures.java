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
import com.nexa.chat.feature.messaging.repository.ConversationRepository;
import com.nexa.chat.feature.messaging.repository.MessagePage;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CancellationException;
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

    final UUID user, epoch;
    final String peer, historyRow;
    final FakeConversationRepository repository;
    final ConversationRepository client;
    final ControlledRepository controlled;
    final Conversation conversation;

    private MessagingTestFixtures(UUID user, UUID epoch, String peer, String history,
                                  boolean controlled) throws Exception {
        this(user, epoch, peer, history, controlled, true, java.util.function.UnaryOperator.identity());
    }

    private MessagingTestFixtures(UUID user, UUID epoch, String peer, String history,
                                  boolean controlled, boolean seedHistory,
                                  java.util.function.UnaryOperator<ConversationRepository> decoration)
            throws Exception {
        this.user = user;
        this.epoch = epoch;
        this.peer = peer;
        this.historyRow = history + " · SENT";
        repository = new FakeConversationRepository(user.toString());
        // Seed through public fake APIs; generated IDs/timestamps remain opaque.
        conversation = await(repository.openDirect(peer));
        Message seed = new Message("33333333-3333-4333-8333-333333333333",
                conversation.getId(), user.toString(), MessageType.TEXT, history);
        if (seedHistory) assertEquals(OutboxState.SENT, await(repository.send(seed)).getState());
        assertEquals(peer, conversation.getTitle());
        ConversationRepository projected = decoration.apply(repository);
        this.controlled = controlled ? new ControlledRepository(projected) : null;
        client = controlled ? this.controlled : projected;
    }

    static MessagingTestFixtures create() throws Exception {
        return new MessagingTestFixtures(USER, EPOCH, PEER, HISTORY, false);
    }

    static MessagingTestFixtures controlled(UUID user, UUID epoch, String peer, String history)
            throws Exception {
        return new MessagingTestFixtures(user, epoch, peer, history, true);
    }

    /** Only for focused UI rendering/capture, not new transport or failure policies. */
    static MessagingTestFixtures presentation(boolean seedHistory,
            java.util.function.UnaryOperator<ConversationRepository> decoration) throws Exception {
        return new MessagingTestFixtures(USER, EPOCH, PEER, HISTORY, true, seedHistory, decoration);
    }

    void assertSeedOnly() throws Exception {
        assertEquals("Only seeded server history", 1,
                await(repository.listMessages(conversation.getId())).size());
        assertEquals("Only seeded outbox", 1,
                await(repository.listOutbox(conversation.getId())).size());
    }

    void releasePending() {
        if (controlled != null) controlled.releasePending();
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
                assertEquals(user.toString(), row.getMessage().getSenderId());
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

    /** One explicitly released send/history result; no timers, transport or worker blocking. */
    static final class ControlledRepository implements ConversationRepository {
        private final ConversationRepository delegate;
        private CompletableFuture<OutboxEntry> sendResult;
        private CompletableFuture<Message> sendEntered;
        private CompletableFuture<List<ServerMessage>> historyResult;
        private CompletableFuture<List<ServerMessage>> historyEntered;
        private boolean holdSend, holdHistory;

        ControlledRepository(ConversationRepository delegate) { this.delegate = delegate; }

        synchronized void holdNextSend() {
            if (sendResult != null) throw new IllegalStateException("One held send per fixture");
            sendResult = new CompletableFuture<>();
            sendEntered = new CompletableFuture<>();
            holdSend = true;
        }

        synchronized void holdNextHistory() {
            if (historyResult != null) throw new IllegalStateException("One held history per fixture");
            historyResult = new CompletableFuture<>();
            historyEntered = new CompletableFuture<>();
            holdHistory = true;
        }

        void awaitSendEntered() throws Exception { await(sendEntered); }
        void awaitHistoryEntered() throws Exception { await(historyEntered); }

        void completeSend() throws Exception {
            Message message = await(sendEntered);
            sendResult.complete(await(delegate.send(message)));
        }

        void completeHistory() throws Exception { historyResult.complete(await(historyEntered)); }

        synchronized void releasePending() {
            if (sendResult != null) sendResult.completeExceptionally(
                    new CancellationException("Test cleanup"));
            if (historyResult != null) historyResult.completeExceptionally(
                    new CancellationException("Test cleanup"));
        }

        @Override public synchronized CompletionStage<OutboxEntry> send(Message message) {
            if (!holdSend) return delegate.send(message);
            holdSend = false;
            sendEntered.complete(message);
            return sendResult;
        }

        @Override public CompletionStage<MessagePage> loadMessages(
                String id, Long before, int limit) {
            return delegate.loadMessages(id, before, limit);
        }

        @Override public CompletionStage<List<Conversation>> listConversations() {
            return delegate.listConversations();
        }
        @Override public CompletionStage<Conversation> openDirect(String peer) {
            return delegate.openDirect(peer);
        }
        @Override public synchronized CompletionStage<List<ServerMessage>> listMessages(String id) {
            if (!holdHistory) return delegate.listMessages(id);
            holdHistory = false;
            delegate.listMessages(id).whenComplete((rows, error) -> {
                if (error == null) historyEntered.complete(rows);
                else historyEntered.completeExceptionally(error);
            });
            return historyResult;
        }
        @Override public CompletionStage<List<OutboxEntry>> listOutbox(String id) {
            return delegate.listOutbox(id);
        }
        @Override public CompletionStage<Void> markRead(String id, ServerMessage through) {
            return delegate.markRead(id, through);
        }
    }

    private static <T> T await(CompletionStage<T> stage) throws Exception {
        return stage.toCompletableFuture().get(5, TimeUnit.SECONDS);
    }
}
