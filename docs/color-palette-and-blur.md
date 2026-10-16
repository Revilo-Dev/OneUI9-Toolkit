# Color palettes and glass effects

The **Use system color palette** preference is enabled by default. It preserves
the native Samsung/device theme, including the behavior previously shown with
the material switch off. It does not apply an additional Android Monet overlay.
Turning it off selects the custom light/dark colors in Preferences. Unset roles
use the library defaults. The obsolete `global_material_color_palette` value is
intentionally ignored so existing installations keep the native default.

Every library view accepts `app:usesColorPalette="false"` to use custom colors
even while the global system palette is enabled. The default is `true`; children
inherit the component's choice. Explicit component color setters still take
priority. For example:

```xml
<dev.oneuiproject.oneui.widget.FloatingNavigationBar
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:usesColorPalette="false"
    app:navigationMenu="@menu/navigation" />
```

`OneUIPalette.setUsesColorPalette(view, false)` changes a component and its child
controls at runtime. Programmatically created standard Android controls can use
`OneUIPalette.wrap(context, null)` as their context. Call
`OneUIPalette.applyToViewTree(root)` after inflating standard controls. The sample
activities use `PaletteInflater.create(activity, originalInflater)` so standard
XML controls also accept the attribute. Apply the tree again when recycled or
deferred children are added; unchanged views are cached.

Primary colors drive active controls; navigation uses a neutral white selection. Chrome icons and labels use Foreground. Subtitles use secondary text, independently of the Secondary accent used for secondary progress; outline colors drive inactive tracks and controls.
Background, foreground, surface, surface variant, and error roles are shared by
the custom widgets. Image captions retain their contrast with the image content.

Glass uses a live backdrop. Preferences contains a continuous **Blur intensity**
slider (0–40 dp, default 18) and a stepped **Glass opacity** slider (0–100% in 5%
steps, default 65%). Opacity controls the tint over the blurred background.
Floating bars, toolbar controls, the drawer, sliders, and dialog blur share the
intensity setting. Disabling blur uses a solid fallback surface. Window-backed
captures retain the previous frame until the next frame is ready.

On Android 12+, toolbar and drawer backdrops reuse hardware display lists and
GPU blur. Older devices use cached, downsampled captures. Backdrop changes,
scrolling, and movement trigger updates; idle surfaces do not poll or redraw.
An intensity of zero skips toolbar/drawer capture. The light thick slider uses its gray Outline track even when blur is enabled; an explicit thick track color also uses a solid track.

The toolbar keeps the same icon layout on both sides of the scroll threshold.
Only the rounded backgrounds and their shadows fade over 180 ms; reversing
scroll direction reverses the fade from its current opacity.

## Backgrounds and surfaces

Custom defaults use `#000000` for dark Background and `#171717` for dark Surface. Light defaults are `#F6F6F6` and white respectively. Card containers use `oui_surface_color` / `oneui.palette.surface`; page and window backgrounds use Background. Changing one role must not flatten the other. Native device mode continues to resolve device theme attributes.

Toggles retain SwitchCompat input and accessibility, with a 18 dp circular thumb inside a 24 dp track. The thumb uses Background; the checked track uses Primary at full opacity. Light card shadows default to 0.5 dp with low shadow alpha. These defaults can be overridden per surface.

Toolbar button geometry and outlines remain fixed throughout the background fade. Both the native overflow wrapper and its inner image button use round feedback; the platform focus highlight is replaced with that same circle; the NEW badge sits at the outer top/end corner. Navigation removes the separate press-circle animation, leaves room around the selected pill, and uses 30% white selection in dark mode and white in light mode. The More grid slides up 24 dp while fading over 180 ms, beginning after its first layout.

See the [wiki](wiki.md) for the complete behavior contract and component recipes.

The base theme declares Background and material Surface separately in both modes. `PalettePreferenceGroupAdapter` applies Surface to native settings rows on every bind, including recycling. Dialogs use shared blur/tint settings and Foreground action labels. Scroll edge fades render in the stationary parent viewport overlay so they stay pinned during fast scrolling.

## Device checks

Build the sample and checks with:

```text
gradlew :sample-app:assembleDebug :sample-app:assembleDebugAndroidTest
```

After installing both debug APKs on an emulator:

```text
adb shell am instrument -w com.sec.sesl.tester.test/dev.oneuiproject.oneui.widget.ComponentBehaviorChecks
```

The checks cover shared/custom control colors, component opt-outs, explicit
color overrides, blur edge behavior, toolbar surface alpha, and icon geometry
while scrolling. They also verify transparent space between controls, circular
feedback, idle layout stability, and backdrop caching. Preferences are
restored after the checks.
