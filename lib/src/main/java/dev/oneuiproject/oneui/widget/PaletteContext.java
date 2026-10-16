package dev.oneuiproject.oneui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.appcompat.view.ContextThemeWrapper;
import dev.oneuiproject.oneui.design.R;

/** Component-local palette choice, inherited by programmatically created children. */
final class PaletteContext extends ContextThemeWrapper {
    boolean usesPalette;

    PaletteContext(Context base, AttributeSet attrs) {
        super(base, copyTheme(base));
        usesPalette = OneUIPalette.usesColorPalette(base);
        if (attrs != null) {
            TypedArray values = base.obtainStyledAttributes(attrs, R.styleable.OneUIComponent);
            usesPalette = values.getBoolean(R.styleable.OneUIComponent_usesColorPalette, usesPalette);
            values.recycle();
        }
        if (!usesPalette || !MaterialColorSettings.isEnabled(base))
            getTheme().applyStyle(R.style.OneUICustomPaletteOverlay, true);
    }

    private static android.content.res.Resources.Theme copyTheme(Context base) {
        android.content.res.Resources.Theme theme = base.getResources().newTheme();
        theme.setTo(base.getTheme());
        return theme;
    }

    void setUsesPalette(boolean enabled) {
        usesPalette = enabled;
        getTheme().setTo(getBaseContext().getTheme());
        if (!enabled || !MaterialColorSettings.isEnabled(this))
            getTheme().applyStyle(R.style.OneUICustomPaletteOverlay, true);
    }
}
