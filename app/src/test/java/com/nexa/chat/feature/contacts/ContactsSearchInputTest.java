package com.nexa.chat.feature.contacts;

import static com.nexa.chat.feature.contacts.ContactsSearchInput.Error.EMPTY;
import static com.nexa.chat.feature.contacts.ContactsSearchInput.Error.INVALID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class ContactsSearchInputTest {
    @Test
    public void missingAndBlankInputCannotPrepareSearch() {
        assertError(EMPTY, null, "VN");
        assertError(EMPTY, "  ", "US");
    }

    @Test
    public void malformedOrIncompleteInputCannotPrepareSearch() {
        assertError(INVALID, "+12", null);
        assertError(INVALID, "+1 202 555 ABCD", "US");
    }

    @Test
    public void normalizesVietnameseNationalNumber() {
        assertPrepared("+84912345678", "0912 345 678", "VN");
    }

    @Test
    public void normalizesUnitedStatesNationalNumber() {
        assertPrepared("+12025550123", "(202) 555-0123", "US");
    }

    @Test
    public void normalizesJapaneseNationalNumber() {
        assertPrepared("+819012345678", "090-1234-5678", "JP");
    }

    @Test
    public void normalizesSouthKoreanNationalNumber() {
        assertPrepared("+821012345678", "010-1234-5678", "KR");
    }

    @Test
    public void explicitCallingCodeNeedsNoDefaultRegionAndOverridesSelection() {
        assertPrepared("+819012345678", "+81 90-1234-5678", null);
        assertPrepared("+819012345678", "+81 90-1234-5678", "VN");
    }

    @Test
    public void nationalNumberDependsOnExplicitSelection() {
        assertError(INVALID, "090-1234-5678", null);
        assertError(INVALID, "090-1234-5678", "US");
        assertPrepared("+819012345678", "090-1234-5678", "JP");
    }

    private static void assertPrepared(String expected, String raw, String region) {
        ContactsSearchInput result = ContactsSearchInput.prepare(raw, region);
        assertNull(result.error);
        assertEquals(expected, result.phoneE164);
    }

    private static void assertError(ContactsSearchInput.Error expected, String raw, String region) {
        ContactsSearchInput result = ContactsSearchInput.prepare(raw, region);
        assertEquals(expected, result.error);
        assertNull(result.phoneE164);
    }
}
