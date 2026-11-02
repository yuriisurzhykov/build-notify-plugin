package me.yuriisoft.buildnotify.mobile.designsystem.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Graphite icon set: 24×24 grid, 2.2 px round strokes (filled where a glyph must
 * read at 12 px). Drawn black and tinted by [Icon][me.yuriisoft.buildnotify.mobile.designsystem.components.Icon].
 */
object GraphiteIcons {

    val Check: ImageVector by lazy {
        stroke("Check", width = 2.8f) {
            moveTo(5f, 12.5f); lineTo(9.5f, 17f); lineTo(19f, 7.5f)
        }
    }

    val Close: ImageVector by lazy {
        stroke("Close", width = 2.8f) {
            moveTo(6f, 6f); lineTo(18f, 18f)
            moveTo(18f, 6f); lineTo(6f, 18f)
        }
    }

    val Minus: ImageVector by lazy {
        stroke("Minus", width = 2.8f) { moveTo(7f, 12f); lineTo(17f, 12f) }
    }

    val ArrowRight: ImageVector by lazy {
        stroke("ArrowRight", width = 2.4f) {
            moveTo(5f, 12f); lineTo(19f, 12f)
            moveTo(13f, 6f); lineTo(19f, 12f); lineTo(13f, 18f)
        }
    }

    val ArrowBack: ImageVector by lazy {
        stroke("ArrowBack") { moveTo(15f, 5f); lineTo(8f, 12f); lineTo(15f, 19f) }
    }

    val ArrowDown: ImageVector by lazy {
        stroke("ArrowDown", width = 2.4f) {
            moveTo(12f, 5f); lineTo(12f, 19f)
            moveTo(6f, 13f); lineTo(12f, 19f); lineTo(18f, 13f)
        }
    }

    val Terminal: ImageVector by lazy {
        stroke("Terminal") {
            moveTo(5f, 7f); lineTo(10f, 12f); lineTo(5f, 17f)
            moveTo(13f, 17f); lineTo(19f, 17f)
        }
    }

    val Rerun: ImageVector by lazy {
        stroke("Rerun") {
            moveTo(20f, 12f)
            arcToRelative(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = -2.3f, dy1 = -5.7f)
            moveTo(20f, 4f); lineTo(20f, 9f); lineTo(15f, 9f)
        }
    }

    val Copy: ImageVector by lazy {
        stroke("Copy", width = 2f) {
            moveTo(11f, 9f); lineTo(18f, 9f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 2f, dy1 = 2f)
            lineTo(20f, 18f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2f, dy1 = 2f)
            lineTo(11f, 20f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2f, dy1 = -2f)
            lineTo(9f, 11f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 2f, dy1 = -2f)
            close()
            moveTo(5f, 15f); lineTo(5f, 5f); lineTo(15f, 5f)
        }
    }

    val Settings: ImageVector by lazy {
        stroke("Settings", width = 2f) {
            moveTo(4f, 7f); lineTo(14f, 7f)
            moveTo(18f, 7f); lineTo(20f, 7f)
            moveTo(4f, 17f); lineTo(8f, 17f)
            moveTo(12f, 17f); lineTo(20f, 17f)
            circle(16f, 7f, 2f)
            circle(10f, 17f, 2f)
        }
    }

    val Info: ImageVector by lazy {
        stroke("Info", width = 2f) {
            circle(12f, 12f, 9f)
            moveTo(12f, 11f); lineTo(12f, 16.5f)
            moveTo(12f, 7.6f); lineTo(12f, 7.7f)
        }
    }

    val Stop: ImageVector by lazy {
        fill("Stop") {
            moveTo(7.5f, 5f); lineTo(16.5f, 5f)
            arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 2.5f, dy1 = 2.5f)
            lineTo(19f, 16.5f)
            arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2.5f, dy1 = 2.5f)
            lineTo(7.5f, 19f)
            arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = -2.5f, dy1 = -2.5f)
            lineTo(5f, 7.5f)
            arcToRelative(2.5f, 2.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 2.5f, dy1 = -2.5f)
            close()
        }
    }

    val Play: ImageVector by lazy {
        fill("Play") { moveTo(8f, 5.5f); lineTo(8f, 18.5f); lineTo(18.5f, 12f); close() }
    }

    val Agent: ImageVector by lazy {
        stroke("Agent", width = 2f) {
            moveTo(12f, 3f); lineTo(13.8f, 8.2f); lineTo(19f, 10f); lineTo(13.8f, 11.8f)
            lineTo(12f, 17f); lineTo(10.2f, 11.8f); lineTo(5f, 10f); lineTo(10.2f, 8.2f); close()
            moveTo(18f, 16f); lineTo(18.8f, 18.2f); lineTo(21f, 19f); lineTo(18.8f, 19.8f)
            lineTo(18f, 22f); lineTo(17.2f, 19.8f); lineTo(15f, 19f); lineTo(17.2f, 18.2f); close()
        }
    }
}

private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    moveTo(cx - r, cy)
    arcToRelative(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = 2 * r, dy1 = 0f)
    arcToRelative(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = -2 * r, dy1 = 0f)
    close()
}

private fun builder(name: String) = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
)

private fun stroke(name: String, width: Float = 2.2f, block: PathBuilder.() -> Unit): ImageVector =
    builder(name).path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = width,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = block,
    ).build()

private fun fill(name: String, block: PathBuilder.() -> Unit): ImageVector =
    builder(name).path(fill = SolidColor(Color.Black), pathBuilder = block).build()
