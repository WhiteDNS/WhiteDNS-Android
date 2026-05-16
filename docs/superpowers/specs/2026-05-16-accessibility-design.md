# Accessibility Improvements — Design Spec
Date: 2026-05-16

## Overview

Add TalkBack/screen-reader accessibility to WhiteDNS, an Android Jetpack Compose app. All fixes are in-place within existing files — no refactoring or component extraction.

## Scope

19 issues across two files (20 new strings total):
- `app/src/main/res/values/strings.xml` — add missing content description strings
- `app/src/main/java/shop/whitedns/client/ui/WhiteDnsScreen.kt` — add semantics modifiers and fix one weak label

## Approach

Use `Modifier.semantics { contentDescription = stringResource(...) }` on clickable containers. This is the idiomatic Compose approach: TalkBack reads the semantics node of the focused container, so an explicit `contentDescription` on the container is the correct and reliable fix. Format strings (`%1$s`, `%1$d`) are used where the label is dynamic.

## Section 1 — New String Resources (`strings.xml`)

Add 19 entries under a new `<!-- Accessibility — Missing Labels -->` comment block at the end of the file (before `</resources>`):

| Key | Value |
|-----|-------|
| `cd_parallel_test_expand` | `"Expand parallel test configurations"` |
| `cd_parallel_test_collapse` | `"Collapse parallel test configurations"` |
| `cd_navigate_to_tab` | `"Navigate to %1$s"` |
| `cd_profile_tab_selected` | `"%1$s, selected tab"` |
| `cd_profile_tab_unselected` | `"%1$s tab"` |
| `cd_telegram_link` | `"Open WhiteDNS Telegram community, opens external app"` |
| `cd_select_profile_item` | `"Select %1$s profile"` |
| `cd_logo_telegram` | `"WhiteDNS logo, opens Telegram community"` |
| `cd_menu_button` | `"Open app menu"` |
| `cd_copy_address` | `"Copy %1$s to clipboard"` |
| `cd_enable_vpn_notification` | `"Enable VPN notification permission"` |
| `cd_allow_background_vpn` | `"Allow WhiteDNS to run in background"` |
| `cd_split_tunnel_app_toggle` | `"Toggle %1$s in split tunnel"` |
| `cd_stat_card_detail` | `"%1$s: %2$s, tap for details"` |
| `cd_section_expand` | `"Expand %1$s section"` |
| `cd_section_collapse` | `"Collapse %1$s section"` |
| `cd_toggle_row_on` | `"%1$s, enabled, tap to disable"` |
| `cd_toggle_row_off` | `"%1$s, disabled, tap to enable"` |
| `cd_scan_autosave_enabled` | `"Scan auto-save enabled"` |
| `cd_worker_count_slider` | `"Worker count slider, currently %1$d workers"` |

## Section 2 — Code Changes (`WhiteDnsScreen.kt`)

### Imports (add after existing `androidx.compose.ui.*` imports)
```kotlin
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
```

### In-place modifier changes

| Composable / Location | Change |
|---|---|
| Scanner info Icon (~line 1546) | Fix `contentDescription = "Scanner info"` → `stringResource(R.string.cd_scan_autosave_enabled)`. Note: `cd_scan_autosave_enabled` already exists in strings.xml. |
| Slider for worker count (~line 1689) | Add `modifier = Modifier.semantics { contentDescription = stringResource(R.string.cd_worker_count_slider, workerCount) }` |
| Parallel test expand/collapse Row (~line 974) | Add `.semantics { contentDescription = if (expanded) stringResource(R.string.cd_parallel_test_collapse) else stringResource(R.string.cd_parallel_test_expand) }` to modifier chain |
| Bottom nav tab Column (~line 1829) | Add `.semantics { contentDescription = stringResource(R.string.cd_navigate_to_tab, tab.label) }` |
| ProfileTabSwitch Box (~line 1880) | Add `.semantics { contentDescription = if (selected) stringResource(R.string.cd_profile_tab_selected, tab.label) else stringResource(R.string.cd_profile_tab_unselected, tab.label) }` |
| Telegram link Text (~line 1931) | Add `.semantics { contentDescription = stringResource(R.string.cd_telegram_link) }` to modifier chain |
| Profile selection card Row (~line 2687) | Add `.semantics { contentDescription = stringResource(R.string.cd_select_profile_item, item.title) }` |
| "W" logo Box (~line 5422) | Add `.semantics { contentDescription = stringResource(R.string.cd_logo_telegram) }` |
| App menu Box (~line 5452) | Add `.semantics { contentDescription = stringResource(R.string.cd_menu_button) }` |
| Copy address Row (~line 5724) | Add `.semantics { contentDescription = stringResource(R.string.cd_copy_address, address) }` |
| Enable VPN notification Box (~line 5789) | Add `.semantics { contentDescription = stringResource(R.string.cd_enable_vpn_notification) }` |
| Allow background VPN Box (~line 5873) | Add `.semantics { contentDescription = stringResource(R.string.cd_allow_background_vpn) }` |
| SplitTunnelAppRow Row (~line 6168) | Add `.semantics { contentDescription = stringResource(R.string.cd_split_tunnel_app_toggle, app.appName) }` |
| Stat card Column (~line 6581) | Add `.semantics { contentDescription = stringResource(R.string.cd_stat_card_detail, label, value) }` |
| LogActionButton Box (~line 7473) | Add `.semantics { contentDescription = label }` |
| SectionCard collapsible Row (~lines 7826–7831) | Add `.semantics { contentDescription = if (expanded) stringResource(R.string.cd_section_collapse, title) else stringResource(R.string.cd_section_expand, title) }` inside the `let` block before `.clickable` |
| ToggleRow Row (~line 7969) | Add `.semantics { contentDescription = if (enabled) stringResource(R.string.cd_toggle_row_on, label) else stringResource(R.string.cd_toggle_row_off, label) }` |

## Notes

- `cd_scan_autosave_enabled` is a new string (20 total). The scanner icon fix requires both a new string and correcting the existing hardcoded label.
- The `SectionCard` collapsible Row uses a `let` block to conditionally apply `.clickable`. The `.semantics` modifier must be placed inside the `let` block, applied only when `collapsible == true`, since the description references `expanded` state which is only meaningful when the section is collapsible.
- `app.appName` is the human-readable display name from `SplitTunnelAppInfo`.
- The `LogActionButton` uses `label` directly (no string resource needed) since it is already a localized string passed by the caller.

## Out of Scope

- Refactoring or file extraction
- Color contrast ratio validation
- `accessibilityLiveRegion` for dynamic content
- `Role` annotations on interactive elements
