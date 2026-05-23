package shop.whitedns.client.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import shop.whitedns.client.model.ResolverProfile

class SplitTunnelAppOptionsTest {
    @Test
    fun visibleOptionsKeepSelectedAppsAtTop() {
        val apps = listOf(
            SplitTunnelAppInfo(packageName = "org.zed", label = "Zed"),
            SplitTunnelAppInfo(packageName = "org.alpha", label = "Alpha"),
            SplitTunnelAppInfo(packageName = "org.beta", label = "Beta"),
        )

        val visible = splitTunnelVisibleAppOptions(
            apps = apps,
            selectedPackages = setOf("org.beta"),
            query = "",
            showSystemApps = false,
        )

        assertEquals(
            listOf("org.beta", "org.alpha", "org.zed"),
            visible.map { it.packageName },
        )
    }

    @Test
    fun visibleOptionsHideUnselectedSystemAppsUntilEnabled() {
        val apps = listOf(
            SplitTunnelAppInfo(packageName = "com.android.gms", label = "Google Play services", isSystemApp = true),
            SplitTunnelAppInfo(packageName = "org.alpha", label = "Alpha"),
            SplitTunnelAppInfo(packageName = "com.android.settings", label = "Settings", isSystemApp = true),
        )

        val visibleWithoutSystem = splitTunnelVisibleAppOptions(
            apps = apps,
            selectedPackages = emptySet(),
            query = "",
            showSystemApps = false,
        )
        val visibleWithSystem = splitTunnelVisibleAppOptions(
            apps = apps,
            selectedPackages = emptySet(),
            query = "",
            showSystemApps = true,
        )

        assertEquals(listOf("org.alpha"), visibleWithoutSystem.map { it.packageName })
        assertEquals(
            listOf("org.alpha", "com.android.gms", "com.android.settings"),
            visibleWithSystem.map { it.packageName },
        )
    }

    @Test
    fun visibleOptionsKeepSelectedSystemAppsVisible() {
        val apps = listOf(
            SplitTunnelAppInfo(packageName = "com.android.gms", label = "Google Play services", isSystemApp = true),
            SplitTunnelAppInfo(packageName = "org.alpha", label = "Alpha"),
        )

        val visible = splitTunnelVisibleAppOptions(
            apps = apps,
            selectedPackages = setOf("com.android.gms"),
            query = "",
            showSystemApps = false,
        )

        assertEquals(listOf("com.android.gms", "org.alpha"), visible.map { it.packageName })
    }

    @Test
    fun visibleOptionsIncludeSelectedPackagesMissingFromAppList() {
        val apps = listOf(
            SplitTunnelAppInfo(packageName = "org.alpha", label = "Alpha"),
        )

        val visible = splitTunnelVisibleAppOptions(
            apps = apps,
            selectedPackages = setOf("com.google.android.gms"),
            query = "",
            showSystemApps = false,
        )

        assertEquals(listOf("com.google.android.gms", "org.alpha"), visible.map { it.packageName })
        assertEquals("com.google.android.gms", visible.first().label)
    }

    @Test
    fun manualPackageSelectionTrimsPackageNameAndIgnoresBlankInput() {
        val selected = addSplitTunnelPackageSelection(
            selectedPackages = setOf("org.alpha"),
            rawPackageName = " com.google.android.gms ",
        )
        val unchanged = addSplitTunnelPackageSelection(
            selectedPackages = selected,
            rawPackageName = " ",
        )

        assertEquals(setOf("org.alpha", "com.google.android.gms"), selected)
        assertEquals(selected, unchanged)
    }

    @Test
    fun orderedSelectionPreservesInstalledOrderAndUnknownPackages() {
        val apps = listOf(
            SplitTunnelAppInfo(packageName = "org.alpha", label = "Alpha"),
            SplitTunnelAppInfo(packageName = "org.beta", label = "Beta"),
            SplitTunnelAppInfo(packageName = "org.zed", label = "Zed"),
        )

        val selected = orderedSplitTunnelPackageSelection(
            apps = apps,
            selectedPackages = setOf("org.zed", "missing.package", "org.alpha"),
        )

        assertEquals(listOf("org.alpha", "org.zed", "missing.package"), selected)
    }

    @Test
    fun resolverDropTargetIndexSkipsDefaultResolverRow() {
        val profiles = listOf(
            ResolverProfile.defaultProfile("1.1.1.1"),
            ResolverProfile(id = "resolver-first", name = "First", resolverText = "8.8.8.8"),
            ResolverProfile(id = "resolver-second", name = "Second", resolverText = "9.9.9.9"),
        )

        assertEquals(0, resolverCustomDropTargetIndex(profiles, fullTargetIndex = 1))
        assertEquals(1, resolverCustomDropTargetIndex(profiles, fullTargetIndex = 2))
    }
}
