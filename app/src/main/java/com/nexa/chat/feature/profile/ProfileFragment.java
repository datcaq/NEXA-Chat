package com.nexa.chat.feature.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nexa.chat.R;

public final class ProfileFragment extends Fragment {
    private final ViewModelProvider.Factory factory;

    public ProfileFragment() {
        this(null);
    }

    ProfileFragment(@Nullable ViewModelProvider.Factory factory) {
        super(R.layout.fragment_profile);
        this.factory = factory;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ProfileViewModel model = (factory == null ? new ViewModelProvider(this)
                : new ViewModelProvider(this, factory)).get(ProfileViewModel.class);
        model.initialize(getString(R.string.profile_mock_name), "");
        ViewCompat.setOnApplyWindowInsetsListener(view, (root, insets) -> {
            Insets safeArea = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            root.setPadding(safeArea.left, safeArea.top, safeArea.right, safeArea.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(view);

        TextInputEditText name = view.findViewById(R.id.profile_name);
        TextInputEditText bio = view.findViewById(R.id.profile_bio);
        TextInputLayout nameField = view.findViewById(R.id.profile_name_field);
        TextView feedback = view.findViewById(R.id.profile_feedback);
        View edit = view.findViewById(R.id.profile_edit);
        View save = view.findViewById(R.id.profile_save);
        View cancel = view.findViewById(R.id.profile_cancel);
        View progress = view.findViewById(R.id.profile_progress);

        TextWatcher draftWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable text) {
                model.updateDraft(name.getText().toString(), bio.getText().toString());
            }
        };
        edit.setOnClickListener(v -> model.edit());
        cancel.setOnClickListener(v -> model.cancel());
        save.setOnClickListener(v -> model.save());

        model.phase().observe(getViewLifecycleOwner(), phase -> {
            // Updating both fields must not overwrite the retained draft during recreation.
            name.removeTextChangedListener(draftWatcher);
            bio.removeTextChangedListener(draftWatcher);
            if (!model.visibleName().contentEquals(name.getText())) {
                name.setText(model.visibleName());
            }
            if (!model.visibleBio().contentEquals(bio.getText())) {
                bio.setText(model.visibleBio());
            }
            name.addTextChangedListener(draftWatcher);
            bio.addTextChangedListener(draftWatcher);

            boolean viewing = phase == ProfileViewModel.Phase.VIEWING
                    || phase == ProfileViewModel.Phase.SAVED;
            boolean saving = phase == ProfileViewModel.Phase.SAVING;
            name.setEnabled(!viewing && !saving);
            bio.setEnabled(!viewing && !saving);
            edit.setVisibility(viewing ? View.VISIBLE : View.GONE);
            cancel.setVisibility(viewing ? View.GONE : View.VISIBLE);
            save.setVisibility(viewing ? View.GONE : View.VISIBLE);
            cancel.setEnabled(!saving);
            save.setEnabled(!saving);
            progress.setVisibility(saving ? View.VISIBLE : View.GONE);
            nameField.setError(phase == ProfileViewModel.Phase.INVALID_NAME
                    ? getText(R.string.profile_name_required) : null);

            int message = 0;
            if (phase == ProfileViewModel.Phase.SAVED) {
                message = R.string.profile_saved_mock;
            } else if (phase == ProfileViewModel.Phase.SAVE_ERROR) {
                message = R.string.profile_save_error;
            } else if (saving) {
                message = R.string.profile_saving;
            }
            feedback.setVisibility(message == 0 ? View.GONE : View.VISIBLE);
            if (message != 0) {
                feedback.setText(message);
            }
        });
    }
}
