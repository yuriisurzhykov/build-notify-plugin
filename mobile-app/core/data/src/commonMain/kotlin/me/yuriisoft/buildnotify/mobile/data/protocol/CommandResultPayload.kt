package me.yuriisoft.buildnotify.mobile.data.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Universal response to any client command.
 * Always arrives with WsEnvelope.correlationId = <id of the original command>.
 *
 * [errorCode] is a machine-readable SNAKE_UPPER constant, suitable for i18n.
 * [message] is a human-readable detail for logging / debugging.
 */
@Serializable
@SerialName("cmd.result")
data class CommandResultPayload(
    val status: CommandStatus,
    val errorCode: String? = null,
    val message: String? = null,
) : WsPayload()
