package me.yuriisoft.buildnotify.mobile.feature.networkstatus

import me.yuriisoft.buildnotify.mobile.network.connection.ConnectionState
import me.yuriisoft.buildnotify.mobile.toast.model.ToastData
import me.yuriisoft.buildnotify.mobile.toast.model.ToastDuration
import me.yuriisoft.buildnotify.mobile.toast.model.ToastType
import me.yuriisoft.buildnotify.mobile.ui.resource.TextResource

/**
 * System-boundary mapping from [ConnectionState] transition pairs to
 * [ToastAction] sequences.
 *
 * The single `when` here is one of the two legitimate sites allowed by
 * code-quality rules — a mapping at the boundary between the network
 * domain and the toast/navigation domain.
 */
object ConnectionToastMapper {

    private const val NETWORK_STATUS_ID = "network-status"

    fun mapTransition(
        previous: ConnectionState,
        current: ConnectionState,
    ): List<ToastAction> = when (previous) {
        is ConnectionState.Connected if current is ConnectionState.Reconnecting                                           -> buildList {
            add(ToastAction.Show(disconnectedToast()))
        }

        is ConnectionState.Reconnecting if current is ConnectionState.Connected                                           -> buildList {
            add(ToastAction.Dismiss(NETWORK_STATUS_ID))
            add(ToastAction.Show(restoredToast()))
        }

        is ConnectionState.Reconnecting if (current is ConnectionState.Failed || current is ConnectionState.Disconnected) -> buildList {
            add(ToastAction.Dismiss(NETWORK_STATUS_ID))
            add(ToastAction.NavigateToDiscovery)
        }

        else                                                                                                              -> emptyList()
    }

    private fun disconnectedToast() = ToastData(
        id = NETWORK_STATUS_ID,
        type = ToastType.Error,
        message = TextResource.RawText("Disconnected. Trying to reconnect\u2026"),
        duration = ToastDuration.Indefinite,
    )

    private fun restoredToast() = ToastData(
        type = ToastType.Success,
        message = TextResource.RawText("Connection restored"),
    )
}
