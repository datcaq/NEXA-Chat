package com.nexa.chat.feature.home;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class HomeTabTest {
    @Test
    public void restoresEveryPersistedTab() {
        for (HomeTab tab : HomeTab.values()) {
            assertEquals(tab, HomeTab.restore(tab.name()));
        }
    }

    @Test
    public void defaultsToMessagesForMissingOrObsoleteState() {
        assertEquals(HomeTab.MESSAGES, HomeTab.restore(null));
        assertEquals(HomeTab.MESSAGES, HomeTab.restore("UNKNOWN"));
    }
}
