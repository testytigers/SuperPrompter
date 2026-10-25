package com.superprompter.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superprompter.domain.PrompterMode
import com.superprompter.domain.Script
import com.superprompter.domain.ScriptLines
import com.superprompter.domain.UserPreferences
import com.superprompter.ui.prompter.LineByLineView
import com.superprompter.ui.prompter.LineCursor
import com.superprompter.ui.prompter.ScriptScroller
import com.superprompter.ui.prompter.rememberPrompterPlayback
import com.superprompter.ui.theme.CyanPrimary
import com.superprompter.ui.theme.DarkSurface
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt

private const val SPEED_STEP = 10f
private const val FONT_STEP = 4f

@Composable
fun FloatingPrompter(
    scriptFlow: Flow<Script?>,
    preferencesFlow: Flow<UserPreferences>,
    onAdjustSpeed: (Float) -> Unit,
    onAdjustFontSize: (Float) -> Unit,
    onCycleSize: () -> Unit,
    onToggleMode: () -> Unit,
    onDrag: (dy: Float) -> Unit,
    onClose: () -> Unit,
) {
    val script by remember(scriptFlow) { scriptFlow }.collectAsState(initial = null)
    val prefs by remember(preferencesFlow) { preferencesFlow }.collectAsState(initial = null)
    val playback = rememberPrompterPlayback(
        speed = prefs?.scrollSpeed ?: UserPreferences.DEFAULT_SCROLL_SPEED,
        autoReverse = prefs?.autoReverse ?: false,
    )
    val lineMode = prefs?.prompterMode == PrompterMode.LINES
    val content = script?.content.orEmpty()
    val splitSentences = prefs?.splitSentences ?: true
    val lines = remember(content, splitSentences) { ScriptLines.split(content, splitSentences) }
    val cursor = remember { LineCursor() }
    val lineIndex = cursor.current(lines.size)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                DarkSurface.copy(alpha = (prefs?.overlayOpacity ?: 0.85f).coerceAtLeast(0.6f)),
                RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            ),
    ) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            val currentScript = script
            val currentPrefs = prefs
            if (currentScript == null || currentPrefs == null) {
                CircularProgressIndicator(color = CyanPrimary)
            } else if (lineMode) {
                LineByLineView(
                    lines = lines,
                    index = lineIndex,
                    prefs = currentPrefs,
                    onNext = { cursor.next(lines.size) },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                ScriptScroller(
                    content = currentScript.content,
                    prefs = currentPrefs,
                    playback = playback,
                    modifier = Modifier.fillMaxSize(),
                    // Reading line right at the top, as close to the front camera as possible.
                    readingLineFraction = 0.02f,
                )
            }
            if (playback.countdown > 0) {
                Text(
                    text = "${playback.countdown}",
                    color = CyanPrimary,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Drag handle: move the window up or down the screen.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.y)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.DragHandle, contentDescription = "Drag to move", tint = Color.White.copy(alpha = 0.5f))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Swallow taps between buttons so they don't toggle playback underneath.
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (lineMode) {
                SmallButton(
                    Icons.Default.SkipPrevious,
                    "Previous line",
                    enabled = cursor.hasPrevious(lines.size),
                ) { cursor.previous(lines.size) }
                Text(
                    text = "${lineIndex + 1}/${lines.size}",
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(44.dp),
                )
                SmallButton(
                    Icons.Default.SkipNext,
                    "Next line",
                    tint = CyanPrimary,
                    enabled = cursor.hasNext(lines.size),
                ) { cursor.next(lines.size) }
            } else {
                SmallButton(
                    icon = if (playback.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                    description = if (playback.isActive) "Pause" else "Play",
                    tint = CyanPrimary,
                    onClick = playback::toggle,
                )
                SmallButton(Icons.Default.Remove, "Slower") { onAdjustSpeed(-SPEED_STEP) }
                Text(
                    text = "${(prefs?.scrollSpeed ?: 0f).roundToInt()}",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.width(28.dp),
                )
                SmallButton(Icons.Default.Add, "Faster") { onAdjustSpeed(SPEED_STEP) }
            }
            SmallButton(Icons.Default.TextDecrease, "Smaller text") { onAdjustFontSize(-FONT_STEP) }
            SmallButton(Icons.Default.TextIncrease, "Bigger text") { onAdjustFontSize(FONT_STEP) }
            SmallButton(
                icon = if (lineMode) Icons.Default.SwapVert else Icons.Default.Subtitles,
                description = if (lineMode) "Switch to scrolling" else "Switch to line by line",
                onClick = onToggleMode,
            )
            SmallButton(Icons.Default.Height, "Change size", onClick = onCycleSize)
            SmallButton(Icons.Default.Close, "Close floating prompter", onClick = onClose)
        }
    }
}

@Composable
private fun SmallButton(
    icon: ImageVector,
    description: String,
    tint: Color = Color.White,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(40.dp)) {
        Icon(
            icon,
            contentDescription = description,
            tint = if (enabled) tint else tint.copy(alpha = 0.3f),
            modifier = Modifier.size(22.dp),
        )
    }
}
