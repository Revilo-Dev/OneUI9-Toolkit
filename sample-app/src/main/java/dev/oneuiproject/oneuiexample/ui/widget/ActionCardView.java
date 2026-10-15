package dev.oneuiproject.oneuiexample.ui.widget;

import dev.oneuiproject.oneui.widget.OneUIPalette;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.sec.sesl.tester.R;

/** Reusable icon, title, subtext and action card in either layout direction. */
public class ActionCardView extends LinearLayout {
    private final ImageView icon;
    private final TextView title;
    private final TextView subtitle;
    private final TextView action;
    private final View iconSurface;
    private int accent = 0xFF7594D8;

    public ActionCardView(@NonNull Context context, boolean vertical) {
        super(context);
        setOrientation(vertical ? VERTICAL : HORIZONTAL);
        setGravity(vertical ? Gravity.TOP : Gravity.CENTER_VERTICAL);
        setPadding(dp(14), dp(14), dp(14), dp(14));
        GradientDrawable background = new GradientDrawable();
        background.setColor(OneUIPalette.color(context, R.color.oui_floating_nav_surface));
        background.setCornerRadius(dp(24));
        setBackground(background);

        android.widget.FrameLayout badge = new android.widget.FrameLayout(context);
        iconSurface = badge;
        int size = vertical ? dp(112) : dp(76);
        LayoutParams badgeParams = new LayoutParams(size, size);
        if (!vertical) badgeParams.rightMargin = dp(16);
        addView(badge, badgeParams);
        icon = new ImageView(context);
        icon.setImageTintList(ColorStateList.valueOf(0xFFFFFFFF));
        badge.addView(icon, new android.widget.FrameLayout.LayoutParams(
                dp(vertical ? 52 : 38), dp(vertical ? 52 : 38), Gravity.CENTER));
        setAccentColor(accent);

        LinearLayout details = new LinearLayout(context);
        details.setOrientation(VERTICAL);
        LayoutParams detailsParams = vertical
                ? new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                : new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        if (vertical) detailsParams.topMargin = dp(12);
        addView(details, detailsParams);
        title = new TextView(context);
        title.setTextColor(OneUIPalette.color(context, R.color.oui_primary_text_color));
        title.setTextSize(vertical ? 17 : 18);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setMaxLines(vertical ? 2 : 1);
        title.setEllipsize(TextUtils.TruncateAt.END);
        details.addView(title);
        subtitle = new TextView(context);
        subtitle.setTextColor(OneUIPalette.color(context, R.color.oui_floating_nav_secondary));
        subtitle.setTextSize(13);
        subtitle.setMaxLines(vertical ? 2 : 1);
        subtitle.setEllipsize(TextUtils.TruncateAt.END);
        LayoutParams subtitleParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = dp(4);
        details.addView(subtitle, subtitleParams);

        action = new TextView(context);
        action.setGravity(Gravity.CENTER);
        action.setTextColor(OneUIPalette.color(context, R.color.oui_primary_text_color));
        action.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        action.setTextSize(14);
        action.setMinWidth(dp(vertical ? 96 : 72));
        action.setMinHeight(dp(36));
        action.setClickable(true);
        action.setFocusable(true);
        GradientDrawable button = new GradientDrawable();
        button.setColor(OneUIPalette.color(context, R.color.oui_floating_nav_selected));
        button.setCornerRadius(dp(20));
        action.setBackground(button);
        LayoutParams actionParams = new LayoutParams(
                vertical ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT, dp(36));
        if (vertical) actionParams.topMargin = dp(12);
        addView(action, actionParams);
    }

    public void setIconResource(@DrawableRes int resource) { icon.setImageResource(resource); }
    public void setTitle(CharSequence text) { title.setText(text); }
    public void setSubtitle(CharSequence text) { subtitle.setText(text); }
    public void setActionText(CharSequence text) { action.setText(text); }
    public void setOnActionClickListener(OnClickListener listener) { action.setOnClickListener(listener); }
    public void setAccentColor(int color) {
        accent = color;
        GradientDrawable badge = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{color, darken(color)});
        badge.setCornerRadius(dp(20));
        iconSurface.setBackground(badge);
    }
    private int darken(int color) {
        return 0xFF000000 | ((int) (android.graphics.Color.red(color) * .55f) << 16)
                | ((int) (android.graphics.Color.green(color) * .55f) << 8)
                | (int) (android.graphics.Color.blue(color) * .55f);
    }
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
