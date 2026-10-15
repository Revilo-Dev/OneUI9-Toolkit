package dev.oneuiproject.oneuiexample.fragment;

import dev.oneuiproject.oneui.widget.OneUIPalette;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sec.sesl.tester.R;

import android.widget.Toast;
import dev.oneuiproject.oneuiexample.base.BaseFragment;

public class SwipeRefreshFragment extends BaseFragment {
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwipeRefreshLayout srl = view.findViewById(R.id.swiperefresh_view);
        AppCompatButton button = view.findViewById(R.id.swiperefresh_button);

        srl.setDistanceToTriggerSync(dp(80));
        srl.setColorSchemeColors(
                OneUIPalette.color(requireContext(), dev.oneuiproject.oneui.design.R.color.oui_floating_nav_primary),
                OneUIPalette.color(requireContext(), dev.oneuiproject.oneui.design.R.color.oui_floating_nav_secondary));
        srl.setProgressBackgroundColorSchemeColor(requireContext().getColor(
                dev.oneuiproject.oneui.design.R.color.oui_floating_nav_surface));
        srl.setProgressViewOffset(false, dp(64), dp(124));
        srl.setOnRefreshListener(() -> {
            Toast.makeText(requireContext(), "onRefresh", Toast.LENGTH_SHORT).show();
            button.setEnabled(true);
        });

        button.seslSetButtonShapeEnabled(true);
        button.setBackgroundTintList(getButtonColor());
        button.setOnClickListener(view1 -> {
            button.setEnabled(false);
            srl.setRefreshing(false);
        });
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.sample3_fragment_swiperefresh;
    }

    @Override
    public int getIconResId() {
        return R.drawable.ic_oui_refresh;
    }

    @Override
    public CharSequence getTitle() {
        return "SwipeRefreshLayout";
    }

    @Override
    public boolean isAppBarEnabled() {
        return false;
    }


    private ColorStateList getButtonColor() {
        TypedValue colorPrimaryDark = new TypedValue();
        mContext.getTheme().resolveAttribute(
                R.attr.colorPrimaryDark, colorPrimaryDark, true);

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_enabled}
        };
        int[] colors = new int[]{
                Color.argb(0xff,
                        Color.red(colorPrimaryDark.data),
                        Color.green(colorPrimaryDark.data),
                        Color.blue(colorPrimaryDark.data)),
                Color.argb(0x4d,
                        Color.red(colorPrimaryDark.data),
                        Color.green(colorPrimaryDark.data),
                        Color.blue(colorPrimaryDark.data))
        };
        return new ColorStateList(states, colors);
    }

}
