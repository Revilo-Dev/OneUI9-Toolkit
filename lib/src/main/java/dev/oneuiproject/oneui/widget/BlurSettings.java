package dev.oneuiproject.oneui.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

/** App-wide switch for backdrop blur; individual components keep their own blur options. */
public final class BlurSettings {
    public static final String KEY = "global_blur_enabled";
    public static final String INTENSITY_KEY = "global_blur_intensity";
    public static final String ALPHA_KEY = "global_blur_alpha";
    public static final int DEFAULT_INTENSITY = 18;
    public static final int DEFAULT_ALPHA = 65;
    /** Downsampling shared by floating glass surfaces throughout the app. */
    public static final int GLASS_SAMPLE_SCALE = 6;

    private BlurSettings() { }

    /** Blur radius in density-independent pixels. Zero keeps a clear backdrop. */
    public static int getIntensity(@NonNull Context context) {
        return Math.max(0, Math.min(40, PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(INTENSITY_KEY, DEFAULT_INTENSITY)));
    }

    public static int getAlpha(@NonNull Context context) {
        return Math.max(0, Math.min(100, PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(ALPHA_KEY, DEFAULT_ALPHA)));
    }

    public static int tint(@NonNull Context context, int color) {
        return (color & 0x00ffffff) | (Math.round(getAlpha(context) * 2.55f) << 24);
    }

    static int sampleRadius(@NonNull Context context, int sampleScale) {
        return Math.round(getIntensity(context) * context.getResources().getDisplayMetrics().density
                / (sampleScale * 1.5f));
    }

    public static boolean isEnabled(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY, true);
    }

    public static void setEnabled(@NonNull Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(KEY, enabled).apply();
    }

    /** Redraws a component immediately when the preference changes. Call once per view. */
    public static void observe(@NonNull View view) {
        observe(view, view::invalidate);
    }

    public static void observe(@NonNull View view, @NonNull Runnable onChange) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        SharedPreferences.OnSharedPreferenceChangeListener listener = (shared, key) -> {
            if (KEY.equals(key) || INTENSITY_KEY.equals(key) || ALPHA_KEY.equals(key)) onChange.run();
        };
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View attached) {
                preferences.registerOnSharedPreferenceChangeListener(listener);
            }
            @Override public void onViewDetachedFromWindow(View detached) {
                preferences.unregisterOnSharedPreferenceChangeListener(listener);
            }
        });
        if (view.isAttachedToWindow())
            preferences.registerOnSharedPreferenceChangeListener(listener);
    }
}
