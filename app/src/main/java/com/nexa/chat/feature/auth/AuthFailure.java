package com.nexa.chat.feature.auth;

/** Internal failure categories without server messages or request data. */
public enum AuthFailure {
    UNAVAILABLE,
    RATE_LIMITED,
    REJECTED
}
