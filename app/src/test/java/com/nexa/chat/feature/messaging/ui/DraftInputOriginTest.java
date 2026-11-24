package com.nexa.chat.feature.messaging.ui;

import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.*;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

/** Exercises the policy called by TextWatcher; does not instantiate Android EditText. */
public class DraftInputOriginTest {
    private final MessagingLogic logic = new MessagingLogic(new HashMap<>());
    private final DraftInputOrigin origin = new DraftInputOrigin();
    private void changed(String text) { if (origin.isGenuineUserInput()) logic.edit("c", text); }
    @Test public void genuineInputUpdatesDraft() {
        changed("user"); assertEquals("user", logic.draft("c")); assertEquals(1, logic.revision("c"));
    }
    @Test public void programmaticRestoreIsNotGenuineAndDoesNotEditRevision() {
        logic.edit("c", "saved"); long revision = logic.revision("c");
        origin.synchronize(() -> { assertFalse(origin.isGenuineUserInput()); changed("saved"); });
        assertEquals("saved", logic.draft("c")); assertEquals(revision, logic.revision("c"));
    }
    @Test public void ackClearRenderDoesNotIncrementRevisionAgain() {
        changed("hello"); long sentRevision = logic.revision("c");
        Message message = new Message("id", "c", "u", MessageType.TEXT, "hello");
        logic.begin(message, false);
        logic.finish(message, sentRevision, OutboxEntry.draft(message).transitionTo(OutboxState.PENDING), null);
        long clearedRevision = logic.revision("c");
        origin.synchronize(() -> changed(""));
        assertEquals("", logic.draft("c")); assertEquals(sentRevision + 1, clearedRevision);
        assertEquals(clearedRevision, logic.revision("c"));
    }
    @Test public void exceptionRestoresGuardAndNextUserInputWorks() {
        assertThrows(IllegalStateException.class, () -> origin.synchronize(() -> { throw new IllegalStateException(); }));
        assertTrue(origin.isGenuineUserInput()); changed("next"); assertEquals("next", logic.draft("c"));
    }
    @Test public void nestedSynchronizationDoesNotReleaseOuterGuard() {
        origin.synchronize(() -> {
            origin.synchronize(() -> changed("inner"));
            assertFalse(origin.isGenuineUserInput()); changed("outer");
        });
        assertEquals(0, logic.revision("c")); changed("user"); assertEquals("user", logic.draft("c"));
    }
    @Test public void oldAckDoesNotClearNewDraftAndRenderDoesNotReclassifyIt() {
        changed("old"); long revision = logic.revision("c");
        Message message = new Message("id", "c", "u", MessageType.TEXT, "old");
        logic.begin(message, false); changed("new"); long newRevision = logic.revision("c");
        logic.finish(message, revision, OutboxEntry.draft(message).transitionTo(OutboxState.PENDING), null);
        origin.synchronize(() -> changed(logic.draft("c")));
        assertEquals("new", logic.draft("c")); assertEquals(newRevision, logic.revision("c"));
    }
}
