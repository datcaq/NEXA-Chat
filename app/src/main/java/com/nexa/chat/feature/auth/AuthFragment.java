package com.nexa.chat.feature.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.nexa.chat.R;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AuthFragment extends Fragment {
    private static final String REGION_KEY = "auth_region";

    private final ViewModelProvider.Factory viewModelFactory;
    private AuthViewModel viewModel;
    private TextWatcher phoneWatcher;
    private boolean ownsSecureWindowFlag;
    private String region;

    public AuthFragment() {
        this(null);
    }

    AuthFragment(@Nullable ViewModelProvider.Factory viewModelFactory) {
        super(R.layout.fragment_auth);
        this.viewModelFactory = viewModelFactory;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewModelProvider provider = viewModelFactory == null
                ? new ViewModelProvider(this) : new ViewModelProvider(this, viewModelFactory);
        viewModel = provider.get(AuthViewModel.class);
        region = savedInstanceState == null ? null : savedInstanceState.getString(REGION_KEY);

        ViewCompat.setOnApplyWindowInsetsListener(view, (root, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            root.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(view);

        MaterialAutoCompleteTextView country = view.findViewById(R.id.auth_country);
        TextInputEditText phone = view.findViewById(R.id.auth_phone);
        TextInputEditText otp = view.findViewById(R.id.auth_otp);
        TextInputEditText password = view.findViewById(R.id.auth_password);
        TextInputLayout passwordField = view.findViewById(R.id.auth_password_field);
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                protectPasswordFromCapture(text.length() != 0);
                if (text.length() != 0) {
                    passwordField.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable text) {}
        });
        TextInputLayout countryField = view.findViewById(R.id.auth_country_field);
        TextInputLayout phoneField = view.findViewById(R.id.auth_phone_field);
        TextInputLayout otpField = view.findViewById(R.id.auth_otp_field);

        setupCountries(country, countryField, phoneField);
        view.findViewById(R.id.auth_submit).setOnClickListener(v -> {
            countryField.setError(null);
            phoneField.setError(null);
            otpField.setError(null);

            String number = null;
            if (viewModel.screen() == AuthViewModel.Screen.OTP) {
                if (!input(otp).matches("[0-9]+")) {
                    otpField.setError(getString(R.string.auth_error_otp));
                    otp.requestFocus();
                    return;
                }
                otp.setText(null);
            } else {
                number = input(phone);
                if (number.isEmpty()) {
                    phoneField.setError(getString(R.string.auth_error_phone_required));
                    phone.requestFocus();
                    return;
                }
                if (region == null && !number.startsWith("+")) {
                    countryField.setError(getString(R.string.auth_error_country));
                    country.requestFocus();
                    return;
                }
            }

            if (viewModel.screen() == AuthViewModel.Screen.SIGN_IN && password.length() == 0) {
                passwordField.setError(getString(R.string.auth_error_password_required));
                password.requestFocus();
                return;
            }

            if (!viewModel.submit(number, region)) {
                phoneField.setError(getString(R.string.auth_error_phone_invalid));
                phone.requestFocus();
                return;
            }
            password.setText(null);
            WindowCompat.getInsetsController(requireActivity().getWindow(), view)
                    .hide(WindowInsetsCompat.Type.ime());
        });

        view.findViewById(R.id.auth_switch).setOnClickListener(v -> {
            countryField.setError(null);
            phoneField.setError(null);
            otpField.setError(null);
            otp.setText(null);
            passwordField.setError(null);
            password.setText(null);
            AuthViewModel.Screen next;
            switch (viewModel.screen()) {
                case SIGN_IN:
                    next = AuthViewModel.Screen.REGISTER;
                    break;
                case REGISTER:
                    next = AuthViewModel.Screen.SIGN_IN;
                    break;
                default:
                    next = AuthViewModel.Screen.REGISTER;
            }
            viewModel.show(next);
        });

        viewModel.state().observe(getViewLifecycleOwner(), state -> render(view, state));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // Restored widget text is not a new edit and must not cancel a retained attempt.
        TextInputEditText phone = requireView().findViewById(R.id.auth_phone);
        phoneWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                viewModel.phoneChanged();
            }

            @Override
            public void afterTextChanged(Editable text) {}
        };
        phone.addTextChangedListener(phoneWatcher);
    }

    @Override
    public void onDestroyView() {
        if (phoneWatcher != null) {
            TextInputEditText phone = requireView().findViewById(R.id.auth_phone);
            phone.removeTextChangedListener(phoneWatcher);
            phoneWatcher = null;
        }
        protectPasswordFromCapture(false);
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(REGION_KEY, region);
        super.onSaveInstanceState(outState);
    }

    private void protectPasswordFromCapture(boolean containsPassword) {
        Window window = requireActivity().getWindow();
        int secureFlag = WindowManager.LayoutParams.FLAG_SECURE;
        if (containsPassword && (window.getAttributes().flags & secureFlag) == 0) {
            window.addFlags(secureFlag);
            ownsSecureWindowFlag = true;
        } else if (!containsPassword && ownsSecureWindowFlag) {
            window.clearFlags(secureFlag);
            ownsSecureWindowFlag = false;
        }
    }

    private void setupCountries(MaterialAutoCompleteTextView country, TextInputLayout countryField,
                                TextInputLayout phoneField) {
        Locale locale = getResources().getConfiguration().getLocales().get(0);
        Collator collator = Collator.getInstance(locale);
        PhoneNumberUtil phoneNumbers = PhoneNumberUtil.getInstance();
        List<String> regions = new ArrayList<>(phoneNumbers.getSupportedRegions());
        regions.sort((a, b) -> collator.compare(countryName(a, locale), countryName(b, locale)));

        List<String> labels = new ArrayList<>(regions.size());
        for (String code : regions) {
            labels.add(getString(R.string.auth_country_option, countryName(code, locale),
                    phoneNumbers.getCountryCodeForRegion(code)));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, labels);
        country.setAdapter(adapter);
        int selected = regions.indexOf(region);
        if (selected >= 0) {
            country.setText(labels.get(selected), false);
        }
        country.setOnItemClickListener((parent, selectedView, position, id) -> {
            String selectedRegion = regions.get(position);
            if (!selectedRegion.equals(region)) {
                region = selectedRegion;
                viewModel.phoneChanged();
            }
            countryField.setError(null);
            phoneField.setError(null);
        });
    }

    private String countryName(String code, Locale language) {
        return new Locale.Builder().setRegion(code).build().getDisplayCountry(language);
    }

    private static String input(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private void render(View view, AuthViewModel.State state) {
        boolean signIn = state.screen == AuthViewModel.Screen.SIGN_IN;
        boolean otp = state.screen == AuthViewModel.Screen.OTP;
        boolean challengeReady = state.challenge != null;
        TextView title = view.findViewById(R.id.auth_title);
        TextView description = view.findViewById(R.id.auth_description);
        TextView feedback = view.findViewById(R.id.auth_feedback);
        MaterialButton submit = view.findViewById(R.id.auth_submit);
        MaterialButton switchScreen = view.findViewById(R.id.auth_switch);

        title.setText(otp ? R.string.auth_otp_title : signIn ? R.string.auth_sign_in : R.string.auth_register);
        if (challengeReady) {
            description.setText(R.string.auth_challenge_description);
            submit.setText(R.string.auth_verify);
        } else {
            description.setText(otp ? R.string.auth_otp_description
                    : signIn ? R.string.auth_password_preview_description : R.string.auth_phone_description);
            submit.setText(otp ? R.string.auth_verify_preview : R.string.auth_continue);
        }
        TextInputLayout otpField = view.findViewById(R.id.auth_otp_field);
        otpField.setHint(getText(challengeReady ? R.string.auth_verification_code : R.string.auth_otp));
        switchScreen.setText(otp ? R.string.auth_change_number
                : signIn ? R.string.auth_switch_register : R.string.auth_switch_sign_in);

        view.findViewById(R.id.auth_phone_form).setVisibility(otp ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.auth_otp_field).setVisibility(otp ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.auth_progress).setVisibility(state.loading ? View.VISIBLE : View.GONE);
        boolean canChangeInput = !state.loading || viewModel.isRegistrationPending();
        view.findViewById(R.id.auth_country_field).setEnabled(canChangeInput);
        view.findViewById(R.id.auth_phone_field).setEnabled(canChangeInput);
        otpField.setEnabled(!state.loading);
        View passwordField = view.findViewById(R.id.auth_password_field);
        passwordField.setVisibility(signIn ? View.VISIBLE : View.GONE);
        passwordField.setEnabled(!state.loading);
        submit.setEnabled(!state.loading && !challengeReady);
        switchScreen.setEnabled(canChangeInput);

        int message = 0;
        switch (state.feedback) {
            case SIGN_IN_PENDING:
                message = R.string.auth_sign_in_pending;
                break;
            case OTP_PREVIEW:
                message = R.string.auth_otp_preview;
                break;
            case VERIFICATION_PENDING:
                message = R.string.auth_verification_pending;
                break;
            case NONE:
                break;
        }
        if (state.failure != null) {
            switch (state.failure) {
                case UNAVAILABLE:
                    message = R.string.auth_challenge_unavailable;
                    break;
                case RATE_LIMITED:
                    message = R.string.auth_challenge_rate_limited;
                    break;
                case REJECTED:
                    message = R.string.auth_challenge_rejected;
                    break;
            }
        }
        feedback.setVisibility(message == 0 ? View.GONE : View.VISIBLE);
        if (message != 0) {
            feedback.setText(message);
        }
    }
}
