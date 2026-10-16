package dev.oneuiproject.oneui.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.oneuiproject.oneui.design.R;

/** Rounded background owned by a toolbar control group, with optional backdrop blur. */
public final class TopActionSurfaceDrawable extends Drawable {
    private static final int SAMPLE = BlurSettings.GLASS_SAMPLE_SCALE;
    private final View host;
    private final View source;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Path path = new Path();
    private Bitmap bitmap;
    private int[] sourcePixels;
    private int[] blurredPixels;
    private boolean blurEnabled;
    private float radius;
    private Integer tintColor;
    private int alpha = 255;
    private boolean captureDirty = true;
    private boolean hardwareDirty = true;
    private final BackdropRenderNode hardwareBackdrop = new BackdropRenderNode();

    public TopActionSurfaceDrawable(View host, View source, float radius) {
        this.host = host;
        this.source = source;
        this.radius = radius;
        BlurSettings.observe(host, this::refreshBackdrop);
        host.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View view) { }
            @Override public void onViewDetachedFromWindow(View view) { release(); }
        });
    }

    public void setBlurEnabled(boolean enabled) { blurEnabled = enabled; refreshBackdrop(); }
    public void refreshBackdrop() { captureDirty = hardwareDirty = true; invalidateSelf(); }
    public void setTintColor(@Nullable Integer color) { tintColor = color; invalidateSelf(); }
    public void setRadius(float pixels) {
        if (radius != pixels) { radius = pixels; invalidateSelf(); }
    }

    @Override public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty() || alpha == 0) return;
        int save = canvas.saveLayerAlpha(bounds.left, bounds.top, bounds.right, bounds.bottom, alpha);
        path.reset();
        path.addRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom,
                Math.min(radius, bounds.height() / 2f),
                Math.min(radius, bounds.height() / 2f), Path.Direction.CW);
        int backdropSave = canvas.save();
        canvas.clipPath(path);
        boolean effectiveBlur = blurEnabled && BlurSettings.isEnabled(host.getContext());
        if (effectiveBlur && BlurSettings.getIntensity(host.getContext()) > 0
                && source.isAttachedToWindow() && !host.isInEditMode()) {
            if (hardwareBackdrop.draw(canvas, host, source, bounds, hardwareDirty)) {
                hardwareDirty = false;
            } else {
                if (captureDirty || bitmap == null) { capture(bounds.width(), bounds.height()); captureDirty = false; }
                if (bitmap != null) {
                    paint.setColor(Color.WHITE);
                    canvas.drawBitmap(bitmap, null, bounds, paint);
                }
            }
        }
        canvas.restoreToCount(backdropSave);
        int solid = OneUIPalette.color(host.getContext(), R.color.oui_floating_nav_surface);
        int glass = OneUIPalette.color(host.getContext(), R.color.oui_floating_nav_glass);
        int tint = tintColor != null ? tintColor : effectiveBlur ? glass : solid;
        paint.setColor(effectiveBlur ? BlurSettings.tint(host.getContext(), tint) : tint);
        // Draw the tint with an antialiased shape rather than a stencil-clipped rectangle.
        float rounded = Math.min(radius, bounds.height() / 2f);
        canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, rounded, rounded, paint);
        canvas.restoreToCount(save);
    }

    private void capture(int hostWidth, int hostHeight) {
        int width = Math.max(1, (hostWidth + SAMPLE - 1) / SAMPLE);
        int height = Math.max(1, (hostHeight + SAMPLE - 1) / SAMPLE);
        if (bitmap == null || bitmap.getWidth() != width || bitmap.getHeight() != height) {
            if (bitmap != null) bitmap.recycle();
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            sourcePixels = new int[width * height];
            blurredPixels = new int[width * height];
        }
        bitmap.eraseColor(Color.TRANSPARENT);
        Canvas sample = new Canvas(bitmap);
        sample.scale((float) width / hostWidth, (float) height / hostHeight);
        int[] here = new int[2];
        int[] there = new int[2];
        host.getLocationInWindow(here);
        source.getLocationInWindow(there);
        sample.translate(there[0] - here[0], there[1] - here[1]);
        try {
            source.draw(sample);
        } catch (IndexOutOfBoundsException badDrawOrder) {
            bitmap.recycle();
            bitmap = null;
            return;
        }
        BackdropBlur.apply(bitmap, sourcePixels, blurredPixels,
                BlurSettings.sampleRadius(host.getContext(), SAMPLE));
    }

    public void release() {
        hardwareBackdrop.release();
        if (bitmap != null) bitmap.recycle();
        bitmap = null;
    }

    @Override public void setAlpha(int alpha) {
        int next = Math.max(0, Math.min(255, alpha));
        if (this.alpha != next) { this.alpha = next; invalidateSelf(); }
    }
    @Override public int getAlpha() { return alpha; }
    @Override public void setColorFilter(@Nullable ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
