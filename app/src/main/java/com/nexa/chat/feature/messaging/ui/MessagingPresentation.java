package com.nexa.chat.feature.messaging.ui;

import com.nexa.chat.feature.messaging.model.ServerMessage;
import java.util.Objects;

/**
 * Per-surface TV2 eligibility foundation. Defaults inactive. No READ/TYPING dispatch.
 * A validity query is not transport cancellation: future adapters must recheck at handoff.
 */
public final class MessagingPresentation {
    private final MessagingRouter router;
    private boolean attached, selected, resumed;
    private long generation;

    public MessagingPresentation(MessagingRouter router) { this.router = Objects.requireNonNull(router); }
    public void setAttached(boolean value) {
        synchronized (router.gate) { if (attached != value) { attached = value; generation++; } }
    }
    /** PROPOSED host input; must be supplied initially and on tab/resume changes. */
    public boolean setHostState(boolean messagesSelected, boolean hostResumed) {
        synchronized (router.gate) {
            if (selected != messagesSelected || resumed != hostResumed) {
                selected = messagesSelected; resumed = hostResumed; generation++;
                return true;
            }
            return false;
        }
    }
    public boolean isActive() {
        synchronized (router.gate) { return router.isActive() && attached && selected && resumed; }
    }
    public boolean canType(String conversationId, boolean genuineUserInput) {
        synchronized (router.gate) { return genuineUserInput && matches(conversationId); }
    }
    public boolean canRead(ServerMessage message, boolean confirmedViewportEvidence) {
        synchronized (router.gate) {
            return confirmedViewportEvidence && message != null
                    && matches(message.getMessage().getConversationId());
        }
    }
    private boolean matches(String id) {
        return isActive() && id != null && id.equals(router.currentRoute().getConversationId());
    }
    public Token capture() {
        synchronized (router.gate) { return new Token(this, generation, router.currentRoute().getRevision()); }
    }
    public boolean accepts(Token token) {
        synchronized (router.gate) {
            return token != null && token.owner == this && isActive()
                    && token.generation == generation && token.routeRevision == router.currentRoute().getRevision();
        }
    }
    public static final class Token {
        private final MessagingPresentation owner;
        private final long generation, routeRevision;
        private Token(MessagingPresentation owner, long generation, long revision) {
            this.owner = owner; this.generation = generation; routeRevision = revision;
        }
    }
}
