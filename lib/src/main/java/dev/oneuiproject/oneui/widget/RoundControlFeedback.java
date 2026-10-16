package dev.oneuiproject.oneui.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Circular press/hover feedback without the device ripple's animated mask. */
final class RoundControlFeedback extends Drawable implements Drawable.Callback {
    private final Drawable content;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private boolean highlighted;

    RoundControlFeedback(@Nullable Drawable content, int color) {
        this.content = content;
        paint.setColor(color);
        if (content != null) content.setCallback(this);
    }

    @Override public void draw(@NonNull Canvas canvas) {
        if (content != null) content.draw(canvas);
        if (highlighted) canvas.drawOval(oval, paint);
    }

    @Override protected void onBoundsChange(Rect bounds) {
        oval.set(bounds);
        if (content != null) content.setBounds(bounds);
    }

    @Override protected boolean onStateChange(int[] states) {
        boolean next = false, enabled = false;
        for (int state : states) {
            if (state == android.R.attr.state_enabled) enabled = true;
            if (state == android.R.attr.state_pressed || state == android.R.attr.state_hovered
                    || state == android.R.attr.state_focused) next = true;
        }
        next &= enabled;
        if (highlighted == next) return false;
        highlighted = next;
        invalidateSelf();
        return true;
    }

    @Override public boolean isStateful() { return true; }
    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); invalidateSelf(); }
    @Override public void setColorFilter(@Nullable ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    @Override public void invalidateDrawable(@NonNull Drawable who) { invalidateSelf(); }
    @Override public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        scheduleSelf(what, when);
    }
    @Override public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) { unscheduleSelf(what); }
}
