package com.yuriisurzhykov.buildnotifier.feature.catalog

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import me.yuriisoft.buildnotify.mobile.core.navigation.Destination

@Serializable
@Immutable
object CatalogDestination : Destination("catalog")