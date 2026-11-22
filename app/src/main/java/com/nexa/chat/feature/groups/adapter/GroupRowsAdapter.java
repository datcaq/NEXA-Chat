package com.nexa.chat.feature.groups.adapter;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import com.google.android.material.button.MaterialButton;
import com.nexa.chat.R;
import java.util.*;

public final class GroupRowsAdapter
    extends ListAdapter<GroupRowsAdapter.Row, GroupRowsAdapter.Holder> {
  public static final class Row {
    public final String id, title, action, secondary;
    public final Runnable click, other;

    public Row(String id, String title, String action, Runnable click) {
      this(id, title, action, click, "", null);
    }

    public Row(
        String id, String title, String action, Runnable click, String secondary, Runnable other) {
      this.id = id;
      this.title = title;
      this.action = action;
      this.click = click;
      this.secondary = secondary;
      this.other = other;
    }
  }

  public GroupRowsAdapter() {
    super(
        new DiffUtil.ItemCallback<Row>() {
          public boolean areItemsTheSame(@NonNull Row a, @NonNull Row b) {
            return a.id.equals(b.id);
          }

          public boolean areContentsTheSame(@NonNull Row a, @NonNull Row b) {
            return false;
          }
        });
  }

  public static class Holder extends RecyclerView.ViewHolder {
    TextView text, avatar;
    MaterialButton action, secondary;

    Holder(View v) {
      super(v);
      text = v.findViewById(R.id.groups_row_text);
      avatar = v.findViewById(R.id.groups_avatar);
      action = v.findViewById(R.id.groups_row_action);
      secondary = v.findViewById(R.id.groups_row_secondary);
    }
  }

  @NonNull
  public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
    return new Holder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_row, parent, false));
  }

  public void onBindViewHolder(@NonNull Holder h, int position) {
    Row row = getItem(position);
    h.text.setText(row.title);
    h.avatar.setText(row.title.isEmpty() ? "N" : row.title.substring(0, 1));
    h.action.setText(row.action);
    h.action.setVisibility(row.action.isEmpty() ? View.GONE : View.VISIBLE);
    h.action.setEnabled(row.click != null);
    h.action.setOnClickListener(
        v -> {
          if (row.click != null) row.click.run();
        });
    h.secondary.setText(row.secondary);
    h.secondary.setVisibility(row.secondary.isEmpty() ? View.GONE : View.VISIBLE);
    h.secondary.setEnabled(row.other != null);
    h.secondary.setOnClickListener(
        v -> {
          if (row.other != null) row.other.run();
        });
  }
}
