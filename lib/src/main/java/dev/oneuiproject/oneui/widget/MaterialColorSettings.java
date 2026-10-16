package dev.oneuiproject.oneui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import dev.oneuiproject.oneui.design.R;

/** App-wide switch between native device theming and the custom One UI colors. */
public final class MaterialColorSettings {
    public static final String KEY = "global_use_color_palette";

    private MaterialColorSettings() { }

    public static boolean isEnabled(@NonNull Context context) {
        // The old off state already followed Samsung's OS theme. Preserve it on upgrade.
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY, true);
    }

    public static void setEnabled(@NonNull Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(KEY, enabled).apply();
    }

    /** Call before inflating an activity's views. A recreated activity picks up changes. */
    public static void applyTheme(@NonNull Activity activity) {
        OneUIPalette.migrateDefaults(activity);
        // Keep the device's native SESL theme when enabled; do not replace it with Monet.
        if (!isEnabled(activity))
            activity.getTheme().applyStyle(R.style.OneUICustomPaletteOverlay, true);
        int background = OneUIPalette.color(activity, R.color.oui_round_and_bgcolor);
        activity.getWindow().setBackgroundDrawable(new ColorDrawable(background));
        activity.getWindow().setStatusBarColor(background);
        activity.getWindow().setNavigationBarColor(background);
    }

    /** Resolves the active primary color for components that draw their own selection surface. */
    public static int accentColor(@NonNull Context context, int fallback) {
        return OneUIPalette.get(context, OneUIPalette.PRIMARY);
    }
}
