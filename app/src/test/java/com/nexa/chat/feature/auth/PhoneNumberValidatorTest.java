package com.nexa.chat.feature.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class PhoneNumberValidatorTest {
    @Test
    public void vietnameseNationalAndInternationalFormatsMatch() {
        assertEquals("+84912345678", PhoneNumberValidator.toE164("0912 345 678", "VN"));
        assertEquals("+84912345678", PhoneNumberValidator.toE164("+84 912 345 678", null));
    }

    @Test
    public void acceptsAnInternationalNumberWithoutSelectingARegion() {
        assertEquals("+14155552671", PhoneNumberValidator.toE164("+1 (415) 555-2671", null));
    }

    @Test
    public void acceptsExplicitCallingCodeWhenAnotherRegionIsSelected() {
        assertEquals("+14155552671", PhoneNumberValidator.toE164("+1 415 555 2671", "VN"));
    }

    @Test
    public void rejectsInvalidNumbersAndExtensions() {
        assertNull(PhoneNumberValidator.toE164("", "VN"));
        assertNull(PhoneNumberValidator.toE164("0912", "VN"));
        assertNull(PhoneNumberValidator.toE164("0912345678", null));
        assertNull(PhoneNumberValidator.toE164("+14155552671 ext. 42", "US"));
        assertNull(PhoneNumberValidator.toE164("+141555ABCDE", "US"));
    }
}
