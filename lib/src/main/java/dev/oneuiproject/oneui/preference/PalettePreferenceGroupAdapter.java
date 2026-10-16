package dev.oneuiproject.oneui.preference;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceViewHolder;
import dev.oneuiproject.oneui.widget.OneUIPalette;

/** Applies Surface to actual preference rows, including recycled native SESL rows. */
@android.annotation.SuppressLint("RestrictedApi")
public final class PalettePreferenceGroupAdapter extends PreferenceGroupAdapter {
    public PalettePreferenceGroupAdapter(PreferenceGroup group) { super(group); }

    @Override public void onBindViewHolder(@NonNull PreferenceViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        View row = holder.itemView;
        OneUIPalette.refreshViewTree(row);
        if (isSection(getItem(position))) {
            row.setBackgroundColor(OneUIPalette.get(row.getContext(), OneUIPalette.BACKGROUND));
            return;
        }
        float radius = 22 * row.getResources().getDisplayMetrics().density;
        boolean top = position == 0 || isSection(getItem(position - 1));
        boolean bottom = position == getItemCount() - 1 || isSection(getItem(position + 1));
        float[] corners = {top ? radius : 0, top ? radius : 0, top ? radius : 0, top ? radius : 0,
                bottom ? radius : 0, bottom ? radius : 0, bottom ? radius : 0, bottom ? radius : 0};
        GradientDrawable surface = new GradientDrawable();
        surface.setColor(OneUIPalette.get(row.getContext(), OneUIPalette.SURFACE));
        surface.setCornerRadii(corners);
        GradientDrawable mask = new GradientDrawable();
        mask.setColor(Color.WHITE);
        mask.setCornerRadii(corners);
        int feedback = androidx.core.graphics.ColorUtils.setAlphaComponent(
                OneUIPalette.get(row.getContext(), OneUIPalette.FOREGROUND), 24);
        int left = row.getPaddingLeft(), right = row.getPaddingRight();
        int upper = row.getPaddingTop(), lower = row.getPaddingBottom();
        row.setBackground(new RippleDrawable(ColorStateList.valueOf(feedback), surface, mask));
        row.setPadding(left, upper, right, lower);
    }

    private boolean isSection(Preference preference) {
        return preference == null || preference instanceof PreferenceCategory
                || preference instanceof DescriptionPreference;
    }
}
