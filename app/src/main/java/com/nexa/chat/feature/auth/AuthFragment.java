package com.nexa.chat.feature.auth;

import android.os.Bundle;
import android.view.View;
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

    private AuthViewModel viewModel;
    private String region;

    public AuthFragment() {
        super(R.layout.fragment_auth);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
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
        TextInputLayout countryField = view.findViewById(R.id.auth_country_field);
        TextInputLayout phoneField = view.findViewById(R.id.auth_phone_field);
        TextInputLayout otpField = view.findViewById(R.id.auth_otp_field);

        setupCountries(country, countryField, phoneField);
        view.findViewById(R.id.auth_submit).setOnClickListener(v -> {
            countryField.setError(null);
            phoneField.setError(null);
            otpField.setError(null);

            if (viewModel.screen() == AuthViewModel.Screen.OTP) {
                if (!input(otp).matches("[0-9]+")) {
                    otpField.setError(getString(R.string.auth_error_otp));
                    otp.requestFocus();
                    return;
                }
                otp.setText(null);
            } else {
                String number = input(phone);
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
                if (PhoneNumberValidator.toE164(number, region) == null) {
                    phoneField.setError(getString(R.string.auth_error_phone_invalid));
                    phone.requestFocus();
                    return;
                }
            }

            WindowCompat.getInsetsController(requireActivity().getWindow(), view)
                    .hide(WindowInsetsCompat.Type.ime());
            viewModel.preview();
        });

        view.findViewById(R.id.auth_switch).setOnClickListener(v -> {
            countryField.setError(null);
            phoneField.setError(null);
            otpField.setError(null);
            otp.setText(null);
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(REGION_KEY, region);
        super.onSaveInstanceState(outState);
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
            region = regions.get(position);
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
        TextView title = view.findViewById(R.id.auth_title);
        TextView description = view.findViewById(R.id.auth_description);
        TextView feedback = view.findViewById(R.id.auth_feedback);
        MaterialButton submit = view.findViewById(R.id.auth_submit);
        MaterialButton switchScreen = view.findViewById(R.id.auth_switch);

        title.setText(otp ? R.string.auth_otp_title : signIn ? R.string.auth_sign_in : R.string.auth_register);
        description.setText(otp ? R.string.auth_otp_description : R.string.auth_phone_description);
        submit.setText(otp ? R.string.auth_verify_preview : R.string.auth_continue);
        switchScreen.setText(otp ? R.string.auth_change_number
                : signIn ? R.string.auth_switch_register : R.string.auth_switch_sign_in);

        view.findViewById(R.id.auth_phone_form).setVisibility(otp ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.auth_otp_field).setVisibility(otp ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.auth_progress).setVisibility(state.loading ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.auth_country_field).setEnabled(!state.loading);
        view.findViewById(R.id.auth_phone_field).setEnabled(!state.loading);
        view.findViewById(R.id.auth_otp_field).setEnabled(!state.loading);
        submit.setEnabled(!state.loading);
        switchScreen.setEnabled(!state.loading);

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
        feedback.setVisibility(message == 0 ? View.GONE : View.VISIBLE);
        if (message != 0) {
            feedback.setText(message);
        }
    }
}
