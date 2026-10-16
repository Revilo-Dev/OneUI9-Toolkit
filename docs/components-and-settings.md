# OneUI Design: component catalogue

Current public components in this **revilodev** fork. Original library by **Yanndroid** and **BlackMesa123**; see [LICENSE](../LICENSE). Start with the [README](../README.md) for source integration and the [wiki](wiki.md) for current behavior and complete recipes.

## Appearance preferences

**Use system color palette** (`global_use_color_palette`, default `true`) preserves native device/SESL theming. Turn it off to use saved custom colors. There is no additional wallpaper overlay. Each component can opt out with `app:usesColorPalette="false"`; custom colors remain editable while the global switch is on. Unset roles use the light/dark defaults. Recreate the activity after changing palette mode or saved colors.

| Role | Use | Default source |
| --- | --- | --- |
| Primary | Checked tracks, active sliders and other active controls | `oui_slider_active_color` |
| Secondary | Secondary accent and progress (not subtitle text) | `OneUIPalette.SECONDARY` |
| Background | Page/window backgrounds; toggle thumbs | `oui_round_and_bgcolor` (black dark, `#F6F6F6` light) |
| Foreground | Main text and general icons | `oui_primary_text_color` |
| Surface | Cards and content groups | `oui_surface_color` (`#171717` dark, white light) |
| Surface variant | Secondary panels and navigation base | `oui_ai_secondary_surface` |
| Outline | Inactive tracks and outlines | `oui_slider_track_color` |
| Error | Warning/error accents | `oui_functional_red_color` |

The navigation selection is independent of Primary: 30% white in dark mode, solid white in light mode. Background tags must distinguish `oneui.palette.background` from `oneui.palette.surface`. Explicit supported component color attributes/setters override their defaults. Image content retains its own colors.

| Preference key | Default | Purpose |
| --- | --- | --- |
| `dark_mode`, `dark_mode_auto` | Follow system | Sample light/dark controls |
| `global_use_color_palette` | true | Native device colors or custom palette |
| `global_blur_enabled` | true | Global gate for participating blur/fades |
| `global_blur_intensity` | 18 | Continuous 0-40 dp radius |
| `global_blur_alpha` | 65 | Tint opacity 0-100%, stepped by 5 in Preferences |
| `oneui_palette_<role>_<light/dark>` | Unset | Separate custom role values per mode |

`global_material_color_palette` is obsolete and ignored. The [palette guide](color-palette-and-blur.md) describes capture behavior and the [wiki](wiki.md) documents per-component styling and syntax.

## Component catalog

The names below are the library's public views, layouts, dialogs, and preferences. `internal` classes and general utilities are implementation details rather than standalone UI components.

| Component | Purpose | Key settings / entry points |
| --- | --- | --- |
| `ToolbarLayout` | Expanding page header, navigation, actions, search, sticky footer | `expandable`, `expanded`, `title`, `subtitle`, `stickyTitleEnabled`, `stickyActionsEnabled`, `stickyActionBlurEnabled`; `setStickyBottomBar`, `showSearchMode`, `getFloatingSearchBar` |
| `DrawerLayout` | Toolbar page with slide-out drawer | `pushContentOnOpen`; `setDrawerCornerRadius`, `setDrawerOpen`, `setDrawerButtonBadge` |
| `AppInfoLayout` | App information and update/status page | `setTitle`, `setStatus`, `setMainButtonClickListener` |
| `SplashLayout` | Static or animated splash content | `animated`, `title`, `image`, `foreground_image`, `background_image`, `animation` |
| `SwitchBarLayout` | Wide settings switch bar | Add to a settings screen as a layout |
| `FloatingNavigationBar` | Floating destination pill, More grid, and optional end action | `navigationMenu`, `showLabels`, `blurEnabled`, `actionIcon`; `bindPages`, `setSelectedIcon`, `setAction`, `setSurfaceColor`, `setSelectedColor` |
| `NavigationPageContainer` | Pages selected by a floating navigation bar | Match child IDs to menu IDs; `setSwipeEnabled`, `setOnSwipeListener` |
| `FloatingToolbar` | Up to five contextual menu actions | `toolbarMenu`, `toolbarShowLabels`, `toolbarLightSurface`; `setBlurEnabled`, `setOnActionClickListener` |
| `FloatingActionBar` | Cancel/confirm bottom bar | `setLabels`, `setOnCancelClickListener`, `setOnConfirmClickListener`, `setLightSurface` |
| `FloatingBarSwitcher` | Replaces a navigation pill with a contextual bar | `setNavigationBar`, `showBar`, `showNavigation`, `setBackdropView`, `setAnimationsEnabled` |
| `FloatingSearchBar` | Floating or persistent search field | `searchPersistent`, `searchAiMode`; `setListener`, `setQuery`, `setHint`, `setSourceView`, `setCloseButtonVisible` |
| `FloatingScrollToTopButton` | Floating return-to-top control | `bind(recyclerView)` |
| `GlassSurfaceView` | Rounded sampled glass backdrop | `setSourceView`, `setCaptureWindow`, `setBlurEnabled`, `setBlurSampleScale`, `setTintColor`, `setCornerRadius` |
| `OneUISlider` | Rounded slider with steps, warning range, vertical mode | `minValue`, `maxValue`, `sliderValue`, `stepSize`, `showTicks`, `sliderVertical`, `sliderFillEnabled`, `sliderHapticEnabled`, `sliderWarningValue`, color attributes |
| `OneUIThickSlider` | Larger volume/brightness style slider | `thickValue`, `thickMinValue`, `thickMaxValue`, `thickVertical`, `thickWarningValue`, `thickIcon`, `thickShowNumber`, `thickShowMenuButton`, color attributes |
| `HapticSeekBar` | Seek bar with haptic ticks | `setTickMark`, `setOnSeekBarChangeListener` |
| `MarginsTabLayout` | Tab layout with One UI spacing | Use as a tab layout; enable/disable with `setEnabled` |
| `IconRowView` | List/card row with icon, subtitle, optional expansion | `setTitle`, `setSubtitle`, `setIconResource`, `setIconBackgroundColor`, `setExpandedText` |
| `ImageTileView` | Image or gradient grid tile | `setTitle`, `setSubtitle`, `setGradientColors`, `setImageUri`, `setImagePath`, `setCornerRadiusDp` |
| `RelatedCard` | Card of related actions or links | `title` attribute; add buttons through the card API |
| `RoundFrameLayout`, `RoundLinearLayout` | Rounded containers with optional surface shadows | `roundedCorners`, `surfaceShadowEnabled`, `surfaceShadowElevation` |
| `Separator` | One UI divider | Place between rows or sections |
| `ProgressiveBlurLayout` | Top/bottom scroll edge color fades | `blurHeight`, `blurTop`, `blurBottom`, `fadeColor`; `setFadeHeights`, `setBlurEdges` |
| `GridMenuDialog` | Rounded grid of menu actions | `setCardBlurEnabled`; populate its grid actions |
| `ProgressDialog` | One UI progress dialog | Standard dialog configuration |
| `StartEndTimePickerDialog` | Start and end time selection | Standard dialog configuration |
| `TipPopup`, `Toast` | Context tip and themed toast | Anchor/message/action for tip; `Toast.makeText` |
| `ColorPickerPreference` | Saved color with preview and picker dialog | `android:key`, `android:defaultValue`, `showAlphaSlider` |
| `OneUISliderPreference` | Saved `OneUISlider` in a preference row | Slider range, steps, ticks, fill XML attributes |
| `SeekBarPreferencePro` | SESL seek bar preference variants | `seekBarMode`, `seekBarSeamless`, `showTickMark`, `overlap`, `units` |
| `HorizontalRadioPreference` | Horizontal mode choice with optional images | `entries`, `entryValues`, `entriesImage`, `entriesSubtitle`, `viewType` |
| `SwitchBarPreference` | Full-width switch preference | Add as a preference row |
| `TipsCardPreference` | Dismissible tip card with actions | Title, summary, `addButton` |
| `DescriptionPreference` | Informational/linked preference text | `positionMode`, `roundedCorners`, optional custom layout |
| `InsetPreferenceCategory` | Category spacer and rounded grouping | `height`, `roundedCorners` |
| `LayoutPreference` | Embeds a custom layout in preferences | `android:layout` |

### Supporting classes

| Class | Role |
| --- | --- |
| `BlurSettings`, `MaterialColorSettings`, `OneUIPalette` | App-wide appearance state and palette resolution |
| `DialogBlur`, `ScrollEdgeFades`, `StickyToolbarControls`, `SurfaceShadow`, `TopActionSurfaceDrawable` | Dialog, scroll, toolbar, and surface behavior |
| `FlowHeaderBehavior`, `FullBleedScrollingBehavior` | CoordinatorLayout scroll behaviors |
| `QREncoder` | QR bitmap generation |
| `ActivityUtils`, `DialogUtils`, `IndexScrollUtils`, `PreferenceUtils`, `SeekBarUtils`, `TabLayoutUtils`, `ViewUtils` | Integration helpers |

The sample has `CardView`, `ActionCardView`, `CarouselView`, and `ShortcutGridView` in its own module. The sample opens at Widgets; search and the drawer include Widgets, Progress bars, Now Bar, Seek bars, Swipe refresh, Preferences, Navigation, Toolbar, App picker, Contacts, Pickers, QR code, and Icons.

## Common recipes

### Floating navigation with page swipes

```java
FloatingNavigationBar bar = findViewById(R.id.floating_nav);
NavigationPageContainer pages = findViewById(R.id.pages);
bar.bindPages(pages); // Each page ID matches a menu item ID.
bar.setPageSwipingEnabled(true);
bar.setBlurEnabled(true);
bar.setSelectedIcon(R.id.nav_grid, R.drawable.nav_grid_filled);
```

Keep bottom space below the page contents so a floating bar does not cover the last item. Up to three destinations show directly; a fourth slot opens More when there are additional items. `setOnItemSelectedListener` handles a menu item without a matching page. `setShowLabels`, `setShowIcons`, `setItemWidth`, `setBarHeight`, and `setAnimationsEnabled` adjust density and motion.

### A stepped slider with a warning range

```xml
<dev.oneuiproject.oneui.widget.OneUISlider
    android:layout_width="match_parent"
    android:layout_height="48dp"
    app:minValue="0"
    app:maxValue="100"
    app:stepSize="10"
    app:showTicks="true"
    app:sliderWarningValue="80" />
```

`OneUISliderPreference` persists the same range and step settings inside a preference screen. A per-view `sliderActiveColor` or `setActiveColor` overrides the global Primary role.

### Sticky header actions and search

```java
ToolbarLayout layout = findViewById(R.id.toolbar_layout);
layout.setStickyActionsEnabled(true);
layout.setStickyActionBlurEnabled(true);
layout.setStickyTitleEnabled(true);
layout.getFloatingSearchBar().setHint("Search items");
layout.setSearchModeListener(listener);
layout.showSearchMode();
```

The toolbar pins its navigation and menu controls as the large header collapses. `getStickyToolbarControls()` exposes icon width, padding, corner radius, insets, and collapse thresholds. `setStickyBottomBar(bar)` keeps a floating footer fixed while page content scrolls.

`PalettePreferenceGroupAdapter` applies Surface to native/recycled preference rows. Chrome labels and icons use Foreground; subtitles and summaries use `OneUIPalette.secondaryText(context)`. Switch tracks are 24 dp tall; the ON/OFF row uses the same standard toggle. Dialogs use shared blur with neutral action labels. Scroll fades stay in the fixed parent overlay.

### Theme changes in your own app

Use the same preference keys shown in `sample3_preferences.xml`, or call the appearance APIs directly. Place `MaterialColorSettings.applyTheme(this)` before view inflation in each activity. For XML pages that use the provided One UI colors as literal resources, mark palette backgrounds with `android:tag="oneui.palette.background"` or `oneui.palette.surface` and call `OneUIPalette.applyToViewTree(root)` after inflation. Recreate the activity after palette or system color mode changes. For fully custom drawables and assets, resolve the palette role in code when constructing them.

The [small settings page example](settings-page-example.md) shows a shorter preference layout. The [README](../README.md) covers installation and the sample app.
