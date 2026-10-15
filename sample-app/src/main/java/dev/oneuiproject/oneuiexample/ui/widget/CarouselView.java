package dev.oneuiproject.oneuiexample.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

/** Horizontal card carousel with a consistent gap and viewport padding. */
public class CarouselView extends HorizontalScrollView {
    private final LinearLayout cards;

    public CarouselView(@NonNull Context context) {
        super(context);
        setHorizontalScrollBarEnabled(false);
        setFillViewport(false);
        setClipToPadding(false);
        setPadding(dp(16), 0, dp(16), 0);
        cards = new LinearLayout(context);
        cards.setOrientation(LinearLayout.HORIZONTAL);
        addView(cards, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    public void addCard(@NonNull View card, int widthDp) {
        addCard(card, widthDp, 0);
    }

    public void addCard(@NonNull View card, int widthDp, int heightDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dp(widthDp), heightDp > 0 ? dp(heightDp) : LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(dp(12));
        cards.addView(card, params);
    }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
