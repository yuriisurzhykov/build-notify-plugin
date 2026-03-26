package me.yuriisoft.buildnotify.mobile.feature.discovery.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val HubIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Hub",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 2f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 10f, 4f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 11.18f, 5.92f)
            lineTo(8.06f, 8.16f)
            arcTo(2.97f, 2.97f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.5f, 7.5f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.5f, 10.5f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.08f, 13.47f)
            lineTo(5.14f, 17.05f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4f, 19f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, 21f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8f, 19f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7.09f, 17.28f)
            lineTo(8.03f, 13.7f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 9.5f, 10.5f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 9.41f, 9.25f)
            lineTo(12.54f, 7f)
            lineTo(15.66f, 9.22f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 17.5f, 13.5f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 20.5f, 10.5f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, 17.5f, 7.5f)
            arcTo(2.97f, 2.97f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15.94f, 8.16f)
            lineTo(12.82f, 5.92f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 14f, 4f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12f, 2f)
            close()
        }
    }.build()
}

internal val LinkIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Link",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3.9f, 12f)
            arcTo(3.1f, 3.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7f, 8.9f)
            horizontalLineTo(11f)
            verticalLineTo(7f)
            horizontalLineTo(7f)
            arcTo(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 7f, 17f)
            horizontalLineTo(11f)
            verticalLineTo(15.1f)
            horizontalLineTo(7f)
            arcTo(3.1f, 3.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.9f, 12f)
            close()
            moveTo(8f, 13f)
            horizontalLineTo(16f)
            verticalLineTo(11f)
            horizontalLineTo(8f)
            verticalLineTo(13f)
            close()
            moveTo(17f, 7f)
            horizontalLineTo(13f)
            verticalLineTo(8.9f)
            horizontalLineTo(17f)
            arcTo(3.1f, 3.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17f, 15.1f)
            horizontalLineTo(13f)
            verticalLineTo(17f)
            horizontalLineTo(17f)
            arcTo(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 17f, 7f)
            close()
        }
    }.build()
}

internal val ShieldIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Shield",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 1f)
            lineTo(3f, 5f)
            verticalLineTo(11f)
            curveTo(3f, 16.55f, 6.84f, 21.74f, 12f, 23f)
            curveTo(17.16f, 21.74f, 21f, 16.55f, 21f, 11f)
            verticalLineTo(5f)
            lineTo(12f, 1f)
            close()
            moveTo(12f, 11.99f)
            horizontalLineTo(19f)
            curveTo(18.47f, 16.11f, 15.72f, 19.78f, 12f, 20.93f)
            verticalLineTo(12f)
            horizontalLineTo(5f)
            verticalLineTo(6.3f)
            lineTo(12f, 3.19f)
            verticalLineTo(11.99f)
            close()
        }
    }.build()
}

internal val RadarIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Radar",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 2f)
            arcTo(10f, 10f, 0f, isMoreThanHalf = true, isPositiveArc = false, 2f, 12f)
            arcTo(10f, 10f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12f, 22f)
            arcTo(10f, 10f, 0f, isMoreThanHalf = true, isPositiveArc = false, 22f, 12f)
            arcTo(10f, 10f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12f, 2f)
            close()
            moveTo(12f, 4f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 20f, 12f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 20f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 4f, 12f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 4f)
            close()
            moveTo(12f, 11f)
            arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12f, 13f)
            arcTo(1f, 1f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12f, 11f)
            close()
            moveTo(12f, 8f)
            arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 12f)
            lineTo(14f, 12f)
            arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12f, 10f)
            verticalLineTo(8f)
            close()
        }
    }.build()
}
