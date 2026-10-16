package dev.oneuiproject.oneui.widget;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import androidx.appcompat.widget.SwitchCompat;
import dev.oneuiproject.oneui.design.R;

/** Keeps the thumb inside its track while retaining SwitchCompat input and accessibility. */
final class ToggleStyle {
    private ToggleStyle() { }

    static void apply(SwitchCompat toggle) {
        int primary = OneUIPalette.get(toggle.getContext(), OneUIPalette.PRIMARY);
        int background = OneUIPalette.get(toggle.getContext(), OneUIPalette.BACKGROUND);
        int outline = OneUIPalette.get(toggle.getContext(), OneUIPalette.OUTLINE);
        ColorStateList trackColors = new ColorStateList(new int[][]{
                {-android.R.attr.state_enabled}, {android.R.attr.state_checked}, {}},
                new int[]{alpha(outline, .38f), primary, outline});
        GradientDrawable track = (GradientDrawable) androidx.core.content.ContextCompat.getDrawable(toggle.getContext(), R.drawable.oui_toggle_track).mutate();
        // SESL keeps separate on/off copies. Give those copies the same state colors.
        track.setColor(trackColors);
        toggle.setTrackDrawable(track);
        toggle.setTrackTintList(trackColors);
        toggle.setThumbDrawable(androidx.core.content.ContextCompat.getDrawable(toggle.getContext(), R.drawable.oui_toggle_thumb).mutate());
        toggle.setThumbTintList(new ColorStateList(new int[][]{
                {-android.R.attr.state_enabled}, {}}, new int[]{alpha(background, .7f), background}));
        toggle.setShowText(false);
        toggle.setSplitTrack(false);
    }

    private static int alpha(int color, float opacity) {
        return (color & 0xffffff) | (Math.round(android.graphics.Color.alpha(color) * opacity) << 24);
    }
}
