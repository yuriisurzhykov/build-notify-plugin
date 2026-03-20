package me.yuriisoft.buildnotify

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class BuildNotifyPluginDisposable : Disposable {
    override fun dispose() {
        // Automatically called when the project is closed
    }

    companion object {
        fun getInstance(project: Project): BuildNotifyPluginDisposable {
            return project.getService(BuildNotifyPluginDisposable::class.java)
        }
    }
}