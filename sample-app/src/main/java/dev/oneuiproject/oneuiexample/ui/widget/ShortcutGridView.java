package dev.oneuiproject.oneuiexample.ui.widget;

import dev.oneuiproject.oneui.widget.OneUIPalette;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.sec.sesl.tester.R;

/** Two-column group of six reusable icon shortcuts. */
public class ShortcutGridView extends LinearLayout {
    private final LinearLayout[] rows = new LinearLayout[3];
    private int count;

    public ShortcutGridView(@NonNull Context context) {
        super(context);
        setOrientation(VERTICAL);
        setPadding(dp(12), dp(12), dp(12), dp(12));
        GradientDrawable background = new GradientDrawable();
        background.setColor(OneUIPalette.color(context, R.color.oui_floating_nav_surface));
        background.setCornerRadius(dp(26));
        setBackground(background);
        for (int i = 0; i < rows.length; i++) {
            rows[i] = new LinearLayout(context);
            rows[i].setOrientation(HORIZONTAL);
            addView(rows[i], new LayoutParams(LayoutParams.MATCH_PARENT, dp(72)));
        }
    }

    public void addShortcut(@DrawableRes int iconRes, CharSequence label, int iconColor,
                            OnClickListener listener) {
        if (count >= 6) throw new IllegalStateException("ShortcutGridView holds six shortcuts");
        LinearLayout item = new LinearLayout(getContext());
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(8), 0, dp(4), 0);
        item.setClickable(true);
        item.setFocusable(true);
        item.setOnClickListener(listener);
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(0, dp(68), 1);
        rows[count / 2].addView(item, itemParams);
        ImageView icon = new ImageView(getContext());
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(iconColor));
        GradientDrawable badge = new GradientDrawable();
        badge.setColor(OneUIPalette.color(getContext(), R.color.oui_grid_menu_icon_circle));
        badge.setCornerRadius(dp(25));
        icon.setBackground(badge);
        icon.setPadding(dp(13), dp(13), dp(13), dp(13));
        item.addView(icon, new LayoutParams(dp(50), dp(50)));
        TextView title = new TextView(getContext());
        title.setText(label);
        title.setTextColor(OneUIPalette.color(getContext(), R.color.oui_primary_text_color));
        title.setTextSize(14);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LayoutParams labelParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        labelParams.setMarginStart(dp(10));
        item.addView(title, labelParams);
        count++;
    }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
