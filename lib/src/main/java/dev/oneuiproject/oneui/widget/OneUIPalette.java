package dev.oneuiproject.oneui.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import dev.oneuiproject.oneui.design.R;

/** Shared color roles for One UI components and the sample application. */
public final class OneUIPalette {
    public static final String PRIMARY = "primary";
    public static final String SECONDARY = "secondary";
    public static final String BACKGROUND = "background";
    public static final String FOREGROUND = "foreground";
    public static final String SURFACE = "surface";
    public static final String SURFACE_VARIANT = "surface_variant";
    public static final String OUTLINE = "outline";
    public static final String ERROR = "error";
    public static final String[] ROLES = {PRIMARY, SECONDARY, BACKGROUND, FOREGROUND,
            SURFACE, SURFACE_VARIANT, OUTLINE, ERROR};

    private static final String PREFIX = "oneui_palette_";

    private OneUIPalette() { }

    /** Discard only saved values matching the former defaults; retain other user colors. */
    static void migrateDefaults(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.getInt("oneui_palette_defaults_version", 0) >= 2) return;
        SharedPreferences.Editor editor = preferences.edit();
        if (preferences.getInt(key(BACKGROUND, true), 0) == 0xff171717)
            editor.remove(key(BACKGROUND, true));
        if (preferences.getInt(key(BACKGROUND, false), 0) == 0xfffcfcfc)
            editor.remove(key(BACKGROUND, false));
        if (preferences.getInt(key(SURFACE, true), 0) == 0xff2e2e30)
            editor.remove(key(SURFACE, true));
        editor.putInt("oneui_palette_defaults_version", 2).apply();
    }

    /** Gives every component an independent, inheritable OS-palette option. */
    public static Context wrap(@NonNull Context context, AttributeSet attrs) {
        return new PaletteContext(context, attrs);
    }

    public static boolean usesColorPalette(@NonNull Context context) {
        Context current = context;
        while (current instanceof android.content.ContextWrapper) {
            if (current instanceof PaletteContext) return ((PaletteContext) current).usesPalette;
            current = ((android.content.ContextWrapper) current).getBaseContext();
        }
        return true;
    }

    public static boolean usesSystemColors(@NonNull Context context) {
        return MaterialColorSettings.isEnabled(context) && usesColorPalette(context);
    }

    /** Runtime equivalent of app:usesColorPalette, including a component's child controls. */
    public static void setUsesColorPalette(@NonNull View component, boolean enabled) {
        Context context = component.getContext();
        while (context instanceof android.content.ContextWrapper) {
            if (context instanceof PaletteContext) {
                ((PaletteContext) context).setUsesPalette(enabled);
                break;
            }
            context = ((android.content.ContextWrapper) context).getBaseContext();
        }
        if (component instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) component;
            for (int i = 0; i < group.getChildCount(); i++)
                setUsesColorPalette(group.getChildAt(i), enabled);
        }
        applyToViewTree(component);
    }

    /** Registers a custom-drawn component's color refresh after construction. */
    public static void observe(@NonNull View view, @NonNull Runnable refresh) {
        view.setTag(R.id.oui_palette_refresh, refresh);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        SharedPreferences.OnSharedPreferenceChangeListener listener = (shared, changed) -> {
            if (MaterialColorSettings.KEY.equals(changed) || (changed != null && changed.startsWith(PREFIX))) {
                view.setTag(R.id.oui_palette_signature, null);
                refresh.run();
                view.invalidate();
            }
        };
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View attached) {
                preferences.registerOnSharedPreferenceChangeListener(listener);
                refresh.run();
            }
            @Override public void onViewDetachedFromWindow(View detached) {
                preferences.unregisterOnSharedPreferenceChangeListener(listener);
            }
        });
        if (view.isAttachedToWindow()) preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public static boolean isDark(@NonNull Context context) {
        return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    public static String key(@NonNull String role, boolean dark) {
        return PREFIX + role + (dark ? "_dark" : "_light");
    }

    /** Original light/dark resources form the unmodified Default OneUI palette. */
    public static int defaultColor(@NonNull Context context, @NonNull String role) {
        switch (role) {
            case PRIMARY: return context.getColor(R.color.oui_slider_active_color);
            case SECONDARY: return context.getColor(R.color.oui_floating_nav_secondary);
            case BACKGROUND: return context.getColor(R.color.oui_round_and_bgcolor);
            case FOREGROUND: return context.getColor(R.color.oui_primary_text_color);
            case SURFACE: return context.getColor(R.color.oui_surface_color);
            case SURFACE_VARIANT: return context.getColor(R.color.oui_ai_secondary_surface);
            case OUTLINE: return context.getColor(R.color.oui_slider_track_color);
            case ERROR: return context.getColor(R.color.oui_functional_red_color);
            default: throw new IllegalArgumentException("Unknown palette role: " + role);
        }
    }

    /** The native device theme takes priority when this component uses the system palette. */
    public static int get(@NonNull Context context, @NonNull String role) {
        int fallback = defaultColor(context, role);
        if (usesSystemColors(context)) {
            int attribute;
            switch (role) {
                case PRIMARY: attribute = R.attr.colorControlActivated; break;
                case SECONDARY: attribute = R.attr.colorSecondary; break;
                case BACKGROUND: attribute = android.R.attr.colorBackground; break;
                case FOREGROUND: attribute = android.R.attr.textColorPrimary; break;
                case SURFACE: attribute = R.attr.colorSurface; break;
                case SURFACE_VARIANT: return fallback;
                case OUTLINE: attribute = R.attr.colorControlNormal; break;
                case ERROR: return fallback;
                default: throw new IllegalArgumentException("Unknown palette role: " + role);
            }
            TypedValue value = new TypedValue();
            if (!context.getTheme().resolveAttribute(attribute, value, true)) return fallback;
            if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT)
                return value.data;
            if (value.resourceId != 0) {
                try { return context.getColorStateList(value.resourceId).getDefaultColor(); }
                catch (android.content.res.Resources.NotFoundException ignored) { }
            }
            return fallback;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key(role, isDark(context)), fallback);
    }

    /** Secondary text follows foreground/theme text contrast, independently of the secondary accent. */
    public static int secondaryText(@NonNull Context context) {
        int text = withAlpha(get(context, FOREGROUND), .7f);
        if (usesSystemColors(context)) {
            TypedValue value = new TypedValue();
            if (context.getTheme().resolveAttribute(android.R.attr.textColorSecondary, value, true)) {
                if (value.resourceId != 0) text = context.getColorStateList(value.resourceId).getDefaultColor();
                else if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT)
                    text = value.data;
            }
        }
        // CollapsingToolbar animates text alpha itself. Resolve the secondary shade
        // into RGB so its alpha animation cannot turn gray text back into white/black.
        return androidx.core.graphics.ColorUtils.compositeColors(text, get(context, BACKGROUND) | 0xff000000);
    }

    /** Use after native/recycled controls have rebound their drawable and text state. */
    public static void refreshViewTree(@NonNull View root) {
        clearSignatures(root);
        applyToViewTree(root);
    }

    private static void clearSignatures(View root) {
        root.setTag(R.id.oui_palette_signature, null);
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) clearSignatures(group.getChildAt(i));
        }
    }

    public static void set(@NonNull Context context, @NonNull String role, boolean dark, int color) {
        defaultColor(context, role); // Validate the role before writing it.
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putInt(key(role, dark), color).apply();
    }

    public static void reset(@NonNull Context context, boolean dark) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        for (String role : ROLES) editor.remove(key(role, dark));
        editor.apply();
    }

    /** Resolves existing component color resources through the active palette. */
    public static int color(@NonNull Context context, @ColorRes int resource) {
        if (resource == R.color.oui_slider_active_color)
            return get(context, PRIMARY);
        if (resource == R.color.oui_floating_nav_secondary)
            return secondaryText(context);
        if (resource == R.color.oui_background_color || resource == R.color.oui_round_and_bgcolor)
            return get(context, BACKGROUND);
        if (resource == R.color.oui_floating_nav_primary || resource == R.color.oui_primary_text_color || resource == R.color.oui_primary_icon_color
                || resource == R.color.oui_grid_menu_dialog_item_icon_text_color
                || resource == R.color.oui_relative_link_title_text_color
                || resource == R.color.oui_unclickablepref_text_color)
            return get(context, FOREGROUND);
        if (resource == R.color.oui_surface_color || resource == R.color.oui_floating_nav_surface || resource == R.color.oui_drawer_panel_fade
                || resource == R.color.oui_tipscardpref_bg_color || resource == R.color.oui_appinfolayout_button_bg_color)
            return get(context, SURFACE);
        if (resource == R.color.oui_ai_secondary_surface || resource == R.color.oui_grid_menu_icon_circle)
            return get(context, SURFACE_VARIANT);
        if (resource == R.color.oui_slider_track_color || resource == R.color.oui_btn_border_color
                || resource == R.color.oui_list_divider_color)
            return get(context, OUTLINE);
        if (resource == R.color.oui_functional_red_color || resource == R.color.oui_slider_warning_color)
            return get(context, ERROR);
        if (resource == R.color.oui_floating_toolbar_light
                || resource == R.color.oui_floating_toolbar_dark)
            return get(context, SURFACE);
        if (resource == R.color.oui_floating_toolbar_light_text
                || resource == R.color.oui_floating_toolbar_dark_text)
            return get(context, FOREGROUND);
        if (resource == R.color.oui_floating_toolbar_glass_light
                || resource == R.color.oui_floating_toolbar_glass_dark)
            return (get(context, SURFACE) & 0x00ffffff) | (context.getColor(resource) & 0xff000000);
        if (resource == R.color.oui_floating_nav_glass || resource == R.color.oui_search_glass
                || resource == R.color.oui_glass_surface || resource == R.color.oui_drawer_glass_tint)
            return (get(context, SURFACE) & 0x00ffffff) | (context.getColor(resource) & 0xff000000);
        if (resource == R.color.oui_floating_nav_selected)
            return context.getColor(resource);
        if (resource == R.color.oui_floating_nav_press)
            return (get(context, FOREGROUND) & 0x00ffffff) | (context.getColor(resource) & 0xff000000);
        return context.getColor(resource);
    }

    /** Applies palette roles to XML colors after a screen has been inflated. */
    @android.annotation.SuppressLint("RestrictedApi")
    public static void applyToViewTree(@NonNull View root) {
        applyToViewTree(root, new PalettePass(root.getContext()));
    }

    /** A layout pass reads each custom palette once, rather than eight reads per view. */
    private static final class PalettePass {
        final boolean systemEnabled;
        final int[][] colors = new int[2][];
        final int[] signatures = new int[2];
        PalettePass(Context context) { systemEnabled = MaterialColorSettings.isEnabled(context); }
        int mode(Context context) {
            int mode = isDark(context) ? 1 : 0;
            if (colors[mode] == null) {
                colors[mode] = new int[ROLES.length];
                int signature = 2;
                for (int i = 0; i < ROLES.length; i++) {
                    colors[mode][i] = get(context, ROLES[i]);
                    signature = 31 * signature + colors[mode][i];
                }
                signatures[mode] = signature;
            }
            return mode;
        }
    }

    @android.annotation.SuppressLint("RestrictedApi")
    private static void applyToViewTree(View root, PalettePass pass) {
        if (root.getVisibility() != View.VISIBLE) return;
        Context context = root.getContext();
        boolean systemColors = pass.systemEnabled && usesColorPalette(context);
        int mode = systemColors ? -1 : pass.mode(context);
        int signature = systemColors ? 1 : pass.signatures[mode];
        Integer previous = (Integer) root.getTag(R.id.oui_palette_signature);
        if (previous != null && previous == signature) {
            if (!(root instanceof ImageTileView)
                    && !(root.getTag(R.id.oui_palette_refresh) instanceof Runnable)) applyToChildren(root, pass);
            return;
        }
        root.setTag(R.id.oui_palette_custom_applied, !systemColors);
        root.setTag(R.id.oui_palette_signature, signature);
        if (systemColors && root.getTag(R.id.oui_palette_original_background) instanceof android.graphics.drawable.Drawable)
            root.setBackground((android.graphics.drawable.Drawable) root.getTag(R.id.oui_palette_original_background));
        refreshCustomView(root);
        // Composite widgets own the selected/secondary colors of their internal controls.
        if (root.getTag(R.id.oui_palette_refresh) instanceof Runnable) return;
        // Photo captions keep the contrast chosen for their image content.
        if (root instanceof ImageTileView) return;
        int primary = systemColors ? get(context, PRIMARY) : pass.colors[mode][0];
        int secondary = systemColors ? get(context, SECONDARY) : pass.colors[mode][1];
        int foreground = systemColors ? get(context, FOREGROUND) : pass.colors[mode][3];
        int textSecondary = secondaryText(context);
        int outline = systemColors ? get(context, OUTLINE) : pass.colors[mode][6];
        ColorStateList control = new ColorStateList(new int[][]{
                new int[]{-android.R.attr.state_enabled}, new int[]{android.R.attr.state_checked},
                new int[]{android.R.attr.state_selected}, new int[]{}},
                new int[]{withAlpha(outline, 0.38f), primary, primary, outline});
        Object tag = root.getTag();
        if (tag instanceof String && ((String) tag).startsWith("oneui.palette.")) {
            String role = ((String) tag).substring("oneui.palette.".length());
            rememberBackground(root);
            root.setBackgroundColor(get(context, role));
        }
        if (root instanceof TextView) {
            TextView text = (TextView) root;
            boolean summary = root.getId() == android.R.id.summary
                    || root.getId() == com.google.android.material.R.id.collapsing_appbar_extended_subtitle
                    || Boolean.TRUE.equals(root.getTag(R.id.oui_palette_secondary_text))
                    || text.getCurrentTextColor() == textSecondary;
            text.setTextColor(enabledColors(summary ? textSecondary : foreground));
            text.setLinkTextColor(primary);
            text.setHighlightColor(withAlpha(primary, 0.3f));
            if (text instanceof EditText) {
                text.setHintTextColor(enabledColors(textSecondary));
                text.setBackgroundTintList(new ColorStateList(new int[][]{
                        new int[]{android.R.attr.state_focused}, new int[]{}}, new int[]{primary, outline}));
            }
            if (text instanceof Button && !(text instanceof CompoundButton)) {
                ColorStateList background = text.getBackgroundTintList();
                Boolean filled = (Boolean) root.getTag(R.id.oui_palette_filled_button);
                boolean colored = filled != null ? filled : background != null
                        && background.getDefaultColor() == defaultColor(context, PRIMARY);
                root.setTag(R.id.oui_palette_filled_button, colored);
                if (colored) text.setBackgroundTintList(enabledColors(primary));
                int onPrimary = androidx.core.graphics.ColorUtils.calculateLuminance(primary) > 0.5
                        ? Color.BLACK : Color.WHITE;
                text.setTextColor(enabledColors(colored ? onPrimary : foreground));
            }
        }
        if (root instanceof SwitchCompat) {
            ToggleStyle.apply((SwitchCompat) root);
        } else if (root instanceof CompoundButton) ((CompoundButton) root).setButtonTintList(control);
        if (root instanceof ProgressBar) {
            ProgressBar progress = (ProgressBar) root;
            progress.setProgressTintList(enabledColors(primary));
            progress.setIndeterminateTintList(enabledColors(primary));
            progress.setSecondaryProgressTintList(enabledColors(secondary));
            progress.setProgressBackgroundTintList(enabledColors(outline));
            if (root instanceof SeekBar) ((SeekBar) root).setThumbTintList(enabledColors(primary));
        }
        // SESL controls have their own base class, independent of android.widget.ProgressBar.
        if (root instanceof androidx.appcompat.widget.SeslProgressBar) {
            androidx.appcompat.widget.SeslProgressBar progress = (androidx.appcompat.widget.SeslProgressBar) root;
            progress.setProgressTintList(enabledColors(primary));
            progress.setIndeterminateTintList(enabledColors(primary));
            progress.setSecondaryProgressTintList(enabledColors(secondary));
            progress.setProgressBackgroundTintList(enabledColors(outline));
            if (root instanceof androidx.appcompat.widget.SeslAbsSeekBar)
                ((androidx.appcompat.widget.SeslAbsSeekBar) root).setThumbTintList(enabledColors(primary));
        }
        if (root instanceof TabLayout) {
            TabLayout tabs = (TabLayout) root;
            tabs.setTabTextColors(textSecondary, foreground);
            tabs.setSelectedTabIndicatorColor(primary);
        }
        if (root instanceof BottomNavigationView) {
            BottomNavigationView navigation = (BottomNavigationView) root;
            ColorStateList navigationText = new ColorStateList(new int[][]{
                    {-android.R.attr.state_enabled}, {android.R.attr.state_checked}, {}},
                    new int[]{withAlpha(foreground, .38f), foreground, textSecondary});
            navigation.setItemIconTintList(navigationText);
            navigation.setItemTextColor(navigationText);
        }
        if (root instanceof Toolbar) {
            Toolbar toolbar = (Toolbar) root;
            toolbar.setTitleTextColor(foreground);
            toolbar.setSubtitleTextColor(textSecondary);
            if (toolbar.getNavigationIcon() != null) toolbar.getNavigationIcon().mutate().setTint(foreground);
            if (toolbar.getOverflowIcon() != null) toolbar.getOverflowIcon().mutate().setTint(foreground);
        }
        if (root instanceof ImageView) {
            ImageView image = (ImageView) root;
            ColorStateList tint = image.getImageTintList();
            if (tint != null)
                image.setImageTintList(ColorStateList.valueOf(get(context, FOREGROUND)));
        }
        if (!(tag instanceof String && ((String) tag).startsWith("oneui.palette."))
                && root.getBackground() instanceof ColorDrawable) {
            int color = ((ColorDrawable) root.getBackground()).getColor();
            if (color == context.getColor(R.color.oui_surface_color)
                    || color == context.getColor(R.color.oui_background_color)) {
                rememberBackground(root);
                root.setBackgroundColor(get(context, SURFACE));
            } else if (color == context.getColor(R.color.oui_round_and_bgcolor)) {
                rememberBackground(root);
                root.setBackgroundColor(get(context, BACKGROUND));
            }
        }
        applyToChildren(root, pass);
    }

    private static int withAlpha(int color, float alpha) {
        return (color & 0x00ffffff) | (Math.round(Color.alpha(color) * alpha) << 24);
    }

    private static void rememberBackground(View view) {
        if (view.getTag(R.id.oui_palette_original_background) != null || view.getBackground() == null) return;
        android.graphics.drawable.Drawable.ConstantState state = view.getBackground().getConstantState();
        if (state != null) view.setTag(R.id.oui_palette_original_background, state.newDrawable().mutate());
    }

    private static ColorStateList enabledColors(int color) {
        return new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled}, new int[]{}},
                new int[]{withAlpha(color, 0.38f), color});
    }

    private static void refreshCustomView(View view) {
        Object refresh = view.getTag(R.id.oui_palette_refresh);
        if (refresh instanceof Runnable) ((Runnable) refresh).run();
    }

    private static void applyToChildren(View root, PalettePass pass) {
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++)
                applyToViewTree(group.getChildAt(i), pass);
        }
    }
}
