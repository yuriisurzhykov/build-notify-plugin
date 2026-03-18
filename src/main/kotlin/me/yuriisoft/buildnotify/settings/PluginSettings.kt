package me.yuriisoft.buildnotify.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "BuildNotifySettings",
    storages = [Storage("buildNotifySettings.xml")]
)
@Service(Service.Level.APP)
class PluginSettings : PersistentStateComponent<PluginSettings.State> {

    data class State(
        var port: Int = 8765,
        var serviceName: String = "AndroidStudio-BuildNotify",
        var sendWarnings: Boolean = true,
        var maxIssuesPerNotification: Int = 20,
        var heartbeatIntervalSec: Int = 30,
    )

    private var _state: State = State()

    override fun getState(): State = _state

    override fun loadState(state: State) {
        _state = state
    }
}