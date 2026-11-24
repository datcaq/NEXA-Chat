package com.nexa.chat.feature.contacts;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.nexa.chat.R;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Standalone Contacts surface. Its future host must provide verified access and server results. */
public final class ContactsFragment extends Fragment {
    private static final String REGION = "contacts.region";
    private static final String SEARCH_UNAVAILABLE = "contacts.searchUnavailable";
    private static final String SENT_INVITATIONS = "contacts.sentInvitations";
    private static final PhoneNumberUtil PHONE_NUMBERS = PhoneNumberUtil.getInstance();

    enum SearchState { INITIAL, LOADING, RESULT, EMPTY, ERROR, UNAVAILABLE }

    private String selectedRegion;
    private boolean searchUnavailable;
    private boolean showingSentInvitations;
    private DialogInterface regionDialog;

    public ContactsFragment() {
        super(R.layout.fragment_contacts);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            selectedRegion = savedInstanceState.getString(REGION);
            searchUnavailable = savedInstanceState.getBoolean(SEARCH_UNAVAILABLE);
            showingSentInvitations = savedInstanceState.getBoolean(SENT_INVITATIONS);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton region = view.findViewById(R.id.contacts_region);
        TextInputEditText phone = view.findViewById(R.id.contacts_phone);
        TextInputLayout phoneField = view.findViewById(R.id.contacts_phone_field);

        showSelectedRegion(region);
        region.setOnClickListener(v -> chooseRegion(region, phoneField));
        view.findViewById(R.id.contacts_search).setOnClickListener(v -> submit(phone, phoneField));
        phone.setOnEditorActionListener((v, action, event) -> {
            if (action != EditorInfo.IME_ACTION_SEARCH) {
                return false;
            }
            submit(phone, phoneField);
            return true;
        });

        renderSearch(searchUnavailable ? SearchState.UNAVAILABLE : SearchState.INITIAL, null);

        MaterialButtonToggleGroup invitations = view.findViewById(R.id.contacts_invitation_tabs);
        TextView invitationStatus = view.findViewById(R.id.contacts_invitation_status);
        invitations.check(showingSentInvitations ? R.id.contacts_sent : R.id.contacts_received);
        showInvitationStatus(invitationStatus);
        invitations.addOnButtonCheckedListener((group, id, checked) -> {
            if (checked) {
                showingSentInvitations = id == R.id.contacts_sent;
                showInvitationStatus(invitationStatus);
            }
        });
    }

    private void submit(TextInputEditText phone, TextInputLayout field) {
        CharSequence input = phone.getText();
        ContactsSearchInput prepared = ContactsSearchInput.prepare(
                input == null ? null : input.toString(), selectedRegion);
        if (prepared.error != null) {
            field.setError(getString(prepared.error == ContactsSearchInput.Error.EMPTY
                    ? R.string.contacts_phone_required : R.string.contacts_phone_invalid));
            return;
        }

        field.setError(null);
        WindowCompat.getInsetsController(requireActivity().getWindow(), phone)
                .hide(WindowInsetsCompat.Type.ime());
        // The E.164 value is ready for a future authenticated lookup. Keep no request or result.
        searchUnavailable = true;
        renderSearch(SearchState.UNAVAILABLE, null);
    }

    private void chooseRegion(MaterialButton button, TextInputLayout phoneField) {
        Locale locale = getResources().getConfiguration().getLocales().get(0);
        Collator collator = Collator.getInstance(locale);
        List<String> regions = new ArrayList<>(PHONE_NUMBERS.getSupportedRegions());
        regions.sort((first, second) -> collator.compare(regionName(first, locale), regionName(second, locale)));
        String[] labels = new String[regions.size()];
        for (int index = 0; index < regions.size(); index++) {
            labels[index] = regionLabel(regions.get(index), locale);
        }

        regionDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.contacts_region_title)
                .setSingleChoiceItems(labels, regions.indexOf(selectedRegion), (dialog, selected) -> {
                    selectedRegion = regions.get(selected);
                    showSelectedRegion(button);
                    phoneField.setError(null);
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setOnDismissListener(dialog -> regionDialog = null)
                .show();
    }

    private void showSelectedRegion(MaterialButton region) {
        if (selectedRegion == null) {
            region.setText(R.string.contacts_choose_region);
            return;
        }
        Locale locale = getResources().getConfiguration().getLocales().get(0);
        region.setText(regionLabel(selectedRegion, locale));
    }

    private String regionLabel(String region, Locale locale) {
        return getString(R.string.contacts_region_option, regionName(region, locale),
                PHONE_NUMBERS.getCountryCodeForRegion(region));
    }

    private static String regionName(String region, Locale locale) {
        return new Locale.Builder().setRegion(region).build().getDisplayCountry(locale);
    }

    // Only a public display label belongs in this presentation boundary.
    void renderSearch(SearchState state, @Nullable CharSequence displayName) {
        View view = requireView();
        TextView status = view.findViewById(R.id.contacts_search_status);
        TextView name = view.findViewById(R.id.contacts_result_name);
        boolean hasResult = state == SearchState.RESULT;
        view.findViewById(R.id.contacts_result).setVisibility(hasResult ? View.VISIBLE : View.GONE);
        name.setText(hasResult ? Objects.requireNonNull(displayName) : null);
        status.setVisibility(hasResult ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.contacts_progress)
                .setVisibility(state == SearchState.LOADING ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.contacts_search).setEnabled(state != SearchState.LOADING);

        switch (state) {
            case INITIAL: status.setText(R.string.contacts_search_initial); break;
            case LOADING: status.setText(R.string.contacts_search_loading); break;
            case EMPTY: status.setText(R.string.contacts_search_empty); break;
            case ERROR: status.setText(R.string.contacts_search_error); break;
            case UNAVAILABLE: status.setText(R.string.contacts_search_unavailable); break;
            case RESULT: status.setText(null); break;
        }
    }

    private void showInvitationStatus(TextView status) {
        status.setText(showingSentInvitations ? R.string.contacts_sent_unavailable
                : R.string.contacts_received_unavailable);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(REGION, selectedRegion);
        outState.putBoolean(SEARCH_UNAVAILABLE, searchUnavailable);
        outState.putBoolean(SENT_INVITATIONS, showingSentInvitations);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        if (regionDialog != null) {
            regionDialog.dismiss();
        }
        super.onDestroyView();
    }
}
