package com.nexa.chat.feature.messaging.ui;

import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.*;
import org.junit.Test;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

/** JVM simulation of UI bindings, not Android Activity/Emulator tests. */
public class MessagingRouterTest {
    @org.junit.After public void closeScopes() { com.nexa.chat.feature.messaging.ui.TestScopes.closeAll(); }
    private final UUID user = UUID.randomUUID();
    private final FakeConversationRepository fake = new FakeConversationRepository(user.toString());
    private final List<CompletableFuture<Conversation>> direct = new ArrayList<>();
    private final List<CompletableFuture<OutboxEntry>> sends = new ArrayList<>();
    private boolean delayedSend;
    private final ConversationRepository repository = (ConversationRepository) Proxy.newProxyInstance(
            ConversationRepository.class.getClassLoader(), new Class<?>[]{ConversationRepository.class}, (proxy, method, args) -> {
                if (method.getName().equals("openDirect")) {
                    CompletableFuture<Conversation> future = new CompletableFuture<>();
                    direct.add(future); return future;
                }
                if (delayedSend && method.getName().equals("send")) {
                    CompletableFuture<OutboxEntry> future = new CompletableFuture<>();
                    sends.add(future); return future;
                }
                try { return method.invoke(fake, args); }
                catch (InvocationTargetException error) { throw error.getCause(); }
            });
    private final MessagingController controller = com.nexa.chat.feature.messaging.ui.TestScopes.create(user, UUID.randomUUID(), repository);
    private final MessagingRouter router = controller.getRouter();
    private final String chat = fake.openDirect("peer").toCompletableFuture().join().getId();
    private MessagingPresentation active() {
        MessagingPresentation p = new MessagingPresentation(router);
        p.setAttached(true); p.setHostState(true, true); router.openConversation(chat); return p;
    }
    private Conversation conversation(String id) { return new Conversation(id, ConversationType.DIRECT); }
    private ServerMessage message() {
        return new ServerMessage(new Message("client", chat, "peer", MessageType.TEXT, "hello"),
                new ServerAcknowledgement("client", "server", 1), MessageStatus.NONE);
    }
    @Test public void initialList() { assertEquals(MessagingRouter.Screen.CONVERSATION_LIST, router.currentRoute().getScreen()); }
    @Test public void opensDetail() { router.openConversation(chat); assertEquals(chat, router.currentRoute().getConversationId()); }
    @Test public void sameConversationDoesNotPublishTransition() {
        AtomicInteger count = new AtomicInteger(); router.observe(Runnable::run, count::incrementAndGet);
        router.openConversation(chat); long revision = router.currentRoute().getRevision();
        router.openConversation(chat); assertEquals(revision, router.currentRoute().getRevision()); assertEquals(2, count.get());
    }
    @Test public void differentConversationChangesRoute() { router.openConversation(chat); router.openConversation("B"); assertEquals("B", router.currentRoute().getConversationId()); }
    @Test public void backConsumesDetailOnce() { router.openConversation(chat); assertTrue(router.handleBack()); assertFalse(router.handleBack()); assertNull(router.currentRoute().getConversationId()); }
    @Test public void rootBackDoesNotPublishOrExitScope() {
        AtomicInteger count = new AtomicInteger(); router.observe(Runnable::run, count::incrementAndGet);
        assertFalse(router.handleBack()); assertEquals(1,count.get()); assertTrue(controller.isActive()); assertTrue(router.isActive());
    }
    @Test public void backKeepsDraftAndRetry() {
        controller.editDraft(chat,"draft"); fake.setFailure(FakeConversationRepository.Failure.OFFLINE);
        OutboxEntry entry = controller.sendDraft(chat).toCompletableFuture().join();
        router.openConversation(chat); router.handleBack();
        assertTrue(controller.canRetry(entry.getMessage().getClientMessageId())); assertTrue(controller.isActive());
        controller.editDraft(chat,"new"); router.openConversation(chat); router.handleBack(); assertEquals("new",controller.draft(chat));
    }
    @Test public void directSuccessNavigatesOnce() {
        AtomicInteger count = new AtomicInteger(); router.observe(Runnable::run,count::incrementAndGet);
        CompletionStage<Conversation> result = controller.openDirectAndNavigate("peer");
        direct.get(0).complete(conversation(chat)); result.toCompletableFuture().join();
        direct.get(0).complete(conversation("other")); assertEquals(chat,router.currentRoute().getConversationId()); assertEquals(2,count.get());
    }
    @Test public void lateAAfterBDoesNotWin() {
        controller.openDirectAndNavigate("A"); controller.openDirectAndNavigate("B");
        direct.get(1).complete(conversation("B")); direct.get(0).complete(conversation("A"));
        assertEquals("B",router.currentRoute().getConversationId());
    }
    @Test public void backAtListCancelsPendingDirect() {
        controller.openDirectAndNavigate("A"); assertFalse(router.handleBack());
        direct.get(0).complete(conversation("A")); assertNull(router.currentRoute().getConversationId());
    }
    @Test public void explicitRouteCancelsPendingDirect() {
        controller.openDirectAndNavigate("A"); router.openConversation("B");
        direct.get(0).complete(conversation("A")); assertEquals("B",router.currentRoute().getConversationId());
    }
    @Test public void invalidationDropsLateDirect() {
        controller.openDirectAndNavigate("A"); controller.invalidate(); direct.get(0).complete(conversation("A"));
        assertFalse(router.isActive()); assertNull(router.currentRoute().getConversationId()); router.openConversation("B");
        assertNull(router.currentRoute().getConversationId());
    }
    @Test public void routeRetainedAcrossSubscriptions() {
        router.openConversation(chat); router.observe(Runnable::run,()->{}).close();
        List<String> rendered = new ArrayList<>();
        router.observe(Runnable::run,()->rendered.add(router.currentRoute().getConversationId()));
        assertEquals(Collections.singletonList(chat),rendered); assertSame(router,controller.getRouter());
    }
    @Test public void recreationRetainsInFlightAndDraft() {
        delayedSend = true; controller.editDraft(chat,"draft"); controller.sendDraft(chat); router.openConversation(chat);
        router.observe(Runnable::run,()->{}).close(); router.observe(Runnable::run,()->{});
        assertEquals("draft",controller.draft(chat)); assertTrue(controller.isSending(chat));
        assertEquals(chat,router.currentRoute().getConversationId()); assertSame(router.getSessionEpoch(),controller.getSessionEpoch());
    }
    @Test public void defaultsInactive() {
        MessagingPresentation p = new MessagingPresentation(router); p.setAttached(true); router.openConversation(chat);
        assertFalse(p.isActive()); assertFalse(p.canRead(message(),true)); assertFalse(p.canType(chat,true));
    }
    @Test public void inactiveTabBlocksReadAndTyping() {
        MessagingPresentation p = active(); p.setHostState(false,true);
        assertFalse(p.canRead(message(),true)); assertFalse(p.canType(chat,true));
    }
    @Test public void pausedHostBlocksReadAndTyping() {
        MessagingPresentation p = active(); p.setHostState(true,false);
        assertFalse(p.canRead(message(),true)); assertFalse(p.canType(chat,true));
    }
    @Test public void listBlocksReadAndTyping() {
        MessagingPresentation p = active(); router.showList();
        assertFalse(p.canRead(message(),true)); assertFalse(p.canType(chat,true));
    }
    @Test public void visibleEvidenceRequired() {
        MessagingPresentation p = active(); assertFalse(p.canRead(message(),false));
        assertFalse(p.canRead(null,true)); assertTrue(p.canRead(message(),true));
    }
    @Test public void actualUserInputAndMatchingConversationRequired() {
        MessagingPresentation p = active(); assertFalse(p.canType(chat,false));
        assertFalse(p.canType("other",true)); assertTrue(p.canType(chat,true));
    }
    @Test public void deactivateReactivateRejectsOldQueuedToken() {
        MessagingPresentation p = active(); MessagingPresentation.Token token = p.capture();
        p.setHostState(false,true); p.setHostState(true,true); assertFalse(p.accepts(token)); assertTrue(p.accepts(p.capture()));
    }
    @Test public void routeChangeRejectsQueuedToken() {
        MessagingPresentation p = active(); MessagingPresentation.Token token = p.capture();
        router.openConversation("B"); router.openConversation(chat); assertFalse(p.accepts(token));
    }
    @Test public void pendingBackRejectsNavigationWaiterToken() {
        MessagingPresentation p = active(); controller.openDirectAndNavigate("A");
        MessagingPresentation.Token token = p.capture(); router.handleBack(); assertFalse(p.accepts(token));
    }
    @Test public void detachedBindingCannotDeliverToReplacement() {
        MessagingPresentation old = active(); MessagingPresentation.Token token = old.capture(); old.setAttached(false);
        MessagingPresentation replacement = active();
        assertFalse(old.accepts(token)); assertFalse(replacement.accepts(token)); assertEquals(chat,router.currentRoute().getConversationId());
    }
    @Test public void ackReconcilesWhileInactiveAndDetached() {
        delayedSend = true; MessagingPresentation p = active(); controller.editDraft(chat,"hello");
        CompletionStage<OutboxEntry> request = controller.sendDraft(chat);
        Message original = controller.localEntries(chat).get(0).getMessage();
        p.setHostState(false,false); p.setAttached(false);
        sends.get(0).complete(fake.send(original).toCompletableFuture().join());
        assertEquals(OutboxState.SENT,request.toCompletableFuture().join().getState());
        assertEquals("",controller.draft(chat)); assertTrue(controller.isActive());
    }
    @Test public void invalidationEndsPresentationAndDoesNotShareAccountState() {
        MessagingPresentation p = active(); MessagingPresentation.Token token = p.capture(); controller.invalidate();
        MessagingController b = com.nexa.chat.feature.messaging.ui.TestScopes.create(UUID.randomUUID(),UUID.randomUUID(),new FakeConversationRepository("B"));
        assertFalse(p.accepts(token)); assertFalse(p.canType(chat,true)); assertNull(b.getRouter().currentRoute().getConversationId());
        assertNotEquals(router.getSessionEpoch(),b.getRouter().getSessionEpoch());
    }
    @Test public void closedQueuedObserverDoesNotRender() {
        QueueExecutor q = new QueueExecutor(); AtomicInteger count = new AtomicInteger();
        MessagingRouter.Subscription s = router.observe(q,count::incrementAndGet); s.close(); router.openConversation(chat);
        q.drain(); assertEquals(0,count.get());
    }
    @Test public void repeatedSubscriptionsDoNotDuplicateCallbacks() {
        QueueExecutor q = new QueueExecutor(); AtomicInteger count = new AtomicInteger();
        for(int i=0;i<5;i++) router.observe(q,count::incrementAndGet).close();
        router.observe(q,count::incrementAndGet); q.drain(); assertEquals(1,count.get());
    }
    @Test(timeout=10000) public void reentrantObserverUsesLatestRouteWithoutRecursion() {
        List<String> routes = new ArrayList<>();
        router.observe(Runnable::run,()-> { String id=router.currentRoute().getConversationId(); routes.add(id); if(id==null)router.openConversation(chat); });
        assertEquals(Arrays.asList(null,chat),routes);
    }
    @Test public void throwingAndRejectingObserversDoNotBreakRoute() {
        assertFalse(router.observe(Runnable::run,()->{throw new IllegalStateException();}).isOpen());
        assertFalse(router.observe(r->{throw new RejectedExecutionException();},()->{}).isOpen());
        router.openConversation(chat); assertEquals(chat,router.currentRoute().getConversationId());
    }
    @Test public void slowObserverDoesNotHoldScopeGate() throws Exception {
        CountDownLatch entered=new CountDownLatch(1), release=new CountDownLatch(1);
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Future<?> observe=pool.submit(()->router.observe(Runnable::run,()->{entered.countDown(); await(release);}));
            await(entered); pool.submit(() -> controller.invalidate()).get(5,TimeUnit.SECONDS);
            assertFalse(router.isActive()); release.countDown(); observe.get(5,TimeUnit.SECONDS);
        } finally {release.countDown();pool.shutdownNow();}
    }
    @Test public void subscribeInvalidateRace() throws Exception {
        QueueExecutor q=new QueueExecutor(); CountDownLatch start=new CountDownLatch(1); AtomicInteger count=new AtomicInteger();
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Future<MessagingRouter.Subscription> s=pool.submit(()->{await(start);return router.observe(q,count::incrementAndGet);});
            Future<?> close=pool.submit(()->{await(start);controller.invalidate();}); start.countDown();
            MessagingRouter.Subscription handle=s.get(5,TimeUnit.SECONDS);close.get(5,TimeUnit.SECONDS);q.drain();
            assertFalse(handle.isOpen());assertEquals(0,count.get());
        } finally {start.countDown();pool.shutdownNow();}
    }
    @Test public void notificationsCoalesceAndPullLatestRoute() {
        QueueExecutor q=new QueueExecutor(); List<String> seen=new ArrayList<>();
        router.observe(q,()->seen.add(router.currentRoute().getConversationId()));
        router.openConversation("A"); router.openConversation("B");q.drain();assertEquals(Collections.singletonList("B"),seen);
    }
    @Test public void explicitLoadIsAllowedOnUnchangedRoute() {
        router.openConversation(chat); long rev=router.currentRoute().getRevision();
        assertNotNull(controller.loadMessages(chat).toCompletableFuture().join());
        assertNotNull(controller.loadMessages(chat).toCompletableFuture().join()); assertEquals(rev,router.currentRoute().getRevision());
    }
    @Test public void titleSurvivesRecreationAndSameRouteDoesNotInvalidatePresentation() {
        MessagingPresentation p = active();
        router.showList(); router.openConversation(chat, "Peer title");
        MessagingPresentation.Token token = p.capture();
        router.openConversation(chat, "Ignored duplicate title");
        assertTrue(p.accepts(token));
        router.observe(Runnable::run, () -> {}).close();
        assertEquals("Peer title", router.currentRoute().getTitle());
    }
    @Test public void oldAccountDirectCompletionDoesNotNavigateNewAccount() {
        controller.openDirectAndNavigate("A"); controller.invalidate();
        MessagingController next = com.nexa.chat.feature.messaging.ui.TestScopes.create(user, UUID.randomUUID(), fake);
        next.getRouter().openConversation("B"); direct.get(0).complete(conversation("A"));
        assertEquals("B", next.getRouter().currentRoute().getConversationId());
        assertNull(router.currentRoute().getConversationId());
    }
    @Test public void invalidConversationIsRejected() { assertThrows(IllegalArgumentException.class,()->router.openConversation(" ")); }
    private static void await(CountDownLatch latch) {
        try {assertTrue(latch.await(5,TimeUnit.SECONDS));} catch(InterruptedException e){Thread.currentThread().interrupt();throw new AssertionError(e);}
    }
    private static class QueueExecutor implements Executor {
        private final Queue<Runnable> queue=new ConcurrentLinkedQueue<>();
        public void execute(Runnable r){queue.add(r);}
        void drain(){Runnable r;while((r=queue.poll())!=null)r.run();}
    }
}
