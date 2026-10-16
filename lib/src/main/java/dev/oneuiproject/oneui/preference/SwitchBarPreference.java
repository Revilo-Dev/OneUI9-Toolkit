package dev.oneuiproject.oneui.preference;

import dev.oneuiproject.oneui.widget.OneUIPalette;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;
import android.view.View;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.TwoStatePreference;

import dev.oneuiproject.oneui.design.R;

public class SwitchBarPreference extends TwoStatePreference {

    public SwitchBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(OneUIPalette.wrap(context, attrs), attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.oui_preference_switch_bar_layout);
    }

    public SwitchBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchBarPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        SwitchCompat toggle = holder.itemView.findViewById(R.id.switch_bar_toggle);
        TextView label = holder.itemView.findViewById(android.R.id.title);
        label.setVisibility(View.VISIBLE);
        label.setText(getContext().getString(mChecked
                ? androidx.appcompat.R.string.sesl_switch_on : androidx.appcompat.R.string.sesl_switch_off));
        toggle.setContentDescription(label.getText());
        toggle.setOnCheckedChangeListener(null);
        toggle.setChecked(mChecked);
        toggle.setOnCheckedChangeListener((switchView, isChecked) -> {
            if (isChecked == mChecked) return;
            if (!callChangeListener(isChecked)) {
                toggle.setChecked(mChecked);
                return;
            }
            setChecked(isChecked);
        });
        OneUIPalette.refreshViewTree(holder.itemView);
        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);
    }

}
