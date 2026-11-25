package com.nexa.chat.feature.groups.ui;

import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.*;
import androidx.recyclerview.widget.*;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nexa.chat.R;
import com.nexa.chat.feature.groups.GroupsIntegration;
import com.nexa.chat.feature.groups.adapter.GroupRowsAdapter;
import com.nexa.chat.feature.groups.model.GroupModels.*;
import com.nexa.chat.feature.groups.viewmodel.GroupsViewModel;
import java.util.*;

public class GroupsActivity extends AppCompatActivity {
  private GroupsViewModel vm;
  private GroupsIntegration.Dependencies dependencies;
  private LinearLayout controls, pages;
  private TextView status;
  private View progress;
  private GroupRowsAdapter adapter;
  private String renderedScreen;
  private com.nexa.chat.feature.groups.GroupsAccountScope.Registration scopeClosure;
  private com.nexa.chat.feature.groups.GroupsAccountScope.Registration scopeAccess;
  private androidx.appcompat.app.AlertDialog activeDialog;

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.groups_activity);
    androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.groups_root),
        (view, insets) -> {
          androidx.core.graphics.Insets bars =
              insets.getInsets(
                  androidx.core.view.WindowInsetsCompat.Type.systemBars()
                      | androidx.core.view.WindowInsetsCompat.Type.ime());
          view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
          return insets;
        });
    controls = findViewById(R.id.groups_controls);
    pages = findViewById(R.id.groups_pages);
    status = findViewById(R.id.groups_status);
    progress = findViewById(R.id.groups_progress);
    MaterialToolbar toolbar = findViewById(R.id.groups_toolbar);
    toolbar.setTitle(R.string.groups_title);
    toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
    toolbar.setNavigationContentDescription(R.string.groups_cancel);
    toolbar.setNavigationOnClickListener(v -> finish());
    dependencies = GroupsIntegration.get();
    if (dependencies == null || !dependencies.scope.isCurrent()) {
      progress.setVisibility(View.GONE);
      status.setText(R.string.groups_setup);
      return;
    }
    vm =
        new ViewModelProvider(
                this,
                new AbstractSavedStateViewModelFactory(this, state) {
                  @NonNull
                  @Override
                  protected <T extends ViewModel> T create(
                      @NonNull String key,
                      @NonNull Class<T> type,
                      @NonNull SavedStateHandle handle) {
                    return type.cast(
                        new GroupsViewModel(
                            handle, dependencies.groups, dependencies.people, dependencies.scope));
                  }
                })
            .get(GroupsViewModel.class);
    RecyclerView list = findViewById(R.id.groups_list);
    list.setLayoutManager(new LinearLayoutManager(this));
    adapter = new GroupRowsAdapter();
    list.setAdapter(adapter);
    scopeClosure =
        dependencies.scope.onClosed(
            () -> {
              dismissDialog();
              controls.removeAllViews();
              pages.removeAllViews();
              adapter.submitList(Collections.emptyList());
              status.setText(R.string.groups_session_ended);
              finish();
            });
    vm.changed.observe(this, v -> render());
    scopeAccess =
        dependencies.scope.onAccessChanged(
            () -> {
              vm.ensureInitialLoad();
              render();
            });
    if (state == null || (!vm.busy && vm.detail == null && vm.groups.isEmpty())) vm.load(0);
  }

  private void render() {
    if (!dependencies.scope.isCurrent()) return;
    boolean available = dependencies.scope.hasAccess();
    controls.setVisibility(available ? View.VISIBLE : View.GONE);
    pages.setVisibility(available ? View.VISIBLE : View.GONE);
    findViewById(R.id.groups_list).setVisibility(available ? View.VISIBLE : View.GONE);
    if (!available) {
      dismissDialog();
      progress.setVisibility(View.VISIBLE);
      status.setText(R.string.groups_session_checking);
      return;
    }
    controls.removeAllViews();
    pages.removeAllViews();
    progress.setVisibility(vm.busy ? View.VISIBLE : View.GONE);
    ((MaterialToolbar) findViewById(R.id.groups_toolbar))
        .setTitle(
            vm.screen().equals("create")
                ? R.string.groups_create
                : vm.screen().equals("detail")
                    ? R.string.groups_detail
                    : vm.screen().equals("members")
                        ? R.string.groups_members
                        : vm.screen().equals("incoming")
                            ? R.string.groups_incoming
                            : vm.screen().equals("outgoing")
                                ? R.string.groups_pending
                                : R.string.groups_title);
    status.setText(vm.busy ? getString(R.string.groups_loading) : "");
    if ("create".equals(renderedScreen) && "detail".equals(vm.screen()))
      status.setText(R.string.groups_success);
    renderedScreen = vm.screen();
    if (!vm.error.isEmpty()) {
      status.setText(
          vm.error.equals("INVALID_NAME")
              ? getString(R.string.groups_invalid)
              : vm.error.equals("BATCH_LIMIT")
                  ? getString(R.string.groups_limit)
                  : getString(R.string.groups_error, vm.error));
      button(controls, R.string.groups_retry, () -> vm.retry());
    }
    List<GroupRowsAdapter.Row> rows = new ArrayList<>();
    switch (vm.screen()) {
      case "create":
        TextView avatar = new TextView(this);
        avatar.setText("N");
        avatar.setTextColor(android.graphics.Color.WHITE);
        avatar.setTextSize(24);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackgroundResource(R.drawable.groups_avatar);
        avatar.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        avatarParams.gravity = Gravity.CENTER_HORIZONTAL;
        avatarParams.bottomMargin = dp(8);
        controls.addView(avatar, avatarParams);
        TextInputEditText name = input(R.string.groups_name, vm.name());
        name.setId(R.id.groups_name_input);
        name.setFilters(new InputFilter[] {new InputFilter.LengthFilter(120)});
        name.addTextChangedListener(
            new TextWatcher() {
              public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

              public void onTextChanged(CharSequence s, int start, int before, int count) {
                vm.name(s.toString());
              }

              public void afterTextChanged(Editable e) {}
            });
        TextInputEditText phone = input(R.string.groups_phone, vm.query());
        phone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        phone.setId(R.id.groups_phone_input);
        button(controls, R.string.groups_search, () -> vm.search(phone.getText().toString()));
        button(controls, R.string.groups_friends, () -> vm.friends());
        MaterialButton selected =
            button(
                controls,
                R.string.groups_review,
                () ->
                    showDialog(
                        new MaterialAlertDialogBuilder(this)
                            .setTitle(getString(R.string.groups_review, vm.selected().size()))
                            .setMessage(
                                vm.selected().isEmpty()
                                    ? getString(R.string.groups_selected_empty)
                                    : vm.selected().stream()
                                        .map(p -> p.displayName)
                                        .collect(java.util.stream.Collectors.joining("\n")))
                            .setPositiveButton(android.R.string.ok, null)));
        selected.setText(getString(R.string.groups_review, vm.selected().size()));
        button(
            controls,
            R.string.groups_submit,
            () ->
                showDialog(
                    new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.groups_confirm_title)
                        .setMessage(
                            getString(
                                R.string.groups_confirm, vm.name().trim(), vm.selected().size()))
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.groups_submit, (d, w) -> vm.create())));
        for (Person p : vm.people) {
          boolean eligible = p.eligible && !p.id.equals(dependencies.people.currentUserId());
          String label =
              p.displayName + (p.publicIdentifier == null ? "" : "\n" + p.publicIdentifier);
          rows.add(
              new GroupRowsAdapter.Row(
                  p.id,
                  label,
                  getString(
                      eligible
                          ? (vm.isSelected(p.id)
                              ? R.string.groups_unselect
                              : R.string.groups_select)
                          : R.string.groups_ineligible),
                  eligible && !vm.busy ? () -> vm.toggle(p) : null));
        }
        break;
      case "detail":
        if (vm.detail != null) {
          text(vm.detail.conversation.title);
          text(getString(R.string.groups_count, vm.detail.memberCount));
          if ("OWNER".equals(vm.detail.myRole)) text(getString(R.string.groups_owner));
          button(controls, R.string.groups_members, () -> vm.show("members"));
          if ("OWNER".equals(vm.detail.myRole))
            button(controls, R.string.groups_pending, () -> vm.show("outgoing"));
          button(
              controls,
              R.string.groups_chat,
              () -> {
                if (dependencies.chat == null)
                  Toast.makeText(this, R.string.groups_chat_missing, Toast.LENGTH_LONG).show();
                else
                  dependencies.scope.dispatch(
                      () -> {
                        if (!isFinishing() && !isDestroyed() && vm.groupId() != null)
                          dependencies.chat.open(this, vm.groupId());
                      });
              });
        }
        break;
      case "incoming":
      case "outgoing":
        for (Invitation i : vm.invitations) {
          String label =
              getString(R.string.groups_invitee, i.inviteeUserId)
                  + "\n"
                  + i.conversationId
                  + " • "
                  + getString(invitationStatus(i.status));
          if (vm.screen().equals("incoming") && "PENDING".equals(i.status))
            rows.add(
                new GroupRowsAdapter.Row(
                    i.id,
                    label,
                    getString(R.string.groups_accept),
                    vm.busy ? null : () -> vm.respond(i, true),
                    getString(R.string.groups_reject),
                    vm.busy ? null : () -> vm.respond(i, false)));
          else rows.add(new GroupRowsAdapter.Row(i.id, label, "", null));
        }
        break;
      case "members":
        for (Member m : vm.members)
          rows.add(
              new GroupRowsAdapter.Row(
                  m.userId,
                  getString(R.string.groups_member, m.userId, m.role) + " • " + m.status,
                  "",
                  null));
        break;
      default:
        button(controls, R.string.groups_create, () -> vm.show("create"));
        button(controls, R.string.groups_incoming, () -> vm.show("incoming"));
        for (Summary g : vm.groups)
          rows.add(
              new GroupRowsAdapter.Row(
                  g.conversation.id,
                  g.conversation.title + "\n" + getString(R.string.groups_count, g.memberCount),
                  getString(R.string.groups_detail),
                  vm.busy ? null : () -> vm.open(g.conversation.id)));
    }
    if (!vm.screen().equals("groups"))
      button(controls, R.string.groups_cancel, () -> vm.show("groups"));
    adapter.submitList(rows);
    if (rows.isEmpty() && !vm.screen().equals("detail") && !vm.busy && vm.error.isEmpty())
      status.setText(R.string.groups_empty);
    if (vm.page > 0) button(pages, R.string.groups_prev, () -> vm.load(vm.page - 1));
    if (vm.hasNext) button(pages, R.string.groups_next, () -> vm.load(vm.page + 1));
  }

  private int invitationStatus(String status) {
    switch (status) {
      case "ACCEPTED":
        return R.string.groups_status_accepted;
      case "REJECTED":
        return R.string.groups_status_rejected;
      case "CANCELLED":
        return R.string.groups_status_cancelled;
      default:
        return R.string.groups_status_pending;
    }
  }

  private MaterialButton button(LinearLayout parent, int label, Runnable action) {
    MaterialButton b = new MaterialButton(this);
    b.setText(label);
    b.setAllCaps(false);
    b.setCornerRadius(dp(12));
    if (label != R.string.groups_create && label != R.string.groups_submit) {
      b.setBackgroundTintList(
          android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
      b.setTextColor(getColor(R.color.groups_primary));
      b.setStrokeColor(
          android.content.res.ColorStateList.valueOf(getColor(R.color.groups_secondary)));
      b.setStrokeWidth(dp(1));
    }
    b.setMinHeight((int) (48 * getResources().getDisplayMetrics().density));
    b.setEnabled(!vm.busy);
    b.setOnClickListener(v -> action.run());
    parent.addView(b);
    return b;
  }

  private TextInputEditText input(int hint, String value) {
    TextInputLayout field = new TextInputLayout(this);
    field.setHint(hint);
    field.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
    field.setBoxCornerRadii(dp(12), dp(12), dp(12), dp(12));
    TextInputEditText edit = new TextInputEditText(this);
    edit.setText(value);
    edit.setSingleLine(true);
    edit.setEnabled(!vm.busy);
    field.addView(edit, new LinearLayout.LayoutParams(-1, -2));
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
    params.bottomMargin = dp(8);
    controls.addView(field, params);
    return edit;
  }

  private void text(String value) {
    TextView t = new TextView(this);
    t.setText(value);
    t.setTextSize(20);
    t.setTextColor(getColor(R.color.groups_primary));
    t.setPadding(0, 16, 0, 16);
    controls.addView(t);
  }

  private int dp(int value) {
    return Math.round(value * getResources().getDisplayMetrics().density);
  }

  private void showDialog(MaterialAlertDialogBuilder builder) {
    if (!dependencies.scope.hasAccess()) return;
    dismissDialog();
    activeDialog = builder.show();
  }

  private void dismissDialog() {
    if (activeDialog != null) {
      activeDialog.dismiss();
      activeDialog = null;
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (dependencies != null && !dependencies.scope.isCurrent()) {
      dependencies.scope.close();
      finish();
    } else if (dependencies != null) dependencies.scope.sessionChanged();
  }

  @Override
  protected void onDestroy() {
    dismissDialog();
    if (scopeClosure != null) scopeClosure.close();
    if (scopeAccess != null) scopeAccess.close();
    super.onDestroy();
  }
}
