package com.nexa.chat.feature.messaging.repository;

import com.nexa.chat.feature.messaging.model.Conversation;
import com.nexa.chat.feature.messaging.model.Message;
import com.nexa.chat.feature.messaging.model.OutboxEntry;
import com.nexa.chat.feature.messaging.model.ServerMessage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * PROVISIONAL feature-local domain port, pending MASTER SPEC and TV1/TV3 review.
 * Not core/contracts, not a REST/WS DTO. No implementation or network calls.
 * Implementations must not block the Android main thread.
 */
public interface ConversationRepository {
    CompletionStage<List<Conversation>> listConversations();

    /** Current user comes from the future auth boundary, never a caller-supplied sender. */
    CompletionStage<Conversation> openDirect(String peerUserId);

    /**
     * Result order must use serverSequence, not device time.
     * Confirmed history only; local pending rows belong to the outbox projection.
     * Pagination and merge policy await the shared contract.
     */
    CompletionStage<List<ServerMessage>> listMessages(String conversationId);

    /** PROPOSED TV2-local pagination: null starts at newest, results ascending. */
    CompletionStage<MessagePage> loadMessages(String conversationId, Long beforeSequence, int limit);
    /** Local projection for rendering pending/failed rows. */
    CompletionStage<List<OutboxEntry>> listOutbox(String conversationId);

    /**
     * Enqueue/retry the SAME logical ID and frozen payload.
     * Completion reports local outbox state, not guaranteed network delivery.
     * Repository must reconcile by clientMessageId and enforce sender authority.
     */
    CompletionStage<OutboxEntry> send(Message message);

    /**
     * Read through a confirmed message in the same conversation.
     * Carries both server ID and sequence; does not choose the wire cursor.
     * Adapter and per-user GROUP semantics await TV1/TV3 approval.
     */
    CompletionStage<Void> markRead(String conversationId, ServerMessage throughMessage);
}
