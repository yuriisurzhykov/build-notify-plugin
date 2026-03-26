package me.yuriisoft.buildnotify.mobile.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// region Mini vector icons for the catalog (no material-icons dependency)
val CheckIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Check",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
        ) {
            moveTo(9f, 16.17f)
            lineTo(4.83f, 12f)
            lineTo(3.41f, 13.41f)
            lineTo(9f, 19f)
            lineTo(21f, 7f)
            lineTo(19.59f, 5.59f)
            close()
        }
    }.build()
}

val CloseIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Close",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
        ) {
            moveTo(19f, 6.41f)
            lineTo(17.59f, 5f)
            lineTo(12f, 10.59f)
            lineTo(6.41f, 5f)
            lineTo(5f, 6.41f)
            lineTo(10.59f, 12f)
            lineTo(5f, 17.59f)
            lineTo(6.41f, 19f)
            lineTo(12f, 13.41f)
            lineTo(17.59f, 19f)
            lineTo(19f, 17.59f)
            lineTo(13.41f, 12f)
            close()
        }
    }.build()
}

val InfoIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Info",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd,
        ) {
            moveTo(12f, 2f)
            arcTo(10f, 10f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 22f)
            arcTo(10f, 10f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 2f)
            close()
            moveTo(11f, 7f)
            lineTo(13f, 7f)
            lineTo(13f, 9f)
            lineTo(11f, 9f)
            close()
            moveTo(11f, 11f)
            lineTo(13f, 11f)
            lineTo(13f, 17f)
            lineTo(11f, 17f)
            close()
        }
    }.build()
}