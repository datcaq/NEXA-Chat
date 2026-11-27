package com.nexa.chat.feature.contacts;

import com.nexa.chat.feature.auth.PhoneNumberValidator;

/** Validates an explicit search without retaining the user's input or starting network work. */
final class ContactsSearchInput {
    enum Error { EMPTY, INVALID }

    final String phoneE164;
    final Error error;

    private ContactsSearchInput(String phoneE164, Error error) {
        this.phoneE164 = phoneE164;
        this.error = error;
    }

    static ContactsSearchInput prepare(String input, String region) {
        if (input == null || input.trim().isEmpty()) {
            return new ContactsSearchInput(null, Error.EMPTY);
        }

        String phoneE164 = PhoneNumberValidator.toE164(input, region);
        return phoneE164 == null
                ? new ContactsSearchInput(null, Error.INVALID)
                : new ContactsSearchInput(phoneE164, null);
    }
}
