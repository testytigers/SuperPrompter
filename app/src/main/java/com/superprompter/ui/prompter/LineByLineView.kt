package com.superprompter.ui.prompter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superprompter.domain.TextAlignment
import com.superprompter.domain.UserPreferences

/** Smallest the text may shrink to (as a share of the chosen font size) to fit a long line. */
private const val MIN_TEXT_SCALE = 0.4f

/** Which card is showing in line-by-line mode. */
@Stable
class LineCursor(initialIndex: Int = 0) {
    private var index by mutableIntStateOf(initialIndex)

    /** The current card, kept in range even if the script was edited to fewer lines. */
    fun current(count: Int): Int = index.coerceIn(0, (count - 1).coerceAtLeast(0))

    fun hasNext(count: Int) = current(count) < count - 1
    fun hasPrevious(count: Int) = current(count) > 0

    fun next(count: Int) {
        index = (current(count) + 1).coerceAtMost((count - 1).coerceAtLeast(0))
    }

    fun previous(count: Int) {
        index = (current(count) - 1).coerceAtLeast(0)
    }

    fun restart() {
        index = 0
    }

    companion object {
        val Saver: Saver<LineCursor, Int> = Saver(save = { it.index }, restore = { LineCursor(it) })
    }
}

@Composable
fun rememberLineCursor(): LineCursor = rememberSaveable(saver = LineCursor.Saver) { LineCursor() }

/**
 * Shows one line of the script at the top of the area (nearest the camera), shrinking it if
 * it's too long to fit. Tapping it moves to the next line. A dim preview of the following
 * line is shown underneath when there's room.
 */
@Composable
fun LineByLineView(
    lines: List<String>,
    index: Int,
    prefs: UserPreferences,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textAlign = when (prefs.textAlignment) {
        TextAlignment.LEFT -> TextAlign.Start
        TextAlignment.CENTER -> TextAlign.Center
        TextAlignment.RIGHT -> TextAlign.End
    }
    BoxWithConstraints(
        modifier
            .clipToBounds()
            .graphicsLayer {
                scaleX = if (prefs.mirrorHorizontal) -1f else 1f
                scaleY = if (prefs.mirrorVertical) -1f else 1f
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onNext,
            ),
    ) {
        val nextLine = lines.getOrNull(index + 1)
        val showPreview = nextLine != null && maxHeight > 160.dp
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 8.dp)) {
            FittedText(
                text = lines.getOrNull(index) ?: "This script is empty.",
                fontSize = prefs.fontSize,
                textAlign = textAlign,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            if (showPreview) {
                Text(
                    text = "Next: $nextLine",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = textAlign,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        }
    }
}

/** Text that shrinks step by step until it fits its box, hidden until it has settled. */
@Composable
private fun FittedText(
    text: String,
    fontSize: Float,
    textAlign: TextAlign,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        // Re-fit whenever the line, font size or available space changes.
        var scale by remember(text, fontSize, maxWidth, maxHeight) { mutableFloatStateOf(1f) }
        var fitted by remember(text, fontSize, maxWidth, maxHeight) { mutableStateOf(false) }
        Text(
            text = text,
            color = Color.White,
            fontSize = (fontSize * scale).sp,
            lineHeight = (fontSize * scale * 1.3f).sp,
            fontWeight = FontWeight.Medium,
            textAlign = textAlign,
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent { if (fitted) drawContent() },
            onTextLayout = { layout ->
                if (layout.didOverflowHeight && scale > MIN_TEXT_SCALE) {
                    scale = (scale * 0.9f).coerceAtLeast(MIN_TEXT_SCALE)
                } else {
                    fitted = true
                }
            },
        )
    }
}
