package com.nexa.chat.feature.shell;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.nexa.chat.MainActivity;
import com.nexa.chat.feature.auth.session.SessionAccessPolicy;
import com.nexa.chat.feature.auth.session.SessionRepository;
import com.nexa.chat.feature.auth.session.SessionSnapshot;

import java.util.UUID;
import java.util.concurrent.Executor;

/** Test-only shell. Its marker views exercise admission without modifying the app launcher. */
final class ShellAdmissionHost implements DefaultLifecycleObserver {
    private final SessionRepository sessions;
    private final LinearLayout surface;
    private final SessionRepository.Subscription subscription;

    private SessionAccessPolicy.Destination destination;
    private UUID userId;
    private UUID epoch;
    private TextView accountContent;

    ShellAdmissionHost(MainActivity activity, SessionRepository sessions, Executor uiExecutor) {
        this.sessions = sessions;
        surface = new LinearLayout(activity);
        surface.setOrientation(LinearLayout.VERTICAL);
        surface.setBackgroundColor(android.graphics.Color.WHITE);
        ((ViewGroup) activity.getWindow().getDecorView()).addView(surface,
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        // Notifications can be stale. Only the latest complete snapshot drives admission.
        render(sessions.currentSnapshot());
        subscription = sessions.observe(uiExecutor, ignored -> render(sessions.currentSnapshot()));
        activity.getLifecycle().addObserver(this);
    }

    SessionAccessPolicy.Destination destination() {
        return destination;
    }

    UUID userId() {
        return userId;
    }

    TextView accountContent() {
        return accountContent;
    }

    void logout() {
        UUID currentEpoch = sessions.currentSnapshot().getSessionEpoch();
        sessions.logout(currentEpoch);
        // Honor synchronous local invalidation even before queued observers run.
        render(sessions.currentSnapshot());
    }

    private void render(SessionSnapshot snapshot) {
        SessionAccessPolicy.Destination next = SessionAccessPolicy.destinationFor(snapshot);
        if (next == SessionAccessPolicy.Destination.HOME
                && snapshot.getCurrentUserId().equals(userId)
                && snapshot.getSessionEpoch().equals(epoch)) {
            return;
        }

        clearAccountContent();
        surface.removeAllViews();
        destination = next;
        TextView label = new TextView(surface.getContext());
        label.setText(next.name());
        surface.addView(label);
        if (next == SessionAccessPolicy.Destination.HOME) {
            userId = snapshot.getCurrentUserId();
            epoch = snapshot.getSessionEpoch();
            accountContent = new TextView(surface.getContext());
            surface.addView(accountContent);
        }
    }

    private void clearAccountContent() {
        if (accountContent != null) {
            accountContent.setText(null);
            accountContent = null;
        }
        userId = null;
        epoch = null;
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        subscription.close();
        clearAccountContent();
        surface.removeAllViews();
        if (surface.getParent() instanceof ViewGroup) {
            ((ViewGroup) surface.getParent()).removeView(surface);
        }
        surface.setVisibility(View.GONE);
        owner.getLifecycle().removeObserver(this);
    }
}
