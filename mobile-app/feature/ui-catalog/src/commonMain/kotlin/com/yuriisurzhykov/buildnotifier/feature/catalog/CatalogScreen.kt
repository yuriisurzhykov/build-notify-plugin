package com.yuriisurzhykov.buildnotifier.feature.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.navigation.NavBackStackEntry
import me.tatarka.inject.annotations.Inject
import me.yuriisoft.buildnotify.mobile.core.navigation.Destination
import me.yuriisoft.buildnotify.mobile.core.navigation.Navigator
import me.yuriisoft.buildnotify.mobile.core.navigation.Screen

@Inject
@Immutable
class CatalogScreen : Screen() {

    override val destination: Destination = CatalogDestination

    @Composable
    override fun Content(
        backStackEntry: NavBackStackEntry,
        navigator: Navigator
    ) {
        CatalogContent()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}