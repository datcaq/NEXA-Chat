package com.nexa.chat.feature.auth;

import java.util.Objects;

/** Opaque reference to a registration challenge. It carries no verification or session data. */
public final class RegistrationChallenge {
    private final String handle;

    public RegistrationChallenge(String handle) {
        this.handle = Objects.requireNonNull(handle, "handle");
        if (handle.isEmpty()) {
            throw new IllegalArgumentException("Challenge handle must not be empty");
        }
    }

    public String getHandle() {
        return handle;
    }

    @Override
    public String toString() {
        return "RegistrationChallenge";
    }
}
