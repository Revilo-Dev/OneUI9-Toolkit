# One UI 9 habit app rulebook

Research checked: **15 October 2026**. Scope: Android phone, tablet, foldable, split screen, and Samsung DeX.

This is a practical specification for bringing a habit app into line with One UI. Samsung's current consumer page describes One UI 9, but its public developer component guidelines are not versioned as a complete One UI 9 specification. Use the documented principles below as the baseline and verify the final appearance on a device running the intended One UI 9 build. Exact new Samsung dimensions, blur strengths, fonts, and animation curves must not be inferred from a marketing image. [Samsung One UI 9 overview](https://www.samsung.com/us/apps/one-ui/), [Samsung developer guidelines](https://developer.samsung.com/one-ui/index.html).

The workspace inspected for this guide contains the **OneUI Design library and sample app**, rather than the habit app itself. The integration notes describe the current local source; they are not an audit of an unseen habit app or a promise about a published library release.

## How to read the rules

| Label | Meaning |
| --- | --- |
| **S** | A rule or recommendation in Samsung's published One UI guidance; not necessarily new in version 9. |
| **A** | Android platform behavior or Android developer guidance. Apply the documented OS and target SDK conditions. |
| **H** | A recommended product rule for this habit app. Adopt it consistently; it is not an official Samsung requirement. |
| **R** | A fact about the library source currently in this workspace. |

Treat H rules as the proposed design contract. They can be adjusted for the actual habit app, but changes should be explicit and consistent across pages.

## Contents

1. [Foundation and sizing](#1-foundation-and-sizing)
2. [Page structure](#2-page-structure)
3. [Headers and collapsing app bars](#3-headers-and-collapsing-app-bars)
4. [Action bars and primary actions](#4-action-bars-and-primary-actions)
5. [Navigation and tabs](#5-navigation-and-tabs)
6. [Lists, cards, and habit rows](#6-lists-cards-and-habit-rows)
7. [Buttons and selection controls](#7-buttons-and-selection-controls)
8. [Dropdowns, overflow, and context menus](#8-dropdowns-overflow-and-context-menus)
9. [Dialogs and bottom sheets](#9-dialogs-and-bottom-sheets)
10. [Forms, keyboards, and validation](#10-forms-keyboards-and-validation)
11. [Dates, schedules, pickers, and sliders](#11-dates-schedules-pickers-and-sliders)
12. [Search and filtering](#12-search-and-filtering)
13. [Gestures, scrolling, Back, and Up](#13-gestures-scrolling-back-and-up)
14. [Material theming and color](#14-material-theming-and-color)
15. [Blur, glass, depth, and system bars](#15-blur-glass-depth-and-system-bars)
16. [Typography, icons, and accessibility](#16-typography-icons-and-accessibility)
17. [Motion, haptics, and sound](#17-motion-haptics-and-sound)
18. [Loading, empty, error, and offline states](#18-loading-empty-error-and-offline-states)
19. [Adaptive layouts and foldables](#19-adaptive-layouts-and-foldables)
20. [Settings and appearance](#20-settings-and-appearance)
21. [Habit app page specifications](#21-habit-app-page-specifications)
22. [Reminders, notifications, and widgets](#22-reminders-notifications-and-widgets)
23. [Mapping to this library](#23-mapping-to-this-library)
24. [Migration order](#24-migration-order)
25. [Acceptance checklist](#25-acceptance-checklist)

## 1. Foundation and sizing

- **BASE01 [S]** Put glanceable information in the upper viewing area and frequent controls in the lower interaction area. This is a reachability principle, not a requirement to leave half of every screen empty. [Samsung basic layout](https://developer.samsung.com/one-ui/layout/basic.html).
- **BASE02 [H]** Make today's check-in the fastest journey: open Today, find a due habit, record it, and see the result without opening an editor.
- **BASE03 [H]** Use one coherent component family across the app. Record exceptions for a chart or custom check-in control instead of letting each screen invent its own controls.
- **BASE04 [H]** Use density-independent units for layout and scalable units for text. Let components grow when translations or user text settings need more space.
- **BASE05 [S]** Samsung's grid guidance calls for at least 24 dp of left and right margin around information and interactive components. Treat full-bleed decoration separately from the content keyline. [Samsung grid system](https://developer.samsung.com/one-ui/layout/grid.html).
- **BASE06 [H]** Establish shared sizing tokens. The following are starting values for this app, not verified One UI 9 measurements. Prefer a compatible library component's own dimensions where they differ.

| Token | Proposed baseline | Adaptation rule |
| --- | --- | --- |
| Compact content keyline | 24 dp each side | Increase or center the content column in wide windows. |
| Spacing scale | 4, 8, 12, 16, 24, 32 dp | Choose from the scale; avoid unrelated values on every page. |
| Interactive target | At least 48 × 48 dp | A visible icon can be smaller; its target must remain large. |
| Toolbar/control bar | Start around 56 dp | Use intrinsic height and grow for text; system insets are additional. |
| Single-line habit row | Start around 64 dp | Increase for schedule, progress, large text, and multiple controls. |
| Grouped surface radius | Start around 24 dp | Keep one shape family; avoid clipping focus rings and ripples. |
| Dialog/sheet corners | Use component defaults | Change through a shared style, not per-dialog overrides. |
| Expanded page title | Start around 32 sp | Allow wrapping and compact alternatives in short windows. |
| Compact title / body | Start around 20 sp / 16 sp | Check with the active system font and 200% text. |
| Supporting text | Start around 14 sp | Keep sufficient contrast and allow wrapping. |
| Overlay clearance | Measured overlay height + gap | Include the applicable system/keyboard inset once. |

## 2. Page structure

- **PAGE01 [H]** Define a page shell: window insets, header, scrollable content, optional fixed actions, and navigation. Give each region one owner so padding and overlays do not accumulate.
- **PAGE02 [S]** Group related content into focus blocks, such as grouped lists, cards, or a single prominent item. Use spacing and calm surfaces to make groups easy to scan. [Samsung basic layout](https://developer.samsung.com/one-ui/layout/basic.html).
- **PAGE03 [H]** Use a large header on primary landing pages when space allows. Use a compact header for editors, dense history, focused searches, and short landscape windows.
- **PAGE04 [H]** Keep one primary vertical scroll container per pane. A habit list inside a separately scrolling dashboard should have a clear reason and tested gesture behavior.
- **PAGE05 [H]** Floating bars may cover the decorative background, but every final list item and its controls must scroll fully above them.
- **PAGE06 [H]** Preserve the page's date, filters, selected habit, scroll position, and header state when returning from a detail page or system screen.
- **PAGE07 [H]** Give every destination a complete empty, loading, error, and populated layout. Do not leave an unexplained blank panel.
- **PAGE08 [H]** Keep app navigation, page commands, filtering, and data entry visually distinct. Each should answer a different question: where am I, what can I do, what am I viewing, or what am I entering?

## 3. Headers and collapsing app bars

Samsung documents both extended and condensed app bars. The extended form makes controls easier to reach; it condenses as content scrolls and expands on the return scroll. It also recommends preserving the previous bar state when the user returns. [Samsung app bars](https://developer.samsung.com/one-ui/comp/app-bar.html).

- **HEAD01 [S]** Use the expanded title centrally within the viewing area, with supporting information beneath it.
- **HEAD02 [S]** A useful dynamic title can replace the page name; keep its supporting actions to at most two.
- **HEAD03 [S]** Keep app bar actions optional and limited; Samsung recommends no more than three. Put additional commands in More.
- **HEAD04 [H]** Today should use the stable title **Today**, with the date and completion summary below. A changing motivational sentence should not make the page identity disappear.
- **HEAD05 [H]** Treat expansion and collapse as states of the same header. Coordinate the large and compact titles so they do not overlap or announce the same heading twice.
- **HEAD06 [H]** Make title visibility an explicit policy. For this habit app, keep the compact title visible once the large title collapses, so users retain their date/page context.
- **HEAD07 [H]** Keep navigation and essential actions available in the collapsed state. Do not let a decorative header transition remove access to Search or More.
- **HEAD08 [H]** Size the expanded region from available height. Short landscape windows, a visible keyboard, or large text should produce a smaller or compact header.
- **HEAD09 [H]** Do not require a full scroll to reach a primary command. Place frequent check-in controls in the content and editor confirmation in a reachable action region.
- **HEAD10 [H]** Restore collapse position together with list position. Reopening Habit detail must not unexpectedly move the list or expand Today above the user's previous location.
- **HEAD11 [H]** Sticky section headings stop below the app bar's safe content boundary. They must never cover navigation targets or the currently focused row.
- **HEAD12 [H]** Use a single navigation affordance at the leading edge: Up for a child page, Close for a modal editor, or a drawer button only if the app actually uses a drawer.

| Header state | Visible content | Action behavior | Verification |
| --- | --- | --- | --- |
| Expanded landing page | Large title, short summary, content beginning below | Main commands remain reachable | The first due habit is still discoverable. |
| Partially collapsed | One continuous title transition | Hit targets remain stable | No duplicated title, jumping actions, or intercepted list touches. |
| Collapsed | Compact title and app bar actions | Essential commands remain available | Status icons, title, and content remain legible together. |
| Search | Query, clear/close controls, result context | Search owns the active input mode | Back and keyboard dismissal do not destroy the previous list state. |
| Selection | Selected count and selection controls | Contextual commands replace normal commands | Leaving selection restores the prior header and navigation. |
| Editor / short window | Compact identity | Save/Cancel remain usable | Keyboard and large text leave the active field visible. |

## 4. Action bars and primary actions

An Android framework ActionBar, this library's floating action controls, and the system navigation bar are different things. Name them clearly in designs and implementation tasks.

| Surface | Job | Habit app example |
| --- | --- | --- |
| Top app bar | Page identity, Up, page-level commands | Today, Search, More. |
| Bottom destination navigation | Switch between peer destinations | Today, Habits, Insights. |
| Contextual bottom toolbar | Commands on selected items | Archive, Move to group, Delete. |
| Editor action bar | Commit or abandon a draft | Cancel and Save. |
| Primary action button | One prominent creation/start command | Add habit or Start session. |
| Android system navigation | Back, Home, Recents / system gestures | Owned by Android. |

- **ACT01 [S]** Bottom action bars are useful for operations on selected items. Samsung allows up to five labeled icon actions; if there are more, show four common actions and More. [Samsung bottom bar](https://developer.samsung.com/one-ui/comp/bottom-bar.html).
- **ACT02 [S]** Samsung's bottom toolbar guidance discourages a toolbar containing only one command. Use a single purpose button for a single action instead. [Samsung bottom bar](https://developer.samsung.com/one-ui/comp/bottom-bar.html).
- **ACT03 [H]** Give the page one dominant primary action. Today may have Add habit; a running timer may instead foreground its session controls.
- **ACT04 [H]** In selection mode, replace destination navigation with the contextual toolbar in the same bottom slot. Show the selected count and a clear exit.
- **ACT05 [H]** Use Save/Cancel only for a draft or transactional change. Ordinary check-ins and simple setting toggles should not require a second Save.
- **ACT06 [H]** Never hide Save, Delete confirmation, or timer Stop merely because the user scrolled. Any auto-hiding decorative bar must return predictably and remain accessible.
- **ACT07 [H]** Keep destructive commands separated from routine recording. Completing today's habit must not sit beside a visually identical Delete history action.
- **ACT08 [H]** Anchor snackbars and temporary feedback above the active bottom controls. Navigation, Save, a snackbar, and the keyboard must not stack on top of one another.
- **ACT09 [H]** Prevent duplicate commits while an action is in flight, and keep the command's purpose visible during progress.

## 5. Navigation and tabs

Samsung's published bottom navigation page specifies text tabs, generally fewer than four, with up to five if needed, and explicitly says users do **not** switch between them by swiping. This is the public baseline; it does not establish the exact styling of every One UI 9 system app. [Samsung bottom navigation](https://developer.samsung.com/one-ui/comp/bottom-navigation.html).

- **NAV01 [H]** Start with three top-level destinations: **Today**, **Habits**, and **Insights**. Put Settings in a consistent page command unless it becomes a frequently used destination.
- **NAV02 [H]** Disable horizontal swiping between top-level destinations. Keep any horizontal calendar gesture local to the calendar.
- **NAV03 [H]** Keep destination order, labels, and selection stable. A destination opens a place; Add habit opens an action and must not become a selected destination.
- **NAV04 [H]** If using the fork's floating icon-and-label navigation, record it as the chosen app variant. Do not describe that presentation or its More grid as an official One UI 9 requirement.
- **NAV05 [H]** Keep destination labels visible by default. Selected state needs more than a subtle color change; combine an indicator, weight, or filled icon with accessible selected semantics.
- **NAV06 [H]** Maintain a separate navigation/scroll state for each destination. Switching to Insights and back should restore Today's date and list.
- **NAV07 [H]** Use tabs inside a destination only for closely related views, such as Week and Month. Avoid two competing tab bars with unclear scope.
- **NAV08 [H]** Do not add a drawer solely to copy the toolkit demo. A small habit app does not need both a drawer and bottom navigation for the same destinations.
- **NAV09 [H]** Specify repeated-tap behavior. A second tap may scroll the current destination to the top, but must not reset filters, change dates, or discard a draft silently.
- **NAV10 [H]** Test notification and widget links into detail pages. Up must lead to a sensible parent even when the user did not enter through the main list.

## 6. Lists, cards, and habit rows

Samsung recommends scannable lists, clear short setting names, contextual subheaders, and a trailing switch for on/off settings. [Samsung lists](https://developer.samsung.com/one-ui/comp/list.html).

- **LIST01 [H]** A habit row should answer: which habit, when due, current progress, and what action is available now.
- **LIST02 [H]** Recommended structure: leading habit icon; name and schedule/progress; trailing check-in control. Keep streak details secondary.
- **LIST03 [H]** Make tapping the row open Habit detail and tapping its trailing control record progress. These targets must be independent, labeled, and non-overlapping.
- **LIST04 [H]** Match the recording control to the habit: checkbox for a binary completion, numeric entry or increment for a quantity, Start/Pause for a duration.
- **LIST05 [H]** Keep completed rows readable. A checkmark and Completed state communicate success more reliably than fading the whole row toward invisibility.
- **LIST06 [H]** Keep Not due, Skipped, Completed, Partial, and Missed distinct. Missing data must not look like a failed habit.
- **LIST07 [H]** Keep row geometry stable during recording. Avoid moving a just-completed habit beneath the user's finger; animate any regrouping after clear feedback.
- **LIST08 [H]** Use grouped rows for routine lists and settings. Reserve individual cards for content that benefits from its own summary, chart, or action grouping.
- **LIST09 [H]** Use one visual grouping system per section. Avoid wrapping every row in another card inside an already rounded card.
- **LIST10 [H]** Expand long names and supporting text rather than truncating the information needed to distinguish similar habits.
- **LIST11 [H]** Show progress with text as well as graphics: **2 of 3 glasses**, **15 of 20 minutes**. Expose the same meaning to accessibility services.
- **LIST12 [H]** Provide explicit Edit and Reorder paths. Long press can reveal shortcuts, but must not be the only way to discover them.

## 7. Buttons and selection controls

Samsung distinguishes flat actions from contained emphasis and recommends a consistent button style within a screen. Keep that consistency within each action group and document any primary/secondary hierarchy chosen for this app. [Samsung buttons](https://developer.samsung.com/one-ui/comp/button.html).

- **CTRL01 [H]** Label actions with a concrete verb: Add habit, Save, Archive, Delete history. Reserve Done for finishing an interaction without an ambiguous save operation.
- **CTRL02 [H]** Use a switch for an immediately applied on/off setting, such as Reminders. Do not use a switch as a navigation arrow or Save button.
- **CTRL03 [H]** Use checkboxes for independent choices and radio buttons for mutually exclusive choices. Use selected chips for compact filters, not as unlabeled switches.
- **CTRL04 [H]** A weekday control is multi-select unless the schedule explicitly permits one day only. Make its selected state and full day name accessible.
- **CTRL05 [H]** Give controls normal, pressed, focused, disabled, selected/checked, error, and loading states where applicable.
- **CTRL06 [H]** Disabled controls should have an understandable cause. If Save is unavailable, show what must be corrected; do not make the user guess from dim text.
- **CTRL07 [H]** Keep icon-only controls recognizable and provide both an accessibility name and a tooltip. A tooltip is a supplement to usable labeling.
- **CTRL08 [H]** Keep at least the proposed 48 dp target for check-in, plus/minus, close, and overflow controls. Expand the target without creating overlap with adjacent actions.
- **CTRL09 [H]** Use a determinate progress indicator when progress is measurable. Use indeterminate progress for an unknown duration, with a clear label describing the operation.
- **CTRL10 [H]** Reserve badges for meaningful counts or attention. Do not use alarming unread-style badges to pressure users about ordinary unfinished habits.

## 8. Dropdowns, overflow, and context menus

Samsung puts extra page commands in the app bar's More menu and uses checkboxes for toggled options there. Its dialog guidance also describes an untitled dropdown context menu on a long-pressed list item. [Samsung app bars](https://developer.samsung.com/one-ui/comp/app-bar.html), [Samsung dialogs](https://developer.samsung.com/one-ui/comp/dialog.html).

- **MENU01 [H]** Use an anchored popup for a short list of commands or a small single choice. Use a sheet or page when options need search, detailed explanations, or multiple dependent inputs.
- **MENU02 [H]** Open menus next to their invoking control and keep them inside the usable window. Recalculate position after resizing or keyboard changes.
- **MENU03 [H]** Distinguish a command menu from a form dropdown. A form dropdown needs a persistent field label and visible selected value.
- **MENU04 [H]** For Sort by, show one selected option. For independent Show completed / Show skipped options, show independent checked states.
- **MENU05 [H]** Group related commands and separate destructive ones. Delete habit should describe the consequence or lead to a confirmation with that explanation.
- **MENU06 [H]** Keep the menu closeable with Back, outside tap, and keyboard Escape where supported. Return focus to the invoking control after dismissal.
- **MENU07 [H]** Do not use a grid for a long list of text settings. A grid suits a small set of recognizable shortcuts; a list suits reading and comparing options.
- **MENU08 [H]** Choose one long-press behavior for a habit row: context menu or selection mode. If both exist, make selection an explicit command inside the menu.
- **MENU09 [H]** Avoid stacked menus and dialogs. Close the current popup before presenting a different modal surface, while preserving any draft.

## 9. Dialogs and bottom sheets

Samsung places choice/confirmation dialogs near the bottom and informational progress dialogs centrally. Decision dialogs remain until the user chooses or dismisses them with Back/outside interaction. Bottom placement does not by itself turn an alert dialog into a draggable bottom sheet. [Samsung dialogs](https://developer.samsung.com/one-ui/comp/dialog.html).

| Need | Recommended surface | Commit model |
| --- | --- | --- |
| Small command list | Anchored menu | Execute the chosen command. |
| Sort order | Menu or compact choice dialog | Apply immediately; no redundant Save. |
| Several filters | Sheet with explicit scope | Either live apply + Close, or draft + Apply/Cancel; choose one. |
| Confirm deletion with history loss | Bottom-positioned alert dialog | Delete only after the explicit destructive action. |
| Rename a habit | Small input dialog | Save/Cancel with field validation. |
| Create or substantially edit a habit | Full page / full-screen editor | Save/Cancel, preserve the draft. |
| Short local progress adjustment | Sheet or compact input dialog | Commit through a clearly named action. |
| Large icon/category collection | Searchable sheet or page | Preserve selected choice on return. |
| Long import/export | Progress region or dialog if blocking is necessary | Show status and safe cancellation where supported. |

- **DIALOG01 [H]** Use a modal only when the user must make a focused choice, confirm a consequential operation, or resolve a blocking issue.
- **DIALOG02 [H]** Give each confirmation a precise title and buttons. Example: **Delete Walking?** with **Cancel** and **Delete**; explain that completion history is also removed if that is true.
- **DIALOG03 [H]** Prefer reversible Archive for removing a habit from active use. Keep permanent deletion explicit and separate.
- **DIALOG04 [H]** Do not confirm every check-in. Provide direct feedback and an Undo path for reversible recording changes.
- **DIALOG05 [H]** Outside tap, Back, swipe-dismiss, and Close must have consistent cancellation semantics. None of them may silently commit an unfinished edit.
- **DIALOG06 [H]** If dismissing would lose meaningful input, preserve the draft or ask whether to discard. Avoid repeatedly asking when nothing changed.
- **DIALOG07 [H]** Long dialog content must scroll while actions remain reachable. At large text, stack actions or expand the surface instead of clipping labels.
- **DIALOG08 [H]** Use sheet drag handles only when the sheet actually supports dragging. Define expanded/collapsed states, nested scrolling, and keyboard behavior together.
- **DIALOG09 [H]** Let TalkBack enter the modal, announce its purpose, keep background controls unavailable, and return focus to the trigger when it closes.
- **DIALOG10 [H]** Show inline validation without dismissing the dialog. A failed rename must preserve the entered name.
- **DIALOG11 [H]** Keep one blocking app modal active at a time. Pickers opened from an editor must return to that editor with its draft intact.
- **DIALOG12 [H]** Use the platform permission dialog for system permissions. Keep any app explanation truthful and tied to the feature the user just requested.

## 10. Forms, keyboards, and validation

- **FORM01 [H]** Create habit in a focused editor: name, tracking type, goal, schedule, reminder, appearance, and optional notes. Hide advanced options until needed.
- **FORM02 [H]** Use persistent labels. A placeholder alone must not be the only explanation of a field after typing begins.
- **FORM03 [H]** Choose input types deliberately: text for a name, decimal/integer for measurable goals, appropriate IME Next/Done actions, and multiline behavior for notes.
- **FORM04 [H]** Keep the focused input, its error, and the relevant action above the keyboard. Remove or replace navigation that has no useful role during editing.
- **FORM05 [H]** Validate near the field and explain the repair: **Enter a goal above 0**, rather than **Invalid value**. Keep all valid input after a failed save.
- **FORM06 [H]** Do not report errors on untouched fields as soon as the page opens. Validate on meaningful interaction and on attempted submission.
- **FORM07 [H]** Preserve drafts across rotation, resizing, app switching, picker launches, and process recreation where practical.
- **FORM08 [H]** Define dirty-state handling once. Save commits, Cancel abandons or confirms discard, and Back follows the same draft policy after the keyboard has been handled.
- **FORM09 [H]** Keep units next to numeric values and announce them. Localize decimal parsing, number formatting, and examples.
- **FORM10 [H]** Provide manual input alternatives for gesture-heavy controls. A goal of 137 should not require dragging a slider precisely.

## 11. Dates, schedules, pickers, and sliders

- **PICK01 [H]** Use compatible date/time picker components and honor the device's 12/24-hour convention and locale.
- **PICK02 [H]** Make schedule types explicit: every day, selected weekdays, every N days, or a weekly target. Avoid making different recurrence models look interchangeable.
- **PICK03 [H]** Show a plain-language summary before saving: **Monday, Wednesday, Friday · 7:30 am**.
- **PICK04 [H]** Distinguish a habit's schedule, the date being viewed, and the date of a recorded entry. Editing one must not silently change another.
- **PICK05 [H]** Show the selected day, today marker, and recorded state using more than color. Offer date navigation buttons as an alternative to swiping.
- **PICK06 [H]** Use a slider for a bounded, approximate range. Use steps and a visible numeric value when the quantity is discrete; provide direct entry for precision.
- **PICK07 [H]** Slider accessible actions must change the value by a meaningful step and expose the current value, range, and units.
- **PICK08 [H]** Reserve warning ranges for a real product meaning. A red slider segment copied from a volume demo does not automatically mean a higher habit goal is dangerous.
- **PICK09 [H]** Specify travel/time-zone behavior for daily boundaries and reminders. Show the date being edited clearly when logging a past day.
- **PICK10 [H]** Define what changing a schedule or goal does to historical statistics. Explain the effect before a change that would reinterpret past results.

## 12. Search and filtering

Samsung search guidance encourages relevant suggestions, recent terms, autocomplete, and forgiving matching. [Samsung search](https://developer.samsung.com/one-ui/comp/search.html).

- **SEARCH01 [H]** Search habit names first; add categories and notes only when useful. Show the scope, especially when archived habits are included.
- **SEARCH02 [H]** Keep query text visible, provide Clear and Close, and update results without losing keyboard focus or jumping to the top on every keystroke.
- **SEARCH03 [H]** Treat Clear query and Exit search as separate actions. Decide whether the first Back hides the keyboard, then ensure another Back closes search predictably.
- **SEARCH04 [H]** Show active filters as visible selected controls and offer Clear filters. Filters must survive opening a result and returning.
- **SEARCH05 [H]** Distinguish **No habits yet**, **No results for “walk”**, and **No habits match these filters**. Provide the relevant action for each.
- **SEARCH06 [H]** Keep completion usable in results if it is available in the normal list. Do not assign the same trailing icon a different meaning in search.
- **SEARCH07 [H]** Announce useful result changes without reading the entire list after each character. Debounce expensive filtering without delaying typing feedback.
- **SEARCH08 [H]** Store recent queries only if useful and provide a clear-history option. Habit names and searches may contain personal information.

## 13. Gestures, scrolling, Back, and Up

- **GEST01 [H]** Keep vertical scrolling as the main content gesture. Do not let a horizontal page recognizer steal diagonal list scrolling or a slider interaction.
- **GEST02 [H]** A swipe on a habit row may expose a reversible shortcut, but provide the same command visibly or through a labeled menu. Do not make permanent deletion an immediate accidental swipe outcome.
- **GEST03 [H]** Use long press for the row behavior chosen in MENU08. Provide an explicit selection or edit path for people who cannot perform long presses reliably.
- **GEST04 [H]** Reordering needs a visible handle or explicit mode, clear start/drop feedback, and Move up/Move down alternatives.
- **GEST05 [H]** Pull to refresh should represent actual refresh work. A local habit list does not need a spinner simply because the user pulls downward at the top.
- **GEST06 [H]** Coordinate pull to refresh with expanded headers: one drag must not simultaneously expand the header, trigger refresh, and move a row.
- **GEST07 [A]** Leave system Back/Home gesture regions usable. Avoid placing app drag targets entirely within reserved gesture areas. [Android predictive Back design](https://developer.android.com/design/ui/mobile/guides/patterns/predictive-back).
- **GEST08 [A]** Use supported Back APIs such as AndroidX's dispatcher/callback system. Legacy `onBackPressed()` and intercepted Back key events are migration concerns, especially at newer target SDKs. [Android predictive Back implementation](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture), [Android 16 target behavior](https://developer.android.com/about/versions/16/behavior-changes-16).
- **GEST09 [H]** Back dismisses the nearest active layer: keyboard when it owns Back, then popup/search/selection, then a child screen, then the app's root behavior. Handle an unsaved editor before leaving it.
- **GEST10 [H]** Up navigates to the logical parent; system Back follows the current task/history. For ordinary in-app detail entry they may coincide, but deep links need a defined parent path.
- **GEST11 [H]** During a predictive Back preview, do not save, delete, or discard data. Commit the navigation only after the gesture completes; cancellation restores the current state.
- **GEST12 [H]** Test touch, TalkBack, keyboard, mouse, and stylus. Every essential operation needs a discoverable alternative to a complex gesture. Samsung explicitly calls for alternatives to drag-and-drop and multifinger gestures. [Samsung interaction and control](https://developer.samsung.com/one-ui/accessibility/interaction-and-control.html).

## 14. Material theming and color

There are three separate concerns: **One UI layout/component behavior**, **Material component/theme compatibility**, and **wallpaper-derived dynamic color**. Enabling one does not automatically provide the others. Android dynamic color was introduced in Android 12 and is applied through an appropriate theme/overlay and supported APIs. [Android dynamic color](https://developer.android.com/develop/ui/views/theming/dynamic-colors).

- **THEME01 [H]** Choose one app theme strategy. For this Views/SESL library, begin with its compatible One UI theme and test supported Material widgets individually. A wholesale Material 3 parent-theme replacement needs a compatibility review.
- **THEME02 [H]** Treat Use wallpaper colors as a color-source setting. It must not silently change navigation, corner shapes, header behavior, or gestures.
- **THEME03 [A]** Offer System default, Light, and Dark; Android recommends System default. Use a true dark theme rather than assuming a color inversion will be sufficient. [Android dark theme](https://developer.android.com/develop/ui/views/theming/darktheme).
- **THEME04 [H]** Define semantic color roles and foreground/background pairs. A Primary fill needs its own readable foreground; general page text color is not automatically safe on it.
- **THEME05 [H]** Keep routine surfaces calm and use accent color sparingly for selection, progress, and the main action. Samsung's color guidance favors calm backgrounds and subdued focus blocks. [Samsung color system](https://developer.samsung.com/one-ui/color/system.html).
- **THEME06 [H]** Keep habit identity colors separate from UI accent colors. Changing wallpaper must not arbitrarily turn a user's green Walking habit into a different category color.
- **THEME07 [H]** Define success, warning, error, and paused states deliberately. Do not use the error role for every missed day or use a dynamic accent as the only indication of success.
- **THEME08 [H]** Test all combinations: light/dark × default/wallpaper/custom. Include menus, sheets, disabled controls, sliders, chart text, snackbars, and system icon contrast.
- **THEME09 [H]** Resolve theme colors in custom drawing as well as XML. A themed screen with hardcoded chart labels or picker colors is an incomplete migration.
- **THEME10 [H]** Preserve drafts and navigation when changing appearance. Recreating an activity is acceptable if saved state restores the user's work.
- **THEME11 [H]** Provide an explicit fallback on devices without supported wallpaper colors. Disable or explain the unavailable choice instead of showing a switch that has no effect.
- **THEME12 [R]** This fork exposes eight palette roles. They are a local API, not a complete Material 3 color scheme; in particular its Secondary role also supplies supporting text/unselected navigation. See [the local component guide](components-and-settings.md#appearance-preferences).

| App color purpose | Existing fork role / integration | Required design check |
| --- | --- | --- |
| Selection and main accent | `OneUIPalette.PRIMARY` | Confirm readable text/icons on every accent fill. |
| Supporting text / unselected navigation | `OneUIPalette.SECONDARY` | Check contrast; do not assume Material secondary-accent semantics. |
| Page background | `OneUIPalette.BACKGROUND` | Coordinate scroll fades and window background. |
| Main text and icons | `OneUIPalette.FOREGROUND` | Test against every surface it appears on. |
| Cards and floating surfaces | `OneUIPalette.SURFACE` | Preserve separation in both light and dark. |
| Secondary panels / icon backgrounds | `OneUIPalette.SURFACE_VARIANT` | Check nested surfaces without excessive layering. |
| Outlines and inactive tracks | `OneUIPalette.OUTLINE` | Keep interactive boundaries perceivable. |
| Error/destructive accent | `OneUIPalette.ERROR` | Pair with text/icon explanation and readable foreground. |
| On-primary, on-error, containers, success, warning | Define app theme roles/styles where needed | These are additional design needs, not APIs supplied by the eight-role palette. |

- **THEME13 [R]** The fork applies its Android 12+ wallpaper overlay when enabled; saved custom light/dark roles provide the alternative palette. Use the documented precedence rather than competing independent color systems.
- **THEME14 [H]** If exposing custom colors to users, preview affected text and controls together and provide Reset. Guard against unreadable foreground/background combinations.

## 15. Blur, glass, depth, and system bars

Samsung describes blur, dimming, and soft shadows as hierarchy tools and cautions against excessive combinations. Effects should make the active layer understandable. [Samsung visual depth](https://developer.samsung.com/one-ui/structure/visual-depth.html).

- **DEPTH01 [H]** Apply translucent effects selectively to floating controls or modal surfaces. Routine habit content should remain readable without blur.
- **DEPTH02 [H]** Supply an opaque or sufficiently tinted fallback when blur is unavailable, disabled, or too costly. Verify contrast against both bright and dark content underneath.
- **DEPTH03 [H]** Avoid a pile of strong shadows, dim layers, and blur. One active modal should produce one clear foreground/background relationship.
- **DEPTH04 [R]** `ProgressiveBlurLayout` and `ScrollEdgeFades` in this fork create **color fades**, not actual pixel blur. Document and tune them accordingly.
- **DEPTH05 [H]** Fade effects must not erase the final row or reduce readable text to an inaccessible strip. Provide real scroll clearance and ensure effects do not consume touches.
- **DEPTH06 [A]** Design for edge-to-edge and current window insets. On Android 16 with target API 36, the edge-to-edge enforcement opt-out is disabled; painting a status-bar color alone does not solve content overlap. [Android 16 target behavior](https://developer.android.com/about/versions/16/behavior-changes-16).
- **DEPTH07 [H]** Let backgrounds extend under system bars where appropriate, while insetting actionable content around status icons, cutouts, navigation, keyboard, and display features.
- **DEPTH08 [H]** Update system icon appearance with the active background, including after a light/dark change or modal transition. Test gesture navigation and three-button navigation separately.
- **DEPTH09 [H]** Calculate footer clearance from the actual measured control region. The sample's fixed padding examples are starting points, not a rule for every font scale and navigation mode.
- **DEPTH10 [H]** Keep blur sampling work bounded. A scrollable list with many independent live glass surfaces needs profiling on a modest supported device.

## 16. Typography, icons, and accessibility

- **ACCESS01 [S]** Support substantial text enlargement without losing content or functionality; Samsung's typography guidance specifies up to 200% for most text. For this app, include supporting text in the same verification. [Samsung layout and typography](https://developer.samsung.com/one-ui/accessibility/layout-and-typo.html).
- **ACCESS02 [H]** Inherit the compatible theme/system font unless there is a documented reason to bundle another. Do not assume proprietary Samsung font files are available or licensed for redistribution.
- **ACCESS03 [H]** Use a small hierarchy: page title, section heading, row title, body, supporting text, and action label. Avoid introducing a new size and weight for each statistic.
- **ACCESS04 [S]** Meet at least 4.5:1 contrast for ordinary small text and the documented large-text exception of 3:1. Do not communicate status through color alone. [Samsung color and contrast](https://developer.samsung.com/one-ui/accessibility/color-contrast.html).
- **ACCESS05 [H]** Use 3:1 as the app's minimum target for meaningful control boundaries and state graphics, and verify focus indicators in both themes.
- **ACCESS06 [S]** Give non-text controls concise names and expose their current state. Avoid including the control type in a label if the accessibility framework already announces it. [Samsung screen reader](https://developer.samsung.com/one-ui/accessibility/screen-reader.html).
- **ACCESS07 [H]** A completion control should announce **Walking, Completed** or **Walking, Not completed**, using a meaningful name plus checked/state semantics.
- **ACCESS08 [H]** Make headings navigable and set a logical traversal order. Decorative icons should not become extra focus stops between a habit name and its action.
- **ACCESS09 [H]** A heatmap needs a legend, a textual summary, and accessible day details. Dense color cells alone cannot communicate history.
- **ACCESS10 [H]** Restore focus after dialogs, deletions, and navigation. If a deleted row vanishes, move focus to a sensible neighbor or section heading.
- **ACCESS11 [H]** Support keyboard focus and activation in DeX and tablet layouts. Hidden floating controls must not remain focusable.
- **ACCESS12 [H]** Test right-to-left layout. Use start/end spacing, mirror directional navigation where appropriate, and keep numbers and units readable.
- **ACCESS13 [H]** Respect reduced-motion preferences and accessibility timeout settings. Undo should also remain possible through a durable history/detail action when a temporary snackbar expires.
- **ACCESS14 [H]** Use one coherent icon family. Keep symbols, optical weight, selected variants, and labels consistent across top actions, menus, and navigation.
- **ACCESS15 [H]** Localize names, plural counts, recurrence summaries, dates, and time. A short English label does not justify a fixed-width control.

## 17. Motion, haptics, and sound

Samsung emphasizes immediate feedback and context-preserving movement. Its published motion guidance gives a general 100–500 ms range, not a single universal One UI 9 duration. [Samsung motion](https://developer.samsung.com/one-ui/motion/basic.html).

- **MOTION01 [H]** Use motion to explain an actual state change: completion, expansion, navigation, selection, or a modal entering/leaving.
- **MOTION02 [H]** Start app transitions around 150–250 ms and tune by distance and context. Treat these as app defaults; finger-tracked gestures follow the finger rather than a fixed timer.
- **MOTION03 [H]** Show pressed/recorded feedback immediately. Decorative success animation must not delay committing the entry or block the next habit.
- **MOTION04 [H]** Interrupt and reverse transitions cleanly. Rapid navigation or reopening a sheet must not leave invisible touch blockers or two selected destinations.
- **MOTION05 [H]** Keep celebrations brief and optional. Routine check-ins should remain calm; avoid a full-screen celebration after every tap.
- **MOTION06 [H]** Match haptic feedback to meaningful moments such as completion or a stepped adjustment, and honor the user's haptic settings. Samsung favors crisp, restrained feedback coordinated with visual motion. [Samsung haptics](https://developer.samsung.com/one-ui/sound-and-haptic/haptic.html).
- **MOTION07 [H]** Do not generate a vibration for every scroll frame, progress redraw, or restored state. Programmatic updates are different from user actions.
- **MOTION08 [H]** Sound should be optional and useful. Opening a menu, changing tabs, and checking a routine habit do not need mandatory sound effects.

## 18. Loading, empty, error, and offline states

Samsung distinguishes brief minor feedback from consequential information requiring a popup, and describes snackbars for feedback with a follow-up action. [Samsung toasts and snackbars](https://developer.samsung.com/one-ui/comp/toast.html).

- **STATE01 [H]** Prefer a local loading indicator over blocking the whole screen. A background sync should not prevent recording another habit.
- **STATE02 [H]** Preserve existing content during refresh. Do not replace a usable list with an empty spinner whenever sync starts.
- **STATE03 [H]** An empty state should explain why it is empty and offer the next action: Add habit, Change date, Clear filters, or Retry.
- **STATE04 [H]** If a write fails, show the failure near its context and retain the user's entry. Never show a completed checkmark for data that was not saved unless a clearly defined pending state exists.
- **STATE05 [H]** Make offline status informative rather than alarming. If local writes are supported, distinguish Saved on this device from Synced.
- **STATE06 [H]** Use a snackbar with Undo for a reversible archive or check-in change. Keep permanent data-loss warnings in a deliberate confirmation flow.
- **STATE07 [H]** Make error recovery actionable: Retry sync, Review entry, or Open settings. Do not rely on a transient toast for an unresolved blocking error.
- **STATE08 [H]** Keep focus and scroll stable as asynchronous content arrives. Loading a chart must not move the check-in control the user is about to tap.
- **STATE09 [H]** Use encouraging, factual language. Prefer **No entry recorded yesterday** to language that frames the user as failing.

## 19. Adaptive layouts and foldables

Android's window size classes describe the **current app window**, which changes with rotation, resizing, folding, and multitasking. Evaluate height as well as width; a wide but short window may not fit a two-pane layout. [Android window size classes](https://developer.android.com/develop/adaptive-apps/guides/use-window-size-classes).

| Available width | Platform size class | Proposed habit app layout |
| --- | --- | --- |
| Below 600 dp | Compact | Single pane, reachable bottom navigation, expandable landing header when height allows. |
| 600–839 dp | Medium | Wider content or list/detail if both panes are useful and height is sufficient. |
| 840–1199 dp | Expanded | List/detail with persistent habit context; rail or appropriate side navigation. |
| 1200–1599 dp | Large | Keep useful content widths; avoid stretching one habit row across the entire window. |
| 1600 dp and above | Extra-large | Deliberate multi-pane workspace where useful, with keyboard/mouse support. |

- **ADAPT01 [H]** Base layout decisions on window dimensions, not device model names or a hardcoded phone/tablet flag.
- **ADAPT02 [H]** On wider screens, use space to show related information, such as habit list plus detail. Do not simply scale all padding and type upward.
- **ADAPT03 [H]** Preserve selected habit, draft, scroll, date, and timer state when changing between one and two panes.
- **ADAPT04 [H]** Keep controls clear of hinges and folds. Use device posture/display-feature information when it affects the usable area.
- **ADAPT05 [H]** Reduce large header space in compact-height windows. Essential content must remain reachable in split screen and landscape.
- **ADAPT06 [H]** Constrain modal/editor width in large windows and choose a deliberate placement. Do not stretch a rename dialog across a monitor.
- **ADAPT07 [H]** Test narrow split screen, folded cover display, unfolded display, landscape, and freely resized DeX windows. Never require a restart to repair layout after resizing.
- **ADAPT08 [A]** Review Android 17 targeting changes: the prior opt-out for large-screen orientation/resizability/aspect restrictions is unavailable at target API 37. Do not build the migration around forcing portrait mode. [Android 17 target behavior](https://developer.android.com/about/versions/17/behavior-changes-17).
- **ADAPT09 [H]** Offer comfortable keyboard and mouse paths: focus order, visible focus, scrolling, Escape dismissal, and appropriately sized pointer targets.

## 20. Settings and appearance

- **SET01 [H]** Group settings into Appearance, Reminders, Tracking behavior, Data/backup, Accessibility/feedback, and About. Use plain names and current-value summaries.
- **SET02 [H]** Apply simple switches immediately and persist them. Use a separate editor only when several values must be committed together.
- **SET03 [H]** Separate theme mode, color source, blur, haptics, and motion options. A setting labeled Material should not secretly control all five.
- **SET04 [H]** Use a precise label such as **Use wallpaper colors** for dynamic color. Explain the unavailable state on unsupported devices.
- **SET05 [H]** Keep reset scopes explicit: Reset light palette, Reset dark palette, Reset appearance, and Delete all habit data are different operations.
- **SET06 [H]** Keep backup/export and destructive data actions separate. Show progress, completion, and useful failure recovery for each.
- **SET07 [H]** Add an About/version area with a readable app version and support path. Toolkit/demo implementation controls do not belong in the everyday habit settings page.

## 21. Habit app page specifications

These are proposed app designs, not screenshots of Samsung apps.

| Page | Header and content | Main actions and navigation | State that must survive return |
| --- | --- | --- | --- |
| Today | Expandable Today header, date, completion summary, due habits, optional Completed group | Check in on rows; Add habit; Search/More; bottom destinations | Viewed date, filters, completion visibility, scroll, collapse. |
| Habits | Expandable Habits header, active/archived scope, grouped habit list | Add habit; Sort; explicit Select/Reorder | Group/sort order, archive scope, scroll. |
| Habit detail | Habit name and readable schedule; current progress; history and statistics | Record/edit entry, Edit habit, Archive/Delete in More; Up | Selected date, history range, scroll. |
| Create habit | Compact editor title; labeled fields and recurrence summary | Cancel/Save in reachable action region | Complete draft, validation, picker selections. |
| Edit habit | Compact editor; existing values and historical-effect explanation where needed | Cancel/Save | Original values, changed values, dirty state. |
| Log progress | Clear habit, date, value, units, optional note | Save/Cancel or one explicit record action | Draft value, selected date, note. |
| History / calendar | Month/week identity, selected day, legend, entry list | Previous/Next, Today shortcut, Edit entry | Visible month, selected day, list position. |
| Insights | Expandable Insights header when height allows; time range, summary, charts, textual findings | Range selector, filter, optional export | Range, habit/group filter, chart drill-down. |
| Timer session | Compact habit identity, elapsed/remaining time, clear running/paused status | Start/Pause/Resume/Stop as appropriate | Session state and timing through app switching. |
| Settings | Grouped preference lists with summaries | Immediate controls; detail pages for complex settings | Current theme and settings; return focus/scroll. |
| Search | Query and results, active filters, empty-result explanation | Clear query, Close search, result actions | Query, filters, previous destination context. |
| Selection mode | Selected count, selectable rows, clear exit | Contextual bottom toolbar replacing navigation | Selection during safe configuration changes. |

- **HABIT01 [H]** For a binary habit, record today's state directly. For quantity/duration habits, show meaningful progress rather than pretending every habit is a checkbox.
- **HABIT02 [H]** Define the effect of a second completion tap. If it undoes completion, give clear feedback; never silently create duplicate entries.
- **HABIT03 [H]** Distinguish the habit definition from its daily entries. Delete entry, Archive habit, and Delete habit with history must have different wording and behavior.
- **HABIT04 [H]** Explain how skipped days and non-scheduled days affect streaks and completion rates. Keep charts and textual summaries consistent with that policy.
- **HABIT05 [H]** Prefer simple progress summaries over a dashboard full of competing cards. Today's next actionable habit should remain obvious.
- **HABIT06 [H]** Make statistics inspectable. Show date range, units, numerator/denominator, and treatment of missing entries where relevant.
- **HABIT07 [H]** Keep timer state separate from animation state. App backgrounding, window resizing, and theme recreation must not reset the recorded session.

## 22. Reminders, notifications, and widgets

- **REM01 [A]** Handle Android 13+ notification permission using the platform flow, requested in context after the user chooses reminders. Denial must not prevent using the habit tracker. [Android notification permission](https://developer.android.com/develop/ui/compose/notifications/notification-permission).
- **REM02 [H]** In reminder settings, show the effective state: enabled, blocked by system permission/settings, or paused. A saved reminder time alone does not prove delivery is allowed.
- **REM03 [H]** Keep reminder copy short and respectful. Allow users to control frequency, quiet periods, and sound rather than treating every missed habit as urgent.
- **REM04 [H]** Offer relevant notification actions such as Complete or Snooze only when they are safe and unambiguous. Target the correct habit and date and avoid duplicate entries.
- **REM05 [H]** Notification taps should open the relevant habit/session with a sensible return path. Preserve existing unsaved work if the app is already open.
- **REM06 [H]** Avoid displaying sensitive habit names on the lock screen without considering the user's notification privacy settings and the app's own privacy options.
- **REM07 [H]** Design widgets for quick glance and quick recording with readable state, useful empty/error behavior, and adaptable sizes. Keep widget content consistent with the app's saved data.
- **REM08 [R]** This sample includes Now Bar-style notification demonstrations. Their presence is not evidence that every custom app view can appear in Samsung's system Now Bar; verify the supported public integration for the target OS/device separately.
- **REM09 [H]** Keep reminder scheduling and ongoing-timer notification behavior in the platform migration checklist. A visual refresh does not validate background execution or exact delivery timing.

## 23. Mapping to this library

The following classes and methods were checked against the current workspace. See [components and settings](components-and-settings.md) for the fuller catalog and [settings example](settings-page-example.md) for preference layout.

| Design need | Current local building block | Integration rule |
| --- | --- | --- |
| Large/collapsing page header | `ToolbarLayout` | Coordinate scroll behavior; choose compact title policy explicitly. |
| Persistent collapsed title | `ToolbarLayout.setStickyTitleEnabled(true)` | Recommended for Today and date-sensitive history context. |
| Collapsed action surfaces | `StickyToolbarControls`, `ToolbarLayout` setters | Configure one shared style and keep full touch targets. |
| Fixed bottom slot | `ToolbarLayout.setStickyBottomBar(...)` | Manage page lifecycle and measurable scroll clearance. |
| Top-level navigation | `FloatingNavigationBar` | Stable destination IDs, accessible titles, visible labels. |
| Destination pages | `NavigationPageContainer` | Direct-child page IDs must match menu IDs. Disable page swipes explicitly. |
| Contextual multi-actions | `FloatingToolbar` | Icons plus visible labels; appropriate action count. |
| Draft confirmation bar | `FloatingActionBar` | Cancel/Save semantics belong to the editor. |
| Navigation/context swap | `FloatingBarSwitcher` | One active bottom control region. |
| Search | `ToolbarLayout.showSearchMode()`, `FloatingSearchBar` | The app owns query filtering and state restoration. |
| Habit list/card row | `IconRowView` or an accessible custom row | Add genuine check-in semantics; a display row alone is insufficient. |
| Bounded goal adjustment | `OneUISlider`, `OneUISliderPreference` | Units, range, step, direct-entry alternative. |
| Palette | `OneUIPalette`, `MaterialColorSettings` | Keep theme attributes and custom-drawn content consistent. |
| Glass/fallback | `GlassSurfaceView`, `BlurSettings`, `DialogBlur` | Validate opaque fallback and OS capability. |
| Scroll edge treatment | `ScrollEdgeFades`, `ProgressiveBlurLayout` | Color fade; avoid duplicate attachment and hidden content. |
| Small shortcut grid | `GridMenuDialog` | Use for recognizable actions, not a complex settings form. |
| Settings | Existing preference classes | Follow the app's semantics and supported theme path. |

### Theme initialization

This ordering matches the local guide. Preserve the activity's own existing lifecycle/state handling around it.

```java
@Override
protected void onCreate(Bundle state) {
    super.onCreate(state);
    MaterialColorSettings.applyTheme(this);
    setContentView(R.layout.activity_main);
    View root = findViewById(R.id.root);
    OneUIPalette.applyToViewTree(root);
}
```

`OneUIPalette.applyToViewTree` handles participating colors/background tags; it is not a universal replacement for theme attributes. Mark applicable XML backgrounds with `oneui.palette.background` or `oneui.palette.surface`, and resolve custom drawable/chart colors explicitly. Recreate after appearance changes with state preservation, as documented by the local sample.

### Header and navigation policy

```java
toolbarLayout.setStickyTitleEnabled(true);

navigationBar.bindPages(pageContainer);
navigationBar.setPageSwipingEnabled(false); // Set after binding.
pageContainer.setSwipeEnabled(false);       // Also explicit on the container.
```

Do not copy every demo option into the habit app. In particular, the local navigation container currently defaults to allowing swipes; the product policy above deliberately disables them.

### Current gaps to resolve during implementation

| Observed source / scope | Consequence | Required follow-up |
| --- | --- | --- |
| `manifest.gradle` uses compile SDK 36 and sample target SDK 33 | The sample's appearance is not proof of modern target behavior. | Plan SDK/dependency upgrades and run the platform behavior checks before claiming readiness. |
| `ToolbarLayout` navigation helper calls legacy `onBackPressed()` | Modern Back handling needs review. | Route navigation through supported Back/Up handling and test nested overlays plus predictive cancellation. |
| Local theme inherits AppCompat/SESL styles | Generic Material 3 recipes may not be compatible unchanged. | Audit required widgets/attributes before replacing the theme parent or dependencies. |
| Palette has eight roles | It lacks a complete set of foreground/container semantic pairs. | Add app-level roles/styles for missing cases and verify contrast. |
| Several XML/demo colors depend on palette participation | A theme switch may miss custom or literal assets. | Inventory habit app resources and custom drawing. |
| Floating bars and sample padding are configurable | Large text/insets can invalidate a fixed clearance. | Measure actual overlays and test the last actionable row. |
| Blur and fade implementations vary by component/API | A glass appearance is not an OS compatibility guarantee. | Test global blur off, unsupported blur, and performance. |
| Habit app source is outside the inspected scope | Component availability does not prove habit workflows work. | Apply this rulebook against the actual app screens and data flows. |

The source observations above describe the files as checked on the research date. Recheck them if the library changes.

## 24. Migration order

| Priority | Work | Completion evidence |
| --- | --- | --- |
| P0 | Inventory actual habit screens, dialogs, menus, custom controls, theme overrides, and gesture listeners | Each item maps to a rule and an owner; missing states are listed. |
| P0 | Fix window insets, keyboard overlap, modern Back behavior, and state restoration | No inaccessible controls or lost drafts during navigation, rotation, resizing, or theme changes. |
| P0 | Unify theme roles and light/dark behavior; cover accessibility basics | Critical journeys work at large text and with TalkBack in every color mode. |
| P1 | Standardize page shells, collapsing headers, destination navigation, and bottom action slots | Today, Habits, Insights, and editors use the agreed structure. |
| P1 | Normalize habit rows, selection, menus, dialogs, and pickers | Completion, progress logging, archive/delete, and editing behave consistently. |
| P1 | Complete search/filtering and loading/empty/error/offline states | No blank or misleading states; recovery actions work. |
| P1 | Adapt to split screen, foldables, and desktop-style windows | State survives layout changes; related panes add value. |
| P2 | Tune motion, haptics, blur, shadows, and scroll fades | Effects remain optional, readable, and smooth on a modest supported device. |
| P2 | Verify reminders, notifications, widgets, and active sessions | The app and external entry points show consistent saved state. |
| Release gate | Verify against an actual intended One UI 9 device/build | Record device, OS/build, theme, font scale, navigation mode, and comparison captures. |

Raise target SDK as a planned platform change with dependency compatibility checks. A version label such as `ui9-v1.0`, a floating pill, or a glass dialog is not sufficient evidence of complete One UI 9 compatibility.

## 25. Acceptance checklist

Use these as manual acceptance criteria for the actual habit app. They are verification tasks, not claims that this documentation change has tested the application.

### Headers, actions, and navigation

- [ ] Expanded/partial/collapsed headers have one clear title, stable actions, and no overlapping targets.
- [ ] Returning from Habit detail restores date, filters, scroll, and header state.
- [ ] Compact landscape and large text do not leave an oversized empty header.
- [ ] Top-level navigation has stable labeled destinations and no page swipes.
- [ ] Add habit is an action; it does not become a selected destination.
- [ ] Selection replaces normal bottom navigation, shows a count, and exits cleanly.
- [ ] Save/Cancel remain accessible with the keyboard and during scrolling.
- [ ] The last row can scroll completely above floating bars and snackbars.

### Components and data safety

- [ ] Row tap opens detail; check-in targets record the correct habit/date without duplicate activation.
- [ ] Binary, numeric, and timed habits use suitable recording controls.
- [ ] Check-in, undo, archive, delete entry, and permanent delete habit have distinct behavior.
- [ ] Menus anchor correctly and return focus after dismissal.
- [ ] Dialog and sheet cancellation never commits unfinished input.
- [ ] Long dialog text and translated action labels stay usable at large text.
- [ ] Date/time pickers honor locale and the device time convention.
- [ ] Invalid form submission preserves input and identifies the field needing repair.
- [ ] Loading, empty, filtered-empty, error, and offline states all have meaningful content.

### Gestures and lifecycle

- [ ] Edge Back and Home gestures work beside carousels, sliders, and calendars.
- [ ] Predictive Back completion and cancellation are both correct.
- [ ] Back handles keyboard, popup, search, selection, editor draft, and child page in the intended order.
- [ ] Long press and dragging have visible or accessible alternatives.
- [ ] Pull to refresh does not conflict with header expansion.
- [ ] Rotation, resizing, app switching, and theme recreation preserve the appropriate state.
- [ ] Deep links, notification taps, and widget taps have a sensible return path.

### Theme and accessibility

- [ ] Default and wallpaper colors both work in light and dark mode.
- [ ] Any custom palette maintains readable foreground/background pairs.
- [ ] Charts, menus, dialogs, snackbars, and disabled controls use the active theme.
- [ ] Blur off and unavailable-blur fallbacks remain fully usable.
- [ ] Status/navigation icons remain readable in both system navigation modes.
- [ ] At 200% text, critical content, actions, and supporting information remain usable.
- [ ] All essential controls meet the chosen touch-target baseline without overlapping.
- [ ] TalkBack exposes names, roles, checked/selected/progress states, headings, and sensible focus order.
- [ ] Completion and history remain understandable in grayscale.
- [ ] Reduced motion, haptics off, keyboard focus, and right-to-left layouts work.

### Devices and external surfaces

- [ ] Phone portrait and landscape, narrow split screen, foldable cover and inner displays, and DeX resizing are covered.
- [ ] Layout adapts to current window width/height and keeps controls away from folds/cutouts.
- [ ] Notification permission allowed/denied and system-blocked reminders have clear states.
- [ ] Notification/widget actions update the correct entry and stay consistent with the app.
- [ ] Timer/session state survives backgrounding and configuration changes.
- [ ] Platform changes for the intended target SDK are reviewed separately from visual styling.
- [ ] The release comparison records the actual One UI 9 build and clearly separates deliberate app choices from verified Samsung behavior.

### Primary references

The source links beside each section support its documented baseline. The most useful starting points are [Samsung's component index](https://developer.samsung.com/one-ui/index.html), [app bars](https://developer.samsung.com/one-ui/comp/app-bar.html), [bottom navigation](https://developer.samsung.com/one-ui/comp/bottom-navigation.html), [dialogs](https://developer.samsung.com/one-ui/comp/dialog.html), [color and contrast](https://developer.samsung.com/one-ui/accessibility/color-contrast.html), [Android dynamic color](https://developer.android.com/develop/ui/views/theming/dynamic-colors), [predictive Back](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture), [adaptive windows](https://developer.android.com/develop/adaptive-apps/guides/use-window-size-classes), and the [Android 17 behavior changes](https://developer.android.com/about/versions/17/behavior-changes-17).
