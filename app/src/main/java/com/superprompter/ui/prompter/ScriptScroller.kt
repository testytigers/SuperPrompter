package com.superprompter.ui.prompter

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superprompter.domain.TextAlignment
import com.superprompter.domain.UserPreferences
import com.superprompter.ui.theme.CyanPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

/** Where the reading line sits, as a fraction of the prompter area: high up, close to the front camera. */
private const val READING_LINE_FRACTION = 0.28f
const val COUNTDOWN_SECONDS = 3

/**
 * Play / pause / countdown state for a scrolling script. Shared by the in-app prompter
 * and the floating prompter shown over other camera apps.
 */
@Stable
class PrompterPlayback(val scrollState: ScrollState) {

    var isPlaying by mutableStateOf(false)
        private set
    var countdown by mutableIntStateOf(0)
        private set
    internal var direction by mutableIntStateOf(1)

    private var onCountdownFinished: (() -> Unit)? = null
    private var scrollAfterCountdown = true

    /** Playing or counting down. */
    val isActive: Boolean get() = isPlaying || countdown > 0

    private val isAtEnd: Boolean get() = scrollState.value >= scrollState.maxValue

    fun rewind() {
        direction = 1
        scrollState.dispatchRawDelta(-scrollState.value.toFloat())
    }

    /**
     * Starts scrolling. From the top (or after finishing) a countdown runs first;
     * [then] runs the moment scrolling actually begins. With [scroll] false only the
     * countdown runs (line-by-line mode), then [then].
     */
    fun start(
        withCountdown: Boolean = scrollState.value == 0 || isAtEnd,
        scroll: Boolean = true,
        then: (() -> Unit)? = null,
    ) {
        if (isActive) return
        if (scroll && isAtEnd) rewind()
        if (withCountdown) {
            onCountdownFinished = then
            scrollAfterCountdown = scroll
            countdown = COUNTDOWN_SECONDS
        } else {
            then?.invoke()
            isPlaying = scroll
        }
    }

    fun pause() {
        isPlaying = false
        countdown = 0
        onCountdownFinished = null
    }

    fun toggle() = if (isActive) pause() else start()

    internal fun tickCountdown() {
        if (countdown == 1) {
            onCountdownFinished?.invoke()
            onCountdownFinished = null
            isPlaying = scrollAfterCountdown
        }
        countdown -= 1
    }

    internal fun reachedEnd() {
        isPlaying = false
    }
}

/** Creates the playback state and runs the countdown and auto-scroll loops for it. */
@Composable
fun rememberPrompterPlayback(speed: Float, autoReverse: Boolean): PrompterPlayback {
    val scrollState = rememberScrollState()
    val playback = remember(scrollState) { PrompterPlayback(scrollState) }
    val density = LocalDensity.current
    val currentSpeed by rememberUpdatedState(speed)
    val currentAutoReverse by rememberUpdatedState(autoReverse)

    LaunchedEffect(playback.countdown) {
        if (playback.countdown <= 0) return@LaunchedEffect
        delay(1_000)
        playback.tickCountdown()
    }

    LaunchedEffect(playback.isPlaying) {
        if (!playback.isPlaying) return@LaunchedEffect
        var lastFrame = withFrameNanos { it }
        while (isActive) {
            val frame = withFrameNanos { it }
            val seconds = (frame - lastFrame) / 1_000_000_000f
            lastFrame = frame
            // dispatchRawDelta doesn't take the scroll lock, so the user can still drag mid-playback.
            scrollState.dispatchRawDelta(
                with(density) { currentSpeed.dp.toPx() } * seconds * playback.direction
            )
            when {
                playback.direction > 0 && scrollState.value >= scrollState.maxValue ->
                    if (currentAutoReverse) playback.direction = -1 else playback.reachedEnd()
                playback.direction < 0 && scrollState.value <= 0 -> playback.direction = 1
            }
        }
    }
    return playback
}

/** Seconds of scrolling left at the current speed. */
@Composable
fun rememberSecondsLeft(playback: PrompterPlayback, speed: Float): Int {
    val density = LocalDensity.current
    val currentSpeed by rememberUpdatedState(speed)
    val secondsLeft by remember(playback) {
        derivedStateOf {
            val scroll = playback.scrollState
            val pxPerSecond = with(density) { currentSpeed.dp.toPx() }
            if (scroll.maxValue == Int.MAX_VALUE || pxPerSecond <= 0f) 0
            else ((scroll.maxValue - scroll.value) / pxPerSecond).roundToInt()
        }
    }
    return secondsLeft
}

/**
 * The scrolling script with its reading line. Sizes itself to whatever area it's given,
 * so it works full screen, in part of the screen, or inside the floating window.
 * Tapping the text toggles playback.
 */
@Composable
fun ScriptScroller(
    content: String,
    prefs: UserPreferences,
    playback: PrompterPlayback,
    modifier: Modifier = Modifier,
    readingLineFraction: Float = READING_LINE_FRACTION,
) {
    val density = LocalDensity.current
    BoxWithConstraints(modifier.clipToBounds()) {
        val viewportHeight = maxHeight
        val readingLineTop = viewportHeight * readingLineFraction
        val lineHeightSp = prefs.fontSize * 1.35f
        val lineHeightDp = with(density) { lineHeightSp.sp.toDp() }

        // Reading line band; mirrored vertically along with the text when needed.
        val bandTop = if (prefs.mirrorVertical) viewportHeight - readingLineTop - lineHeightDp else readingLineTop
        Box(
            Modifier
                .offset(y = bandTop)
                .fillMaxWidth()
                .height(lineHeightDp)
                .background(CyanPrimary.copy(alpha = 0.12f)),
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .width(6.dp)
                    .height(lineHeightDp)
                    .background(CyanPrimary),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = if (prefs.mirrorHorizontal) -1f else 1f
                    scaleY = if (prefs.mirrorVertical) -1f else 1f
                }
                .verticalScroll(playback.scrollState)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = playback::toggle,
                )
                // Padding lets the first line start on the reading line and the last line scroll up to it.
                .padding(
                    top = readingLineTop,
                    bottom = (viewportHeight - readingLineTop - lineHeightDp).coerceAtLeast(0.dp),
                )
                .padding(horizontal = 28.dp),
        ) {
            Text(
                text = content,
                color = Color.White,
                fontSize = prefs.fontSize.sp,
                lineHeight = lineHeightSp.sp,
                fontWeight = FontWeight.Medium,
                textAlign = when (prefs.textAlignment) {
                    TextAlignment.LEFT -> TextAlign.Start
                    TextAlignment.CENTER -> TextAlign.Center
                    TextAlignment.RIGHT -> TextAlign.End
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
