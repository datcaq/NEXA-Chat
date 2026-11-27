package com.nexa.chat.feature.profile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProfileViewModelTest {
    @Test
    public void rejectsEmptyAndWhitespaceOnlyNames() {
        assertFalse(ProfileViewModel.hasDisplayName(""));
        assertFalse(ProfileViewModel.hasDisplayName(" \t\n"));
        assertFalse(ProfileViewModel.hasDisplayName("\u00a0\u3000"));
    }

    @Test
    public void acceptsNonBlankNamesWithoutInventingLengthOrScriptPolicy() {
        assertTrue(ProfileViewModel.hasDisplayName("Á"));
        assertTrue(ProfileViewModel.hasDisplayName("山田"));
        assertTrue(ProfileViewModel.hasDisplayName("  Minh An  "));
    }
}
