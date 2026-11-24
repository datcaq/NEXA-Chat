package com.nexa.chat.feature.auth;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public final class PhoneNumberValidator {
    private static final PhoneNumberUtil PHONE_NUMBERS = PhoneNumberUtil.getInstance();

    private PhoneNumberValidator() {}

    public static String toE164(String input, String region) {
        String number = input.trim();
        if (!number.matches("\\+?[\\p{Nd} ().-]+")
                || (!number.startsWith("+") && !PHONE_NUMBERS.getSupportedRegions().contains(region))) {
            return null;
        }

        try {
            PhoneNumber parsed = PHONE_NUMBERS.parse(number, region);
            if (parsed.hasExtension() || !PHONE_NUMBERS.isValidNumber(parsed)) {
                return null;
            }
            if (!number.startsWith("+") && !PHONE_NUMBERS.isValidNumberForRegion(parsed, region)) {
                return null;
            }
            return PHONE_NUMBERS.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            return null;
        }
    }
}
