package dev.oneuiproject.oneui.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import dev.oneuiproject.oneui.design.R;

/** App-wide switch between the One UI palette and Android wallpaper accent colors. */
public final class MaterialColorSettings {
    public static final String KEY = "global_material_color_palette";

    private MaterialColorSettings() { }

    public static boolean isEnabled(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY, false);
    }

    public static void setEnabled(@NonNull Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(KEY, enabled).apply();
    }

    /** Call before inflating an activity's views. A recreated activity picks up changes. */
    public static void applyTheme(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isEnabled(activity))
            activity.getTheme().applyStyle(R.style.OneUIMaterialPaletteOverlay, true);
        int background = OneUIPalette.color(activity, R.color.oui_round_and_bgcolor);
        activity.getWindow().setBackgroundDrawable(new ColorDrawable(background));
        activity.getWindow().setStatusBarColor(background);
        activity.getWindow().setNavigationBarColor(background);
    }

    /** Resolves the active primary color for components that draw their own selection surface. */
    public static int accentColor(@NonNull Context context, int fallback) {
        return isEnabled(context) || PreferenceManager.getDefaultSharedPreferences(context)
                .contains(OneUIPalette.key(OneUIPalette.PRIMARY, OneUIPalette.isDark(context)))
                ? OneUIPalette.get(context, OneUIPalette.PRIMARY) : fallback;
    }
}
