# Accessibility Improvements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add TalkBack/screen-reader accessibility to all 19 missing or weak labels in WhiteDNS.

**Architecture:** Two-file change — add 20 content description strings to `strings.xml`, then apply `.semantics { contentDescription = ... }` modifiers in-place on clickable composables in `WhiteDnsScreen.kt`. No refactoring, no new files.

**Tech Stack:** Kotlin, Jetpack Compose, `androidx.compose.ui.semantics`, Android string resources.

**Prerequisites:** Android SDK configured via `local.properties` (`sdk.dir=/path/to/sdk`) or `ANDROID_HOME` env var. Required for build verification steps.

---

## Files

| File | Change |
|------|--------|
| `app/src/main/res/values/strings.xml` | Add 20 new `<string>` entries |
| `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` | Add 2 imports + 17 in-place modifier changes |

---

### Task 1: Add accessibility string resources

**Files:**
- Modify: `app/src/main/res/values/strings.xml` (after line 220, before `</resources>`)

- [ ] **Step 1: Add the 20 new strings**

Open `app/src/main/res/values/strings.xml`. Insert the following block immediately before the closing `</resources>` tag (currently at line 221):

```xml
    <!-- Accessibility — Missing Labels -->
    <string name="cd_parallel_test_expand">Expand parallel test configurations</string>
    <string name="cd_parallel_test_collapse">Collapse parallel test configurations</string>
    <string name="cd_navigate_to_tab">Navigate to %1$s</string>
    <string name="cd_profile_tab_selected">%1$s, selected tab</string>
    <string name="cd_profile_tab_unselected">%1$s tab</string>
    <string name="cd_telegram_link">Open WhiteDNS Telegram community, opens external app</string>
    <string name="cd_select_profile_item">Select %1$s profile</string>
    <string name="cd_logo_telegram">WhiteDNS logo, opens Telegram community</string>
    <string name="cd_menu_button">Open app menu</string>
    <string name="cd_copy_address">Copy %1$s to clipboard</string>
    <string name="cd_enable_vpn_notification">Enable VPN notification permission</string>
    <string name="cd_allow_background_vpn">Allow WhiteDNS to run in background</string>
    <string name="cd_split_tunnel_app_toggle">Toggle %1$s in split tunnel</string>
    <string name="cd_stat_card_detail">%1$s: %2$s, tap for details</string>
    <string name="cd_section_expand">Expand %1$s section</string>
    <string name="cd_section_collapse">Collapse %1$s section</string>
    <string name="cd_toggle_row_on">%1$s, enabled, tap to disable</string>
    <string name="cd_toggle_row_off">%1$s, disabled, tap to enable</string>
    <string name="cd_scan_autosave_enabled">Scan auto-save enabled</string>
    <string name="cd_worker_count_slider">Worker count slider, currently %1$d workers</string>
```

- [ ] **Step 2: Verify XML is well-formed**

```bash
python3 -m xml.etree.ElementTree app/src/main/res/values/strings.xml 2>&1 || python3 -c "import xml.etree.ElementTree as ET; ET.parse('app/src/main/res/values/strings.xml'); print('XML valid')"
```

Expected: `XML valid` (no error output)

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/values/strings.xml
git commit -m "feat(a11y): add 20 missing content description strings"
```

---

### Task 2: Add semantics imports to WhiteDnsScreen.kt

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt`

The file currently imports from `androidx.compose.ui.*` (lines ~104–135). `semantics` is not yet imported.

- [ ] **Step 1: Add two imports**

Find the block of `androidx.compose.ui.*` imports. After line `import androidx.compose.ui.res.stringResource` (currently ~line 133), add:

```kotlin
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
```

- [ ] **Step 2: Verify the file still compiles (if SDK is configured)**

```bash
./gradlew compileDebugKotlin 2>&1 | tail -5
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "feat(a11y): import semantics modifier"
```

---

### Task 3: Fix weak Scanner info contentDescription

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` (~line 1546)

The `Icons.Rounded.Check` icon currently has `contentDescription = "Scanner info"` — vague and not useful to screen reader users.

- [ ] **Step 1: Replace the hardcoded string**

Find this exact block (~line 1544):
```kotlin
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Scanner info",
                tint = WhiteDnsPalette.AccentText,
                modifier = Modifier.size(16.dp),
            )
```

Replace with:
```kotlin
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = stringResource(R.string.cd_scan_autosave_enabled),
                tint = WhiteDnsPalette.AccentText,
                modifier = Modifier.size(16.dp),
            )
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "fix(a11y): replace vague scanner info contentDescription"
```

---

### Task 4: Add semantics to parallel test expand/collapse Row

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` (~line 973)

The Row that expands/collapses parallel test configurations is clickable but has no label for TalkBack.

- [ ] **Step 1: Add semantics modifier**

Find this exact block (~line 973):
```kotlin
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    haptic.performLight()
                    onExpandedChange(!expanded)
                }
                .padding(vertical = 8.dp, horizontal = 4.dp),
```

Replace with:
```kotlin
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .semantics {
                    contentDescription = if (expanded) {
                        context.getString(R.string.cd_parallel_test_collapse)
                    } else {
                        context.getString(R.string.cd_parallel_test_expand)
                    }
                }
                .clickable {
                    haptic.performLight()
                    onExpandedChange(!expanded)
                }
                .padding(vertical = 8.dp, horizontal = 4.dp),
```

Note: check whether `context` is already available in the enclosing composable scope (look for `val context = LocalContext.current` nearby). If not, add `val context = LocalContext.current` at the top of that composable function.

Alternatively, if `stringResource` is available at the composable scope (it requires `@Composable` context — which it is inside a composable), use:
```kotlin
                .semantics {
                    contentDescription = if (expanded) {
                        context.getString(R.string.cd_parallel_test_collapse)
                    } else {
                        context.getString(R.string.cd_parallel_test_expand)
                    }
                }
```

**Important:** `stringResource()` cannot be called inside a `.semantics { }` lambda (it's not a `@Composable` context). Use `context.getString(R.string.xxx)` instead, where `context` comes from `LocalContext.current`.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "feat(a11y): label parallel test expand/collapse row"
```

---

### Task 5: Add semantics to bottom nav tab Columns

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` (~line 1829)

Each tab Column in the bottom navigation is clickable but has no container-level label. TalkBack focuses the container, not the child Icon.

- [ ] **Step 1: Add semantics modifier**

Find this exact block (~line 1828):
```kotlin
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(background)
                        .clickable {
                            haptic.performLight()
                            onTabSelected(tab)
                        }
                        .padding(vertical = 8.dp),
```

Replace with:
```kotlin
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(background)
                        .semantics {
                            contentDescription = context.getString(
                                R.string.cd_navigate_to_tab, tab.label
                            )
                        }
                        .clickable {
                            haptic.performLight()
                            onTabSelected(tab)
                        }
                        .padding(vertical = 8.dp),
```

Ensure `val context = LocalContext.current` exists in the enclosing composable.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "feat(a11y): label bottom nav tab columns"
```

---

### Task 6: Add semantics to ProfileTabSwitch Boxes

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` (~line 1880)

Each tab Box in the `ProfileTabSwitch` is clickable but has no TalkBack label.

- [ ] **Step 1: Add semantics modifier**

Find this exact block (~line 1880):
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
                    .clickable {
                        haptic.performLight()
                        onTabSelected(tab)
                    }
                    .padding(horizontal = 8.dp, vertical = 11.dp),
```

Replace with:
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
                    .semantics {
                        contentDescription = if (selected) {
                            context.getString(R.string.cd_profile_tab_selected, tab.label)
                        } else {
                            context.getString(R.string.cd_profile_tab_unselected, tab.label)
                        }
                    }
                    .clickable {
                        haptic.performLight()
                        onTabSelected(tab)
                    }
                    .padding(horizontal = 8.dp, vertical = 11.dp),
```

Ensure `val context = LocalContext.current` exists in the `ProfileTabSwitch` composable.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "feat(a11y): label profile tab switch boxes"
```

---

### Task 7: Add semantics to Telegram link Text

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` (~line 1929)

The clickable Telegram URL Text has no announcement that it opens an external app.

- [ ] **Step 1: Add semantics modifier**

Find this exact block (~line 1928):
```kotlin
        Text(
            text = WhiteDnsTelegramUrl,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable {
                    haptic.performLight()
                    openWhiteDnsTelegram(context)
                }
                .padding(horizontal = 8.dp, vertical = 3.dp),
```

Replace with:
```kotlin
        Text(
            text = WhiteDnsTelegramUrl,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .semantics {
                    contentDescription = context.getString(R.string.cd_telegram_link)
                }
                .clickable {
                    haptic.performLight()
                    openWhiteDnsTelegram(context)
                }
                .padding(horizontal = 8.dp, vertical = 3.dp),
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "feat(a11y): label telegram link text"
```

---

### Task 8: Add semantics to profile selection card Row

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` (~line 2687)

The resolver/connection profile selection card Row is clickable but gives TalkBack no context about what selecting it does.

- [ ] **Step 1: Add semantics modifier**

Find this exact block (~line 2687):
```kotlin
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) WhiteDnsPalette.AccentSurface else WhiteDnsPalette.Surface)
            .border(
                1.5.dp,
                if (selected) WhiteDnsPalette.Accent.copy(alpha = 0.28f) else WhiteDnsPalette.Border,
                RoundedCornerShape(12.dp),
            )
            .clickable {
                haptic.performLight()
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 11.dp),
```

Replace with:
```kotlin
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) WhiteDnsPalette.AccentSurface else WhiteDnsPalette.Surface)
            .border(
                1.5.dp,
                if (selected) WhiteDnsPalette.Accent.copy(alpha = 0.28f) else WhiteDnsPalette.Border,
                RoundedCornerShape(12.dp),
            )
            .semantics {
                contentDescription = context.getString(R.string.cd_select_profile_item, item.title)
            }
            .clickable {
                haptic.performLight()
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 11.dp),
```

Ensure `val context = LocalContext.current` exists in the enclosing composable. Verify `item.title` is the correct field name by checking the data class used at this call site.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "feat(a11y): label profile selection card"
```

---

### Task 9: Add semantics to "W" logo Box and app menu Box

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` (~lines 5422 and 5452)

Two clickable Boxes in the app header: the "W" logo (opens Telegram) and the menu icon (opens overflow menu).

- [ ] **Step 1: Label the "W" logo Box**

Find this exact block (~line 5421):
```kotlin
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(WhiteDnsPalette.SurfaceAlt)
                    .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(9.dp))
                    .clickable {
                        haptic.performLight()
                        openWhiteDnsTelegram(context)
                    },
```

Replace with:
```kotlin
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(WhiteDnsPalette.SurfaceAlt)
                    .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(9.dp))
                    .semantics { contentDescription = context.getString(R.string.cd_logo_telegram) }
                    .clickable {
                        haptic.performLight()
                        openWhiteDnsTelegram(context)
                    },
```

- [ ] **Step 2: Label the app menu Box**

Find this exact block (~line 5451):
```kotlin
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (overflowExpanded) WhiteDnsPalette.AccentSurface else WhiteDnsPalette.Surface,
                    )
                    .border(
                        1.5.dp,
                        if (overflowExpanded) WhiteDnsPalette.Accent.copy(alpha = 0.28f) else WhiteDnsPalette.Border,
                        RoundedCornerShape(10.dp),
                    )
                    .clickable {
                        haptic.performLight()
                        overflowExpanded = true
                    },
```

Replace with:
```kotlin
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (overflowExpanded) WhiteDnsPalette.AccentSurface else WhiteDnsPalette.Surface,
                    )
                    .border(
                        1.5.dp,
                        if (overflowExpanded) WhiteDnsPalette.Accent.copy(alpha = 0.28f) else WhiteDnsPalette.Border,
                        RoundedCornerShape(10.dp),
                    )
                    .semantics { contentDescription = context.getString(R.string.cd_menu_button) }
                    .clickable {
                        haptic.performLight()
                        overflowExpanded = true
                    },
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "feat(a11y): label W logo and app menu boxes"
```

---

### Task 10: Add semantics to copy address Row

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` (~line 5723)

The address display Row is clickable (copies to clipboard) but gives TalkBack no indication of what it does.

- [ ] **Step 1: Add semantics modifier**

Find this exact block (~line 5723):
```kotlin
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WhiteDnsPalette.Input)
                .border(2.5.dp, WhiteDnsPalette.Divider, RoundedCornerShape(10.dp))
                .clickable(onClick = onCopy)
                .padding(horizontal = 12.dp, vertical = 11.dp),
```

Replace with:
```kotlin
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WhiteDnsPalette.Input)
                .border(2.5.dp, WhiteDnsPalette.Divider, RoundedCornerShape(10.dp))
                .semantics { contentDescription = context.getString(R.string.cd_copy_address, address) }
                .clickable(onClick = onCopy)
                .padding(horizontal = 12.dp, vertical = 11.dp),
```

`address` is the composable parameter — verify its name at the call site. Ensure `context` is available.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "feat(a11y): label copy address row"
```

---

### Task 11: Add semantics to VPN permission Boxes

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` (~lines 5788 and 5872)

Two clickable permission request Boxes lack TalkBack labels.

- [ ] **Step 1: Label the Enable VPN Notification Box**

Find this exact block (~line 5788):
```kotlin
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Warning.copy(alpha = 0.32f), RoundedCornerShape(10.dp))
                .clickable {
                    haptic.performMedium()
                    onClick()
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "ENABLE VPN NOTIFICATION",
```

Replace the modifier chain with:
```kotlin
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Warning.copy(alpha = 0.32f), RoundedCornerShape(10.dp))
                .semantics { contentDescription = context.getString(R.string.cd_enable_vpn_notification) }
                .clickable {
                    haptic.performMedium()
                    onClick()
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "ENABLE VPN NOTIFICATION",
```

- [ ] **Step 2: Label the Allow Background VPN Box**

Find this exact block (~line 5872):
```kotlin
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Warning.copy(alpha = 0.32f), RoundedCornerShape(10.dp))
                .clickable {
                    haptic.performMedium()
                    onClick()
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "ALLOW BACKGROUND VPN",
```

Replace the modifier chain with:
```kotlin
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WhiteDnsPalette.Surface)
                .border(1.5.dp, WhiteDnsPalette.Warning.copy(alpha = 0.32f), RoundedCornerShape(10.dp))
                .semantics { contentDescription = context.getString(R.string.cd_allow_background_vpn) }
                .clickable {
                    haptic.performMedium()
                    onClick()
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "ALLOW BACKGROUND VPN",
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "feat(a11y): label VPN permission request boxes"
```

---

### Task 12: Add semantics to SplitTunnelAppRow

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` (~line 6167)

The split tunnel app Row is clickable (toggles app in/out of tunnel) but TalkBack can't describe it. `SplitTunnelAppInfo` has fields `packageName: String` and `label: String` — use `app.label` for the human-readable name.

- [ ] **Step 1: Add semantics modifier**

Find this exact block (~line 6167):
```kotlin
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .clickable {
                haptic.performLight()
                onToggle()
            }
            .padding(vertical = 9.dp, horizontal = 6.dp),
```

Replace with:
```kotlin
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .semantics {
                contentDescription = context.getString(R.string.cd_split_tunnel_app_toggle, app.label)
            }
            .clickable {
                haptic.performLight()
                onToggle()
            }
            .padding(vertical = 9.dp, horizontal = 6.dp),
```

Ensure `val context = LocalContext.current` exists in `SplitTunnelAppRow`.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "feat(a11y): label split tunnel app toggle row"
```

---

### Task 13: Add semantics to ResolverRuntimeValue Column

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` (~line 6580)

The `ResolverRuntimeValue` composable is a clickable stat card with no container-level label.

- [ ] **Step 1: Add semantics modifier**

Find this exact block (~line 6580):
```kotlin
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(WhiteDnsPalette.Surface)
            .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
```

Replace with:
```kotlin
    val context = LocalContext.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(WhiteDnsPalette.Surface)
            .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(10.dp))
            .semantics {
                contentDescription = context.getString(R.string.cd_stat_card_detail, label, value)
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
```

Note: `val context = LocalContext.current` is added here if not already present in `ResolverRuntimeValue`.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "feat(a11y): label resolver runtime value stat card"
```

---

### Task 14: Add semantics to LogActionButton

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` (~line 7472)

`LogActionButton` receives a `label: String` parameter. The Box is clickable but the `label` is not surfaced to TalkBack.

- [ ] **Step 1: Add semantics modifier**

Find this exact block (~line 7472):
```kotlin
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(WhiteDnsPalette.Surface)
            .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(8.dp))
            .clickable {
                haptic.performLight()
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
```

Replace with:
```kotlin
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(WhiteDnsPalette.Surface)
            .border(1.5.dp, WhiteDnsPalette.Border, RoundedCornerShape(8.dp))
            .semantics { contentDescription = label }
            .clickable {
                haptic.performLight()
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
```

No string resource needed — `label` is already a localized string passed by the caller.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "feat(a11y): label log action button"
```

---

### Task 15: Add semantics to SectionCard collapsible Row

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` (~lines 7823–7835)

The `SectionCard` composable has a collapsible header Row that uses a `let` block to conditionally apply `.clickable`. The semantics must also be applied conditionally (only when `collapsible == true`).

- [ ] **Step 1: Add semantics inside the let block**

Find this exact block (~line 7823):
```kotlin
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .let { modifier ->
                    if (collapsible) {
                        modifier.clickable {
                            haptic.performLight()
                            onToggle()
                        }
                    } else {
                        modifier
                    }
                }
                .padding(horizontal = 14.dp, vertical = 13.dp),
```

Replace with:
```kotlin
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .let { modifier ->
                    if (collapsible) {
                        modifier
                            .semantics {
                                contentDescription = if (expanded) {
                                    context.getString(R.string.cd_section_collapse, title)
                                } else {
                                    context.getString(R.string.cd_section_expand, title)
                                }
                            }
                            .clickable {
                                haptic.performLight()
                                onToggle()
                            }
                    } else {
                        modifier
                    }
                }
                .padding(horizontal = 14.dp, vertical = 13.dp),
```

Ensure `val context = LocalContext.current` exists in the `SectionCard` composable. Also verify `title` and `expanded` are parameters/state of `SectionCard` — check the function signature at ~line 7800.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "feat(a11y): label section card collapsible header"
```

---

### Task 16: Add semantics to ToggleRow and Slider

**Files:**
- Modify: `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` (~lines 7968 and 1689)

Two remaining issues: the `ToggleRow` clickable Row wrapper and the worker count `Slider`.

- [ ] **Step 1: Add semantics to ToggleRow Row**

Find this exact block (~line 7968):
```kotlin
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performLight()
                onToggle()
            }
            .padding(vertical = 10.dp),
```

Replace with:
```kotlin
    val context = LocalContext.current
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
```

- [ ] **Step 2: Add semantics to Slider**

Find this exact block (~line 1689):
```kotlin
        Slider(
            value = workerCount.toFloat(),
            onValueChange = { value ->
                onWorkerCountChange(value.roundToInt().coerceIn(ScanWorkerMin, ScanWorkerMax))
            },
            enabled = enabled,
            valueRange = ScanWorkerMin.toFloat()..ScanWorkerMax.toFloat(),
            steps = ScanWorkerMax - ScanWorkerMin - 1,
            colors = SliderDefaults.colors(
```

Replace with:
```kotlin
        val context = LocalContext.current
        Slider(
            value = workerCount.toFloat(),
            onValueChange = { value ->
                onWorkerCountChange(value.roundToInt().coerceIn(ScanWorkerMin, ScanWorkerMax))
            },
            enabled = enabled,
            valueRange = ScanWorkerMin.toFloat()..ScanWorkerMax.toFloat(),
            steps = ScanWorkerMax - ScanWorkerMin - 1,
            modifier = Modifier.semantics {
                contentDescription = context.getString(R.string.cd_worker_count_slider, workerCount)
            },
            colors = SliderDefaults.colors(
```

Note: `workerCount` is an `Int` — `cd_worker_count_slider` uses `%1$d` format specifier, which matches.

- [ ] **Step 3: Verify build**

```bash
./gradlew compileDebugKotlin 2>&1 | tail -5
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
git commit -m "feat(a11y): label toggle row and worker count slider"
```

---

### Task 17: Final verification

- [ ] **Step 1: Check all new string keys are referenced in code**

```bash
for key in cd_parallel_test_expand cd_parallel_test_collapse cd_navigate_to_tab cd_profile_tab_selected cd_profile_tab_unselected cd_telegram_link cd_select_profile_item cd_logo_telegram cd_menu_button cd_copy_address cd_enable_vpn_notification cd_allow_background_vpn cd_split_tunnel_app_toggle cd_stat_card_detail cd_section_expand cd_section_collapse cd_toggle_row_on cd_toggle_row_off cd_scan_autosave_enabled cd_worker_count_slider; do
  grep -q "R.string.$key" app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt && echo "OK: $key" || echo "MISSING: $key"
done
```

Expected: all 20 lines print `OK: <key>`

- [ ] **Step 2: Check no remaining hardcoded "Scanner info" string**

```bash
grep -n '"Scanner info"' app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt
```

Expected: no output

- [ ] **Step 3: Full build**

```bash
./gradlew compileDebugKotlin 2>&1 | tail -5
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Final commit if any fixups needed**

```bash
git add -p
git commit -m "fix(a11y): fixup accessibility labels after verification"
```
