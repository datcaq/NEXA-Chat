package com.nexa.chat.feature.home;

enum HomeTab {
    MESSAGES,
    CONTACTS,
    GROUPS,
    PROFILE;

    static HomeTab restore(String savedName) {
        for (HomeTab tab : values()) {
            if (tab.name().equals(savedName)) {
                return tab;
            }
        }
        return MESSAGES;
    }
}
