package dev.oneuiproject.oneui.widget;

import android.content.Context;
import android.animation.ValueAnimator;
import android.content.res.TypedArray;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.oneuiproject.oneui.design.R;
import dev.oneuiproject.oneui.dialog.GridMenuDialog;

/** A floating, single-selection navigation pill with an optional independent end action. */
public class FloatingNavigationBar extends LinearLayout {
    public interface OnItemSelectedListener {
        void onItemSelected(int itemId);
    }

    private static final int MAX_ITEMS = 4;
    private static final int DOWNSAMPLE = BlurSettings.GLASS_SAMPLE_SCALE;
    private final List<NavigationItem> items = new ArrayList<>();
    private final NavigationItem overflowItem;
    private final SelectionPill pill;
    private final ImageView action;
    private final Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path clipPath = new Path();
    private Bitmap backdrop;
    private int[] sourcePixels;
    private int[] blurredPixels;
    private int selectedId = View.NO_ID;
    private boolean showLabels = true;
    private boolean showIcons = true;
    private boolean blurEnabled = true;
    private boolean animationsEnabled = true;
    private boolean capturingBackdrop;
    private OnItemSelectedListener itemListener;
    private NavigationPageContainer pages;
    private View backdropView;
    private OnClickListener actionListener;
    private Drawable actionIcon;
    private CharSequence actionDescription;
    private int itemWidth;
    private int barHeight;
    private int selectionInset;
    private float lightShadowElevation;
    private Integer surfaceColorOverride;
    private Integer selectedColorOverride;

    public FloatingNavigationBar(Context context) { this(context, null); }

    public FloatingNavigationBar(Context context, AttributeSet attrs) { this(context, attrs, 0); }

    public FloatingNavigationBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(OneUIPalette.wrap(context, attrs), attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);
        setSaveEnabled(true);
        BlurSettings.observe(this);
        itemWidth = dp(70);
        barHeight = dp(60);
        selectionInset = dp(5);
        lightShadowElevation = dp(1);
        int menuRes = 0;
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingNavigationBar,
                    defStyleAttr, 0);
            showLabels = a.getBoolean(R.styleable.FloatingNavigationBar_showLabels, true);
            blurEnabled = a.getBoolean(R.styleable.FloatingNavigationBar_blurEnabled, true);
            menuRes = a.getResourceId(R.styleable.FloatingNavigationBar_navigationMenu, 0);
            actionIcon = a.getDrawable(R.styleable.FloatingNavigationBar_actionIcon);
            actionDescription = a.getText(R.styleable.FloatingNavigationBar_actionContentDescription);
            a.recycle();
        }
        pill = new SelectionPill(getContext());
        pill.setOrientation(HORIZONTAL);
        pill.setGravity(Gravity.CENTER_VERTICAL);
        pill.setPadding(dp(18), dp(4), dp(18), dp(4));
        addView(pill, new LayoutParams(LayoutParams.WRAP_CONTENT, barHeight));
        overflowItem = new NavigationItem(View.generateViewId(),
                getContext().getDrawable(R.drawable.oui_ic_navigation_more), "Menu");
        overflowItem.view.setOnClickListener(v -> showOverflowMenu());
        action = new ImageView(getContext());
        action.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        action.setPadding(dp(19), dp(19), dp(19), dp(19));
        action.setClickable(true);
        action.setFocusable(true);
        action.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onClick(v);
        });
        addView(action, new LayoutParams(barHeight, barHeight));
        updateSurfaces();
        if (menuRes != 0) inflateMenu(menuRes);
        updateAction();
        updateSelection();
        OneUIPalette.observe(this, () -> { updateSurfaces(); updateSelection(); invalidate(); });
    }

    /** Menu items supply stable IDs, icons and titles. Extra destinations move into More. */
    public void inflateMenu(@MenuRes int menuRes) {
        PopupMenu menu = new PopupMenu(getContext(), this);
        menu.getMenuInflater().inflate(menuRes, menu.getMenu());
        clearItems();
        Menu entries = menu.getMenu();
        for (int i = 0; i < entries.size(); i++) {
            MenuItem item = entries.getItem(i);
            if (item.isVisible()) addItem(item.getItemId(), item.getIcon(), item.getTitle());
        }
    }

    public void clearItems() {
        items.clear();
        pill.removeAllViews();
        selectedId = View.NO_ID;
        updateItemWidths();
    }

    public void addItem(int id, @DrawableRes int iconRes, @NonNull CharSequence title) {
        addItem(id, getContext().getDrawable(iconRes), title);
    }

    public void addItem(int id, @Nullable Drawable icon, @NonNull CharSequence title) {
        if (id == View.NO_ID) throw new IllegalArgumentException("Navigation items need a stable ID");
        for (NavigationItem item : items) {
            if (item.id == id) throw new IllegalArgumentException("Duplicate navigation item ID");
        }
        NavigationItem item = new NavigationItem(id, icon, title);
        items.add(item);
        if (selectedId == View.NO_ID) selectedId = id;
        rebuildVisibleItems();
        updateSelection();
    }

    /** Optional filled icon shown only while the destination is selected. */
    public void setSelectedIcon(int id, @DrawableRes int iconRes) {
        setSelectedIcon(id, getContext().getDrawable(iconRes));
    }

    public void setSelectedIcon(int id, @Nullable Drawable icon) {
        for (NavigationItem item : items) {
            if (item.id == id) {
                item.selectedIcon = icon;
                updateSelection();
                return;
            }
        }
        throw new IllegalArgumentException("Unknown navigation item ID");
    }

    public void setSelectedItemId(int id) {
        for (NavigationItem item : items) {
            if (item.id == id) {
                selectedId = id;
                updateSelection();
                if (pages != null && pages.hasPage(id)) pages.showPage(id, animationsEnabled);
                return;
            }
        }
        throw new IllegalArgumentException("Unknown navigation item ID");
    }

    public int getSelectedItemId() { return selectedId; }

    public void setOnItemSelectedListener(@Nullable OnItemSelectedListener listener) {
        itemListener = listener;
    }

    public void setShowLabels(boolean show) {
        showLabels = show;
        updateSelection();
    }

    public boolean isShowingLabels() { return showLabels; }

    public void setShowIcons(boolean show) { showIcons = show; updateSelection(); }
    public boolean isShowingIcons() { return showIcons; }

    public void setBlurEnabled(boolean enabled) {
        blurEnabled = enabled;
        invalidate();
    }

    public boolean isBlurEnabled() { return blurEnabled; }
    /** The page content visible behind this bar. */
    public void setBackdropView(@Nullable View view) { backdropView = view; invalidate(); }

    public void setItemWidth(int pixels) {
        itemWidth = Math.max(dp(48), pixels);
        updateItemWidths();
    }

    public void setBarHeight(int pixels) {
        barHeight = Math.max(dp(48), pixels);
        LayoutParams pillParams = (LayoutParams) pill.getLayoutParams();
        pillParams.height = barHeight;
        pill.setLayoutParams(pillParams);
        updateAction();
        updateSurfaces();
        requestLayout();
    }

    public void setSelectionInset(int pixels) {
        selectionInset = Math.max(0, pixels);
        pill.invalidate();
    }

    public void setLightShadowElevation(float pixels) {
        lightShadowElevation = Math.max(0, pixels);
        updateSurfaces();
    }

    public void setSurfaceColor(@Nullable Integer color) {
        surfaceColorOverride = color;
        updateSurfaces();
        invalidate();
    }

    public void setSelectedColor(@Nullable Integer color) {
        selectedColorOverride = color;
        updateSelection();
    }

    public void setAnimationsEnabled(boolean enabled) { animationsEnabled = enabled; }
    public boolean isAnimationsEnabled() { return animationsEnabled; }

    /** Bind direct child pages by matching their IDs to navigation item IDs. */
    public void bindPages(@NonNull NavigationPageContainer container) {
        pages = container;
        container.setOnSwipeListener(direction -> {
            int index = -1;
            for (int i = 0; i < items.size(); i++)
                if (items.get(i).id == selectedId) { index = i; break; }
            for (int i = index + direction; i >= 0 && i < items.size(); i += direction) {
                int id = items.get(i).id;
                if (container.hasPage(id)) {
                    setSelectedItemId(id);
                    if (itemListener != null) itemListener.onItemSelected(id);
                    break;
                }
            }
        });
        int initial = container.hasPage(selectedId) ? selectedId : View.NO_ID;
        if (initial == View.NO_ID)
            for (NavigationItem item : items)
                if (container.hasPage(item.id)) { initial = item.id; break; }
        if (initial != View.NO_ID) setSelectedItemId(initial);
    }

    public void setPageSwipingEnabled(boolean enabled) {
        if (pages != null) pages.setSwipeEnabled(enabled);
    }

    public boolean isPageSwipingEnabled() { return pages != null && pages.isSwipeEnabled(); }

    public void setAction(@Nullable Drawable icon, @Nullable CharSequence description,
                          @Nullable OnClickListener listener) {
        actionIcon = icon;
        actionDescription = description;
        actionListener = listener;
        updateAction();
    }

    public void setAction(@DrawableRes int iconRes, @NonNull CharSequence description,
                          @NonNull OnClickListener listener) {
        setAction(getContext().getDrawable(iconRes), description, listener);
    }

    public void clearAction() { setAction((Drawable) null, null, null); }

    private List<NavigationItem> visibleItems() {
        if (items.size() < MAX_ITEMS) return items;
        List<NavigationItem> visible = new ArrayList<>(items.subList(0, MAX_ITEMS - 1));
        visible.add(overflowItem);
        return visible;
    }

    private int selectedIndex() {
        for (int i = 0; i < items.size(); i++)
            if (items.get(i).id == selectedId) return i;
        return -1;
    }

    private void rebuildVisibleItems() {
        pill.removeAllViews();
        for (NavigationItem item : visibleItems()) pill.addView(item.view);
        updateItemWidths();
    }

    private void showOverflowMenu() {
        if (items.size() < MAX_ITEMS) return;
        GridMenuDialog menu = new GridMenuDialog(getContext());
        int count = items.size() - (MAX_ITEMS - 1);
        menu.setSpanCount(Math.min(4, count));
        menu.setMaxVisibleRows(2);
        for (int i = MAX_ITEMS - 1; i < items.size(); i++) {
            NavigationItem item = items.get(i);
            menu.addItem(menu.newItem(item.id, item.defaultIcon, item.label.getText()));
        }
        menu.setOnItemClickListener(clicked -> {
            int id = clicked.getItemId();
            if (selectedId != id) {
                setSelectedItemId(id);
                if (itemListener != null) itemListener.onItemSelected(id);
            }
            return true;
        });
        menu.show();
    }

    private void updateItemWidths() {
        LayoutParams pillParams = (LayoutParams) pill.getLayoutParams();
        pillParams.width = LayoutParams.WRAP_CONTENT;
        pillParams.weight = 0;
        pill.setLayoutParams(pillParams);
        for (NavigationItem item : visibleItems()) {
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    itemWidth, LayoutParams.MATCH_PARENT);
            item.view.setLayoutParams(itemParams);
        }
        requestLayout();
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desired = Math.min(items.size(), MAX_ITEMS) * itemWidth + dp(36)
                + (action.getVisibility() == VISIBLE ? barHeight + dp(8) : 0);
        int available = View.MeasureSpec.getSize(widthMeasureSpec);
        widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                Math.min(desired, available), View.MeasureSpec.EXACTLY);
        int count = visibleItems().size();
        int endWidth = action.getVisibility() == VISIBLE ? barHeight + dp(8) : 0;
        int cellWidth = count == 0 ? itemWidth : Math.min(itemWidth,
                Math.max(1, (Math.min(desired, available) - dp(36) - endWidth) / count));
        for (NavigationItem item : visibleItems()) {
            ViewGroup.LayoutParams params = item.view.getLayoutParams();
            if (params.width != cellWidth) { params.width = cellWidth; item.view.setLayoutParams(params); }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void updateAction() {
        action.setVisibility(actionIcon == null ? GONE : VISIBLE);
        action.setImageDrawable(actionIcon);
        action.setContentDescription(actionDescription);
        action.setClickable(actionIcon != null && actionListener != null);
        action.setFocusable(actionIcon != null && actionListener != null);
        LayoutParams lp = (LayoutParams) action.getLayoutParams();
        lp.width = barHeight;
        lp.height = barHeight;
        lp.leftMargin = dp(8);
        action.setLayoutParams(lp);
        invalidate();
    }

    private void updateSurfaces() {
        boolean light = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES;
        pill.setBackground(null);
        action.setBackground(null);
        SurfaceShadow.apply(pill, light, lightShadowElevation, barHeight / 2f);
        SurfaceShadow.apply(action, light, lightShadowElevation, barHeight / 2f);
    }

    private int navigationSurface() {
        return surfaceColorOverride != null ? surfaceColorOverride
                : OneUIPalette.get(getContext(), OneUIPalette.isDark(getContext())
                        ? OneUIPalette.SURFACE : OneUIPalette.SURFACE_VARIANT);
    }

    private void updateSelection() {
        int primary = OneUIPalette.color(getContext(), R.color.oui_floating_nav_primary);
        int selectedColor = selectedColorOverride != null ? selectedColorOverride
                : OneUIPalette.color(getContext(), R.color.oui_floating_nav_selected);
        for (NavigationItem item : visibleItems()) {
            boolean selected = item.id == selectedId
                    || (item == overflowItem && selectedIndex() >= MAX_ITEMS - 1);
            item.view.setSelected(selected);
            Drawable icon = selected && item.selectedIcon != null
                    ? item.selectedIcon : item.defaultIcon;
            if (item.icon.getDrawable() != icon) item.icon.setImageDrawable(icon);
            item.label.setVisibility(showLabels ? VISIBLE : GONE);
            item.icon.setVisibility(showIcons && icon != null ? VISIBLE : GONE);
            LinearLayout.LayoutParams labelParams = (LinearLayout.LayoutParams)
                    item.label.getLayoutParams();
            int labelGap = showIcons && icon != null && showLabels ? dp(4) : 0;
            if (labelParams.topMargin != labelGap) {
                labelParams.topMargin = labelGap;
                item.label.setLayoutParams(labelParams);
            }
            item.label.setTextColor(primary);
            item.icon.setColorFilter(primary);
        }
        pill.setHighlightColor(selectedColor);
        pill.post(() -> {
            for (NavigationItem item : visibleItems()) {
                if (item.id == selectedId || (item == overflowItem
                        && selectedIndex() >= MAX_ITEMS - 1)) {
                    pill.moveHighlight(item.view.getLeft(), item.view.getWidth(), animationsEnabled);
                    break;
                }
            }
        });
        action.setColorFilter(primary);
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        if (!capturingBackdrop) super.draw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;
        boolean hasAction = action.getVisibility() == VISIBLE;
        int surface = navigationSurface();
        int overlay = surface;
        if (isBlurActive() && getParent() instanceof ViewGroup && !isInEditMode()) captureBackdrop();
        drawShape(canvas, pill.getLeft(), pill.getTop(), pill.getRight(), pill.getBottom(), surface, overlay);
        if (hasAction) drawShape(canvas, action.getLeft(), action.getTop(),
                action.getRight(), action.getBottom(), surface, overlay);
    }

    private void drawShape(Canvas canvas, int left, int top, int right, int bottom, int solid, int glass) {
        int save = canvas.save();
        clipPath.reset();
        float radius = (bottom - top) / 2f;
        clipPath.addRoundRect(left, top, right, bottom, radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        if (isBlurActive() && backdrop != null) {
            canvas.drawBitmap(backdrop, null, new Rect(0, 0, getWidth(), getHeight()), bitmapPaint);
            shapePaint.setColor(BlurSettings.tint(getContext(), glass));
        } else shapePaint.setColor(solid);
        canvas.drawRect(left, top, right, bottom, shapePaint);
        canvas.restoreToCount(save);
    }

    private boolean isBlurActive() {
        return blurEnabled && BlurSettings.isEnabled(getContext());
    }

    private void captureBackdrop() {
        FloatingBarSwitcher switcher = getParent() instanceof FloatingBarSwitcher
                ? (FloatingBarSwitcher) getParent() : null;
        View source = backdropView != null ? backdropView
                : switcher != null && switcher.getBackdropView() != null
                        ? switcher.getBackdropView() : getRootView();
        int width = Math.max(1, (getWidth() + DOWNSAMPLE - 1) / DOWNSAMPLE);
        int height = Math.max(1, (getHeight() + DOWNSAMPLE - 1) / DOWNSAMPLE);
        if (backdrop == null || backdrop.getWidth() != width || backdrop.getHeight() != height) {
            if (backdrop != null) backdrop.recycle();
            backdrop = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            sourcePixels = new int[width * height];
            blurredPixels = new int[width * height];
        }
        backdrop.eraseColor(android.graphics.Color.TRANSPARENT);
        Canvas capture = new Canvas(backdrop);
        capture.scale((float) width / getWidth(), (float) height / getHeight());
        int[] position = new int[2];
        int[] hostPosition = new int[2];
        getLocationInWindow(position);
        source.getLocationInWindow(hostPosition);
        capture.translate(-(position[0] - hostPosition[0]),
                -(position[1] - hostPosition[1]));
        capturingBackdrop = true;
        if (switcher != null) switcher.setBackdropCapture(true);
        try { source.draw(capture); } finally {
            if (switcher != null) switcher.setBackdropCapture(false);
            capturingBackdrop = false;
        }
        BackdropBlur.apply(backdrop, sourcePixels, blurredPixels, BlurSettings.sampleRadius(getContext(), DOWNSAMPLE));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (backdrop != null) {
            backdrop.recycle();
            backdrop = null;
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.selectedId = selectedId;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState state = (SavedState) parcelable;
        super.onRestoreInstanceState(state.getSuperState());
        for (NavigationItem item : items) {
            if (item.id == state.selectedId) {
                selectedId = state.selectedId;
                break;
            }
        }
        updateSelection();
        if (pages != null && pages.hasPage(selectedId)) pages.showPage(selectedId, false);
    }

    private static class SavedState extends BaseSavedState {
        int selectedId;
        SavedState(Parcelable state) { super(state); }
        SavedState(Parcel parcel) { super(parcel); selectedId = parcel.readInt(); }
        @Override public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(selectedId);
        }
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override public SavedState createFromParcel(Parcel source) { return new SavedState(source); }
            @Override public SavedState[] newArray(int size) { return new SavedState[size]; }
        };
    }

    private class NavigationItem {
        final int id;
        final LinearLayout view;
        final ImageView icon;
        final TextView label;
        final Drawable defaultIcon;
        Drawable selectedIcon;

        NavigationItem(int id, Drawable drawable, CharSequence title) {
            this.id = id;
            defaultIcon = drawable;
            view = new LinearLayout(getContext());
            view.setOrientation(VERTICAL);
            view.setGravity(Gravity.CENTER);
            view.setContentDescription(title);
            view.setClickable(true);
            view.setFocusable(true);
            view.setOnClickListener(v -> {
                Runnable select = () -> {
                    if (selectedId != id) {
                        selectedId = id;
                        updateSelection();
                        if (pages != null && pages.hasPage(id)) pages.showPage(id, animationsEnabled);
                        if (itemListener != null) itemListener.onItemSelected(id);
                    }
                };
                select.run();
            });
            icon = new ImageView(getContext());
            icon.setImageDrawable(drawable);
            icon.setVisibility(drawable == null ? GONE : VISIBLE);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            view.addView(icon, new LinearLayout.LayoutParams(dp(24), dp(24)));
            label = new TextView(getContext());
            label.setText(title);
            label.setTextSize(12);
            label.setSingleLine(true);
            label.setEllipsize(android.text.TextUtils.TruncateAt.END);
            label.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(4);
            view.addView(label, lp);
        }
    }

    private class SelectionPill extends LinearLayout {
        private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float highlightLeft;
        private float highlightWidth;
        private boolean initialized;
        private ValueAnimator animator;

        SelectionPill(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            if (changed) {
                for (NavigationItem item : visibleItems()) {
                    if (item.id == selectedId || (item == overflowItem
                            && selectedIndex() >= MAX_ITEMS - 1)) {
                        moveHighlight(item.view.getLeft(), item.view.getWidth(), false);
                        break;
                    }
                }
            }
        }

        void setHighlightColor(int color) { highlightPaint.setColor(color); invalidate(); }

        void moveHighlight(float left, float width, boolean animate) {
            if (width <= 0) return;
            if (animator != null) animator.cancel();
            if (!initialized || !animate) {
                highlightLeft = left;
                highlightWidth = width;
                initialized = true;
                invalidate();
                return;
            }
            float oldLeft = highlightLeft;
            float oldWidth = highlightWidth;
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(175);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(value -> {
                float fraction = (float) value.getAnimatedValue();
                highlightLeft = oldLeft + (left - oldLeft) * fraction;
                highlightWidth = oldWidth + (width - oldWidth) * fraction;
                invalidate();
            });
            animator.start();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (initialized) canvas.drawRoundRect(highlightLeft - dp(8), selectionInset,
                    highlightLeft + highlightWidth + dp(8), getHeight() - selectionInset,
                    (getHeight() - selectionInset * 2) / 2f,
                    (getHeight() - selectionInset * 2) / 2f, highlightPaint);
        }
    }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
