package dev.oneuiproject.oneui.widget;

import android.content.res.Configuration;
import android.animation.ValueAnimator;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.AbsListView;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import java.util.IdentityHashMap;
import java.util.Map;

/** Styles the toolbar's actual control groups when content scrolls below them. */
public final class StickyToolbarControls {
    private final AppBarLayout appBar;
    private final Toolbar toolbar;
    private final View content;
    private final Drawable toolbarBackground;
    private View navigation;
    private ActionMenuView actions;
    private Drawable navigationBackground;
    private Drawable actionsBackground;
    private TopActionSurfaceDrawable navigationSurface;
    private TopActionSurfaceDrawable actionsSurface;
    private Drawable navigationFeedback;
    private final Map<View, Drawable> actionBackgrounds = new IdentityHashMap<>();
    private final ViewOutlineProvider surfaceOutline = new ViewOutlineProvider() {
        @Override public void getOutline(View target, Outline outline) {
            outline.setRoundRect(0, 0, target.getWidth(), target.getHeight(),
                    target == navigation ? target.getHeight() / 2f : cornerRadius);
        }
    };
    private int navPaddingLeft, navPaddingTop, navPaddingRight, navPaddingBottom;
    private int navigationWidth, navigationHeight;
    private int navigationMarginStart, navigationMarginEnd;
    private float navigationTranslationY;
    private int actionPaddingLeft, actionPaddingTop, actionPaddingRight, actionPaddingBottom;
    private int actionsMarginStart, actionsMarginEnd, actionsHeight;
    private float actionsTranslationY;
    private boolean enabled = true;
    private boolean active;
    private boolean blurEnabled;
    private boolean requireContentScroll = true;
    private int offset;
    private int horizontalInset;
    private int verticalOffset;
    private int collapseTolerance;
    private int itemWidth;
    private int surfacePadding;
    private int cornerRadius;
    private float shadowElevation;
    private Integer tintColor;
    private float surfaceAlpha;
    private ValueAnimator surfaceAnimator;
    private final Map<View, int[]> originalActionSizes = new IdentityHashMap<>();

    public StickyToolbarControls(AppBarLayout appBar, Toolbar toolbar, View content) {
        this.appBar = appBar;
        this.toolbar = toolbar;
        this.content = content;
        toolbarBackground = toolbar.getBackground();
        horizontalInset = dp(16);
        verticalOffset = 0;
        collapseTolerance = dp(2);
        itemWidth = dp(44);
        surfacePadding = dp(4);
        cornerRadius = dp(26);
        shadowElevation = dp(3);
        toolbar.setClipChildren(false);
        toolbar.setClipToPadding(false);
        appBar.addOnOffsetChangedListener((bar, verticalOffset) -> {
            offset = verticalOffset;
            refresh();
        });
        toolbar.getViewTreeObserver().addOnGlobalLayoutListener(this::refresh);
        content.getViewTreeObserver().addOnScrollChangedListener(this::refresh);
        content.getViewTreeObserver().addOnGlobalLayoutListener(this::refresh);
        toolbar.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View view) { refresh(); }
            @Override public void onViewDetachedFromWindow(View view) {
                if (surfaceAnimator != null) surfaceAnimator.cancel();
            }
        });
    }

    public void setEnabled(boolean value) { enabled = value; refresh(); }
    public boolean isEnabled() { return enabled; }
    public void setRequireContentScroll(boolean value) { requireContentScroll = value; refresh(); }
    public void setHorizontalInset(int pixels) { horizontalInset = Math.max(0, pixels); refresh(); }
    public void setVerticalOffset(int pixels) { verticalOffset = pixels; refresh(); }
    public void setCollapseTolerance(int pixels) { collapseTolerance = Math.max(0, pixels); refresh(); }
    public void setBlurEnabled(boolean value) {
        blurEnabled = value;
        if (navigationSurface != null) navigationSurface.setBlurEnabled(value);
        if (actionsSurface != null) actionsSurface.setBlurEnabled(value);
    }
    public boolean isBlurEnabled() { return blurEnabled; }
    public boolean isContentScrolled() { return hasScrolledContent(content); }
    public void setShadowElevation(float pixels) { shadowElevation = Math.max(0, pixels); refresh(); }
    public void setTintColor(@Nullable Integer color) {
        tintColor = color;
        if (navigationSurface != null) navigationSurface.setTintColor(color);
        if (actionsSurface != null) actionsSurface.setTintColor(color);
    }
    /** Sets each icon's equal-width touch target. */
    public void setItemWidth(int pixels) { itemWidth = Math.max(dp(40), pixels); refresh(); }
    public void setSurfacePadding(int pixels) { surfacePadding = Math.max(0, pixels); refresh(); }
    public void setCornerRadius(int pixels) {
        cornerRadius = Math.max(0, pixels);
        if (actionsSurface != null) actionsSurface.setRadius(cornerRadius);
        if (navigation != null) navigation.invalidateOutline();
        if (actions != null) actions.invalidateOutline();
    }
    @Nullable public View getNavigationControl() { return navigation; }
    @Nullable public ActionMenuView getActionsControl() { return actions; }

    public void refresh() {
        Drawable background = enabled ? null : toolbarBackground;
        if (toolbar.getBackground() != background) toolbar.setBackground(background);
        boolean shouldShow = enabled && appBar.getTotalScrollRange() > 0
                && Math.abs(offset) >= appBar.getTotalScrollRange() - collapseTolerance
                && (!requireContentScroll || hasScrolledContent(content));
        findControls();
        // Geometry is identical in both states. Only the glass behind the icons fades.
        styleNavigation();
        styleActions();
        if (shouldShow != active) {
            active = shouldShow;
            if (surfaceAnimator != null) surfaceAnimator.cancel();
            surfaceAnimator = ValueAnimator.ofFloat(surfaceAlpha, active ? 1f : 0f);
            surfaceAnimator.setDuration(180);
            surfaceAnimator.addUpdateListener(animation -> {
                surfaceAlpha = (float) animation.getAnimatedValue();
                updateSurfaceAlpha();
            });
            surfaceAnimator.start();
        }
        if (active) {
            if (navigationSurface != null) navigationSurface.refreshBackdrop();
            if (actionsSurface != null) actionsSurface.refreshBackdrop();
        }
    }

    private void updateSurfaceAlpha() {
        int alpha = Math.round(255 * surfaceAlpha);
        if (navigationSurface != null) navigationSurface.setAlpha(alpha);
        if (actionsSurface != null) actionsSurface.setAlpha(alpha);
        if (navigation != null) updateOutline(navigation, surfaceAlpha > 0);
        if (actions != null) updateOutline(actions, surfaceAlpha > 0);
    }

    private void installSurface(View view, TopActionSurfaceDrawable surface, Drawable original) {
        int left = view.getPaddingLeft(), top = view.getPaddingTop();
        int right = view.getPaddingRight(), bottom = view.getPaddingBottom();
        surface.setAlpha(Math.round(255 * surfaceAlpha));
        // The device's old action-button mask can be polygonal. Use one matching mask.
        if (view == navigation) {
            navigationFeedback = circularFeedback(surface);
            view.setBackground(navigationFeedback);
        } else view.setBackground(surface);
        view.setPadding(left, top, right, bottom);
    }

    private Drawable circularFeedback(@Nullable Drawable content) {
        return new RoundControlFeedback(content, OneUIPalette.color(toolbar.getContext(),
                dev.oneuiproject.oneui.design.R.color.oui_floating_nav_press));
    }

    private void findControls() {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof AppCompatImageButton && child != navigation) {
                navigation = child;
                if (android.os.Build.VERSION.SDK_INT >= 26)
                    child.setDefaultFocusHighlightEnabled(false);
                navigationBackground = child.getBackground();
                navPaddingLeft = child.getPaddingLeft();
                navPaddingTop = child.getPaddingTop();
                navPaddingRight = child.getPaddingRight();
                navPaddingBottom = child.getPaddingBottom();
                navigationWidth = child.getLayoutParams().width;
                navigationHeight = child.getLayoutParams().height;
                ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                navigationMarginStart = margins.getMarginStart();
                navigationMarginEnd = margins.getMarginEnd();
                navigationTranslationY = child.getTranslationY();
                navigationSurface = new TopActionSurfaceDrawable(child, content, cornerRadius);
                navigationSurface.setBlurEnabled(blurEnabled);
                navigationSurface.setTintColor(tintColor);
                installSurface(child, navigationSurface, navigationBackground);
            } else if (child instanceof ActionMenuView && child != actions) {
                actions = (ActionMenuView) child;
                actions.setClipChildren(false);
                actions.setClipToPadding(false);
                actionsBackground = child.getBackground();
                actionPaddingLeft = child.getPaddingLeft();
                actionPaddingTop = child.getPaddingTop();
                actionPaddingRight = child.getPaddingRight();
                actionPaddingBottom = child.getPaddingBottom();
                ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                actionsMarginStart = margins.getMarginStart();
                actionsMarginEnd = margins.getMarginEnd();
                actionsHeight = child.getLayoutParams().height;
                actionsTranslationY = child.getTranslationY();
                actionsSurface = new TopActionSurfaceDrawable(child, content, cornerRadius);
                actionsSurface.setBlurEnabled(blurEnabled);
                actionsSurface.setTintColor(tintColor);
                installSurface(child, actionsSurface, actionsBackground);
            }
        }
    }

    private void styleNavigation() {
        if (navigation == null) return;
        boolean show = enabled && toolbar.getNavigationIcon() != null;
        Drawable background = show ? navigationFeedback : navigationBackground;
        if (navigation.getBackground() != background) navigation.setBackground(background);
        int left = show ? surfacePadding : navPaddingLeft;
        int top = show ? surfacePadding : navPaddingTop;
        int right = show ? surfacePadding : navPaddingRight;
        int bottom = show ? surfacePadding : navPaddingBottom;
        if (navigation.getPaddingLeft() != left || navigation.getPaddingTop() != top
                || navigation.getPaddingRight() != right || navigation.getPaddingBottom() != bottom)
            navigation.setPadding(left, top, right, bottom);
        ViewGroup.LayoutParams params = navigation.getLayoutParams();
        int size = itemWidth + surfacePadding * 2;
        if (show) navigationSurface.setRadius(size / 2f);
        int width = show ? size : navigationWidth;
        int height = show ? size : navigationHeight;
        if (params.width != width || params.height != height) {
            params.width = width;
            params.height = height;
            navigation.setLayoutParams(params);
        }
        setMargins(navigation, show, navigationMarginStart, navigationMarginEnd);
        navigation.setTranslationY(navigationTranslationY + (show ? verticalOffset : 0));
        updateOutline(navigation, surfaceAlpha > 0);
    }

    private void styleActions() {
        if (actions == null) return;
        int count = 0;
        for (int i = 0; i < actions.getChildCount(); i++)
            if (actions.getChildAt(i).getVisibility() == View.VISIBLE) count++;
        boolean show = enabled && count > 0;
        Drawable background = show ? actionsSurface : actionsBackground;
        if (actions.getBackground() != background) actions.setBackground(background);
        int left = show ? surfacePadding : actionPaddingLeft;
        int top = show ? surfacePadding : actionPaddingTop;
        int right = show ? surfacePadding : actionPaddingRight;
        int bottom = show ? surfacePadding : actionPaddingBottom;
        if (actions.getPaddingLeft() != left || actions.getPaddingTop() != top
                || actions.getPaddingRight() != right || actions.getPaddingBottom() != bottom)
            actions.setPadding(left, top, right, bottom);
        if (show) {
            for (int i = 0; i < actions.getChildCount(); i++) {
                View child = actions.getChildAt(i);
                if (child.getVisibility() != View.VISIBLE) continue;
                if (android.os.Build.VERSION.SDK_INT >= 26)
                    child.setDefaultFocusHighlightEnabled(false);
                styleOverflow(child);
                ViewGroup.LayoutParams params = child.getLayoutParams();
                if (!originalActionSizes.containsKey(child))
                    originalActionSizes.put(child, new int[]{params.width, params.height,
                            child.getMinimumWidth(), child.getMinimumHeight()});
                if (params.width != itemWidth || params.height != itemWidth) {
                    params.width = itemWidth;
                    params.height = itemWidth;
                    child.setLayoutParams(params);
                }
                // These setters request layout even when the value is unchanged.
                if (child.getMinimumWidth() != itemWidth) child.setMinimumWidth(itemWidth);
                if (child.getMinimumHeight() != itemWidth) child.setMinimumHeight(itemWidth);
                if (!actionBackgrounds.containsKey(child)) {
                    actionBackgrounds.put(child, child.getBackground());
                    int l = child.getPaddingLeft(), t = child.getPaddingTop();
                    int r = child.getPaddingRight(), b = child.getPaddingBottom();
                    child.setBackground(circularFeedback(null));
                    child.setPadding(l, t, r, b);
                }
            }
        } else if (!originalActionSizes.isEmpty()) {
            for (Map.Entry<View, int[]> entry : originalActionSizes.entrySet()) {
                View child = entry.getKey();
                int[] size = entry.getValue();
                ViewGroup.LayoutParams childParams = child.getLayoutParams();
                if (childParams != null && (childParams.width != size[0]
                        || childParams.height != size[1])) {
                    childParams.width = size[0];
                    childParams.height = size[1];
                    child.setLayoutParams(childParams);
                }
                child.setMinimumWidth(size[2]);
                child.setMinimumHeight(size[3]);
            }
            originalActionSizes.clear();
            for (Map.Entry<View, Drawable> entry : actionBackgrounds.entrySet())
                entry.getKey().setBackground(entry.getValue());
            actionBackgrounds.clear();
        }
        ViewGroup.LayoutParams params = actions.getLayoutParams();
        int height = show ? itemWidth + surfacePadding * 2 : actionsHeight;
        if (params.width != ViewGroup.LayoutParams.WRAP_CONTENT || params.height != height) {
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = height;
            actions.setLayoutParams(params);
        }
        setMargins(actions, show, actionsMarginStart, actionsMarginEnd);
        actions.setTranslationY(actionsTranslationY + (show ? verticalOffset : 0));
        updateOutline(actions, surfaceAlpha > 0);
    }

    private void styleOverflow(View child) {
        if (!(child instanceof android.widget.FrameLayout)) return;
        android.widget.FrameLayout wrapper = (android.widget.FrameLayout) child;
        wrapper.setClipChildren(false);
        wrapper.setClipToPadding(false);
        for (int i = 0; i < wrapper.getChildCount(); i++) {
            View inner = wrapper.getChildAt(i);
            if (inner instanceof android.widget.ImageView && !actionBackgrounds.containsKey(inner)) {
                actionBackgrounds.put(inner, inner.getBackground());
                inner.setStateListAnimator(null);
                if (android.os.Build.VERSION.SDK_INT >= 26)
                    inner.setDefaultFocusHighlightEnabled(false);
                inner.setTranslationZ(0);
                inner.setBackground(circularFeedback(null));
                android.widget.FrameLayout.LayoutParams params =
                        (android.widget.FrameLayout.LayoutParams) inner.getLayoutParams();
                params.width = itemWidth;
                params.height = itemWidth;
                params.gravity = android.view.Gravity.CENTER;
                inner.setLayoutParams(params);
            } else if (inner instanceof ViewGroup) {
                // Keep the badge outside the three-dot glyph, at the button's top end.
                android.widget.FrameLayout.LayoutParams params =
                        (android.widget.FrameLayout.LayoutParams) inner.getLayoutParams();
                int gravity = android.view.Gravity.TOP | android.view.Gravity.END;
                if (params.gravity != gravity || params.topMargin != -dp(2)
                        || params.getMarginEnd() != -dp(4)) {
                    params.gravity = gravity;
                    params.topMargin = -dp(2);
                    params.setMarginEnd(-dp(4));
                    inner.setLayoutParams(params);
                }
            }
        }
    }

    private void setMargins(View view, boolean show, int originalStart, int originalEnd) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int start = show ? horizontalInset : originalStart;
        int end = show ? horizontalInset : originalEnd;
        if (params.getMarginStart() != start || params.getMarginEnd() != end) {
            params.setMarginStart(start);
            params.setMarginEnd(end);
            view.setLayoutParams(params);
        }
    }

    private void updateOutline(View view, boolean show) {
        // One outline throughout the fade prevents device-specific shape transitions.
        if (enabled) {
            view.setStateListAnimator(null);
            if (view.getTranslationZ() != 0) view.setTranslationZ(0);
            if (view.getOutlineProvider() != surfaceOutline) view.setOutlineProvider(surfaceOutline);
            int mode = toolbar.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            float elevation = mode == Configuration.UI_MODE_NIGHT_YES
                    ? Math.min(shadowElevation, dp(2)) : shadowElevation;
            elevation *= surfaceAlpha;
            if (view.getElevation() != elevation) view.setElevation(elevation);
        } else {
            if (view.getElevation() != 0) view.setElevation(0);
            if (view.getOutlineProvider() != ViewOutlineProvider.BACKGROUND)
                view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        }
    }

    private boolean hasScrolledContent(View view) {
        if (view.getVisibility() != View.VISIBLE) return false;
        if (view.canScrollVertically(-1)) return true;
        if (view instanceof RecyclerView || view instanceof NestedScrollView
                || view instanceof ScrollView || view instanceof AbsListView) return false;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++)
                if (hasScrolledContent(group.getChildAt(i))) return true;
        }
        return false;
    }

    private int dp(float value) {
        return Math.round(value * toolbar.getResources().getDisplayMetrics().density);
    }
}
