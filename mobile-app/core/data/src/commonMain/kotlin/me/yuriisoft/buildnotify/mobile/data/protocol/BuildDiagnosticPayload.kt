package me.yuriisoft.buildnotify.mobile.data.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A compiler diagnostic detected during the build.
 * When [filePath], [line], and [column] are present the client can render
 * a tappable source reference; otherwise the diagnostic is project-level.
 */
@Serializable
@SerialName("build.diagnostic")
data class BuildDiagnosticPayload(
    val buildId: String,
    val projectName: String,
    val severity: DiagnosticSeverity,
    val message: String,
    val detail: String? = null,
    val filePath: String? = null,
    val line: Int? = null,
    val column: Int? = null,
) : WsPayload()
