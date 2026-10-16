# OneUI Design wiki

This is the current behavior and usage guide for the **revilodev** fork. The [README](../README.md) covers building and integration; the [component catalogue](components-and-settings.md) lists public classes. Original OneUIProject authors and license are credited in [LICENSE](../LICENSE).

## Sample app

Widgets is the entry page. The Home page has been removed. Page search and the drawer share the same page instances. Demo views are created on demand, restored by class, and hidden pages stop receiving the resumed lifecycle. Appearance changes retain the selected page without duplicating fragments.

| Page | Components and features |
| --- | --- |
| Widgets | Buttons, switches, checkboxes, radios, spinner, floating search, cards and carousel |
| ProgressBar | Horizontal, circular, determinate and indeterminate progress |
| Now Bar | Live notification, timer and media demonstrations |
| SeekBar | Continuous, stepped, warning, vertical and thick sliders |
| Swipe refresh | Pull-to-refresh and scrolling |
| Preferences | Dark mode, device/custom palette, blur intensity, opacity and persistent controls |
| Navigation | Floating navigation, swiping pages, icon/label variants and More grid |
| Toolbar | Floating contextual toolbar and Cancel/Save action bar |
| App picker | Installed-app list and selection |
| Contacts / Index scroll | Contact rows, alphabet index, section labels and NEW menu badge |
| Pickers | Date, time, number and color selection |
| QR code | Generate and share QR output |
| Icons | Searchable icon collection |

## Appearance contract

### Palette selection

The global **Use system color palette** switch defaults on. On uses the native device/SESL theme, preserving the appearance formerly obtained with the material switch off. Off uses the custom light/dark palette. Unset custom roles use library defaults. The old `global_material_color_palette` key is ignored; the current key is `global_use_color_palette`. No additional Monet overlay is applied by this switch. On first launch after this update, saved values matching the former default dark Background (`#171717`), light Background (`#FCFCFC`) or dark Surface (`#2E2E30`) are cleared so the corrected defaults take effect. Other saved colors are retained.

Chrome icons and labels use Foreground (white/dark text as appropriate). Subtitles and summaries use `OneUIPalette.secondaryText(context)`, resolved from theme secondary text or Foreground against Background, independently of the Secondary accent. Dialog OK/Cancel labels also use Foreground.

Every library view accepts `app:usesColorPalette="true|false"`. True follows the global selection; false forces custom colors for that component and its children. Standard XML controls participate through `PaletteInflater`; programmatic controls can use `OneUIPalette.wrap(context, null)`. The Java equivalent is `OneUIPalette.setUsesColorPalette(view, false)`.

```java
MaterialColorSettings.setEnabled(context, false);
OneUIPalette.set(context, OneUIPalette.PRIMARY, true, 0xff438aff);
OneUIPalette.set(context, OneUIPalette.SURFACE, true, 0xff171717);
int color = OneUIPalette.get(context, OneUIPalette.PRIMARY);
OneUIPalette.reset(context, true); // Only the dark palette.
```

The boolean in `set`/`reset` selects dark (`true`) or light (`false`). Saved values use `oneui_palette_<role>_dark` or `_light`. Recreate the activity after changing mode or a saved palette color. Components with explicit color setters or XML color attributes retain those overrides. Custom image/gradient content keeps its intended colors.

### Background and surface are separate

Custom dark Background is **pure black**; Surface is **#171717**. Custom light Background is **#F6F6F6**; Surface is **white**. Background belongs to pages and windows. Surface belongs to cards and content groups. Secondary surfaces use Surface variant. Foreground, Primary, Secondary, Outline and Error cover text, accents, inactive controls and warnings.

Mark ordinary solid containers explicitly when applying the palette tree:

```xml
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/oui_round_and_bgcolor"
    android:tag="oneui.palette.background">

    <dev.oneuiproject.oneui.widget.RoundLinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="@color/oui_surface_color"
        android:tag="oneui.palette.surface"
        app:surfaceShadowEnabled="false" />
</FrameLayout>
```

Native preference rows use `PalettePreferenceGroupAdapter`, which paints Surface on binding and rebinding instead of depending on the legacy selector background. Disable the native preference corner decoration (`seslSetRoundedCorner(false)`) when using this adapter; it provides the grouped corners itself. Also disable the native list bottom fill with `getListView().seslSetFillBottomEnabled(false)`.

Resolve colors for custom drawables with `OneUIPalette.get`, or map a supported legacy color resource with `OneUIPalette.color`. Apply the view tree after adding standard controls. Do not reuse a page-background tag for a card.

### Controls and depth

- Toggles use an **18 dp circle inside a 24 dp track**, with Background on the thumb and Primary on the checked track. Disabled/off states remain distinguishable. Input, focus, RTL and accessibility remain handled by SwitchCompat.
- Light thick sliders use the same gray Outline role as other inactive tracks. Dark thick sliders may use glass. Explicit track colors remain solid.
- Light rounded cards default to **0.5 dp** elevation with low-opacity shadows. `surfaceShadowEnabled` and `surfaceShadowElevation` remain available. Dark surfaces use their surface color without a light-theme shadow.
- Navigation has a wider base around its selected pill. The selection is **30% white in dark mode**, **white in light mode**, independent of Primary. The separate temporary press circle and scale animation have been removed.

### Header and menu motion

Toolbar icon bounds stay the same before and after scrolling. Only the rounded surface behind them fades over **180 ms**, reversing from its current alpha if scrolling reverses. The space between controls stays transparent. Navigation and overflow use circular feedback for press, hover and focus. Native ripple/focus masks and state animations do not morph the floating outline. The overflow button's inner image also receives the matching feedback.

The Contacts NEW badge sits at the outside **top/end** corner, clear of the three-dot glyph. The More menu uses one **24 dp upward slide plus fade over 180 ms**, after its first layout. Window animation is disabled for that dialog to avoid a second entrance animation and flicker.

### Live blur

| Preference | Key | Range / default |
| --- | --- | --- |
| Blur effects | `global_blur_enabled` | true |
| Blur intensity | `global_blur_intensity` | Continuous 0–40 dp, default 18 |
| Glass opacity | `global_blur_alpha` | 0–100%, steps of 5, default 65 |

Opacity controls the color tint over the blurred content. The drawer uses an actual sampled backdrop. Modern toolbar/drawer surfaces use GPU blur; older paths use cached, downsampled images. Captures update with content/scroll/position changes, retain the previous valid frame during asynchronous capture, and release buffers on detach. Toolbar/drawer surfaces skip captures at zero intensity and do not poll while idle. Individual component blur flags are respected when the global switch is enabled. Turning blur off uses a solid surface.

`ProgressiveBlurLayout` and `ScrollEdgeFades` provide themed **gradient fades**, rather than backdrop pixel blur. Automatic fades draw on the stationary parent viewport overlay, with their visibility read during drawing. They never follow or animate toward a content scroll offset, including during fast flings. See [implementation details](color-palette-and-blur.md).

## Component recipes

Examples assume an Android layout root declaring `xmlns:android="http://schemas.android.com/apk/res/android"` and `xmlns:app="http://schemas.android.com/apk/res-auto"`. View classes live under `dev.oneuiproject.oneui.widget`, layouts under `.layout`, dialogs under `.dialog`, and preferences under `.preference`.

### Sliders

```xml
<dev.oneuiproject.oneui.widget.OneUISlider
    android:id="@+id/level"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:contentDescription="Level"
    app:minValue="0" app:maxValue="100" app:sliderValue="40"
    app:stepSize="10" app:showTicks="true"
    app:sliderWarningValue="80"
    app:usesColorPalette="false" />

<dev.oneuiproject.oneui.widget.OneUIThickSlider
    android:id="@+id/brightness"
    android:layout_width="match_parent"
    android:layout_height="64dp"
    android:contentDescription="Brightness"
    app:thickMinValue="0" app:thickMaxValue="100" app:thickValue="50"
    app:thickShowNumber="true" app:thickShowMenuButton="true" />
```

```java
level.setOnValueChangeListener((slider, value, fromUser) -> updateLevel(value));
brightness.setOnMenuClickListener(() -> openBrightnessOptions());
```

Omit `stepSize`/`showTicks` for a continuous slider. Other standard slider attributes: `sliderVertical`, `sliderFillEnabled`, `sliderHapticEnabled`, `leadingIcon`, `sliderActiveColor`, `sliderTrackColor`, `thumbFillColor`. Thick equivalents: `thickVertical`, `thickIcon`, `thickWarningValue`, `thickActiveColor`, `thickTrackColor`. A vertical thick slider needs a taller layout, such as 80 × 260 dp.

### Toggle

```xml
<androidx.appcompat.widget.SwitchCompat
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:text="Notifications"
    android:checked="true"
    app:usesColorPalette="false" />
```

Use `setOnCheckedChangeListener` normally. The shared palette pass applies the contained thumb/track geometry to SwitchCompat, including switches inside preference rows. Inflate through `PaletteInflater` and apply the view tree for standard controls.

### Navigation and More

```xml
<!-- res/menu/navigation.xml -->
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:id="@+id/page_home" android:title="Home" android:icon="@drawable/ic_home" />
    <item android:id="@+id/page_saved" android:title="Saved" android:icon="@drawable/ic_saved" />
</menu>
```

```xml
<dev.oneuiproject.oneui.widget.NavigationPageContainer
    android:id="@+id/pages" android:layout_width="match_parent"
    android:layout_height="match_parent" android:paddingBottom="88dp">
    <FrameLayout android:id="@id/page_home"
        android:layout_width="match_parent" android:layout_height="match_parent" />
    <FrameLayout android:id="@id/page_saved"
        android:layout_width="match_parent" android:layout_height="match_parent" />
</dev.oneuiproject.oneui.widget.NavigationPageContainer>
```

```xml
<dev.oneuiproject.oneui.widget.FloatingNavigationBar
    android:id="@+id/navigation" android:layout_width="wrap_content"
    android:layout_height="60dp" app:navigationMenu="@menu/navigation"
    app:showLabels="true" app:blurEnabled="true" />
```

```java
navigation.bindPages(pages);
navigation.setBackdropView(pages);
navigation.setPageSwipingEnabled(true);
navigation.setOnItemSelectedListener(id -> handleDestination(id));
navigation.setSelectedItemId(R.id.page_home);
```

Four or more items use three direct destinations and More. More holds the remaining destinations in a scrollable grid. `setShowLabels`, `setShowIcons`, `setSelectedIcon`, `setItemWidth`, `setBarHeight`, `setSelectionInset`, `setSurfaceColor`, `setSelectedColor`, and `setAnimationsEnabled` provide local adjustments. `setAction(icon, description, listener)` adds an independent end button. Keep room below scrolling content for the overlay.

### Page header, drawer and bottom bars

```xml
<dev.oneuiproject.oneui.layout.DrawerLayout
    android:id="@+id/screen" android:layout_width="match_parent"
    android:layout_height="match_parent" app:title="Example"
    app:stickyActionsEnabled="true" app:stickyActionBlurEnabled="true">
    <FrameLayout android:id="@+id/content"
        android:layout_width="match_parent" android:layout_height="match_parent"
        app:layout_location="main_content" />
    <androidx.recyclerview.widget.RecyclerView android:id="@+id/drawer_items"
        android:layout_width="match_parent" android:layout_height="match_parent"
        app:layout_location="drawer_panel" />
</dev.oneuiproject.oneui.layout.DrawerLayout>
```

`ToolbarLayout` supplies the same header without a drawer. `setStickyBottomBar(bar)` moves a floating bar into the fixed footer slot; clear it with `setStickyBottomBar(null)` when changing pages. `getStickyToolbarControls()` exposes icon width, surface padding, insets, blur, radius and collapse tolerance. Avoid changing icon dimensions in a scroll callback.

```xml
<dev.oneuiproject.oneui.widget.FloatingToolbar
    android:id="@+id/tools" android:layout_width="match_parent"
    android:layout_height="56dp" app:toolbarMenu="@menu/selection_actions"
    app:toolbarShowLabels="true" />
```

```java
tools.setOnActionClickListener(id -> handleAction(id));
tools.setBlurEnabled(true);
actionBar.setLabels("Cancel", "Save"); // FloatingActionBar
actionBar.setOnCancelClickListener(v -> cancel());
actionBar.setOnConfirmClickListener(v -> save());
switcher.setNavigationBar(navigation); // FloatingBarSwitcher, bars are direct children.
switcher.setBackdropView(pages);
switcher.showBar(tools);
switcher.showNavigation();
```

### Search, rows, cards and scroll helpers

```java
search.setSourceView(scrollContent); // FloatingSearchBar
search.setHint("Search items");
search.setPersistent(true);
search.setCloseButtonVisible(false);
search.setOnVoiceRequested(() -> startVoiceSearch());
search.setListener(listener);

row.setTitle("Morning routine"); // IconRowView
row.setSubtitle("Three activities");
row.setIconResource(R.drawable.ic_routine);
row.setExpandedText("Optional additional details");

tile.setTitle("Collection"); // ImageTileView
tile.setSubtitle("Eight items");
tile.setGradientColors(0xff789bc9, 0xff324a68);

scrollToTop.bind(recyclerView); // FloatingScrollToTopButton
ScrollEdgeFades.attach(recyclerView);
```

`RoundLinearLayout` and `RoundFrameLayout` accept `roundedCorners`, `surfaceShadowEnabled` and `surfaceShadowElevation`. `RelatedCard` groups related actions. `GlassSurfaceView` is the shared rounded blur surface with `setSourceView`, `setCaptureWindow`, `setBlurEnabled`, `setCornerRadius` and `setTintColor`. The full [catalogue](components-and-settings.md#component-catalog) also includes dialogs, splash/app information layouts and utilities.

### Grid menu

```java
GridMenuDialog menu = new GridMenuDialog(context);
menu.inflateMenu(R.menu.more_actions);
menu.setSpanCount(4);
menu.setMaxVisibleRows(2);
menu.setCardBlurEnabled(true);
menu.setOnItemClickListener(item -> {
    handleAction(item.getItemId());
    return true; // Dismiss after handling.
});
menu.show();
```

The rounded card owns its blur and entrance motion. Supplying a second window animation produces competing motion and should be avoided.

### Preferences

```xml
<PreferenceScreen xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <SwitchPreferenceCompat android:key="global_use_color_palette"
        android:title="Use system color palette" android:defaultValue="true" />
    <dev.oneuiproject.oneui.preference.OneUISliderPreference
        android:key="global_blur_intensity" android:title="Blur intensity"
        android:defaultValue="18" app:minValue="0" app:maxValue="40" />
    <dev.oneuiproject.oneui.preference.OneUISliderPreference
        android:key="global_blur_alpha" android:title="Glass opacity"
        android:defaultValue="65" app:minValue="0" app:maxValue="100"
        app:stepSize="5" app:showTicks="true" />
</PreferenceScreen>
```

Load with `PreferenceFragmentCompat.setPreferencesFromResource`. Override `onCreateAdapter(PreferenceScreen)` to return `new PalettePreferenceGroupAdapter(screen)` for palette-aware native rows. Slider rows persist values through the preference framework. `ColorPickerPreference` supports `showAlphaSlider`; `SeekBarPreferencePro` exposes SESL modes; `HorizontalRadioPreference` supports entries, values, subtitles and images. Use the [complete sample preferences](../sample-app/src/main/res/xml/sample3_preferences.xml) for palette pickers and appearance listeners, or the [smaller settings example](settings-page-example.md) for a minimal screen. A custom color picker's arbitrary key does not automatically become a palette role.

## Maintenance and verification

Source versions live in [manifest.gradle](../manifest.gradle). `lib` contains reusable components; sample-only showcase classes stay in `sample-app`. New pages should be registered once in `MainActivity.initFragmentList`; search and the drawer derive their links from that list. Use class-based page navigation rather than hardcoded drawer positions.

Shared appearance code belongs in `OneUIPalette`, `BlurSettings` and the surface helpers. Do not mutate layout parameters during drawing or call layout setters repeatedly with unchanged values. Invalidate captures on relevant changes, release listeners/buffers on detach, and avoid idle redraw timers. Source formatting and line endings are defined by `.editorconfig` and `.gitattributes`. Generated APKs, captures, SDK paths and credentials remain outside tracked source.

Build and device-check commands are in the [README](../README.md#repository-map-and-checks). Checks cover control colors, opt-outs, explicit overrides, light/dark background separation, toggle geometry, gray thick tracks, neutral navigation selection and sizing, toolbar geometry/feedback/transparency, idle layout stability, blur caching, every demo destination and appearance recreation. The runner restores settings afterward. Samsung-specific transitions also require a real-device visual check.

Known limits: legacy SESL dependencies remain; the sample target is API 33; some lint findings predate these changes. Compatibility with API 23 and older blur fallbacks should be checked on those devices before publishing. This source update does not claim a completed Android target/SESL migration. The [habit-app rulebook](one-ui-9-habit-app-rulebook.md) is separate research/proposed guidance, not an implementation checklist for this gallery.
