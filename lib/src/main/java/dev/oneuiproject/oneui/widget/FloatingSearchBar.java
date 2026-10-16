package dev.oneuiproject.oneui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.content.ActivityNotFoundException;
import android.graphics.drawable.Drawable;
import android.graphics.Outline;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import dev.oneuiproject.oneui.design.R;

/** Bottom floating search surface. Its listener owns filtering and any page action visibility. */
public class FloatingSearchBar extends FrameLayout {
    public interface Listener {
        void onQueryChanged(String query);
        default void onQuerySubmitted(String query) { }
        default void onVisibilityChanged(boolean visible) { }
    }

    private final GlassSurfaceView surface;
    private final View aiSurface;
    private final ImageView searchIcon;
    private final EditText input;
    private final ImageButton close;
    private final Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF borderBounds = new RectF();
    private Listener listener;
    private Runnable closeRequested;
    private Runnable voiceRequested;
    private boolean open;
    private boolean suppressTextCallback;
    private boolean persistent;
    private boolean aiMode;
    private CharSequence normalHint = "Search";
    private boolean originalMarginsSaved;
    private int originalStartMargin;
    private int originalEndMargin;
    private static final int AI_START = 0xFF66A1F3;
    private static final int AI_END = 0xFF22C9A6;
    private int openDuration = 180;
    private int closeDuration = 130;

    public FloatingSearchBar(Context context) { this(context, null); }
    public FloatingSearchBar(Context context, AttributeSet attrs) {
        super(OneUIPalette.wrap(context, attrs), attrs);
        int minHeight = dp(56);
        setMinimumHeight(minHeight);
        setClipChildren(false);
        setClipToPadding(false);
        setVisibility(GONE);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dp(28));
            }
        });
        setElevation(dp(10));
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(Math.max(0.4f, getResources().getDisplayMetrics().density * 0.25f));
        border.setColor(0x60FFFFFF);
        surface = new GlassSurfaceView(getContext());
        surface.setCornerRadius(dp(28));
        surface.setTintColor(OneUIPalette.color(getContext(), R.color.oui_search_glass));
        addView(surface, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        aiSurface = new View(getContext());
        aiSurface.setVisibility(GONE);
        addView(aiSurface, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LinearLayout row = new LinearLayout(getContext());
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        addView(row, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        searchIcon = new ImageView(getContext());
        searchIcon.setImageResource(R.drawable.oui_ic_search);
        searchIcon.setContentDescription(null);
        int primary = OneUIPalette.color(getContext(), R.color.oui_primary_text_color);
        ColorStateList iconTint = ColorStateList.valueOf(primary);
        searchIcon.setImageTintList(iconTint);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconParams.leftMargin = dp(20);
        iconParams.rightMargin = dp(12);
        row.addView(searchIcon, iconParams);

        input = new EditText(getContext());
        input.setSingleLine(true);
        input.setGravity(Gravity.CENTER_VERTICAL);
        input.setIncludeFontPadding(false);
        input.setPadding(0, 0, 0, 0);
        input.setTextSize(16);
        input.setTextColor(primary);
        input.setHighlightColor(0x33777777);
        input.setHintTextColor(OneUIPalette.color(getContext(), R.color.oui_floating_nav_secondary));
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        input.setHint("Search");
        row.addView(input, new LinearLayout.LayoutParams(0, dp(48), 1));
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!suppressTextCallback && listener != null) listener.onQueryChanged(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return false;
            if (listener != null) listener.onQuerySubmitted(input.getText().toString());
            return true;
        });

        close = new ImageButton(getContext());
        close.setImageDrawable(new VoiceIconDrawable(false, 2.2f, dp(18)));
        close.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        close.setPadding(dp(8), dp(8), dp(8), dp(8));
        close.setImageTintList(null);
        close.setContentDescription("Voice search");
        close.setBackgroundResource(R.drawable.oui_search_item_feedback);
        close.setOnClickListener(v -> {
            if (voiceRequested != null) voiceRequested.run();
            else {
                try {
                    getContext().startActivity(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM));
                } catch (ActivityNotFoundException ignored) { }
            }
        });
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        closeParams.leftMargin = dp(4);
        closeParams.rightMargin = dp(12);
        row.addView(close, closeParams);
        if (attrs != null) {
            TypedArray options = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingSearchBar);
            boolean startPersistent = options.getBoolean(
                    R.styleable.FloatingSearchBar_searchPersistent, false);
            boolean startAi = options.getBoolean(R.styleable.FloatingSearchBar_searchAiMode, false);
            options.recycle();
            setPersistent(startPersistent);
            setAiMode(startAi);
        }
        OneUIPalette.observe(this, () -> {
            searchIcon.setImageTintList(android.content.res.ColorStateList.valueOf(
                    OneUIPalette.color(getContext(), R.color.oui_primary_text_color)));
            setAiMode(aiMode);
        });
    }

    @Override protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        float halfStroke = border.getStrokeWidth() / 2f;
        borderBounds.set(halfStroke, halfStroke,
                getWidth() - halfStroke, getHeight() - halfStroke);
        canvas.drawRoundRect(borderBounds, getHeight() / 2f, getHeight() / 2f, border);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        applyPersistentMargins();
        scheduleBlurRefresh();
    }

    @Override protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        scheduleBlurRefresh();
    }

    private void scheduleBlurRefresh() {
        if (surface != null && isAttachedToWindow() && isShown()) surface.refreshCapture();
    }

    public void setListener(@Nullable Listener callback) { listener = callback; }
    /** Keeps the full search field visible as an embedded or floating page widget. */
    public void setPersistent(boolean enabled) {
        persistent = enabled;
        animate().cancel();
        open = enabled;
        setVisibility(enabled ? VISIBLE : GONE);
        setAlpha(1f);
        setTranslationY(0);
        setScaleX(1f);
        setScaleY(1f);
        if (!enabled) {
            setQuery("", true);
            if (listener != null) listener.onVisibilityChanged(false);
        }
        applyPersistentMargins();
    }
    public boolean isPersistent() { return persistent; }
    public void setAiMode(boolean enabled) {
        aiMode = enabled;
        surface.setVisibility(enabled ? GONE : VISIBLE);
        aiSurface.setVisibility(enabled ? VISIBLE : GONE);
        searchIcon.setVisibility(enabled ? GONE : VISIBLE);
        input.setPadding(enabled ? dp(24) : 0, 0, 0, 0);
        input.setHint(enabled ? "What are you looking for?" : normalHint);
        input.setTextColor(OneUIPalette.get(getContext(), OneUIPalette.FOREGROUND));
        input.setHintTextColor(OneUIPalette.secondaryText(getContext()));
        input.getPaint().setShader(null);
        input.invalidate();
        close.setImageDrawable(new VoiceIconDrawable(enabled, 2.2f, dp(18)));
        close.setImageTintList(ColorStateList.valueOf(OneUIPalette.get(getContext(), OneUIPalette.FOREGROUND)));
        if (enabled) {
            GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{0xFF364B6F, OneUIPalette.color(getContext(), R.color.oui_ai_secondary_surface)});
            gradient.setCornerRadius(dp(28));
            aiSurface.setBackground(gradient);
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                setOutlineAmbientShadowColor(0x6666A1F3);
                setOutlineSpotShadowColor(0x5566A1F3);
            }
            setElevation(dp(14));
            border.setColor(0x8866A1F3);
        } else {
            setElevation(dp(10));
            border.setColor(0x60FFFFFF);
        }
        surface.setTintColor(OneUIPalette.color(getContext(), R.color.oui_search_glass));
        invalidate();
    }
    public boolean isAiMode() { return aiMode; }
    public void setOnVoiceRequested(@Nullable Runnable callback) { voiceRequested = callback; }
    private void applyPersistentMargins() {
        if (!(getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams)) return;
        android.view.ViewGroup.MarginLayoutParams params =
                (android.view.ViewGroup.MarginLayoutParams) getLayoutParams();
        if (!originalMarginsSaved) {
            originalStartMargin = params.getMarginStart();
            originalEndMargin = params.getMarginEnd();
            originalMarginsSaved = true;
        }
        int extra = persistent ? dp(8) : 0;
        if (params.getMarginStart() != originalStartMargin + extra
                || params.getMarginEnd() != originalEndMargin + extra) {
            params.setMarginStart(originalStartMargin + extra);
            params.setMarginEnd(originalEndMargin + extra);
            setLayoutParams(params);
        }
    }
    public void setOnCloseRequested(@Nullable Runnable callback) { closeRequested = callback; }
    public void setSourceView(@Nullable android.view.View source) { surface.setSourceView(source); }
    public GlassSurfaceView getSurface() { return surface; }
    public EditText getInput() { return input; }
    public ImageButton getCloseButton() { return close; }
    /** Hide the exit action for a search field that stays on its page. */
    public void setCloseButtonVisible(boolean visible) {
        close.setVisibility(visible ? VISIBLE : GONE);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) input.getLayoutParams();
        params.rightMargin = visible ? 0 : dp(20);
        input.setLayoutParams(params);
    }
    public ImageView getSearchIcon() { return searchIcon; }
    public void setHint(CharSequence hint) {
        normalHint = hint;
        if (!aiMode) input.setHint(hint);
    }
    public void setSearchIcon(@Nullable Drawable icon) { searchIcon.setImageDrawable(icon); }
    public void setCloseIcon(@Nullable Drawable icon) { close.setImageDrawable(icon); }
    public void setAnimationDurations(int openMillis, int closeMillis) {
        openDuration = Math.max(0, openMillis);
        closeDuration = Math.max(0, closeMillis);
    }
    public boolean isOpen() { return open; }
    public String getQuery() { return input.getText().toString(); }
    public void setQuery(@Nullable CharSequence query, boolean notify) {
        suppressTextCallback = !notify;
        input.setText(query == null ? "" : query);
        input.setSelection(input.length());
        suppressTextCallback = false;
    }

    public void open(@Nullable CharSequence previousQuery) {
        if (isOpen()) return;
        open = true;
        setQuery(previousQuery, false);
        animate().cancel();
        setVisibility(VISIBLE);
        setAlpha(0f);
        setTranslationY(dp(28));
        setScaleX(.94f);
        setScaleY(.94f);
        animate().alpha(1f).translationY(0).scaleX(1f).scaleY(1f)
                .setDuration(openDuration).withEndAction(() -> {
                    input.requestFocus();
                    ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                }).start();
        if (listener != null) listener.onVisibilityChanged(true);
    }

    public void close() {
        if (!isOpen()) return;
        open = false;
        animate().cancel();
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(input.getWindowToken(), 0);
        input.clearFocus();
        setQuery("", false);
        if (listener != null) {
            listener.onQueryChanged("");
            listener.onVisibilityChanged(false);
        }
        animate().alpha(0f).translationY(dp(20)).scaleX(.96f).scaleY(.96f)
                .setDuration(closeDuration).withEndAction(() -> setVisibility(GONE)).start();
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class VoiceIconDrawable extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final boolean gradient;
        private final float stroke;
        private final int size;
        VoiceIconDrawable(boolean gradient, float stroke, int size) {
            this.gradient = gradient;
            this.stroke = stroke;
            this.size = size;
        }
        @Override public int getIntrinsicWidth() { return size; }
        @Override public int getIntrinsicHeight() { return size; }
        @Override public void draw(Canvas canvas) {
            android.graphics.Rect bounds = getBounds();
            int save = canvas.save();
            canvas.translate(bounds.left, bounds.top);
            canvas.scale(bounds.width() / 24f, bounds.height() / 24f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(gradient ? AI_START : 0xFF8A8A90);
            paint.setShader(gradient ? new LinearGradient(4, 0, 20, 0,
                    AI_START, AI_END, Shader.TileMode.CLAMP) : null);
            canvas.drawRoundRect(9, 3, 15, 15, 3, 3, paint);
            canvas.drawArc(5, 7, 19, 19, 0, 180, false, paint);
            canvas.drawLine(12, 19, 12, 22, paint);
            canvas.drawLine(8, 22, 16, 22, paint);
            paint.setShader(null);
            canvas.restoreToCount(save);
        }
        @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
        @Override public void setColorFilter(android.graphics.ColorFilter filter) {
            paint.setColorFilter(filter);
        }
        @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
    }
}
