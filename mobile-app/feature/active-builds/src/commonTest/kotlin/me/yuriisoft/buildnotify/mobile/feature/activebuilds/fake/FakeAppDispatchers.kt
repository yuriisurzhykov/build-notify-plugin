package me.yuriisoft.buildnotify.mobile.feature.activebuilds.fake

import kotlinx.coroutines.CoroutineDispatcher
import me.yuriisoft.buildnotify.mobile.core.dispatchers.AppDispatchers

class FakeAppDispatchers(
    dispatcher: CoroutineDispatcher,
) : AppDispatchers.Abstract(
    main = dispatcher,
    io = dispatcher,
    default = dispatcher,
)
