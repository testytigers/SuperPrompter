package com.superprompter.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superprompter.domain.PrompterMode
import com.superprompter.domain.PrompterSize
import com.superprompter.domain.TextAlignment
import com.superprompter.domain.UserPreferences
import com.superprompter.ui.theme.CyanPrimary
import com.superprompter.ui.theme.DarkOnSurface
import com.superprompter.ui.theme.DarkOnSurfaceVariant
import com.superprompter.ui.theme.DarkSurface
import com.superprompter.ui.theme.DarkSurfaceVariant
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DarkSurface,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = CyanPrimary,
                    navigationIconContentColor = CyanPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            )
        },
    ) { padding ->
        val prefs = preferences
        if (prefs == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyanPrimary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsGroup("Mode") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Prompter mode", color = DarkOnSurface, fontSize = 16.sp)
                    Text(
                        "Scroll the script continuously, or show one line at a time and tap Next.",
                        color = DarkOnSurfaceVariant,
                        fontSize = 13.sp,
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val options = PrompterMode.entries
                        options.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = prefs.prompterMode == mode,
                                onClick = { viewModel.setPrompterMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                    activeContentColor = CyanPrimary,
                                    inactiveContainerColor = DarkSurfaceVariant,
                                    inactiveContentColor = DarkOnSurfaceVariant,
                                ),
                            ) {
                                Text(mode.label)
                            }
                        }
                    }
                }
                SwitchSetting(
                    title = "Split into sentences",
                    subtitle = "In line-by-line mode, show long paragraphs one sentence at a time. " +
                        "Turn off to show each line exactly as you typed it.",
                    checked = prefs.splitSentences,
                    onCheckedChange = viewModel::setSplitSentences,
                )
            }

            SettingsGroup("Scrolling") {
                SliderSetting(
                    title = "Scroll speed",
                    value = prefs.scrollSpeed,
                    range = UserPreferences.MIN_SCROLL_SPEED..UserPreferences.MAX_SCROLL_SPEED,
                    valueLabel = { "${it.roundToInt()}" },
                    onCommit = viewModel::setScrollSpeed,
                )
                SwitchSetting(
                    title = "Auto reverse",
                    subtitle = "Scroll back up when the script ends instead of stopping",
                    checked = prefs.autoReverse,
                    onCheckedChange = viewModel::setAutoReverse,
                )
            }

            SettingsGroup("Text") {
                SliderSetting(
                    title = "Font size",
                    value = prefs.fontSize,
                    range = UserPreferences.MIN_FONT_SIZE..UserPreferences.MAX_FONT_SIZE,
                    valueLabel = { "${it.roundToInt()} sp" },
                    onCommit = viewModel::setFontSize,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Alignment", color = DarkOnSurface, fontSize = 16.sp)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val options = TextAlignment.entries
                        options.forEachIndexed { index, alignment ->
                            SegmentedButton(
                                selected = prefs.textAlignment == alignment,
                                onClick = { viewModel.setAlignment(alignment) },
                                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                    activeContentColor = CyanPrimary,
                                    inactiveContainerColor = DarkSurfaceVariant,
                                    inactiveContentColor = DarkOnSurfaceVariant,
                                ),
                            ) {
                                Text(alignment.name.lowercase().replaceFirstChar { it.uppercase() })
                            }
                        }
                    }
                }
                SwitchSetting(
                    title = "Show word count",
                    subtitle = "Display words and time remaining in the prompter",
                    checked = prefs.showWordCounter,
                    onCheckedChange = viewModel::setShowWordCounter,
                )
            }

            SettingsGroup("Display") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Prompter size", color = DarkOnSurface, fontSize = 16.sp)
                    Text(
                        "How much of the screen the script covers, from the top. Smaller leaves more of the camera visible.",
                        color = DarkOnSurfaceVariant,
                        fontSize = 13.sp,
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val options = PrompterSize.entries
                        options.forEachIndexed { index, size ->
                            SegmentedButton(
                                selected = prefs.prompterSize == size,
                                onClick = { viewModel.setPrompterSize(size) },
                                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                    activeContentColor = CyanPrimary,
                                    inactiveContainerColor = DarkSurfaceVariant,
                                    inactiveContentColor = DarkOnSurfaceVariant,
                                ),
                            ) {
                                Text(size.label)
                            }
                        }
                    }
                }
                SliderSetting(
                    title = "Backdrop dimming",
                    value = prefs.overlayOpacity,
                    range = UserPreferences.MIN_OPACITY..UserPreferences.MAX_OPACITY,
                    valueLabel = { "${(it * 100).roundToInt()}%" },
                    onCommit = viewModel::setOpacity,
                )
                SwitchSetting(
                    title = "Mirror horizontally",
                    subtitle = "For beam-splitter glass teleprompter rigs",
                    checked = prefs.mirrorHorizontal,
                    onCheckedChange = viewModel::setMirrorHorizontal,
                )
                SwitchSetting(
                    title = "Mirror vertically",
                    subtitle = "Flip the text upside down",
                    checked = prefs.mirrorVertical,
                    onCheckedChange = viewModel::setMirrorVertical,
                )
            }
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title.uppercase(),
            color = CyanPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            content()
        }
    }
}

/** Keeps the drag local and only persists once the user lets go, so DataStore isn't written every frame. */
@Composable
private fun SliderSetting(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueLabel: (Float) -> String,
    onCommit: (Float) -> Unit,
) {
    var dragValue by remember(value) { mutableFloatStateOf(value) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = DarkOnSurface, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Text(valueLabel(dragValue), color = CyanPrimary, fontSize = 14.sp)
        }
        Slider(
            value = dragValue,
            onValueChange = { dragValue = it },
            onValueChangeFinished = { onCommit(dragValue) },
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = CyanPrimary,
                activeTrackColor = CyanPrimary,
                inactiveTrackColor = DarkSurfaceVariant,
            ),
        )
    }
}

@Composable
private fun SwitchSetting(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, color = DarkOnSurface, fontSize = 16.sp)
            Text(subtitle, color = DarkOnSurfaceVariant, fontSize = 13.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DarkSurface,
                checkedTrackColor = CyanPrimary,
                uncheckedThumbColor = DarkOnSurfaceVariant,
                uncheckedTrackColor = DarkSurfaceVariant,
            ),
        )
    }
}
