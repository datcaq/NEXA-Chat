package com.nexa.chat.feature.messaging.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;
import android.widget.TextView;

import com.nexa.chat.R;
import com.nexa.chat.feature.messaging.model.OutboxState;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Locale;

/** View-only formatting and decoration. No repository, navigation or account state. */
final class MessagingUi {
    private MessagingUi() {}

    static int size(Context context, int resource) {
        return context.getResources().getDimensionPixelSize(resource);
    }

    static void textSize(TextView view, int resource) {
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                view.getResources().getDimension(resource));
    }

    static GradientDrawable surface(Context context, int color, boolean outlined) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(context.getColor(color));
        drawable.setCornerRadius(size(context, R.dimen.messaging_corner));
        if (outlined) drawable.setStroke(size(context, R.dimen.messaging_stroke),
                context.getColor(R.color.messaging_outline));
        return drawable;
    }

    static Drawable rowBackground(Context context) {
        return new RippleDrawable(ColorStateList.valueOf(
                context.getColor(R.color.messaging_outline)),
                surface(context, R.color.messaging_surface, true), null);
    }

    static int state(OutboxState state) {
        switch (state) {
            case DRAFT: return R.string.messaging_state_draft;
            case PENDING: return R.string.messaging_state_pending;
            case SENDING: return R.string.messaging_state_sending;
            case SENT: return R.string.messaging_state_sent;
            case FAILED: return R.string.messaging_state_failed;
            default: throw new IllegalArgumentException("Unknown outbox state");
        }
    }

    static int stateDescription(OutboxState state) {
        switch (state) {
            case DRAFT: return R.string.messaging_state_draft_description;
            case PENDING: return R.string.messaging_state_pending_description;
            case SENDING: return R.string.messaging_state_sending_description;
            case SENT: return R.string.messaging_state_sent_description;
            case FAILED: return R.string.messaging_state_failed_description;
            default: throw new IllegalArgumentException("Unknown outbox state");
        }
    }

    /** Missing/malformed timestamps stay absent. Never substitute the current or ACK time. */
    static String timestamp(String source) {
        if (source == null || source.isEmpty()) return "";
        try {
            return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    .withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault())
                    .format(Instant.parse(source));
        } catch (DateTimeParseException invalid) {
            return "";
        }
    }

    static Drawable avatar(Context context, String title) {
        return new InitialAvatar(context, title);
    }

    /** Decorative, deterministic initial. The adjacent title supplies the accessible identity. */
    private static final class InitialAvatar extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final String initial;
        private final int size, background, foreground;

        InitialAvatar(Context context, String title) {
            String value = title.trim();
            initial = value.isEmpty() ? "?" : new String(Character.toChars(value.codePointAt(0)))
                    .toUpperCase(Locale.ROOT);
            size = size(context, R.dimen.messaging_avatar);
            background = context.getColor(R.color.messaging_accent_container);
            foreground = context.getColor(R.color.messaging_accent);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setTextSize(size * 0.45f);
            setBounds(0, 0, size, size);
        }

        @Override public void draw(Canvas canvas) {
            float x = getBounds().exactCenterX(), y = getBounds().exactCenterY();
            paint.setColor(background);
            canvas.drawCircle(x, y, Math.min(getBounds().width(), getBounds().height()) / 2f, paint);
            paint.setColor(foreground);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(initial, x, y - (paint.ascent() + paint.descent()) / 2f, paint);
        }
        @Override public int getIntrinsicWidth() { return size; }
        @Override public int getIntrinsicHeight() { return size; }
        @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
        @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
