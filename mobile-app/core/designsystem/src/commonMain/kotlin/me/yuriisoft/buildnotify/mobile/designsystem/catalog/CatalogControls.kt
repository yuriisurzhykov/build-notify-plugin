package me.yuriisoft.buildnotify.mobile.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.components.Button
import me.yuriisoft.buildnotify.mobile.designsystem.components.ButtonVariant
import me.yuriisoft.buildnotify.mobile.designsystem.components.ConnectionBadge
import me.yuriisoft.buildnotify.mobile.designsystem.components.DangerButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.IconButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.OutlineButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.PrimaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.SecondaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.components.StatusBadge
import me.yuriisoft.buildnotify.mobile.designsystem.components.StatusPill
import me.yuriisoft.buildnotify.mobile.designsystem.components.Tag
import me.yuriisoft.buildnotify.mobile.designsystem.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.theme.BuildTone
import me.yuriisoft.buildnotify.mobile.designsystem.theme.GraphiteTheme

@Composable
internal fun StatusSection() {
    val spacing = GraphiteTheme.spacing
    Section("Статусы") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.s),
            verticalArrangement = Arrangement.spacedBy(spacing.s),
        ) {
            StatusPill(BuildTone.Running, "Идёт")
            StatusPill(BuildTone.Success, "Успешно")
            StatusPill(BuildTone.Failure, "Ошибка")
            StatusPill(BuildTone.Warning, "Отменена")
            Tag("агент")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.m)) {
            StatusBadge(BuildTone.Success, contentDescription = "Успешно")
            StatusBadge(BuildTone.Failure, contentDescription = "Ошибка")
            StatusBadge(BuildTone.Running, contentDescription = "Идёт")
        }
        ConnectionBadge("MyProject · Android Studio", connected = true)
    }
}

@Composable
internal fun ButtonsSection() {
    val spacing = GraphiteTheme.spacing
    Section("Кнопки") {
        PrimaryButton(
            "Передать агенту",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = GraphiteIcons.ArrowRight,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
            OutlineButton(
                "Перезапустить",
                onClick = {},
                modifier = Modifier.weight(1f),
                leadingIcon = GraphiteIcons.Rerun,
            )
            SecondaryButton("Лог", onClick = {}, modifier = Modifier.weight(1f), leadingIcon = GraphiteIcons.Terminal)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
            DangerButton("Остановить", onClick = {}, modifier = Modifier.weight(1f), leadingIcon = GraphiteIcons.Stop)
            Button(
                "Недоступно",
                onClick = {},
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.Secondary,
                enabled = false,
            )
        }
        Row {
            IconButton(GraphiteIcons.ArrowBack, contentDescription = "Назад", onClick = {})
            IconButton(GraphiteIcons.Copy, contentDescription = "Скопировать лог", onClick = {})
            IconButton(GraphiteIcons.Settings, contentDescription = "Настройки", onClick = {})
        }
    }
}
