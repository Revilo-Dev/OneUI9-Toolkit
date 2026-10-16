package dev.oneuiproject.oneui.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import java.util.WeakHashMap;

/** Applies the shared blur setting to popups backed by an Android dialog window. */
public final class DialogBlur {
    private static final WeakHashMap<Dialog, View> BACKDROPS = new WeakHashMap<>();

    private DialogBlur() { }

    public static void apply(@Nullable Dialog dialog) {
        if (dialog == null) return;
        Window window = dialog.getWindow();
        if (window == null) return;
        OneUIPalette.applyToViewTree(window.getDecorView());
        int foreground = OneUIPalette.get(dialog.getContext(), OneUIPalette.FOREGROUND);
        for (int id : new int[]{android.R.id.button1, android.R.id.button2, android.R.id.button3}) {
            View action = window.findViewById(id);
            if (action instanceof android.widget.TextView) {
                ((android.widget.TextView) action).setTextColor(foreground);
                action.setBackgroundTintList(null);
            }
        }
        View panel = window.findViewById(androidx.appcompat.R.id.parentPanel);
        if (panel != null) panel.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        clearPanelFills(window.getDecorView(), OneUIPalette.get(dialog.getContext(), OneUIPalette.SURFACE),
                OneUIPalette.get(dialog.getContext(), OneUIPalette.BACKGROUND));
        android.graphics.drawable.GradientDrawable glass = new android.graphics.drawable.GradientDrawable();
        int surface = OneUIPalette.get(dialog.getContext(), OneUIPalette.SURFACE);
        glass.setColor(BlurSettings.isEnabled(dialog.getContext())
                ? BlurSettings.tint(dialog.getContext(), surface) : surface);
        glass.setCornerRadius(dp(dialog.getContext(), 28));
        window.setBackgroundDrawable(glass);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        boolean enabled = BlurSettings.isEnabled(dialog.getContext());
        int behind = dp(dialog.getContext(), BlurSettings.getIntensity(dialog.getContext()));
        enabled &= behind > 0;
        window.setBackgroundBlurRadius(enabled ? behind : 0);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.setBlurBehindRadius(enabled ? behind : 0);
        window.setAttributes(attributes);
        if (enabled) window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        else window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        Activity activity = activityFrom(dialog.getContext());
        if (activity == null) return;
        View backdrop = activity.findViewById(android.R.id.content);
        if (backdrop == null) return;
        if (enabled) {
            backdrop.setRenderEffect(RenderEffect.createBlurEffect(behind, behind,
                    Shader.TileMode.CLAMP));
            BACKDROPS.put(dialog, backdrop);
        } else release(dialog);
    }

    public static void release(@Nullable Dialog dialog) {
        if (dialog == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        View backdrop = BACKDROPS.remove(dialog);
        if (backdrop != null) backdrop.setRenderEffect(null);
    }

    private static void clearPanelFills(View view, int surface, int background) {
        if (!(view instanceof android.view.ViewGroup)) return;
        if (view.getBackground() instanceof android.graphics.drawable.ColorDrawable) {
            int color = ((android.graphics.drawable.ColorDrawable) view.getBackground()).getColor();
            if (color == surface || color == background) view.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }
        android.view.ViewGroup group = (android.view.ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) clearPanelFills(group.getChildAt(i), surface, background);
    }

    @Nullable private static Activity activityFrom(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) return (Activity) context;
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
