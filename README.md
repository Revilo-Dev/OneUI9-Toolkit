# OneUI Design · revilodev fork

Android One UI components, layouts, dialogs and preferences, with a runnable component gallery. Maintained by **revilodev**, based on [OneUIProject/oneui-design](https://github.com/OneUIProject/oneui-design) by **Yanndroid** and **BlackMesa123**. Original credits and copyright remain in [LICENSE](LICENSE).

**[Component wiki](docs/wiki.md)** · **[Component catalogue](docs/components-and-settings.md)** · **[Palette and blur](docs/color-palette-and-blur.md)** · **[Settings example](docs/settings-page-example.md)**

## What is included

- Widgets, progress bars, Now Bar notifications, seek bars, swipe refresh, Preferences, Navigation, Toolbar, app picker, Contacts, date/time/color pickers, QR codes and icons.
- Collapsing headers with fixed icon positions. Rounded action backgrounds fade in behind the icons without moving them. The header remains transparent between controls.
- Live frosted drawer and floating surfaces, shared blur intensity and glass opacity controls, and solid fallbacks when blur is off.
- Device color palette support, independent custom light/dark colors, and `usesColorPalette` on individual components.
- Continuous, stepped, vertical, warning-range and thick sliders; a smaller toggle thumb contained inside its track.
- Floating navigation with page swipes and a More grid; floating toolbars, search and contextual Cancel/Save actions.
- Rounded cards, icon rows, image tiles, scroll-to-top controls and preference components.

The sample starts at Widgets and creates demo pages when opened. Search and the drawer use the same page list. The sample-only carousel, action card and shortcut grid classes live in `sample-app`; they are separate from the library API.

## Build and run

Open the repository in Android Studio with Android SDK 36 and a compatible JDK. This checkout was built with **JDK 21**, **Gradle 9.5.1** and **Android Gradle Plugin 9.3.1**. Versions are centralized in [manifest.gradle](manifest.gradle). The library supports **API 23+**; the sample compiles against **36** and targets **33**. These values describe the current source, rather than a completed migration to a newer Android target.

```shell
# Windows: use gradlew.bat in place of ./gradlew.
./gradlew :sample-app:assembleDebug
./gradlew :sample-app:installDebug
./gradlew :lib:assembleRelease
```

The sample APK is written to `sample-app/build/outputs/apk/debug/sample-app-debug.apk`. The library AAR is written under `lib/build/outputs/aar/`. The [bundled release APK](sample-app/release/sample-app-release.apk) and old screenshots are historical snapshots; build the current source for these changes.

After dependencies have been downloaded, add `--offline` to build from the local cache. The legacy SESL picker artifacts currently share a namespace; the compatibility settings in [gradle.properties](gradle.properties) are intentional.

## Use this fork in an app

For the exact APIs in this checkout, include the `lib` module in your project and depend on it:

```groovy
// settings.gradle, after copying the lib directory into your project
include ':app', ':lib'

// app/build.gradle
dependencies {
    implementation project(':lib')
}
```

Keep the SESL dependency versions aligned with [manifest.gradle](manifest.gradle). This library uses the OneUIProject SESL variants of AppCompat and Material. The upstream Maven coordinate `io.github.oneuiproject:design:<version>` is available separately; it does not automatically contain this fork's unpublished changes.

Apply the theme:

```xml
<application android:theme="@style/OneUITheme" ... />
```

Apply appearance settings before inflating each activity. Use `PaletteInflater` when standard XML controls should also read `usesColorPalette`:

```java
private LayoutInflater paletteInflater;

@Override public LayoutInflater getLayoutInflater() {
    if (paletteInflater == null) {
        paletteInflater = PaletteInflater.create(this, super.getLayoutInflater());
    }
    return paletteInflater;
}

@Override protected void onCreate(Bundle state) {
    super.onCreate(state);
    MaterialColorSettings.applyTheme(this);
    View content = getLayoutInflater().inflate(R.layout.screen, null);
    setContentView(content);
    OneUIPalette.applyToViewTree(content);
}
```

Import these helpers from `dev.oneuiproject.oneui.widget`. Reapply the tree when adding recycled or deferred standard controls; unchanged views are cached. The sample activities provide complete working examples. Recreate the activity after changing the system palette switch or saved palette colors so XML resources and native controls reload together.

## Component syntax

Declare `xmlns:app="http://schemas.android.com/apk/res-auto"` on the layout root. Library views use their full Java names in XML:

```xml
<dev.oneuiproject.oneui.widget.OneUISlider
    android:id="@+id/volume"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:contentDescription="Volume"
    app:minValue="0"
    app:maxValue="100"
    app:sliderValue="50"
    app:stepSize="10"
    app:showTicks="true"
    app:usesColorPalette="false" />
```

```java
OneUISlider volume = findViewById(R.id.volume);
volume.setOnValueChangeListener((slider, value, fromUser) -> updateVolume(value));
```

For a continuous slider, omit `stepSize` and `showTicks`. `OneUIThickSlider` uses the `thick…` attributes, such as `thickValue`, `thickVertical` and `thickShowNumber`. Its light-mode track is gray. Explicit `sliderActiveColor`, `thickTrackColor`, and matching setters override the corresponding default color.

```xml
<dev.oneuiproject.oneui.widget.FloatingNavigationBar
    android:id="@+id/navigation"
    android:layout_width="wrap_content"
    android:layout_height="60dp"
    app:navigationMenu="@menu/navigation"
    app:showLabels="true"
    app:blurEnabled="true" />
```

```java
navigation.bindPages(findViewById(R.id.pages)); // Page IDs match menu item IDs.
navigation.setBackdropView(findViewById(R.id.pages));
navigation.setOnItemSelectedListener(id -> onDestinationChanged(id));
```

Navigation shows up to three direct destinations and a More button when there are four or more items. Its selection is white at 30% opacity in dark mode and solid white in light mode. More opens with a short upward slide and fade. See the [wiki recipes](docs/wiki.md#component-recipes) for menus, cards, thick sliders, search, toolbars and preferences.

## Colors and blur

**Use system color palette** is on by default and retains the device's native SESL/OS colors. Turning it off uses the saved custom palette, falling back to defaults for unset roles. It does not install an extra wallpaper-color overlay. The previous material switch's off appearance is preserved as the new system-enabled behavior.

Each component defaults to `app:usesColorPalette="true"`. Set it to `false` to use custom colors even while the global system setting is on. In Java, call `OneUIPalette.setUsesColorPalette(view, false)`. Programmatic standard controls can be constructed with `OneUIPalette.wrap(context, null)`.

| Role | Custom default / purpose |
| --- | --- |
| Background | Pure black in dark mode, `#F6F6F6` in light mode; page/window background |
| Surface | `#171717` dark, white light; cards and content groups |
| Primary | Active controls, checked toggle track and slider fill |
| Secondary | Secondary accent and secondary progress; independent of secondary text |
| Foreground | Main text and icons |
| Surface variant | Secondary surfaces and navigation base |
| Outline | Inactive controls and slider tracks |
| Error | Warning/error accents |

Toggle thumbs use Background. Tracks are 24 dp tall with an 18 dp thumb; the ON/OFF preference uses the same toggle and standard row sizing. Cards use Surface independently of Background. Saved values matching the previous defaults migrate to these corrected defaults; other custom colors are retained. Light cards have a subtle default shadow; `surfaceShadowEnabled` and `surfaceShadowElevation` remain available per card. Navigation's neutral selection is independent of Primary. Chrome icons and labels use Foreground, and subtitles use `OneUIPalette.secondaryText(context)` rather than the Secondary accent. Native Preferences rows use `PalettePreferenceGroupAdapter` to retain Surface after recycling.

```java
MaterialColorSettings.setEnabled(context, false);
OneUIPalette.set(context, OneUIPalette.PRIMARY, true, 0xff438aff); // dark palette
int surface = OneUIPalette.get(context, OneUIPalette.SURFACE);
OneUIPalette.reset(context, true); // reset dark mode only
BlurSettings.setEnabled(context, true);
```

Preferences exposes continuous **Blur intensity** (0–40 dp, default 18) and stepped **Glass opacity** (0–100%, steps of 5, default 65%). The keys are `global_blur_intensity` and `global_blur_alpha`; `global_blur_enabled` controls the global switch. Modern toolbar/drawer surfaces use GPU blur; older versions use cached downsampled captures. Scroll edge fades are themed gradients drawn in the stationary parent overlay, so they stay pinned during fast flings. Dialog action labels use Foreground and dialog surfaces use shared blur/opacity settings. API support and implementation limits are detailed in the [palette guide](docs/color-palette-and-blur.md).

## Repository map and checks

| Location | Contents |
| --- | --- |
| `lib/src/main/java/dev/oneuiproject/oneui` | Public layouts, widgets, dialogs, preferences and utilities |
| `lib/src/main/res` | Themes, colors, drawables and XML attributes |
| `sample-app/src/main` | Component demos and app appearance settings |
| `sample-app/src/androidTest` | Device component behavior and rendering checks |
| `docs/wiki.md` | Current behavior contract, usage recipes and maintenance notes |
| `docs/one-ui-9-habit-app-rulebook.md` | Separate design research and proposed habit-app guidance |

```shell
./gradlew :sample-app:assembleDebug :sample-app:assembleDebugAndroidTest
adb install -r sample-app/build/outputs/apk/debug/sample-app-debug.apk
adb install -r sample-app/build/outputs/apk/androidTest/debug/sample-app-debug-androidTest.apk
adb shell am instrument -w com.sec.sesl.tester.test/dev.oneuiproject.oneui.widget.ComponentBehaviorChecks
./gradlew :lib:lintDebug
```

The instrumentation runner restores appearance preferences after checking control colors, light/dark rendering, toggle geometry, toolbar stability, navigation sizing, blur caching and recreation. Lint currently reports legacy issues, including API-level and restricted SESL API usage; a successful APK build is not a claim that lint is clean. Use a Samsung device for final OEM hover/blur verification as well as an emulator.

Formatting defaults are defined in [.editorconfig](.editorconfig); [.gitattributes](.gitattributes) keeps source line endings consistent. Build outputs remain ignored. Do not commit local SDK paths, generated captures or signing credentials.

## Credits and license

[MIT license](LICENSE). Thanks to the original OneUIProject contributors, Samsung for One UI, and Google for Android, Jetpack and Material Components. See [upstream documentation](https://oneuiproject.github.io/design/) for the original APIs; this fork's current behavior is documented locally above.
