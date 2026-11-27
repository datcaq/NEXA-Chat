package com.nexa.chat.feature.profile;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

public final class ProfileViewModel extends ViewModel {
    enum Phase { VIEWING, EDITING, INVALID_NAME, SAVING, SAVE_ERROR, SAVED }

    private final MutableLiveData<Phase> phase = new MutableLiveData<>(Phase.VIEWING);
    private final Executor mockScheduler;
    private final BooleanSupplier mockSaveSucceeds;
    private String name;
    private String bio;
    private String draftName;
    private String draftBio;
    private boolean cleared;

    public ProfileViewModel() {
        this(defaultMockScheduler(), () -> true);
    }

    // The mock scheduler completes on the main thread.
    ProfileViewModel(Executor mockScheduler, BooleanSupplier mockSaveSucceeds) {
        this.mockScheduler = mockScheduler;
        this.mockSaveSucceeds = mockSaveSucceeds;
    }

    private static Executor defaultMockScheduler() {
        Handler handler = new Handler(Looper.getMainLooper());
        return action -> handler.postDelayed(action, 350);
    }

    void initialize(String mockName, String mockBio) {
        if (name == null) {
            name = mockName;
            bio = mockBio;
        }
    }

    LiveData<Phase> phase() {
        return phase;
    }

    String visibleName() {
        return hasDraft() ? draftName : name;
    }

    String visibleBio() {
        return hasDraft() ? draftBio : bio;
    }

    void edit() {
        if (!hasDraft()) {
            draftName = name;
            draftBio = bio;
            phase.setValue(Phase.EDITING);
        }
    }

    void updateDraft(String nameInput, String bioInput) {
        if (!hasDraft() || phase.getValue() == Phase.SAVING) {
            return;
        }
        draftName = nameInput;
        draftBio = bioInput;
        if (phase.getValue() == Phase.SAVE_ERROR
                || (phase.getValue() == Phase.INVALID_NAME && hasDisplayName(nameInput))) {
            phase.setValue(Phase.EDITING);
        }
    }

    void cancel() {
        if (hasDraft() && phase.getValue() != Phase.SAVING) {
            phase.setValue(Phase.VIEWING);
        }
    }

    void save() {
        if (!hasDraft() || phase.getValue() == Phase.SAVING) {
            return;
        }
        if (!hasDisplayName(draftName)) {
            phase.setValue(Phase.INVALID_NAME);
            return;
        }
        phase.setValue(Phase.SAVING);
        mockScheduler.execute(() -> {
            if (cleared) {
                return;
            }
            if (mockSaveSucceeds.getAsBoolean()) {
                name = draftName.trim();
                bio = draftBio;
                phase.setValue(Phase.SAVED);
            } else {
                phase.setValue(Phase.SAVE_ERROR);
            }
        });
    }

    private boolean hasDraft() {
        return phase.getValue() != Phase.VIEWING && phase.getValue() != Phase.SAVED;
    }

    static boolean hasDisplayName(String value) {
        return value.codePoints().anyMatch(codePoint ->
                !Character.isWhitespace(codePoint) && !Character.isSpaceChar(codePoint));
    }

    @Override
    protected void onCleared() {
        cleared = true;
    }
}
