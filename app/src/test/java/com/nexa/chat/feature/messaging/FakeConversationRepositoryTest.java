package com.nexa.chat.feature.messaging;
import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.FakeConversationRepository;
import org.junit.Test;
import static org.junit.Assert.*;
public class FakeConversationRepositoryTest {
    @Test public void retryAndPagesKeepOneLogicalMessage() {
        FakeConversationRepository r = new FakeConversationRepository("user");
        String c = r.openDirect("peer").toCompletableFuture().join().getId();
        assertEquals(c, r.openDirect("peer").toCompletableFuture().join().getId());
        Message first = new Message("1", c, "user", MessageType.TEXT, "hello");
        r.setFailure(FakeConversationRepository.Failure.OFFLINE);
        assertEquals(OutboxState.PENDING, r.send(first).toCompletableFuture().join().getState());
        r.setFailure(FakeConversationRepository.Failure.NONE);
        r.send(first).toCompletableFuture().join();
        r.send(first).toCompletableFuture().join();
        r.send(new Message("2", c, "user", MessageType.TEXT, "next")).toCompletableFuture().join();
        assertEquals(2, r.listMessages(c).toCompletableFuture().join().size());
        assertEquals(2, r.page(c, Long.MAX_VALUE, 1).toCompletableFuture().join().get(0).getServerSequence());
        assertEquals(1, r.page(c, 2, 1).toCompletableFuture().join().get(0).getServerSequence());
        try {
            r.send(new Message("1", c, "user", MessageType.TEXT, "changed")).toCompletableFuture().join();
            fail("Payload replay must fail");
        } catch (java.util.concurrent.CompletionException expected) { }
    }
}
