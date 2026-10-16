package dev.oneuiproject.oneui.widget;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.preference.PreferenceManager;
import com.google.android.material.appbar.AppBarLayout;
import dev.oneuiproject.oneuiexample.activity.MainActivity;
import java.util.Map;

/** Device checks without an extra test framework dependency. Run with am instrument -w. */
public class ComponentBehaviorChecks extends Instrumentation {
    private Throwable failure;
    private int checks;
    private volatile java.util.concurrent.CountDownLatch resumed = new java.util.concurrent.CountDownLatch(1);
    private volatile Activity resumedActivity;

    @Override public void onCreate(Bundle arguments) { super.onCreate(arguments); start(); }

    @Override public void callActivityOnResume(Activity activity) {
        super.callActivityOnResume(activity);
        resumedActivity = activity;
        resumed.countDown();
    }

    @Override public void onStart() {
        Activity activity = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getTargetContext());
        Map<String, ?> original = preferences.getAll();
        int originalNightMode = androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode();
        try {
            getTargetContext().startActivity(new Intent(getTargetContext(), MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            require(resumed.await(15, java.util.concurrent.TimeUnit.SECONDS), "Sample activity resumes");
            activity = resumedActivity;
            settle();
            final Activity screen = activity;
            onMain(() -> checkPalette(screen, preferences));
            onMain(() -> checkAppearance(screen, preferences));
            onMain(() -> checkBlur(screen));
            onMain(() -> checkFixedFades(screen));
            onMain(() -> {
                AppBarLayout bar = screen.findViewById(dev.oneuiproject.oneui.design.R.id.toolbarlayout_app_bar);
                bar.setExpanded(false, false);
            });
            settle();
            Toolbar toolbar = screen.findViewById(dev.oneuiproject.oneui.design.R.id.toolbarlayout_main_toolbar);
            final int[][] before = new int[1][];
            onMain(() -> before[0] = geometry(toolbar));
            onMain(() -> {
                NestedScrollView content = first(screen.getWindow().getDecorView(), NestedScrollView.class);
                require(content != null, "Default sample has scrollable content");
                content.scrollTo(0, 600);
            });
            settle();
            onMain(() -> {
                int[] after = geometry(toolbar);
                require(toolbar.getBackground() == null, "Floating controls have no full-width toolbar background");
                AppBarLayout header = screen.findViewById(dev.oneuiproject.oneui.design.R.id.toolbarlayout_app_bar);
                Bitmap headerImage = Bitmap.createBitmap(header.getWidth(), header.getHeight(), Bitmap.Config.ARGB_8888);
                header.draw(new Canvas(headerImage));
                require(Color.alpha(headerImage.getPixel(header.getWidth() / 2,
                        header.getHeight() - toolbar.getHeight() / 2)) == 0,
                        "The space between floating controls stays transparent");
                headerImage.recycle();
                require(before[0].length > 0 && java.util.Arrays.equals(before[0], after),
                        "Toolbar icons keep their bounds on scroll: " + java.util.Arrays.toString(before[0])
                                + " -> " + java.util.Arrays.toString(after));
                for (int i = 0; i < toolbar.getChildCount(); i++) {
                    View child = toolbar.getChildAt(i);
                    if (child instanceof androidx.appcompat.widget.AppCompatImageButton
                            || child instanceof androidx.appcompat.widget.ActionMenuView)
                        require(child.getAlpha() == 1f, "Scrolling never fades toolbar icons");
                    if (child instanceof androidx.appcompat.widget.AppCompatImageButton) {
                        if (android.os.Build.VERSION.SDK_INT >= 26) require(!child.getDefaultFocusHighlightEnabled(),
                                "Navigation replaces the native polygonal focus highlight");
                        require(child.getBackground() instanceof RoundControlFeedback,
                                "Navigation feedback avoids the device ripple mask");
                        RoundControlFeedback feedback = new RoundControlFeedback(null, Color.WHITE);
                        feedback.setBounds(0, 0, 40, 40);
                        feedback.setState(new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed});
                        Bitmap circle = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888);
                        feedback.draw(new Canvas(circle));
                        require(circle.getPixel(0, 0) == Color.TRANSPARENT && circle.getPixel(20, 20) == Color.WHITE,
                                "Pressed feedback draws a circle with clear corners");
                        circle.recycle();
                    }
                }
            });
            final java.util.concurrent.atomic.AtomicInteger idleLayouts = new java.util.concurrent.atomic.AtomicInteger();
            StringBuilder requested = new StringBuilder();
            android.view.ViewTreeObserver.OnGlobalLayoutListener layout = () -> {
                idleLayouts.incrementAndGet();
                if (requested.length() < 1800) describeLayoutRequests(screen.getWindow().getDecorView(), requested);
            };
            onMain(() -> toolbar.getViewTreeObserver().addOnGlobalLayoutListener(layout));
            settle();
            onMain(() -> {
                toolbar.getViewTreeObserver().removeOnGlobalLayoutListener(layout);
                require(idleLayouts.get() <= 1, "Idle toolbar does not keep requesting layout: " + idleLayouts.get()
                        + " Requests: " + requested);
            });
            checkGlassCaching(screen, preferences);
            checkGallery(preferences);
            resumed = new java.util.concurrent.CountDownLatch(1);
            onMain(() -> {
                MainActivity current = (MainActivity) resumedActivity;
                current.openPage(dev.oneuiproject.oneuiexample.fragment.PreferencesFragment.class);
                current.recreate();
            });
            require(resumed.await(15, java.util.concurrent.TimeUnit.SECONDS), "Palette recreation resumes");
            settle();
            onMain(() -> {
                MainActivity recreated = (MainActivity) resumedActivity;
                int preferencesScreens = 0;
                for (androidx.fragment.app.Fragment fragment : recreated.getSupportFragmentManager().getFragments()) {
                    if (fragment instanceof dev.oneuiproject.oneuiexample.fragment.PreferencesFragment) {
                        preferencesScreens++;
                        require(!fragment.isHidden(), "Palette changes keep Preferences selected");
                    }
                }
                require(preferencesScreens == 1, "Recreation reuses the Preferences screen");
            });
        } catch (Throwable error) { failure = error; }
        finally {
            SharedPreferences.Editor restore = preferences.edit().clear();
            for (Map.Entry<String, ?> entry : original.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Boolean) restore.putBoolean(entry.getKey(), (Boolean) value);
                else if (value instanceof Integer) restore.putInt(entry.getKey(), (Integer) value);
                else if (value instanceof String) restore.putString(entry.getKey(), (String) value);
                else if (value instanceof Long) restore.putLong(entry.getKey(), (Long) value);
                else if (value instanceof Float) restore.putFloat(entry.getKey(), (Float) value);
                else if (value instanceof java.util.Set)
                    restore.putStringSet(entry.getKey(), new java.util.HashSet<>((java.util.Set<String>) value));
            }
            restore.commit();
            if (resumedActivity != null) runOnMainSync(() -> {
                resumedActivity.finish();
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(originalNightMode);
            });
        }
        Bundle result = new Bundle();
        result.putString("stream", failure == null ? checks + " component checks passed\n"
                : android.util.Log.getStackTraceString(failure));
        finish(failure == null ? Activity.RESULT_OK : Activity.RESULT_CANCELED, result);
    }

    private void checkGallery(SharedPreferences preferences) throws Exception {
        preferences.edit().putBoolean(MaterialColorSettings.KEY, false)
                .putInt(BlurSettings.INTENSITY_KEY, 18).putInt(BlurSettings.ALPHA_KEY, 65).commit();
        for (boolean dark : new boolean[]{false, true}) {
            resumed = new java.util.concurrent.CountDownLatch(1);
            onMain(() -> {
                boolean wasDark = OneUIPalette.isDark(resumedActivity);
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(dark
                        ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                        : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                if (wasDark == dark) resumedActivity.recreate();
            });
            require(resumed.await(15, java.util.concurrent.TimeUnit.SECONDS), "Gallery mode recreation resumes");
            settle();
            String mode = dark ? "dark" : "light";
            MainActivity current = (MainActivity) resumedActivity;
            onMain(() -> current.openPage(dev.oneuiproject.oneuiexample.fragment.WidgetsFragment.class));
            settle();
            onMain(() -> {
                NestedScrollView scroll = first(current.getWindow().getDecorView(), NestedScrollView.class);
                scroll.scrollTo(0, 0);
                View root = current.findViewById(com.sec.sesl.tester.R.id.drawerLayout);
                require(root.getBackground() instanceof android.graphics.drawable.ColorDrawable
                        && ((android.graphics.drawable.ColorDrawable) root.getBackground()).getColor()
                        == OneUIPalette.get(current, OneUIPalette.BACKGROUND), "Actual page root uses Background");
            });
            settle();
            capture("widgets-" + mode);
            onMain(() -> first(current.getWindow().getDecorView(), NestedScrollView.class).scrollTo(0, 600));
            settle();
            capture("floating-header-" + mode);
            for (androidx.fragment.app.Fragment page : current.getSamplePages()) {
                onMain(() -> current.openPage(page.getClass()));
                settle();
                onMain(() -> {
                    int visible = 0;
                    for (androidx.fragment.app.Fragment existing : current.getSupportFragmentManager().getFragments()) {
                        if (existing instanceof dev.oneuiproject.oneuiexample.base.FragmentInfo && !existing.isHidden()) {
                            visible++;
                            require(existing.getClass() == page.getClass() && existing.getView() != null,
                                    "Catalogue opens " + page.getClass().getSimpleName());
                        }
                    }
                    require(visible == 1, "Exactly one gallery page is visible");
                });
                if (page instanceof dev.oneuiproject.oneuiexample.fragment.SeekBarFragment
                        || page instanceof dev.oneuiproject.oneuiexample.fragment.TabsFragment
                        || page instanceof dev.oneuiproject.oneuiexample.fragment.PreferencesFragment
                        || page instanceof dev.oneuiproject.oneuiexample.fragment.IndexScrollFragment)
                    capture(page.getClass().getSimpleName() + "-" + mode);
                if (page instanceof dev.oneuiproject.oneuiexample.fragment.PreferencesFragment) {
                    onMain(() -> checkPreferenceRows((dev.oneuiproject.oneuiexample.fragment.PreferencesFragment) page));
                    final androidx.appcompat.app.AlertDialog[] dialog = new androidx.appcompat.app.AlertDialog[1];
                    onMain(() -> {
                        dialog[0] = new androidx.appcompat.app.AlertDialog.Builder(current)
                                .setTitle("Dialog appearance").setMessage("Neutral actions and a blurred surface")
                                .setPositiveButton("OK", null).setNegativeButton("Cancel", null).create();
                        dialog[0].show();
                        DialogBlur.apply(dialog[0]);
                        int foreground = OneUIPalette.get(current, OneUIPalette.FOREGROUND);
                        require(dialog[0].getButton(-1).getCurrentTextColor() == foreground
                                        && dialog[0].getButton(-2).getCurrentTextColor() == foreground,
                                "Dialog actions use foreground, independently of accent colors");
                        require((dialog[0].getWindow().getAttributes().flags
                                        & android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND) != 0,
                                "Dialog blur is enabled");
                    });
                    settle();
                    capture("dialog-" + mode);
                    onMain(() -> { DialogBlur.release(dialog[0]); dialog[0].dismiss(); });
                    settle();
                }
                if (page instanceof dev.oneuiproject.oneuiexample.fragment.TabsFragment) {
                    FloatingNavigationBar bar = current.findViewById(com.sec.sesl.tester.R.id.tabs_floating_nav);
                    onMain(() -> {
                        ViewGroup pill = (ViewGroup) bar.getChildAt(0);
                        pill.getChildAt(pill.getChildCount() - 1).performClick();
                    });
                    settle();
                    capture("more-" + mode);
                    getUiAutomation().performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);
                    settle();
                }
            }
        }
        resumed = new java.util.concurrent.CountDownLatch(1);
        onMain(() -> {
            preferences.edit().putBoolean(MaterialColorSettings.KEY, true).commit();
            MainActivity current = (MainActivity) resumedActivity;
            current.openPage(dev.oneuiproject.oneuiexample.fragment.PreferencesFragment.class);
            current.recreate();
        });
        require(resumed.await(15, java.util.concurrent.TimeUnit.SECONDS), "Device palette recreation resumes");
        settle();
        onMain(() -> {
            for (androidx.fragment.app.Fragment page : ((MainActivity) resumedActivity).getSupportFragmentManager().getFragments())
                if (page instanceof dev.oneuiproject.oneuiexample.fragment.PreferencesFragment && !page.isHidden())
                    checkPreferenceRows((dev.oneuiproject.oneuiexample.fragment.PreferencesFragment) page);
        });
        capture("preferences-device");
    }

    private void capture(String name) throws java.io.IOException {
        Bitmap screenshot = getUiAutomation().takeScreenshot();
        require(screenshot != null, "Screenshot available for " + name);
        try (java.io.FileOutputStream stream = new java.io.FileOutputStream(
                new java.io.File(getTargetContext().getCacheDir(), "qa-" + name + ".png"))) {
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, stream);
        } finally { screenshot.recycle(); }
    }

    private void checkPalette(Activity activity, SharedPreferences preferences) {
        int primary = 0xffc32373, secondary = 0xff427b61;
        preferences.edit().putBoolean(MaterialColorSettings.KEY, false)
                .putInt(OneUIPalette.key(OneUIPalette.PRIMARY, OneUIPalette.isDark(activity)), primary)
                .putInt(OneUIPalette.key(OneUIPalette.SECONDARY, OneUIPalette.isDark(activity)), secondary).commit();
        Context context = OneUIPalette.wrap(activity, null);
        require(OneUIPalette.get(context, OneUIPalette.PRIMARY) == primary, "Custom primary resolves");
        require(OneUIPalette.get(context, OneUIPalette.SECONDARY) == secondary, "Custom secondary resolves");
        SwitchCompat toggle = new SwitchCompat(context);
        AppCompatCheckBox checkbox = new AppCompatCheckBox(context);
        ProgressBar progress = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        for (View view : new View[]{toggle, checkbox, progress}) OneUIPalette.applyToViewTree(view);
        int[] checked = {android.R.attr.state_enabled, android.R.attr.state_checked};
        require(toggle.getTrackTintList().getColorForState(checked, 0) == primary, "Switch track uses full custom primary");
        require(toggle.getThumbTintList().getColorForState(checked, 0)
                == OneUIPalette.get(context, OneUIPalette.BACKGROUND), "Switch thumb uses background");
        require(checkbox.getButtonTintList().getColorForState(checked, 0) == primary, "Checkbox uses custom primary");
        require(progress.getProgressTintList().getDefaultColor() == primary, "Progress uses custom primary");
        require(progress.getSecondaryProgressTintList().getDefaultColor() == secondary, "Progress uses secondary");
        androidx.appcompat.widget.SeslProgressBar seslProgress = new androidx.appcompat.widget.SeslProgressBar(context);
        OneUIPalette.applyToViewTree(seslProgress);
        require(seslProgress.getProgressTintList().getDefaultColor() == primary, "SESL progress uses primary");
        require(seslProgress.getSecondaryProgressTintList().getDefaultColor() == secondary, "SESL progress uses secondary");
        HapticSeekBar seslSeekBar = new HapticSeekBar(context, null);
        OneUIPalette.applyToViewTree(seslSeekBar);
        require(seslSeekBar.getThumbTintList().getDefaultColor() == primary, "SESL seek bar uses primary");
        OneUISlider slider = new OneUISlider(activity);
        preferences.edit().putBoolean(MaterialColorSettings.KEY, true).commit();
        OneUIPalette.setUsesColorPalette(slider, false);
        require(slider.getActiveColor() == primary, "Component can opt out of OS colors");
        slider.setActiveColor(0xff8040ff);
        OneUIPalette.setUsesColorPalette(slider, true);
        require(slider.getActiveColor() == 0xff8040ff, "Explicit component colors survive palette changes");
    }

    private void checkAppearance(Activity activity, SharedPreferences preferences) {
        preferences.edit().putBoolean(MaterialColorSettings.KEY, false).commit();
        preferences.edit().remove("oneui_palette_defaults_version")
                .putInt(OneUIPalette.key(OneUIPalette.BACKGROUND, true), 0xff171717)
                .putInt(OneUIPalette.key(OneUIPalette.BACKGROUND, false), 0xfffcfcfc)
                .putInt(OneUIPalette.key(OneUIPalette.SURFACE, true), 0xff2e2e30).commit();
        OneUIPalette.migrateDefaults(activity);
        require(!preferences.contains(OneUIPalette.key(OneUIPalette.BACKGROUND, true))
                && !preferences.contains(OneUIPalette.key(OneUIPalette.SURFACE, true)),
                "Former saved defaults migrate to the new background/surface defaults");
        preferences.edit().remove("oneui_palette_defaults_version")
                .putInt(OneUIPalette.key(OneUIPalette.BACKGROUND, true), 0xff122334).commit();
        OneUIPalette.migrateDefaults(activity);
        require(preferences.getInt(OneUIPalette.key(OneUIPalette.BACKGROUND, true), 0) == 0xff122334,
                "Default migration preserves other custom colors");
        preferences.edit().remove(OneUIPalette.key(OneUIPalette.BACKGROUND, true)).commit();
        for (boolean dark : new boolean[]{false, true}) {
            android.content.res.Configuration configuration = new android.content.res.Configuration(
                    activity.getResources().getConfiguration());
            configuration.uiMode = (configuration.uiMode & ~android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                    | (dark ? android.content.res.Configuration.UI_MODE_NIGHT_YES
                            : android.content.res.Configuration.UI_MODE_NIGHT_NO);
            android.view.ContextThemeWrapper themed = new android.view.ContextThemeWrapper(activity,
                    dev.oneuiproject.oneui.design.R.style.OneUITheme);
            themed.applyOverrideConfiguration(configuration);
            Context context = OneUIPalette.wrap(themed, null);
            FloatingSearchBar search = new FloatingSearchBar(context);
            search.setAiMode(true);
            android.widget.EditText input = first(search, android.widget.EditText.class);
            require(input.getCurrentTextColor() == OneUIPalette.get(context, OneUIPalette.FOREGROUND)
                            && input.getPaint().getShader() == null,
                    "Search text remains neutral in AI mode");
            if (dark) require(OneUIPalette.defaultColor(context, OneUIPalette.BACKGROUND) == Color.BLACK,
                    "Default custom dark background is pure black");
            require(OneUIPalette.defaultColor(context, OneUIPalette.BACKGROUND)
                            != OneUIPalette.defaultColor(context, OneUIPalette.SURFACE),
                    "Background and surface stay distinct in " + (dark ? "dark" : "light") + " mode");
            require(OneUIPalette.secondaryText(context) != OneUIPalette.get(context, OneUIPalette.SECONDARY),
                    "Secondary text is independent of the secondary accent");
            preferences.edit().putBoolean(MaterialColorSettings.KEY, true).commit();
            require(OneUIPalette.get(context, OneUIPalette.SURFACE) != OneUIPalette.get(context, OneUIPalette.BACKGROUND),
                    "Device theme also separates material Surface and Background");
            View deviceCard = new View(context);
            deviceCard.setTag("oneui.palette.surface");
            OneUIPalette.applyToViewTree(deviceCard);
            require(((android.graphics.drawable.ColorDrawable) deviceCard.getBackground()).getColor()
                            == OneUIPalette.get(context, OneUIPalette.SURFACE),
                    "Device-themed cards receive material Surface");
            preferences.edit().putBoolean(MaterialColorSettings.KEY, false).commit();
            View surface = new View(context);
            surface.setTag("oneui.palette.surface");
            surface.setBackgroundColor(OneUIPalette.defaultColor(context, OneUIPalette.BACKGROUND));
            OneUIPalette.applyToViewTree(surface);
            require(((android.graphics.drawable.ColorDrawable) surface.getBackground()).getColor()
                            == OneUIPalette.get(context, OneUIPalette.SURFACE), "Surface tag retains its own role");
            SwitchCompat toggle = new SwitchCompat(context);
            toggle.setChecked(true);
            OneUIPalette.applyToViewTree(toggle);
            int width = Math.round(300 * context.getResources().getDisplayMetrics().density);
            int height = Math.round(64 * context.getResources().getDisplayMetrics().density);
            OneUISlider regular = new OneUISlider(context);
            android.graphics.drawable.GradientDrawable leading = new android.graphics.drawable.GradientDrawable();
            leading.setColor(Color.WHITE);
            regular.setLeadingIcon(leading);
            regular.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
            regular.layout(0, 0, width, height);
            Bitmap iconImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            regular.draw(new Canvas(iconImage));
            require(iconImage.getPixel(leading.getBounds().centerX(), leading.getBounds().centerY())
                            == OneUIPalette.get(context, OneUIPalette.FOREGROUND),
                    "Slider leading icon uses foreground rather than primary accent");
            iconImage.recycle();
            toggle.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
            toggle.layout(0, 0, width, height);
            toggle.jumpDrawablesToCurrentState();
            Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            toggle.draw(new Canvas(image));
            android.graphics.Rect track = toggle.getTrackDrawable().getBounds();
            android.graphics.Rect thumb = ((android.graphics.drawable.InsetDrawable)
                    toggle.getThumbDrawable()).getDrawable().getBounds();
            require(track.contains(thumb) && thumb.height() < track.height(), "Toggle circle fits inside track");
            require(Math.abs(thumb.width() - thumb.height()) <= 1, "Toggle thumb remains circular");
            require(image.getPixel(thumb.centerX(), thumb.centerY())
                    == OneUIPalette.get(context, OneUIPalette.BACKGROUND), "Rendered toggle thumb uses background");
            image.recycle();
            FloatingNavigationBar navigation = new FloatingNavigationBar(context);
            navigation.setBlurEnabled(false);
            for (int id = 1; id <= 5; id++) navigation.addItem(id, (android.graphics.drawable.Drawable) null, "Page " + id);
            navigation.setAction(dev.oneuiproject.oneui.design.R.drawable.oui_ic_navigation_more,
                    "Search", v -> { });
            navigation.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST));
            navigation.layout(0, 0, navigation.getMeasuredWidth(), navigation.getMeasuredHeight());
            require(navigation.getChildAt(1).getRight() <= navigation.getWidth(), "Navigation end action fits");
            ViewGroup pill = (ViewGroup) navigation.getChildAt(0);
            for (int i = 0; i < pill.getChildCount(); i++) {
                android.widget.TextView label = first(pill.getChildAt(i), android.widget.TextView.class);
                require(label.getCurrentTextColor() == OneUIPalette.get(context, OneUIPalette.FOREGROUND),
                        "Every navigation label uses foreground text color");
            }
            require(pill.getChildAt(0).getLeft() >= 8 * context.getResources().getDisplayMetrics().density,
                    "Navigation surface has room around selection");
            for (int i = 0; i < pill.getChildCount(); i++) require(pill.getChildAt(i).getBackground() == null,
                    "Navigation adds no intermediate press circle");
            require(OneUIPalette.color(context, dev.oneuiproject.oneui.design.R.color.oui_floating_nav_selected)
                            == (dark ? 0x4dffffff : Color.WHITE), "Navigation selection stays neutral");
            if (!dark) {
                OneUIThickSlider slider = new OneUIThickSlider(context);
                slider.setValue(0);
                slider.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
                slider.layout(0, 0, width, height);
                image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                slider.draw(new Canvas(image));
                require(image.getPixel(width / 2, height / 2) == OneUIPalette.get(context, OneUIPalette.OUTLINE),
                        "Light thick slider renders a gray track even with blur enabled");
                image.recycle();
            }
        }
        ImageTileView tile = new ImageTileView(activity);
        tile.setTitle("Photo caption");
        OneUIPalette.applyToViewTree(tile);
        OneUIPalette.applyToViewTree(tile);
        require(first(tile, android.widget.TextView.class).getCurrentTextColor() == Color.WHITE,
                "Cached palette passes preserve image caption contrast");
        require(((MainActivity) activity).getSamplePages().size() == 13, "Search and drawer list every demo page");
        require(((MainActivity) activity).getSupportFragmentManager().getFragments().size() == 1,
                "Startup loads only the Widgets screen");
        OneUIPalette.refreshViewTree(activity.getWindow().getDecorView());
        android.widget.TextView expandedSubtitle = activity.findViewById(
                com.google.android.material.R.id.collapsing_appbar_extended_subtitle);
        require(expandedSubtitle != null && expandedSubtitle.getCurrentTextColor()
                        == OneUIPalette.secondaryText(expandedSubtitle.getContext()),
                "Expanded toolbar subtitle uses secondary text rather than accent");
    }

    private void checkPreferenceRows(dev.oneuiproject.oneuiexample.fragment.PreferencesFragment page) {
        androidx.recyclerview.widget.RecyclerView list = page.getListView();
        require(list.getAdapter() instanceof dev.oneuiproject.oneui.preference.PalettePreferenceGroupAdapter,
                "Preferences use the palette adapter for native and recycled rows");
        dev.oneuiproject.oneui.preference.PalettePreferenceGroupAdapter adapter =
                (dev.oneuiproject.oneui.preference.PalettePreferenceGroupAdapter) list.getAdapter();
        int nativeSwitchPosition = adapter.getPreferenceAdapterPosition("dark_mode_auto");
        androidx.preference.PreferenceViewHolder recycled = adapter.onCreateViewHolder(list,
                adapter.getItemViewType(nativeSwitchPosition));
        adapter.onBindViewHolder(recycled, nativeSwitchPosition);
        int nextSwitchPosition = adapter.getPreferenceAdapterPosition(MaterialColorSettings.KEY);
        adapter.onBindViewHolder(recycled, nextSwitchPosition);
        OneUIPalette.applyToViewTree(recycled.itemView);
        android.graphics.drawable.GradientDrawable rebound = (android.graphics.drawable.GradientDrawable)
                ((android.graphics.drawable.RippleDrawable) recycled.itemView.getBackground()).getDrawable(0);
        require(rebound.getColor().getDefaultColor() == OneUIPalette.get(list.getContext(), OneUIPalette.SURFACE),
                "Recycled native preference retains Surface after palette passes");
        for (int i = 0; i < list.getChildCount(); i++) {
            View row = list.getChildAt(i);
            int position = list.getChildAdapterPosition(row);
            androidx.preference.Preference preference = adapter.getItem(position);
            if (preference instanceof androidx.preference.PreferenceCategory
                    || preference instanceof dev.oneuiproject.oneui.preference.DescriptionPreference) continue;
            require(row.getBackground() instanceof android.graphics.drawable.RippleDrawable,
                    "Preference row has a dedicated surface and feedback");
            android.graphics.drawable.GradientDrawable surface = (android.graphics.drawable.GradientDrawable)
                    ((android.graphics.drawable.RippleDrawable) row.getBackground()).getDrawable(0);
            require(surface.getColor().getDefaultColor() == OneUIPalette.get(row.getContext(), OneUIPalette.SURFACE),
                    "Preference card uses Surface rather than page Background");
            android.widget.TextView summary = row.findViewById(android.R.id.summary);
            if (summary != null && summary.isEnabled() && summary.getVisibility() == View.VISIBLE)
                require(summary.getCurrentTextColor() == OneUIPalette.secondaryText(summary.getContext()),
                        "Preference summary uses secondary text rather than secondary accent");
            SwitchCompat toggle = first(row, SwitchCompat.class);
            if (toggle != null) require(toggle.getTrackDrawable().getIntrinsicHeight()
                            == Math.round(24 * row.getResources().getDisplayMetrics().density),
                    "Preference toggle shares the slimmer 24 dp track");
        }
    }

    private void checkFixedFades(Activity activity) {
        BlurSettings.setEnabled(activity, true);
        android.widget.FrameLayout host = new android.widget.FrameLayout(activity);
        NestedScrollView scroll = new NestedScrollView(activity);
        View content = new View(activity);
        content.setBackgroundColor(Color.BLUE);
        content.setMinimumHeight(1400);
        scroll.addView(content, new ViewGroup.LayoutParams(120, 1400));
        host.addView(scroll, new android.widget.FrameLayout.LayoutParams(120, 180));
        activity.addContentView(host, new ViewGroup.LayoutParams(120, 180));
        host.measure(View.MeasureSpec.makeMeasureSpec(120, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(180, View.MeasureSpec.EXACTLY));
        host.layout(0, 0, 120, 180);
        ScrollEdgeFades fades = ScrollEdgeFades.attach(scroll).setColor(Color.WHITE).setHeights(20, 20);
        Bitmap image = Bitmap.createBitmap(120, 180, Bitmap.Config.ARGB_8888);
        for (int y : new int[]{200, 900, 50, 700}) {
            scroll.setTranslationY(y == 900 ? 30 : 0);
            scroll.scrollTo(0, y);
            image.eraseColor(Color.TRANSPARENT);
            host.draw(new Canvas(image));
            require(Color.blue(image.getPixel(60, 1)) == 255 && Color.red(image.getPixel(60, 1)) > 220
                            && Color.red(image.getPixel(60, 178)) > 220,
                    "Both fades stay fixed after scroll " + y + ": scrollY=" + scroll.getScrollY()
                            + ", contentHeight=" + content.getHeight() + ", viewport=" + scroll.getHeight()
                            + ", shown=" + scroll.isShown() + ", top=" + Integer.toHexString(image.getPixel(60, 1))
                            + ", bottom=" + Integer.toHexString(image.getPixel(60, 178)));
            require(image.getPixel(60, 90) == Color.BLUE, "Fixed fades leave the viewport center clear");
        }
        image.recycle();
        fades.detach();
        BlurSettings.setEnabled(activity, false);
        BlurSettings.setEnabled(activity, true);
        image = Bitmap.createBitmap(120, 180, Bitmap.Config.ARGB_8888);
        host.draw(new Canvas(image));
        require(image.getPixel(60, 1) == Color.BLUE, "Detached edge fades do not return on preference changes");
        image.recycle();
        ((ViewGroup) host.getParent()).removeView(host);
    }

    private void checkBlur(Activity activity) {
        Bitmap bitmap = Bitmap.createBitmap(37, 19, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[37 * 19], scratch = new int[pixels.length];
        java.util.Arrays.fill(pixels, 0xff429c62);
        bitmap.setPixels(pixels, 0, 37, 0, 0, 37, 19);
        BackdropBlur.apply(bitmap, pixels, scratch, 7);
        require(bitmap.getPixel(0, 0) == 0xff429c62 && bitmap.getPixel(36, 18) == 0xff429c62,
                "Blur preserves a uniform color at image edges");
        for (int y = 0; y < 19; y++) for (int x = 0; x < 37; x++)
            pixels[y * 37 + x] = x < 18 ? Color.BLACK : Color.WHITE;
        bitmap.setPixels(pixels, 0, 37, 0, 0, 37, 19);
        BackdropBlur.apply(bitmap, pixels, scratch, 0);
        require(bitmap.getPixel(17, 8) == Color.BLACK, "Zero blur preserves detail");
        BackdropBlur.apply(bitmap, pixels, scratch, 4);
        int edge = Color.red(bitmap.getPixel(17, 8));
        require(edge > 40 && edge < 200, "Blur softens a high contrast edge");
        View host = new View(activity);
        TopActionSurfaceDrawable surface = new TopActionSurfaceDrawable(host, host, 10);
        surface.setBounds(0, 0, 37, 19);
        bitmap.eraseColor(Color.MAGENTA);
        surface.setAlpha(0);
        surface.draw(new Canvas(bitmap));
        require(bitmap.getPixel(18, 9) == Color.MAGENTA, "Hidden toolbar surface draws nothing");
        surface.setAlpha(128);
        surface.draw(new Canvas(bitmap));
        int faded = bitmap.getPixel(18, 9);
        surface.setAlpha(255);
        surface.draw(new Canvas(bitmap));
        require(faded != bitmap.getPixel(18, 9), "Toolbar surface alpha fades independently");
        bitmap.recycle();
    }

    private void checkGlassCaching(Activity activity, SharedPreferences preferences) throws InterruptedException {
        java.util.concurrent.atomic.AtomicInteger samples = new java.util.concurrent.atomic.AtomicInteger();
        final GlassSurfaceView[] glass = new GlassSurfaceView[1];
        Bitmap output = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
        onMain(() -> {
            preferences.edit().putBoolean(BlurSettings.KEY, true).putInt(BlurSettings.INTENSITY_KEY, 18).commit();
            View source = new View(activity) {
                @Override protected void onDraw(Canvas canvas) { samples.incrementAndGet(); canvas.drawColor(Color.BLUE); }
            };
            source.layout(0, 0, 80, 80);
            glass[0] = new GlassSurfaceView(activity);
            glass[0].setSourceView(source);
            glass[0].layout(0, 0, 80, 80);
            glass[0].draw(new Canvas(output));
            require(samples.get() == 1, "Glass samples its initial backdrop");
        });
        settle();
        onMain(() -> {
            glass[0].draw(new Canvas(output));
            require(samples.get() == 1, "Redrawing idle glass reuses the backdrop");
            glass[0].refreshCapture();
            glass[0].draw(new Canvas(output));
            require(samples.get() == 2, "Glass refreshes after a backdrop change");
            preferences.edit().putInt(BlurSettings.INTENSITY_KEY, 0).commit();
            glass[0].refreshCapture();
            glass[0].draw(new Canvas(output));
            require(samples.get() == 2, "Zero blur intensity does not capture content");
        });
        output.recycle();
    }

    private int[] geometry(Toolbar toolbar) {
        java.util.ArrayList<Integer> bounds = new java.util.ArrayList<>();
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (!(child instanceof androidx.appcompat.widget.AppCompatImageButton)
                    && !(child instanceof androidx.appcompat.widget.ActionMenuView)) continue;
            bounds.add(child.getLeft()); bounds.add(child.getTop());
            bounds.add(child.getWidth()); bounds.add(child.getHeight());
            if (child instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) child;
                for (int j = 0; j < group.getChildCount(); j++) {
                    View icon = group.getChildAt(j);
                    bounds.add(icon.getLeft()); bounds.add(icon.getTop());
                    bounds.add(icon.getWidth()); bounds.add(icon.getHeight());
                }
            }
        }
        int[] result = new int[bounds.size()];
        for (int i = 0; i < result.length; i++) result[i] = bounds.get(i);
        return result;
    }

    private void describeLayoutRequests(View view, StringBuilder output) {
        if (view.isLayoutRequested() && output.length() < 1800) {
            output.append(view.getClass().getSimpleName()).append('/');
            try { output.append(view.getResources().getResourceEntryName(view.getId())); }
            catch (android.content.res.Resources.NotFoundException ignored) { output.append(view.getId()); }
            output.append(' ');
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) describeLayoutRequests(group.getChildAt(i), output);
        }
    }

    private <T extends View> T first(View root, Class<T> type) {
        if (type.isInstance(root)) return type.cast(root);
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                T found = first(group.getChildAt(i), type);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void onMain(Runnable task) {
        runOnMainSync(() -> { try { task.run(); } catch (Throwable error) { failure = error; } });
        if (failure != null) throw new AssertionError(failure);
    }

    private void settle() throws InterruptedException {
        java.util.concurrent.CountDownLatch frame = new java.util.concurrent.CountDownLatch(1);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(frame::countDown, 350);
        require(frame.await(5, java.util.concurrent.TimeUnit.SECONDS), "UI responds within five seconds");
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
