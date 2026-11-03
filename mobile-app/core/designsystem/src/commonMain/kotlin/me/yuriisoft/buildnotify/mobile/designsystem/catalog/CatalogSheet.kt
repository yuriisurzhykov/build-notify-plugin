package me.yuriisoft.buildnotify.mobile.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.yuriisoft.buildnotify.mobile.designsystem.atoms.texts.Text
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.Res
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_action_hand_off
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_action_open_in
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_agent_any
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_agent_copy
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_agent_installed
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_agent_last_used
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_attach_log
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_attach_log_size
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_compile_errors
import me.yuriisoft.buildnotify.mobile.designsystem.generated.resources.catalog_section_sheet
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.buttons.PrimaryButton
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.fields.ChoiceCard
import me.yuriisoft.buildnotify.mobile.designsystem.molecules.fields.Switch
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.lists.SettingRow
import me.yuriisoft.buildnotify.mobile.designsystem.organisms.surfaces.SheetSurface
import me.yuriisoft.buildnotify.mobile.designsystem.particles.icons.GraphiteIcons
import me.yuriisoft.buildnotify.mobile.designsystem.particles.text.TextValue
import me.yuriisoft.buildnotify.mobile.designsystem.particles.theme.GraphiteTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SheetSection() {
    var fullLog by remember { mutableStateOf(false) }
    var agent by remember { mutableStateOf(0) }
    Section(stringResource(Res.string.catalog_section_sheet), padded = false) {
        SheetSurface {
            SheetHeader()
            AgentChoices(selected = agent, onSelect = { agent = it })
            SettingRow(
                stringResource(Res.string.catalog_attach_log),
                onClick = { fullLog = !fullLog },
                subtitle = stringResource(
                    Res.string.catalog_attach_log_size,
                    CatalogSamples.LOG_LINES,
                    CatalogSamples.LOG_KILOBYTES,
                ),
            ) {
                Switch(checked = fullLog, onCheckedChange = { fullLog = it })
            }
            PrimaryButton(
                stringResource(Res.string.catalog_action_open_in, CatalogSamples.CLAUDE_CODE),
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = GraphiteIcons.ArrowRight,
            )
        }
    }
}

@Composable
private fun SheetHeader() {
    val type = GraphiteTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.xs)) {
        Text(stringResource(Res.string.catalog_action_hand_off), style = type.headline)
        Text(
            TextValue.join(
                listOf(
                    TextValue.of(CatalogSamples.PAYMENT_SDK),
                    TextValue.of(
                        Res.plurals.catalog_compile_errors,
                        CatalogSamples.COMPILE_ERRORS,
                        CatalogSamples.COMPILE_ERRORS,
                    ),
                ),
                separator = TextValue.DotSeparator,
            ),
            style = type.bodySmall,
            color = GraphiteTheme.colors.contentSecondary,
        )
    }
}

@Composable
private fun AgentChoices(selected: Int, onSelect: (Int) -> Unit) {
    val agents = listOf(
        CatalogSamples.CLAUDE_CODE to stringResource(Res.string.catalog_agent_last_used),
        CatalogSamples.CODEX to stringResource(Res.string.catalog_agent_installed),
        stringResource(Res.string.catalog_agent_copy) to stringResource(Res.string.catalog_agent_any),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(GraphiteTheme.spacing.s)) {
        agents.forEachIndexed { index, (title, hint) ->
            ChoiceCard(
                title,
                hint,
                selected = selected == index,
                onClick = { onSelect(index) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
