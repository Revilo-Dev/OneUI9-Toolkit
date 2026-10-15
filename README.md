<p align="center">
  <img loading="lazy" src="readme-res/design-readme-header.png"/>
  <br><br>
  <a href="https://t.me/oneuiproject"><img src="https://img.shields.io/badge/Telegram-OneUI_Project-blue.svg?style=for-the-badge&logo=Telegram"/></a>
  <a href="https://mvnrepository.com/artifact/io.github.oneuiproject/design"><img src="https://img.shields.io/maven-central/v/io.github.oneuiproject/design?color=%23C71A36&label=Maven&logo=Apache%20Maven&logoColor=%23C11920&style=for-the-badge"/></a>
  <h3 align="center"><a href="sample-app/release/sample-app-release.apk">Bundled sample APK</a></h3>
</p>

<h1 align="center">OneUI Design · revilodev fork</h1>

This fork is developed by **revilodev** and builds on the original [OneUIProject/oneui-design](https://github.com/OneUIProject/oneui-design) by **Yanndroid** and **BlackMesa123**. Their work and copyright remain credited in [LICENSE](LICENSE).

**Full guide:** [Components and settings](docs/components-and-settings.md) lists the public components, appearance preferences, blur, palette roles, component styles, and integration examples.

In this repository you'll find:
- Source code of the [oneui-design library](#oneui-design-module)
- Source code of the [One UI Sample app](#oneui-sample-app)
- The [complete component and settings guide](docs/components-and-settings.md)

Any form of contribution, suggestions, bug report or feature request will be welcome.

# OneUI Design Module

The `oneui-design` library (distinct from the older [OneUI Design Library](https://github.com/OneUIProject/OneUI-Design-Library)) contains custom One UI views, layouts, dialogs, and preferences. It builds on [oneui-core](https://github.com/OneUIProject/oneui-core). This fork's current API and settings are documented in [components-and-settings.md](docs/components-and-settings.md); the [original documentation](https://oneuiproject.github.io/design/) covers the upstream project.

## Usage

- Make sure your Android project has [oneui-core](https://github.com/OneUIProject/oneui-core#usage) libraries implemented;

- Add the design library in your dependencies:
```groovy
dependencies {
    // ...
    implementation 'io.github.oneuiproject:design:<version>'
}
```

- Apply the main theme in your AndroidManifest file:
```xml
<application
    ...
    android:theme="@style/OneUITheme" >
    ...
</application>
```

If you use the sample's app-wide palette behavior, apply it before inflating each activity:

```java
@Override protected void onCreate(Bundle state) {
    super.onCreate(state);
    MaterialColorSettings.applyTheme(this);
    setContentView(R.layout.activity_main);
}
```

Recreate the activity after changing Material mode or a saved palette color. The sample app already wires this into its Preferences page.

# OneUI Sample App

<p align="center"><img loading="lazy" src="readme-res/sample-ss1.jpg" height="280"/> <img loading="lazy" src="readme-res/sample-ss2.jpg" height="280"/> <img loading="lazy" src="readme-res/sample-ss3.jpg" height="280"/> <img loading="lazy" src="readme-res/sample-ss4.jpg" height="280"/> <img loading="lazy" src="readme-res/sample-ss5.jpg" height="280"/></p>

The One UI Sample app showcases components from [oneui-core](https://github.com/OneUIProject/oneui-core) and this [oneui-design](#oneui-design-module) module. Its drawer pages include Widgets, Progress bars, Now Bar, Seek bars, Swipe refresh, Preferences, Tabs, Toolbar, App picker, Index scroll, Pickers, QR code, and Icons. Build the app from source for the latest changes; the [bundled APK](sample-app/release/sample-app-release.apk) is a release snapshot.

## Features
- Supports Android 6.0 (api 23) and above;
- Supports both Samsung and non-Samsung devices;
- Includes `ProgressiveBlurLayout` for fading blur at the top and bottom of scrollable content (API 23+);
- Includes `FloatingNavigationBar` for a floating navigation pill with optional labels, backdrop blur, and a separate end action;
- Includes `OneUISlider` and `FloatingToolbar` for the thicker slider treatment and compact contextual actions;
- Includes a component-wide **Default OneUI** palette with editable light and dark Primary, Secondary, Background, Foreground, Surface, Surface variant, Outline, and Error roles;
- Can switch between saved OneUI colors and Android 12+ wallpaper colors in Preferences;
- Includes an app-wide blur switch and local blur, tint, shadow, shape, and motion controls;
- Includes Now Bar, shortcut, carousel, and action card demonstrations in the sample app;
- Example UI for the following components:
  - AppCompat (Base widgets/theme);
  - Material Components ([Tabs](https://material.io/components/tabs), [Bottom navigation](https://material.io/components/bottom-navigation));
  - Date/Time/Color pickers;
  - ListViews (AppPicker, Contacts, Icons);
  - Preferences;
  - Swipe to Refresh;

## Appearance and component settings

Open **Preferences** in the sample app to change light/dark mode, turn blur on or off, select Android wallpaper colors, and edit the **Default OneUI palette**. With Material theming disabled, participating library components use the app's Default OneUI palette throughout the app. Its starting colors are the existing light and dark resources; editing a role saves a separate color for each mode. Use **Reset this mode to Default OneUI** to restore those defaults. Android 12+ wallpaper colors take priority while Material theming is enabled.

Use `OneUIPalette.get(context, OneUIPalette.PRIMARY)` to read a semantic color, `OneUIPalette.set(context, role, dark, color)` to change one, and `OneUIPalette.reset(context, dark)` to reset a mode. `OneUIPalette.color(context, R.color.oui_...)` maps existing component resources to the active palette. For XML views, the sample calls `OneUIPalette.applyToViewTree(root)` after inflation and tags palette backgrounds with `oneui.palette.background` or `oneui.palette.surface`. Direct per-view setters still let you style an individual control. The [full guide](docs/components-and-settings.md#appearance-preferences) explains all eight roles and integration steps.

`BlurSettings.setEnabled(context, false)` turns off participating backdrop blur and scroll fades app-wide. Use `setBlurEnabled` on an individual floating control, `setStickyActionBlurEnabled` on `ToolbarLayout`, `setCardBlurEnabled` on `GridMenuDialog`, or `setBlurSampleScale` on `GlassSurfaceView` for local styling. The [component table](docs/components-and-settings.md#component-catalog) lists the rest of the public views and their settings.

The sample also includes these newer demonstrations:

| Demo | What it shows |
| --- | --- |
| Now Bar | Normal and timer notifications with a live status display |
| Navigation | A floating bar with a More grid for extra destinations and page swipes |
| Widgets | Horizontal carousel, action cards, and a six-shortcut grid |
| Search | A floating search pill with voice input and filtered results |

# Scroll edge fades

The Flow-style progressive effect is a themed color fade, not a pixel blur. `ToolbarLayout` and `DrawerLayout` install it on every scrollable page descendant, including `RecyclerView`, `ListView`, `ScrollView`, and `NestedScrollView`. The scrolling content overlaps the collapsed toolbar so the top fade begins directly below the status bar. It appears only when more content exists in that direction and never consumes touch events. To use it outside those layouts, call `ScrollEdgeFades.attach(scrollable)` or wrap a single scrolling view in `ProgressiveBlurLayout`. Both default to 56dp edges. The existing `blurHeight`, `blurTop`, and `blurBottom` XML names remain supported by the wrapper.

```xml
<dev.oneuiproject.oneui.widget.ProgressiveBlurLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:blurHeight="56dp">

    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</dev.oneuiproject.oneui.widget.ProgressiveBlurLayout>
```

Use `setFadeHeights(topPx, bottomPx)`, `setBlurEdges(top, bottom)`, and `setFadeColor(color)` on the wrapper. Both fade components use the nearest solid surface color by default. `ScrollEdgeFades.attach(view)` returns a controller with `setHeights`, `setEdges`, `setColor`, and `detach`. Use `attachTree(root, surfaceColor)` for translucent panels such as a drawer. A second `attach` call returns the existing controller.

## Pinned action bar and floating search

`ToolbarLayout` pins its toolbar when the large header collapses. Once page content scrolls behind it, the actual navigation button and action menu become rounded control groups. Both groups use the same height; the navigation button is circular and the action group measures around its visible icons with equal icon widths and padding. The toolbar remains transparent. The compact page title appears when the header closes and leaves as the content scrolls; enable `setStickyTitleEnabled(true)` to keep it pinned. Screens can add actions through `getToolbar()` and configure the groups through `setStickyActionsEnabled`, `setStickyActionBlurEnabled`, `setStickyActionShadowElevation`, and `setStickyActionTintColor`. `getStickyToolbarControls()` also exposes icon width, group padding, action group corner radius, horizontal inset, collapse tolerance, and content scroll trigger options. A standalone `CoordinatorLayout` can use `StickyToolbarControls(appBar, toolbar, content)`, as the About screen does.

`showSearchMode()` opens the 56dp bottom search pill above the navigation bar or keyboard. Its close button and Back clear the query and restore the page. `showSearchMode(initialQuery)` restores a query without sending a duplicate text callback. Screens own filtering through `setSearchModeListener`; use `getFloatingSearchBar()` to customize the hint, icons, animation timing, blur surface, and input field. Opening search hides the sticky bottom bar and footer, then restores them on close.

```java
ToolbarLayout layout = findViewById(R.id.toolbar_layout);
layout.getToolbar().inflateMenu(R.menu.my_actions);
layout.getFloatingSearchBar().setHint("Search items");
layout.getFloatingSearchBar().setAnimationDurations(180, 130);
layout.getStickyToolbarControls().setCornerRadius(
        Math.round(28 * layout.getResources().getDisplayMetrics().density));
layout.getStickyToolbarControls().setItemWidth(
        Math.round(44 * layout.getResources().getDisplayMetrics().density));
layout.setStickyActionBlurEnabled(true);
```

The sample's search action filters its page list as text changes. For application data, connect `SearchModeListener.onQueryTextChange` to the page's adapter and clear that filter when the callback receives an empty string.

## Floating navigation

`FloatingNavigationBar` is a reusable view for compact navigation. Add it over content in a `FrameLayout`, leaving a bottom content inset so the last row stays reachable. Its menu items provide stable IDs, icons, and accessible titles. Up to three destinations appear directly; a fourth slot opens a More grid for additional destinations. An optional circular end action remains independent of navigation selection. For outlined icons that fill when selected, call `setSelectedIcon(itemId, filledDrawable)` after inflating the menu. The sample Navigation page demonstrates this for Grid, List, Cards, and Settings.

```xml
<dev.oneuiproject.oneui.widget.FloatingNavigationBar
    android:id="@+id/floating_nav"
    android:layout_width="wrap_content"
    android:layout_height="58dp"
    android:layout_gravity="bottom|center_horizontal"
    android:layout_margin="16dp"
    app:navigationMenu="@menu/main_destinations"
    app:showLabels="true"
    app:blurEnabled="true"
    app:actionIcon="@drawable/ic_add"
    app:actionContentDescription="Add item" />
```

```java
FloatingNavigationBar nav = findViewById(R.id.floating_nav);
nav.setOnItemSelectedListener(itemId -> showDestination(itemId));
nav.setAction(R.drawable.ic_add, "Add item", v -> addItem());
// Optional: nav.setShowLabels(false); nav.setShowIcons(false);
// nav.setBlurEnabled(false); nav.setAnimationsEnabled(false); nav.clearAction();
// nav.setItemWidth(68dp); nav.setBarHeight(60dp); nav.setSelectedColor(color);
// nav.setSelectedIcon(R.id.nav_grid, R.drawable.sample_nav_grid_filled);
```

To link actual pages, put them as direct children of `NavigationPageContainer`. Give each page the same ID as its menu item, then bind once:

```java
FloatingNavigationBar nav = findViewById(R.id.floating_nav);
NavigationPageContainer pages = findViewById(R.id.pages);
nav.bindPages(pages);
nav.setPageSwipingEnabled(true); // Optional; false disables horizontal page swipes.
```

Place the page container and bar as siblings in a full-height `FrameLayout`, with the bar aligned to the bottom. Leave 88dp of bottom space in the page container so content stays reachable. A menu item without a matching page can be handled by `setOnItemSelectedListener`, as the sample does for its popup. The sample has gradient album tiles on Grid, icon rows on List, expandable cards on Cards, and controls on Settings. Press and page transitions follow `setAnimationsEnabled`. With blur enabled, the view samples content behind it; solid mode skips this work. Surfaces and selection colors come from the active palette, with the original light and dark resources as defaults. Samsung's [bottom navigation guidance](https://developer.samsung.com/one-ui/comp/bottom-navigation.html) describes the tab behavior and [bottom bar guidance](https://developer.samsung.com/one-ui/comp/bottom-bar.html) describes bottom actions.

`IconRowView` powers both the plain List rows and expandable Cards rows. Set `setIconResource`, `setIconBackgroundColor`, `setIconTintColor`, `setTitle`, and `setSubtitle` for either style. Call `setExpandedText("Example text")` to add a chevron and expandable detail; omit it for a plain row. Cards disable automatic scroll edge fades with `ScrollEdgeFades.setAutoAttachEnabled(scrollView, false)`.

`ImageTileView` is used by the Grid examples. Set its title and subtitle, then choose a gradient or an image source. The sample uses gradients with placeholder titles and random counts; it does not bundle photos.

```java
ImageTileView tile = new ImageTileView(context);
tile.setTitle("My album");
tile.setSubtitle("42 photos");
tile.setGradientColors(0xFF8B97AF, 0xFF434B69); // Default visual without an image.
tile.setImageUri(latestPhotoUri);                 // URI your app retrieved from MediaStore.
// Or: tile.setImageUri(pickedPhotoUri);         // URI returned by the system photo picker.
// Or: tile.setImagePath(localImagePath);        // Local path, file://, or content:// string.
// Or: tile.setImageResource(R.drawable.my_photo);// Image packaged with your app.
// tile.setImageUri(null);                       // Show the gradient again.
```

Pass a readable URI for the latest or picked photo; the app that chooses the photo also handles the required media access. The tile displays it with center cropping and keeps the gradient underneath as a fallback.

For one to three destinations without an end action, the bar sizes itself to its items. Use `android:layout_gravity="bottom|center_horizontal"` inside a `FrameLayout` to center a compact bar. Four or more destinations use the fourth slot for More.

## Sliders

`OneUISlider` supports a thick rounded track, a palette-colored active segment, and a circular thumb filled with the current background color. It works from API 23, supports touch, keyboard and accessibility adjustments, and can show discrete steps, vertical orientation, haptic steps, and a warning threshold. The sample's stepped sliders have no fill.

Set `sliderWarningValue` to mark the end of a slider with a light red warning segment. Crossing the threshold changes the active fill, thumb outline, and leading icon to the theme's error accent. `setWarningAreaColor` changes the warning segment color.

`OneUIThickSlider` provides a larger volume or brightness control in horizontal or vertical form. Set `thickIcon`, `thickShowNumber`, `thickShowMenuButton`, and `thickWarningValue` independently. The menu button calls `setOnMenuClickListener`; the warning range colors the filled bar with the error accent and keeps the icon legible in white or black. The sample SeekBar page shows both orientations.

`FloatingSearchBar.setCloseButtonVisible(false)` removes the exit action from a search field that stays on its page. Place `FloatingScrollToTopButton` after the scrolling view in a `FrameLayout`, then call `bind(recyclerView)`; its elevation keeps it above the page's scroll fade. Give it a larger bottom margin when a floating search bar is present. The Icons example uses an 88dp bottom margin.

```xml
<dev.oneuiproject.oneui.widget.OneUISlider
    android:id="@+id/volume_slider"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:contentDescription="Volume"
    app:minValue="0"
    app:maxValue="100"
    app:sliderValue="50"
    app:leadingIcon="@drawable/ic_volume" />
```

Use `app:stepSize` with `app:showTicks="true"` for fixed levels and `app:sliderFillEnabled="false"` for a track without fill. `app:sliderVertical`, `app:sliderHapticEnabled`, and `app:sliderWarningValue` enable the other variants. `setOnValueChangeListener` reports the new value and whether it came from the user. Active, track, and thumb colors can be overridden per view in XML.

`OneUISliderPreference` puts the same slider in a persistent preference row. Set a unique `android:key`, `android:defaultValue`, and the same `app:minValue`, `app:maxValue`, `app:stepSize`, `app:showTicks`, and `app:sliderFillEnabled` attributes used by the view. Preference change listeners can accept or reject user changes.

The active segment uses the theme's `colorPrimary` by default. Use `app:sliderActiveColor` or `setActiveColor` to override it for one slider.

## Floating toolbar

`FloatingToolbar` displays up to five independent actions from an Android menu resource. Each visible item needs an ID and title; an icon is optional. The title remains the accessibility label when visual labels are hidden. Use `app:toolbarLightSurface` to choose the light or dark pill, `app:toolbarShowLabels` to control captions, and `setBlurEnabled` for a glass surface. `FloatingActionBar` is a two-command Cancel/Save bar with configurable labels and callbacks. Both use the same light/dark surface colors as navigation.

```xml
<dev.oneuiproject.oneui.widget.FloatingToolbar
    android:id="@+id/selection_toolbar"
    android:layout_width="match_parent"
    android:layout_height="56dp"
    app:toolbarMenu="@menu/selection_actions"
    app:toolbarLightSurface="false" />
```

```java
FloatingToolbar toolbar = findViewById(R.id.selection_toolbar);
toolbar.setOnActionClickListener(actionId -> handleAction(actionId));
toolbar.setBlurEnabled(true);
```

`FloatingBarSwitcher` keeps a navigation bar and one or more contextual bars in a single sticky bottom slot. Add the bars as direct children, call `setNavigationBar(nav)`, then `showBar(toolbar)` or `showNavigation()`. Set `setBackdropView(pageContainer)` when blurred bars should sample only the page behind them. `setAnimationsEnabled(false)` makes the swap immediate. The sample Toolbar page has a Toolbar tab and an Action bar tab, each with an open button. Samsung's [bottom bar guidance](https://developer.samsung.com/one-ui/comp/bottom-bar.html) recommends up to five bottom actions with icons and text; the floating presentation follows the supplied reference.

When a page uses `ToolbarLayout` or `DrawerLayout`, call `setStickyBottomBar(bar)` after its view is created. This moves the bar into a fixed overlay above the scrolling page; call `setStickyBottomBar(null)` when leaving the page. The sample does this when switching between Navigation and Toolbar. `GlassSurfaceView` provides the shared rounded blur surface used by the collapsed app bar and floating drawer. The expanded app bar retains its flat presentation. The theme gives AppCompat alert dialogs a rounded translucent surface and uses Android window blur on API 31+; older versions retain the translucent surface.

`BlurSettings` provides an app-wide override for glass surfaces, dialog backdrops, and progressive scroll fades. The sample exposes its `global_blur_enabled` key in Preferences. A component's own blur option still controls that component when the global setting is on. Call `BlurSettings.setEnabled(context, false)` to switch all blur off in code.

`GridMenuDialog` keeps the page sharp and blurs its rounded menu card by default. Call `setCardBlurEnabled(false)` for a solid card; the app-wide blur setting still overrides the card.

For a commented settings page built from the existing preference components, see [settings-page-example.md](docs/settings-page-example.md).

`FloatingSearchBar` can be embedded as a full search field with `setPersistent(true)`. Set its scroll source with `setSourceView`, supply a `Listener` for filtering or submission, and customize its hint, icons, and animation durations. The sample Widgets page uses the same class as the toolbar search overlay.

## Android 17 / One UI modernization plan

The build uses Gradle 9.5.1, Android Gradle Plugin 9.3, and `compileSdk` 36. The sample targets API 33 and the project still uses older SESL dependencies. `ui9-v1.0` identifies the first migration release and does **not** claim that the whole library has been migrated yet.

1. Review available SESL releases and upgrade dependencies together, checking API compatibility. Keep `minSdk 23` until a dependency or feature forces a change; gate new platform APIs by SDK level. The legacy picker artifacts share a namespace and currently require the `android.uniquePackageNames=false` migration setting.
2. Install the Android 17 SDK and raise `compileSdk` to 37. Update the sample's `targetSdk` separately after testing Android 15+ edge-to-edge, window insets, predictive back, permissions, and Android 17 behavior changes.
3. Audit theme colors, typography, spacing, shapes, iconography, motion, dark mode, contrast, and touch targets against the current [Samsung One UI guidance](https://developer.samsung.com/one-ui/index.html). Remove reliance on undocumented Samsung internals where possible so non-Samsung devices remain supported.
4. Make layouts adaptive for tablets, foldables, landscape, split screen, and large font sizes. Add device and API-level visual checks, especially API 23, 31, 35, and 37.
5. Update publishing coordinates, README links, release artifacts, changelog, sample screens, and documentation for this fork before publishing a release.

Candidate library components: edge-to-edge and inset-aware screen scaffolds; adaptive navigation and responsive panes; current One UI color and typography tokens; bottom action bars and sheets; updated search and list patterns; accessible loading, empty, and error states; motion and haptic helpers; and a foldable-aware layout helper. These are proposals for implementation and visual review, not existing APIs.

# More info
- [One UI 4](https://design.samsung.com/global/contents/one-ui-4/)
- [One UI Design Guide](https://developer.samsung.com/one-ui/index.html)
- [One UI Design Guide (PDF)](https://design.samsung.com/global/contents/one-ui/download/oneui_design_guide_eng.pdf)

# Special thanks
- [Google](https://developer.android.com/jetpack) for their Jetpack and Material Components libraries.
- [Samsung](https://www.samsung.com/) for their awesome OneUI Design. :)
- All the current and future [contributors](https://github.com/OneUIProject/oneui-design/graphs/contributors) and issue reporters. :D
