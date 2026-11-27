package com.nexa.chat.feature.messaging.ui;

/** Main-thread-only origin policy shared by restore and reconciliation. No View references. */
final class DraftInputOrigin {
    private int programmaticDepth;

    boolean isGenuineUserInput() { return programmaticDepth == 0; }

    void synchronize(Runnable update) {
        programmaticDepth++;
        try { update.run(); }
        finally { programmaticDepth--; }
    }
}
