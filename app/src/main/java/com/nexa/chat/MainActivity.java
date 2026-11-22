package com.nexa.chat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Skeleton entry only. TV1 can call the same integration from the shared Nhóm tab.
        findViewById(R.id.groups_entry).setOnClickListener(
                view -> com.nexa.chat.feature.groups.GroupsIntegration.open(this));
    }
}
