package me.yuriisoft.buildnotify.mobile.ui.resource

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.jetbrains.compose.resources.StringResource

@Composable
fun textResource(resource: StringResource): TextResource {
    return remember(resource) {
        TextResource.ResText(resource)
    }
}

@Composable
fun textResource(resource: StringResource, vararg args: Any): TextResource {
    return remember(resource, args) {
        TextResource.ResText(resource, *args)
    }
}

@Composable
fun textResource(resource: String): TextResource {
    return remember(resource) {
        TextResource.RawText(resource)
    }
}