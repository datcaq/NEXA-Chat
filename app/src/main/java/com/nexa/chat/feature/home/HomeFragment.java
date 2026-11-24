package com.nexa.chat.feature.home;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nexa.chat.R;

/** Home surface for a future authenticated host. Session and module wiring belong to the host. */
public final class HomeFragment extends Fragment {
    private static final String TAB_KEY = "home.selectedTab";

    private HomeTab selectedTab = HomeTab.MESSAGES;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            selectedTab = HomeTab.restore(savedInstanceState.getString(TAB_KEY));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        for (HomeTab tab : HomeTab.values()) {
            ViewGroup container = view.findViewById(containerId(tab));
            View placeholder = getLayoutInflater().inflate(R.layout.home_unavailable, container, false);
            ImageView icon = placeholder.findViewById(R.id.home_unavailable_icon);
            icon.setImageResource(iconId(tab));
            TextView description = placeholder.findViewById(R.id.home_unavailable_description);
            description.setText(descriptionId(tab));
            container.addView(placeholder);
        }

        BottomNavigationView navigation = view.findViewById(R.id.home_navigation);
        navigation.setSelectedItemId(menuId(selectedTab));
        renderTab(view, navigation);
        navigation.setOnItemSelectedListener(item -> {
            for (HomeTab tab : HomeTab.values()) {
                if (menuId(tab) == item.getItemId()) {
                    selectedTab = tab;
                    renderTab(view, navigation);
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TAB_KEY, selectedTab.name());
    }

    private void renderTab(View view, BottomNavigationView navigation) {
        TextView title = view.findViewById(R.id.home_tab_title);
        title.setText(navigation.getMenu().findItem(menuId(selectedTab)).getTitle());
        for (HomeTab tab : HomeTab.values()) {
            view.findViewById(containerId(tab)).setVisibility(
                    tab == selectedTab ? View.VISIBLE : View.GONE);
        }
    }

    private static int menuId(HomeTab tab) {
        switch (tab) {
            case MESSAGES: return R.id.home_tab_messages;
            case CONTACTS: return R.id.home_tab_contacts;
            case GROUPS: return R.id.home_tab_groups;
            case PROFILE: return R.id.home_tab_profile;
        }
        throw new AssertionError(tab);
    }

    private static int containerId(HomeTab tab) {
        switch (tab) {
            case MESSAGES: return R.id.home_panel_messages;
            case CONTACTS: return R.id.home_panel_contacts;
            case GROUPS: return R.id.home_panel_groups;
            case PROFILE: return R.id.home_panel_profile;
        }
        throw new AssertionError(tab);
    }

    private static int descriptionId(HomeTab tab) {
        switch (tab) {
            case MESSAGES: return R.string.home_unavailable_messages;
            case CONTACTS: return R.string.home_unavailable_contacts;
            case GROUPS: return R.string.home_unavailable_groups;
            case PROFILE: return R.string.home_unavailable_profile;
        }
        throw new AssertionError(tab);
    }

    private static int iconId(HomeTab tab) {
        switch (tab) {
            case MESSAGES: return R.drawable.ic_home_messages;
            case CONTACTS: return R.drawable.ic_home_contacts;
            case GROUPS: return R.drawable.ic_home_groups;
            case PROFILE: return R.drawable.ic_home_profile;
        }
        throw new AssertionError(tab);
    }
}
