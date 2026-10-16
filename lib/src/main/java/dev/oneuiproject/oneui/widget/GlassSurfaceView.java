package dev.oneuiproject.oneui.widget;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.graphics.Outline;
import android.view.PixelCopy;
import android.view.Window;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;

import dev.oneuiproject.oneui.design.R;

/** A reusable rounded glass surface that blurs a supplied view behind it on API 23+. */
public class GlassSurfaceView extends View {
    private int sampleScale = BlurSettings.GLASS_SAMPLE_SCALE;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Path clip = new Path();
    private View source;
    private Window captureWindow;
    private Bitmap bitmap;
    private int[] pixels;
    private int[] blurred;
    private float radius;
    private boolean blurEnabled = true;
    private Integer tintColor;
    private boolean capturePending;
    private boolean captureDirty = true;
    private int captureGeneration;
    private long lastCaptureTime;
    private final BackdropRenderNode hardwareBackdrop = new BackdropRenderNode();
    private final Rect backdropBounds = new Rect();
    private ViewTreeObserver hostObserver, sourceObserver;
    private final int[] position = new int[2];
    private int lastX = Integer.MIN_VALUE, lastY = Integer.MIN_VALUE;
    private int lastWidth, lastHeight;
    private final Runnable pendingRefresh = this::invalidate;
    private final ViewTreeObserver.OnScrollChangedListener scrollChanged = () -> {
        if (isShown()) refreshCapture();
    };
    private final ViewTreeObserver.OnPreDrawListener preDraw = () -> {
        if (!isShown() || !blurEnabled || !BlurSettings.isEnabled(getContext())) return true;
        View backdrop = observedSource();
        getLocationOnScreen(position);
        boolean moved = lastX != position[0] || lastY != position[1]
                || lastWidth != getWidth() || lastHeight != getHeight();
        lastX = position[0]; lastY = position[1];
        lastWidth = getWidth(); lastHeight = getHeight();
        // A sibling's dirty state reflects actual backdrop updates. An ancestor also
        // becomes dirty when this surface redraws, so observing it would feed back forever.
        if (moved || (backdrop != null && !isAncestor(backdrop) && backdrop.isDirty())) refreshCapture();
        return true;
    };

    public GlassSurfaceView(Context context) { this(context, null); }
    public GlassSurfaceView(Context context, @Nullable AttributeSet attrs) {
        super(OneUIPalette.wrap(context, attrs), attrs);
        radius = dp(28);
        setWillNotDraw(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        BlurSettings.observe(this, this::refreshCapture);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
    }

    public void setSourceView(@Nullable View view) {
        source = view;
        observeBackdrop();
        refreshCapture();
    }

    /** Requests a fresh window sample; useful when content moves behind a floating bar. */
    public void refreshCapture() {
        captureDirty = true;
        invalidate();
    }

    /** Copies an already rendered activity window instead of redrawing its view tree. */
    public void setCaptureWindow(@Nullable Window window) {
        captureWindow = window;
        captureGeneration++;
        capturePending = false;
        captureDirty = true;
        observeBackdrop();
        invalidate();
    }
    public void setCornerRadius(float pixels) {
        radius = Math.max(0, pixels);
        invalidateOutline();
        invalidate();
    }
    public void setBlurEnabled(boolean enabled) { blurEnabled = enabled; invalidate(); }
    /** Lower values keep more detail in the frosted background. */
    public void setBlurSampleScale(int scale) {
        sampleScale = Math.max(2, scale);
        captureDirty = true;
        invalidate();
    }
    public void setTintColor(@Nullable Integer color) { tintColor = color; invalidate(); }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) return;
        clip.reset();
        clip.addRoundRect(0, 0, getWidth(), getHeight(), radius, radius, Path.Direction.CW);
        int save = canvas.save();
        canvas.clipPath(clip);
        boolean effectiveBlur = blurEnabled && BlurSettings.isEnabled(getContext());
        if (effectiveBlur && BlurSettings.getIntensity(getContext()) > 0 && !isInEditMode()) {
            backdropBounds.set(0, 0, getWidth(), getHeight());
            boolean hardware = hardwareBackdrop.draw(canvas, this, source, backdropBounds, captureDirty);
            if (hardware) {
                captureDirty = false;
                lastCaptureTime = SystemClock.uptimeMillis();
            } else if (captureWindow != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestWindowCapture();
            } else if (source != null && source != this
                    && captureDirty && SystemClock.uptimeMillis() - lastCaptureTime >= 32) {
                capture();
                captureDirty = false;
                lastCaptureTime = SystemClock.uptimeMillis();
            } else if (source == null) captureDirty = false;
            if (!hardware && bitmap != null) {
                paint.setColor(android.graphics.Color.WHITE);
                canvas.drawBitmap(bitmap, null, new Rect(0, 0, getWidth(), getHeight()), paint);
            }
        }
        int tint = effectiveBlur && tintColor != null ? tintColor
                : OneUIPalette.color(getContext(), effectiveBlur ? R.color.oui_glass_surface
                        : R.color.oui_floating_nav_surface);
        paint.setColor(effectiveBlur ? BlurSettings.tint(getContext(), tint) : tint);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        canvas.restoreToCount(save);
        // At most one pending redraw for a dirty backdrop; an idle surface does no work.
        if (effectiveBlur && captureDirty && !capturePending && isShown()
                && BlurSettings.getIntensity(getContext()) > 0 && observedSource() != null) {
            removeCallbacks(pendingRefresh);
            postDelayed(pendingRefresh, 32);
        }
    }

    private void requestWindowCapture() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || capturePending || !captureDirty
                || SystemClock.uptimeMillis() - lastCaptureTime < 32)
            return;
        View decor = captureWindow.getDecorView();
        if (!decor.isAttachedToWindow()) return;
        int[] here = new int[2];
        int[] there = new int[2];
        getLocationOnScreen(here);
        decor.getLocationOnScreen(there);
        Rect region = new Rect(here[0] - there[0], here[1] - there[1],
                here[0] - there[0] + getWidth(), here[1] - there[1] + getHeight());
        if (!region.intersect(0, 0, decor.getWidth(), decor.getHeight())) return;
        Bitmap sample = Bitmap.createBitmap(Math.max(1, getWidth() / sampleScale),
                Math.max(1, getHeight() / sampleScale), Bitmap.Config.ARGB_8888);
        int generation = captureGeneration;
        capturePending = true;
        try {
            PixelCopy.request(captureWindow, region, sample, result -> {
                if (generation != captureGeneration || !isAttachedToWindow()) {
                    sample.recycle();
                    return;
                }
                capturePending = false;
                if (result == PixelCopy.SUCCESS) {
                    blurBitmap(sample);
                    Bitmap previous = bitmap;
                    bitmap = sample;
                    if (previous != null) previous.recycle();
                    captureDirty = false;
                    lastCaptureTime = SystemClock.uptimeMillis();
                } else {
                    sample.recycle();
                    lastCaptureTime = SystemClock.uptimeMillis();
                    captureDirty = false;
                }
                invalidate();
            }, new Handler(Looper.getMainLooper()));
        } catch (IllegalArgumentException error) {
            capturePending = false;
            captureDirty = false;
            sample.recycle();
        }
    }

    private void blurBitmap(Bitmap sample) {
        int width = sample.getWidth();
        int height = sample.getHeight();
        if (pixels == null || pixels.length != width * height) {
            pixels = new int[width * height];
            blurred = new int[width * height];
        }
        BackdropBlur.apply(sample, pixels, blurred, BlurSettings.sampleRadius(getContext(), sampleScale));
    }

    private void capture() {
        int width = Math.max(1, (getWidth() + sampleScale - 1) / sampleScale);
        int height = Math.max(1, (getHeight() + sampleScale - 1) / sampleScale);
        if (bitmap == null || bitmap.getWidth() != width || bitmap.getHeight() != height) {
            if (bitmap != null) bitmap.recycle();
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            pixels = new int[width * height];
            blurred = new int[width * height];
        }
        bitmap.eraseColor(0);
        Canvas sample = new Canvas(bitmap);
        sample.scale((float) width / getWidth(), (float) height / getHeight());
        int[] here = new int[2];
        int[] there = new int[2];
        getLocationOnScreen(here);
        source.getLocationOnScreen(there);
        sample.translate(there[0] - here[0], there[1] - here[1]);
        try {
            source.draw(sample);
        } catch (IndexOutOfBoundsException badDrawOrder) {
            // A view hierarchy changing mid-frame cannot be sampled safely.
            bitmap.recycle();
            bitmap = null;
            return;
        }
        blurBitmap(bitmap);
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopObserving();
        hardwareBackdrop.release();
        removeCallbacks(pendingRefresh);
        captureGeneration++;
        capturePending = false;
        if (bitmap != null) { bitmap.recycle(); bitmap = null; }
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        observeBackdrop();
        refreshCapture();
    }

    private View observedSource() {
        return source != null ? source : captureWindow != null ? captureWindow.getDecorView() : null;
    }

    private boolean isAncestor(View candidate) {
        android.view.ViewParent parent = getParent();
        while (parent instanceof View) {
            if (parent == candidate) return true;
            parent = parent.getParent();
        }
        return false;
    }

    private void observeBackdrop() {
        stopObserving();
        if (!isAttachedToWindow()) return;
        hostObserver = getViewTreeObserver();
        hostObserver.addOnPreDrawListener(preDraw);
        View backdrop = observedSource();
        if (backdrop != null && backdrop.isAttachedToWindow()) {
            sourceObserver = backdrop.getViewTreeObserver();
            sourceObserver.addOnScrollChangedListener(scrollChanged);
            if (sourceObserver != hostObserver) sourceObserver.addOnPreDrawListener(preDraw);
        }
    }

    private void stopObserving() {
        if (hostObserver != null && hostObserver.isAlive()) hostObserver.removeOnPreDrawListener(preDraw);
        if (sourceObserver != null && sourceObserver.isAlive()) {
            sourceObserver.removeOnScrollChangedListener(scrollChanged);
            if (sourceObserver != hostObserver) sourceObserver.removeOnPreDrawListener(preDraw);
        }
        hostObserver = sourceObserver = null;
    }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
