# Accessibility Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix four concrete TalkBack regressions reported by blind users — restore "selected" announcement on bottom-nav tabs, eliminate triple-repeated tab announcements, ensure the Connect button & Parallel Test rows announce in the in-app language, and replace custom "tap to enable/disable" toggle strings with native `Role.Switch` / `Role.Checkbox` semantics.

**Architecture:** Replace custom `Modifier.semantics { contentDescription = ... }` blocks that overlay clickable rows with native Compose semantics roles — `Role.Tab` via `Modifier.selectable`, `Role.Switch` and `Role.Checkbox` via `Modifier.toggleable` — combined with `mergeDescendants = true` on the row and `clearAndSetSemantics {}` on the inner Icon/Switch/Checkbox. This lets TalkBack speak the row exactly once and announce state ("selected", "on", "checked") from the platform locale automatically. For the one screen-reader string that doesn't have a native equivalent (the Connect button icon description), migrate it into the existing `WhiteDnsStrings` CompositionLocal so it follows the in-app language picker like every other localized string.

**Tech Stack:** Kotlin, Jetpack Compose, `androidx.compose.foundation.selection.{selectable, toggleable}`, `androidx.compose.ui.semantics.{Role, semantics, clearAndSetSemantics}`, existing `WhiteDnsStrings` interface + `WhiteDnsL10n` CompositionLocal pattern.

---

## What is currently wrong (summary)

The four problems reported by the user are confirmed in the code:

1. **Triple announcement on bottom-nav tabs.** `BottomNavigationBar` at `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt:1910-1944` sets `contentDescription = "Navigate to {label}"` on the outer `Column` *without* `mergeDescendants = true`. The child `Icon` then defines its own `contentDescription = localizedLabel`, and the child `Text` is read verbatim by TalkBack. Result: "Navigate to Connect, Connect, Connect."

2. **No "selected" state on bottom-nav tabs.** Neither `BottomNavigationBar` (line 1910) nor `ProfileTabSwitch` (line 1969) uses `Modifier.selectable(selected, role = Role.Tab)`. TalkBack therefore never speaks "selected" — the user must guess from visual cues which tab is active.

3. **Connect button announces in English regardless of in-app language.** The icon's `contentDescription` at line 6657-6663 uses `stringResource(R.string.cd_connect_button_*)`. `stringResource()` reads from the **Android system locale**, which is independent of the in-app language picker. The button label text (`btnConnect`) already migrated to the in-app `WhiteDnsL10n` CompositionLocal (line 6530-6532), so the visible label is Persian while the screen-reader description is still English.

4. **Parallel Test (and other ToggleRow toggles) read "tap to enable/disable" instead of native state.** `ToggleRow` at line 8287-8300 wraps a row with a custom `Modifier.semantics { contentDescription = context.getString(R.string.cd_toggle_row_on/off, label) }`. The inner native `Switch` (line 8313) is never reached by the screen reader because the row owns the focus. The custom string forces TalkBack to read "X enabled, tap to disable" instead of letting the platform say "X, switch, on / off, double-tap to toggle." `ParallelTestConfigRow` at line 1151-1194 has the same shape — a `clickable` row wrapping a native `Checkbox`, with the focusable click target on the row rather than the checkbox, so the platform never speaks "checked / not checked."

## Root cause

Two distinct root causes underlie the four symptoms:

- **A11y semantics were stacked on top of components rather than expressed through them.** The codebase consistently puts `Modifier.semantics { contentDescription = ... }` *and* a child `Icon(contentDescription = ...)` *and* a visible `Text(...)` on the same logical control without merging. Compose treats these as three independent semantics nodes. The fix is the standard Compose idiom: `Modifier.selectable` / `Modifier.toggleable` with `mergeDescendants = true`, and `clearAndSetSemantics {}` on inner decoration that should not have its own focus.

- **There are two parallel localization channels.** Visible UI labels were migrated to the in-app `WhiteDnsStrings` CompositionLocal during the Persian localization work (commit `8f554c9`). Accessibility strings (`cd_*` keys in `res/values/strings.xml`) were *not*. `stringResource()` and `context.getString()` resolve against the Android system locale, so when a user picks Persian inside the app while their device is in English, every `cd_*` string still reads in English. The two channels need to be unified.

## Specific code changes (overview)

| Component | Current | Fix |
|-----------|---------|-----|
| `BottomNavigationBar` row Column | `.semantics { contentDescription = "Navigate to X" }` + Icon cd + Text | `.selectable(selected, role = Role.Tab)` + `.semantics(mergeDescendants = true) {}` + Icon `clearAndSetSemantics {}` |
| `ProfileTabSwitch` row Box | `.semantics { cd = "X, selected tab" / "X tab" }` | `.selectable(selected, role = Role.Tab)` + `.semantics(mergeDescendants = true) {}` |
| `ToggleRow` row | `.semantics { cd = "X enabled, tap to disable" }` + `.clickable` wrapping a `Switch` | `.toggleable(enabled, role = Role.Switch)` + `Switch` with `clearAndSetSemantics {}` |
| `ParallelTestConfigRow` row | `.clickable` wrapping a `Checkbox` | `.toggleable(checked, role = Role.Checkbox)` + `Checkbox` with `clearAndSetSemantics {}` |
| `ConnectButton` icon cd | `stringResource(R.string.cd_connect_button_*)` (system locale) | `WhiteDnsL10n.cdConnectButton*` (in-app locale, new in `WhiteDnsStrings`) |

## Why these changes solve the problem

- `Modifier.selectable(selected, role = Role.Tab)` is how Compose tells the accessibility framework "this is a tab and its selected state is `selected`." TalkBack then automatically appends "selected" (or its Persian equivalent if the system locale is Persian) — no custom string needed.
- `mergeDescendants = true` collapses the row into a single semantics node, so TalkBack reads it once. `clearAndSetSemantics {}` on the inner Icon removes its redundant `contentDescription` from the tree.
- `Modifier.toggleable(value, role = Role.Switch / Role.Checkbox)` does the same trick for on/off and checked/unchecked — TalkBack announces the role and state natively. The custom `cd_toggle_row_*` and the row-level `clickable` go away. The native `Switch` / `Checkbox` keep their `onCheckedChange` so the visible widget still toggles on tap, but their semantics are cleared so the row owns the announcement.
- Migrating `cd_connect_button_*` into `WhiteDnsStrings` + `WhiteDnsL10n` is the same pattern already used for `cdParallelTestSpeed`, `cdSelected`, etc. It is the path of least surprise and matches what every other localized string in the app does.

### Known limitation (do not attempt to "fix" without discussion)

The native role/state words ("Tab", "selected", "Switch", "on", "Checkbox", "checked") come from the **TalkBack engine**, which reads them in the device's system locale. If the device's system language is English and the user picks Persian inside the app, the user will hear `<Persian label>` followed by the English word "selected." This is consistent with how every other Compose app on Android behaves, and Google does not expose an API to override these. The user feedback explicitly asks for native state announcement, so accepting this trade-off is the right call: a user whose device is set to Persian gets fully Persian announcements; a user whose device is English but in-app is Persian gets mixed announcements (which is still strictly better than the current English-only custom string). If the team later wants fully in-app-locale announcements, the path is to wrap the Activity context in `createConfigurationContext` with the in-app locale and call `applyOverrideConfiguration` in `attachBaseContext` — this is out of scope for this plan and should be its own design discussion because it affects every system component (notifications, status bar, etc.), not just a11y.

---

## File Structure

| File | Responsibility | Action |
|------|---------------|--------|
| `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` | Compose UI for all the components named above | Modify (5 composables + imports) |
| `app/src/main/java/shop/whitedns/client/ui/WhiteDnsStrings.kt` | In-app localization interface and English/Persian implementations | Modify (add 4 strings to interface + both impls) |
| `app/src/main/java/shop/whitedns/client/ui/WhiteDnsTheme.kt` | `WhiteDnsL10n` composable getters over `LocalWhiteDnsStrings` | Modify (add 4 getters) |
| `app/src/main/res/values/strings.xml` | Android resource strings (system-locale) | Modify (remove now-unused `cd_navigate_to_tab`, `cd_profile_tab_*`, `cd_toggle_row_*`, `cd_connect_button_*`) |
| `app/src/main/res/values-fa/strings.xml` | Persian resource strings | Modify (remove same keys) |

No new files. Each task below changes one component and leaves the rest of the file untouched.

---

## Task 1: Add Compose semantics imports to WhiteDnsScreen.kt

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt:130-140`

Tasks 2-5 use `Modifier.selectable`, `Modifier.toggleable`, `Role`, and `clearAndSetSemantics`. None are currently imported. Add them once so subsequent tasks compile.

- [ ] **Step 1: Inspect current semantics imports**

Run: `grep -n "import androidx.compose.ui.semantics\|import androidx.compose.foundation.selection" app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt`

Expected output:
```
134:import androidx.compose.ui.semantics.contentDescription
135:import androidx.compose.ui.semantics.semantics
```

(No `selectable`, `toggleable`, `Role`, or `clearAndSetSemantics` imports yet.)

- [ ] **Step 2: Add the four new imports**

Insert these four lines alongside the existing `androidx.compose.ui.semantics.*` imports (after line 135). Keep alphabetical ordering inside each package.

```kotlin
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
```

After your edit, lines 134-139 (approximate) should read:

```kotlin
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
```

Note: the two `selection` imports belong logically with the existing `androidx.compose.foundation.*` block earlier in the file (around line 35-60) per the file's existing convention. If you prefer to keep alphabetical grouping by package, place them there instead — both placements compile. The example above groups them with `semantics` for review convenience.

- [ ] **Step 3: Verify the file compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`. If it fails with "unresolved reference: selectable" or similar, your import path is wrong — re-check the four lines above.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "chore(a11y): add Compose selection/Role/clearAndSetSemantics imports"
```

---

## Task 2: Convert BottomNavigationBar to Role.Tab with merged semantics

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt:1910-1944`

The outer `Column` for each tab currently has both a custom `contentDescription` and a `clickable`, and its `Icon` child re-states the label. Replace the custom semantics + `clickable` with `Modifier.selectable(selected, role = Role.Tab)`, merge descendants, and clear the Icon's semantics so the row speaks once.

- [ ] **Step 1: Read the current implementation**

Read `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` lines 1897-1944. Confirm the shape matches:

```kotlin
WhiteDnsTab.entries.forEach { tab ->
    val selected = selectedTab == tab
    val localizedLabel = tabLabel(tab)
    // ...animateColorAsState blocks...
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .semantics {
                contentDescription = context.getString(
                    R.string.cd_navigate_to_tab, localizedLabel
                )
            }
            .clickable {
                haptic.performLight()
                onTabSelected(tab)
            }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = localizedLabel,
            tint = color,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = localizedLabel,
            // ...style...
        )
    }
}
```

- [ ] **Step 2: Replace the Column modifier and the Icon's contentDescription**

Replace the `Column { modifier = ... }` chain with the version below. The key changes:
- Remove `.semantics { contentDescription = ... }` entirely.
- Replace `.clickable { ... }` with `.selectable(selected = selected, role = Role.Tab, onClick = { ... })`.
- Add `.semantics(mergeDescendants = true) {}` so the Column collects its children into one announcement.
- On the inner `Icon`, set `contentDescription = null` (it's now decorative; the merged row reads the Text instead). Note: setting `contentDescription = null` is sufficient — `clearAndSetSemantics` is overkill for an `Icon` and would also strip role information used by some screen-reader users. Save `clearAndSetSemantics` for the `Switch`/`Checkbox` cases in Tasks 4-5.
- The `context` local declared at line 1881 in `BottomNavigationBar` becomes unused once `context.getString` is removed; leave the `val context = LocalContext.current` line for now — it is still referenced by other parts of the surrounding code (verify with a quick grep after the edit; if it truly is unused, the Kotlin compiler will warn and you can delete it as a follow-up).

Edit the Column block to:

```kotlin
Column(
    modifier = Modifier
        .weight(1f)
        .clip(RoundedCornerShape(14.dp))
        .background(background)
        .selectable(
            selected = selected,
            role = Role.Tab,
            onClick = {
                haptic.performLight()
                onTabSelected(tab)
            },
        )
        .semantics(mergeDescendants = true) {}
        .padding(vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
) {
    Icon(
        imageVector = tab.icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(20.dp),
    )
    Text(
        text = localizedLabel,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 9.sp,
            color = color,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.5.sp,
        ),
    )
}
```

- [ ] **Step 3: Verify the file compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`. If the compiler warns "parameter 'context' is never used" inside `BottomNavigationBar`, remove the `val context = LocalContext.current` line at 1881.

- [ ] **Step 4: Manual TalkBack check**

Install the debug build on a device with TalkBack enabled and the app's in-app language set to Persian.
- Swipe through the bottom-nav tabs. Each tab should be announced **once**, with the localized label, and the currently-active tab should have "selected" appended (in the device's system locale).
- Verify the inactive tab does *not* announce "selected" and instead announces only the label and the "tab" role (e.g. "Connect, tab, double-tap to activate").

Document the result in the commit message if anything is off.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "fix(a11y): use Role.Tab + selectable for bottom nav tabs

Restores 'selected' state announcement and eliminates the triple
read of each tab label by merging descendants and dropping the
Icon's redundant contentDescription."
```

---

## Task 3: Convert ProfileTabSwitch to Role.Tab with merged semantics

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt:1966-2007`

Same pattern as the bottom nav, applied to the profile (Personal / Shared) tab strip. Currently uses `cd_profile_tab_selected` / `cd_profile_tab_unselected`. Replace with `selectable` + merged semantics.

- [ ] **Step 1: Read the current implementation**

Read `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` lines 1966-2007. Confirm the shape:

```kotlin
ProfileTab.entries.forEach { tab ->
    val selected = selectedTab == tab
    val localizedProfileLabel = profileTabLabel(tab)
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(...)
            .semantics {
                contentDescription = if (selected) {
                    context.getString(R.string.cd_profile_tab_selected, localizedProfileLabel)
                } else {
                    context.getString(R.string.cd_profile_tab_unselected, localizedProfileLabel)
                }
            }
            .clickable {
                haptic.performLight()
                onTabSelected(tab)
            }
            .padding(horizontal = 8.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = localizedProfileLabel, ...)
    }
}
```

- [ ] **Step 2: Replace the Box modifier**

Replace the chain to drop the custom `.semantics` and `.clickable`, and use `.selectable(selected, role = Role.Tab, onClick = ...)` + merged-semantics:

```kotlin
Box(
    modifier = Modifier
        .weight(1f)
        .clip(RoundedCornerShape(10.dp))
        .background(
            if (selected) {
                WhiteDnsPalette.Accent
            } else {
                Color.Transparent
            },
        )
        .selectable(
            selected = selected,
            role = Role.Tab,
            onClick = {
                haptic.performLight()
                onTabSelected(tab)
            },
        )
        .semantics(mergeDescendants = true) {}
        .padding(horizontal = 8.dp, vertical = 11.dp),
    contentAlignment = Alignment.Center,
) {
    Text(
        text = localizedProfileLabel,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 9.sp,
            color = if (selected) WhiteDnsPalette.OnAccent else WhiteDnsPalette.Muted,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.4.sp,
        ),
    )
}
```

If `val context = LocalContext.current` at line 1954 is now unused, remove it (the compiler will say so).

- [ ] **Step 3: Verify the file compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Manual TalkBack check**

With TalkBack enabled, navigate to a screen that shows the profile tab switch (the Profiles screen). Swipe to each tab. Expected: one announcement per tab, "selected" appended only to the active one.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "fix(a11y): use Role.Tab + selectable for ProfileTabSwitch

Drops custom cd_profile_tab_selected/unselected strings in favor of
the platform's native Tab role announcement."
```

---

## Task 4: Convert ToggleRow to Role.Switch via toggleable

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt:8279-8329`

`ToggleRow` is used for several settings toggles, including Parallel Test enable/disable. The row currently announces "X enabled, tap to disable" because of a custom `contentDescription`. Replace with `Modifier.toggleable(value, role = Role.Switch, onValueChange = ...)` so TalkBack speaks the label, "switch", and the on/off state natively. Clear the inner `Switch`'s semantics so it doesn't get a second focus stop.

- [ ] **Step 1: Read the current implementation**

Read `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` lines 8278-8329. Confirm:

```kotlin
@Composable
private fun ToggleRow(
    label: String,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = if (enabled) {
                    context.getString(R.string.cd_toggle_row_on, label)
                } else {
                    context.getString(R.string.cd_toggle_row_off, label)
                }
            }
            .clickable {
                haptic.performLight()
                onToggle()
            }
            .padding(vertical = 10.dp),
        // ...
    ) {
        Text(text = label, ...)
        Switch(
            checked = enabled,
            onCheckedChange = {
                haptic.performLight()
                onToggle()
            },
            // ...colors...
        )
    }
}
```

- [ ] **Step 2: Replace the Row + Switch implementation**

```kotlin
@Composable
private fun ToggleRow(
    label: String,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val haptic = rememberHapticFeedback()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = enabled,
                role = Role.Switch,
                onValueChange = {
                    haptic.performLight()
                    onToggle()
                },
            )
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                color = WhiteDnsPalette.FieldLabel,
                fontWeight = FontWeight.Medium,
            ),
        )
        Switch(
            checked = enabled,
            onCheckedChange = null,
            modifier = Modifier.clearAndSetSemantics {},
            colors = SwitchDefaults.colors(
                checkedThumbColor = WhiteDnsPalette.OnAccent,
                checkedTrackColor = WhiteDnsPalette.Accent,
                checkedBorderColor = WhiteDnsPalette.Accent,
                uncheckedThumbColor = WhiteDnsPalette.Muted,
                uncheckedTrackColor = WhiteDnsPalette.Input,
                uncheckedBorderColor = WhiteDnsPalette.ControlBorder,
            ),
        )
    }
}
```

Three important details:
- `onCheckedChange = null` on the `Switch` makes it visually a switch but non-interactive on its own — the parent `Row.toggleable` handles the click. This is the recommended Compose pattern when a parent row owns the toggle gesture.
- `Modifier.clearAndSetSemantics {}` on the `Switch` removes it from the a11y tree so TalkBack only stops on the row, not on the row *and* the switch.
- The `val context = LocalContext.current` line is removed because `context.getString` is no longer called.

- [ ] **Step 3: Verify the file compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`. If you see "unresolved reference: clearAndSetSemantics", Task 1's imports were not added — go back and add them.

- [ ] **Step 4: Manual TalkBack check**

With TalkBack enabled and the in-app language set to Persian, open App Settings (or wherever `ToggleRow` is used) and focus on a toggle. Expected announcement: `<label>` + "switch" + "on" / "off" + "double-tap to toggle". Tap to toggle; the state word should switch ("on" ↔ "off") and TalkBack should re-announce. The phrase "tap to enable" / "tap to disable" must **not** appear.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "fix(a11y): make ToggleRow a Role.Switch toggleable row

Drops custom cd_toggle_row_on/off strings. The native Switch role
makes TalkBack announce on/off naturally, fulfilling the user's
'use native controls' request."
```

---

## Task 5: Convert ParallelTestConfigRow to Role.Checkbox via toggleable

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt:1142-1194`

This is the per-config row inside the Parallel Test selector. Currently the `Row` is `clickable` and wraps a `Checkbox` whose `onCheckedChange` separately calls `onToggle`. Result: two focus stops (row, then checkbox) and no role announcement on the row. Replace with `toggleable(checked, role = Role.Checkbox)` and clear the `Checkbox`'s semantics. Also respect the `enabled` parameter — `toggleable` supports it directly.

- [ ] **Step 1: Read the current implementation**

Read `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` lines 1142-1194. Confirm:

```kotlin
@Composable
private fun ParallelTestConfigRow(
    label: String,
    detail: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val contentAlpha = if (enabled) 1f else 0.46f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            enabled = enabled,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors( /* ... */ ),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, /* ... */)
            Text(text = detail, /* ... */)
        }
    }
}
```

- [ ] **Step 2: Replace the Row + Checkbox**

```kotlin
@Composable
private fun ParallelTestConfigRow(
    label: String,
    detail: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val contentAlpha = if (enabled) 1f else 0.46f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = { onToggle() },
            )
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            enabled = enabled,
            onCheckedChange = null,
            modifier = Modifier.clearAndSetSemantics {},
            colors = CheckboxDefaults.colors(
                checkedColor = WhiteDnsPalette.Accent,
                uncheckedColor = WhiteDnsPalette.ControlBorder,
                checkmarkColor = WhiteDnsPalette.OnAccent,
                disabledCheckedColor = WhiteDnsPalette.Accent.copy(alpha = 0.42f),
                disabledUncheckedColor = WhiteDnsPalette.ControlBorder.copy(alpha = 0.42f),
            ),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = WhiteDnsPalette.Ink.copy(alpha = contentAlpha),
                    fontWeight = FontWeight.Medium,
                ),
            )
            Text(
                text = detail,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = WhiteDnsPalette.Muted.copy(alpha = contentAlpha),
                ),
            )
        }
    }
}
```

- [ ] **Step 3: Verify the file compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Manual TalkBack check**

With TalkBack enabled, open the Parallel Test panel and focus a config row. Expected: `<label>` + `<detail>` + "checkbox" + "checked" / "not checked" + "double-tap to toggle". Tap; state should flip and re-announce. A disabled row should be announced as "disabled" by TalkBack and not respond to double-tap.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "fix(a11y): make ParallelTestConfigRow a Role.Checkbox toggleable row

Single focus stop per config, native checked/not-checked announcement,
honors the row's enabled parameter for the screen reader."
```

---

## Task 6: Migrate cd_connect_button_* into WhiteDnsStrings + WhiteDnsL10n

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsStrings.kt` (interface ~line 305, English impl ~line 806, Persian impl ~line 1287)
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsTheme.kt` (`WhiteDnsL10n` getters around line 447-451)
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt:6657-6663`

The Connect button's icon `contentDescription` uses `stringResource(R.string.cd_connect_button_*)`, which follows the **Android system locale**, not the in-app picker. Migrate the three keys (`cd_connect_button_disconnected`, `cd_connect_button_connecting`, `cd_connect_button_connected`) into the in-app `WhiteDnsStrings` pipeline so the button announces in whatever language the user picked inside the app — the same pipeline as `btnConnect` itself. The fourth key (`cd_connect_button_disabled`) is currently unreferenced in the code (verify with grep); leave that as-is and document.

- [ ] **Step 1: Verify which cd_connect_button_* keys are referenced**

Run: `grep -rn "cd_connect_button" app/src/main/java app/src/main/res`

Expected hits in code: three lines in `WhiteDnsScreen.kt` around 6659-6661 referencing `disconnected`, `connecting`, `connected`. Hits in `res/values/strings.xml`: all four (`disconnected`, `connecting`, `connected`, `disabled`). The `disabled` resource is dead — flag it and leave for a follow-up cleanup, do not remove in this task.

- [ ] **Step 2: Add three interface entries to WhiteDnsStrings**

Edit `app/src/main/java/shop/whitedns/client/ui/WhiteDnsStrings.kt`. Locate the existing `cd*` block in the `interface WhiteDnsStrings` (around lines 305-309 for parallel-test cd strings). Insert these three lines in that block, immediately after `val cdParallelTestPing: String`:

```kotlin
    val cdConnectButtonDisconnected: String
    val cdConnectButtonConnecting: String
    val cdConnectButtonConnected: String
```

- [ ] **Step 3: Add English implementations**

In the same file, locate `object EnglishStrings : WhiteDnsStrings` (around line 525-) and find the existing `override val cdParallelTestSpeed = ...` block (around 806-807). Insert immediately after:

```kotlin
    override val cdConnectButtonDisconnected = "Connect button - tap to start VPN"
    override val cdConnectButtonConnecting = "Connecting - establishing VPN connection"
    override val cdConnectButtonConnected = "Stop button - tap to disconnect VPN"
```

(Strings copied verbatim from `res/values/strings.xml` so behavior is identical when system locale = English.)

- [ ] **Step 4: Add Persian implementations**

In the same file, locate `object PersianStrings : WhiteDnsStrings` (around line 1010-) and find the existing `override val cdParallelTestSpeed = ...` block (around 1287-1288). Insert immediately after:

```kotlin
    override val cdConnectButtonDisconnected = "دکمه اتصال - برای شروع VPN ضربه بزنید"
    override val cdConnectButtonConnecting = "در حال اتصال - برقراری اتصال VPN"
    override val cdConnectButtonConnected = "دکمه توقف - برای قطع VPN ضربه بزنید"
```

(Translations match the tone of the existing Persian `cd_*` strings in `res/values-fa/strings.xml`. If that file has different official translations, prefer those — copy them across rather than re-translating.)

- [ ] **Step 5: Add WhiteDnsL10n getters**

Edit `app/src/main/java/shop/whitedns/client/ui/WhiteDnsTheme.kt`. Locate the existing `val cdParallelTestSpeed: String @Composable get() = ...` line (~447) and insert immediately after the parallel-test pair:

```kotlin
    val cdConnectButtonDisconnected: String @Composable get() = LocalWhiteDnsStrings.current.cdConnectButtonDisconnected
    val cdConnectButtonConnecting: String @Composable get() = LocalWhiteDnsStrings.current.cdConnectButtonConnecting
    val cdConnectButtonConnected: String @Composable get() = LocalWhiteDnsStrings.current.cdConnectButtonConnected
```

- [ ] **Step 6: Update ConnectButton to consume WhiteDnsL10n**

Edit `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` lines 6657-6663. Replace:

```kotlin
                        contentDescription = stringResource(
                            when (status) {
                                ConnectionStatus.DISCONNECTED -> R.string.cd_connect_button_disconnected
                                ConnectionStatus.CONNECTING -> R.string.cd_connect_button_connecting
                                ConnectionStatus.CONNECTED -> R.string.cd_connect_button_connected
                            }
                        ),
```

with:

```kotlin
                        contentDescription = when (status) {
                            ConnectionStatus.DISCONNECTED -> WhiteDnsL10n.cdConnectButtonDisconnected
                            ConnectionStatus.CONNECTING -> WhiteDnsL10n.cdConnectButtonConnecting
                            ConnectionStatus.CONNECTED -> WhiteDnsL10n.cdConnectButtonConnected
                        },
```

Also, the Connect circle (the clickable `Box` at lines 6632-6644) is currently a click target with no merged semantics, which means TalkBack reads the Icon's `contentDescription` and then the Text label and the progress text separately. Add `.semantics(mergeDescendants = true) {}` after the `.clickable(...)` modifier so the whole button announces as one focusable node:

Edit lines 6632-6644 to add the merged-semantics modifier:

```kotlin
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape)
                    .background(if (enabled) WhiteDnsPalette.Surface else WhiteDnsPalette.SurfaceAlt)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            haptic.performMedium()
                            onClick()
                        },
                    )
                    .semantics(mergeDescendants = true) {},
                contentAlignment = Alignment.Center,
            ) {
```

Note: `mergeDescendants` here intentionally keeps the visible button label (CONNECT / اتصال) and the icon description as separate text contributions to the merged node — TalkBack will read the icon description, then the label, then any progress text, but as a **single focus stop** rather than three. This is the correct trade-off given the user reported the Connect button reads in English: now both the icon cd and the label come from `WhiteDnsL10n`, so both follow the in-app picker.

- [ ] **Step 7: Verify everything compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`. If "unresolved reference: cdConnectButtonDisconnected" appears, you missed one of Steps 2/3/4/5.

- [ ] **Step 8: Manual TalkBack check**

Install the build. With TalkBack on and in-app language set to Persian (with device system language in English to make the gap visible):
- Focus the Connect button while disconnected. Expected: Persian content description (`دکمه اتصال...`) followed by the Persian label (`اتصال`), as a single focus stop.
- Tap to connect. While connecting, the button should re-announce in Persian.
- Once connected, the description should be the Persian stop string.

Compare against current behavior: the description should no longer be in English at any state.

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsStrings.kt \
        app/src/main/java/shop/whitedns/client/ui/WhiteDnsTheme.kt \
        app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "fix(a11y): localize Connect button screen-reader strings

Migrates cd_connect_button_* into WhiteDnsStrings / WhiteDnsL10n so the
button's accessibility description follows the in-app language picker,
not the device system locale. Adds merged semantics so the button is a
single focus stop."
```

---

## Task 7: Remove now-unused cd_* resource strings

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fa/strings.xml`

After Tasks 2-6, six resource keys are no longer referenced from Kotlin code: `cd_navigate_to_tab`, `cd_profile_tab_selected`, `cd_profile_tab_unselected`, `cd_toggle_row_on`, `cd_toggle_row_off`, `cd_connect_button_disconnected`, `cd_connect_button_connecting`, `cd_connect_button_connected`. The `cd_connect_button_disabled` key was unused before this work and remains unused — it can be removed too. Delete them from both locale files so they don't bit-rot.

- [ ] **Step 1: Confirm no remaining usages**

Run: `grep -rn "cd_navigate_to_tab\|cd_profile_tab_\|cd_toggle_row_\|cd_connect_button_" app/src/main/java`

Expected output: **no matches**. If anything matches, that task left a stale reference — go back and fix it before removing the resource.

- [ ] **Step 2: Remove keys from `res/values/strings.xml`**

Open `app/src/main/res/values/strings.xml` and delete these lines (use the exact `<string name="...">` keys, not the surrounding context — they currently live at lines 13-16 and 225-240, but line numbers will shift as you delete):

- `cd_connect_button_disconnected`
- `cd_connect_button_connecting`
- `cd_connect_button_connected`
- `cd_connect_button_disabled`
- `cd_navigate_to_tab`
- `cd_profile_tab_selected`
- `cd_profile_tab_unselected`
- `cd_toggle_row_on`
- `cd_toggle_row_off`

- [ ] **Step 3: Remove the same keys from `res/values-fa/strings.xml`**

Delete the corresponding Persian translations of the same nine keys. Use the same key names — XML keys are language-independent.

- [ ] **Step 4: Verify the build still passes**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. Android lint will flag any remaining `R.string.cd_*` reference, which would mean Step 1's grep missed something.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/values/strings.xml app/src/main/res/values-fa/strings.xml
git commit -m "chore(a11y): remove cd_* resources superseded by native semantics

These keys are no longer referenced from Kotlin code now that bottom
nav, profile tabs, ToggleRow, ParallelTestConfigRow, and the Connect
button use native Role-based semantics or the in-app WhiteDnsL10n
pipeline."
```

---

## Task 8: Full TalkBack regression walkthrough

**Files:** (no code changes — verification only)

Before declaring the work done, walk the user's exact reported flows and confirm each is fixed.

- [ ] **Step 1: Install the debug build on a real device**

Run: `./gradlew :app:installDebug`
Open the app and turn on TalkBack (Settings → Accessibility → TalkBack).

- [ ] **Step 2: Bottom nav announcement check**

- Set in-app language to Persian.
- Swipe-navigate to the bottom navigation bar. Focus each tab in turn.
- **Pass criteria:** Each tab is announced **once**, in Persian, with "selected" (in device locale) appended to the current tab. No "Navigate to..." prefix. No triple-announce.
- Repeat with in-app language set to English. Same criteria with English labels.

- [ ] **Step 3: Profile tab switch check**

- Navigate to the Profiles screen.
- Focus each profile tab (Personal / Shared).
- **Pass criteria:** Single announcement per tab, label localized, "selected" appended only on the active one.

- [ ] **Step 4: ToggleRow check (Parallel Test enable/disable lives here)**

- Open App Settings (or wherever ToggleRow appears).
- Focus a toggle.
- **Pass criteria:** Announcement of the form `<label>, switch, on/off, double-tap to toggle` (state and "switch" word come from device locale). No "tap to enable / tap to disable" phrase. Double-tap flips state and re-announces.

- [ ] **Step 5: Parallel Test config row check**

- Open the Parallel Test config row panel.
- Focus a checkbox row.
- **Pass criteria:** Announcement of the form `<label>, <detail>, checkbox, checked/not checked, double-tap to toggle`. Single focus stop per config (no separate stop on the checkbox itself). Double-tap flips the state.

- [ ] **Step 6: Connect button check**

- Set in-app language to Persian (device system can stay English).
- Focus the Connect button while disconnected. **Pass criteria:** Persian description text. Single focus stop (no separate stop on icon vs. text label).
- Tap to connect; observe the announcement during CONNECTING and after CONNECTED. Both should be in Persian.

- [ ] **Step 7: Note any failures inline and re-open the relevant task**

If any pass criterion fails, do not "fix it up" inside Task 8 — go back to the relevant earlier task and amend, then re-run Task 8.

- [ ] **Step 8: Final commit (if no fixes were needed, this step is a no-op — skip it)**

If Tasks 2-7 already produced the right behavior, do not create an empty commit here.

---

## Self-review

Cross-checked against the user's four explicit asks:

1. **Restore "selected" tab announcement** → Task 2 (bottom nav) + Task 3 (profile tabs) via `Role.Tab` + `selectable`. ✓
2. **Eliminate redundant tab announcements** → Task 2 via `mergeDescendants = true` on the row + `contentDescription = null` on the Icon. ✓
3. **Localize Connect button and Parallel Test in the user's chosen in-app language** → Task 6 migrates Connect button cd strings into `WhiteDnsStrings`/`WhiteDnsL10n` (in-app pipeline). Parallel Test no longer uses custom strings (Tasks 4 & 5 use native `Role.Switch`/`Role.Checkbox`), so the question of *which* localization channel applies to the label-portion is moot — the row uses the visible label, which is already in `WhiteDnsL10n`, plus a state word from the device locale (documented limitation). ✓
4. **Replace custom toggle implementations with native controls** → Task 4 (`Role.Switch` on `ToggleRow`) + Task 5 (`Role.Checkbox` on `ParallelTestConfigRow`). The native Switch and Checkbox now own the visual state; the row owns the click + semantic role. ✓

Placeholder scan: no "TBD", "add appropriate", "implement later", or "similar to Task N" left in the plan. Code blocks are complete for every modification step.

Type/name consistency: `cdConnectButtonDisconnected`/`Connecting`/`Connected` (camelCase) used identically in WhiteDnsStrings interface, EnglishStrings, PersianStrings, WhiteDnsL10n, and the ConnectButton call site. `Role.Tab` / `Role.Switch` / `Role.Checkbox` referenced consistently with import added in Task 1. `mergeDescendants = true` spelled identically in Tasks 2, 3, and 6.
