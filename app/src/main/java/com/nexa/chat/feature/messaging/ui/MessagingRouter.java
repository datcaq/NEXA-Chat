package com.nexa.chat.feature.messaging.ui;

import com.nexa.chat.feature.messaging.ChatNavigator;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * TV2 internal account router, not a Home contract. Owned by MessagingController.
 * Route mutation and account invalidation share gate. Callbacks/executors run outside gate.
 * Notifications are serialized pull-latest signals; an already claimed callback may finish.
 */
public final class MessagingRouter implements ChatNavigator {
    public enum Screen { CONVERSATION_LIST, CHAT_DETAIL }
    public static final class Route {
        private final String conversationId;
        private final String title;
        private final long revision;
        private Route(String id, long revision) { this(id, revision, id == null ? "" : id); }
        private Route(String id, long revision, String title) { conversationId = id; this.revision = revision; this.title = title; }
        public String getTitle() { return title; }
        public Screen getScreen() { return conversationId == null ? Screen.CONVERSATION_LIST : Screen.CHAT_DETAIL; }
        public String getConversationId() { return conversationId; }
        public long getRevision() { return revision; }
    }
    final Object gate;
    private final UUID userId, epoch;
    private final Set<Subscription> observers = new HashSet<>();
    private Route route = new Route(null, 0);
    private boolean active = true;
    private long intent;

    MessagingRouter(UUID userId, UUID epoch, Object gate) {
        this.userId = userId; this.epoch = epoch; this.gate = gate;
    }
    public UUID getCurrentUserId() { return userId; }
    public UUID getSessionEpoch() { return epoch; }
    public boolean isActive() { synchronized (gate) { return active; } }
    public Route currentRoute() { synchronized (gate) { return route; } }

    @Override public void openConversation(String id) { openConversation(id, id); }
    public void openConversation(String id, String title) {
        requireId(id);
        Objects.requireNonNull(title);
        boolean changed;
        synchronized (gate) {
            if (!active) return;
            intent++;
            changed = setRouteLocked(id, title);
        }
        if (changed) publish();
    }
    public void showList() {
        boolean changed;
        synchronized (gate) {
            if (!active) return;
            intent++;
            changed = setRouteLocked(null);
        }
        if (changed) publish();
    }
    @Override public void back() { handleBack(); }
    public boolean handleBack() {
        boolean consumed;
        synchronized (gate) {
            if (!active) return false;
            intent++; // Also cancels a pending DIRECT intent while already at list.
            consumed = route.conversationId != null;
            if (consumed) setRouteLocked(null);
        }
        if (consumed) publish();
        return consumed;
    }
    long navigationIntent() { synchronized (gate) { return intent; } }
    long beginDirect() {
        synchronized (gate) { return active ? ++intent : -1; }
    }
    boolean isCurrentIntent(long token) {
        synchronized (gate) { return active && token >= 0 && intent == token; }
    }
    void completeDirect(long token, String id, String title) {
        boolean changed;
        synchronized (gate) { changed = completeDirectLocked(token, id, title); }
        if (changed) publish();
    }
    // Owner calls under the shared gate so operation reconciliation and navigation are atomic.
    boolean completeDirectLocked(long token, String id, String title) {
        requireId(id);
        if (!active || token < 0 || token != intent) return false;
        intent++;
        return setRouteLocked(id, title);
    }
    private boolean setRouteLocked(String id) { return setRouteLocked(id, id == null ? "" : id); }
    private boolean setRouteLocked(String id, String title) {
        if (Objects.equals(route.conversationId, id)) return false;
        route = new Route(id, route.revision + 1, title);
        return true;
    }
    // Called ONLY by the owner while holding the shared account gate; no external calls.
    void invalidateLocked() {
        active = false;
        intent++;
        route = new Route(null, route.revision + 1);
        for (Subscription observer : new ArrayList<>(observers)) observer.closeLocked();
    }
    private static void requireId(String id) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Blank conversation ID");
    }
    public Subscription observe(Executor executor, Runnable listener) {
        Subscription subscription = new Subscription(Objects.requireNonNull(executor), Objects.requireNonNull(listener));
        synchronized (gate) {
            if (active) observers.add(subscription);
            else subscription.closeLocked();
        }
        subscription.signal();
        return subscription;
    }
    void publish() {
        List<Subscription> copy;
        synchronized (gate) { copy = new ArrayList<>(observers); }
        for (Subscription observer : copy) observer.signal();
    }
    public final class Subscription implements AutoCloseable {
        private Executor executor;
        private Runnable listener;
        private boolean closed, dirty, scheduled;
        private Subscription(Executor executor, Runnable listener) { this.executor = executor; this.listener = listener; }
        private void signal() {
            Executor delivery;
            synchronized (gate) {
                if (closed || !active) return;
                dirty = true;
                if (scheduled) return;
                scheduled = true;
                delivery = executor;
            }
            try { delivery.execute(this::drain); }
            catch (RuntimeException rejected) { close(); }
        }
        private void drain() {
            while (true) {
                Runnable callback;
                synchronized (gate) {
                    if (closed || !active || !dirty) { scheduled = false; return; }
                    dirty = false;
                    callback = listener;
                }
                try { callback.run(); }
                catch (RuntimeException failure) { close(); return; }
            }
        }
        private void closeLocked() {
            closed = true; dirty = false; listener = null; executor = null; observers.remove(this);
        }
        public boolean isOpen() { synchronized (gate) { return !closed; } }
        @Override public void close() { synchronized (gate) { closeLocked(); } }
    }
}
