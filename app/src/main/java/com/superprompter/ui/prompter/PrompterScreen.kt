package com.superprompter.ui.prompter

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superprompter.domain.PrompterMode
import com.superprompter.domain.PrompterSize
import com.superprompter.domain.ScriptLines
import com.superprompter.domain.Script
import com.superprompter.domain.UserPreferences
import com.superprompter.domain.formatDuration
import com.superprompter.overlay.FloatingPrompterService
import com.superprompter.ui.theme.CyanPrimary
import com.superprompter.ui.theme.DarkOnSurface
import com.superprompter.ui.theme.DarkSurface
import com.superprompter.ui.theme.GlassBackground

private const val SPEED_STEP = 10f
private const val FONT_STEP = 4f
private val RecordRed = Color(0xFFFF3B4E)

@Composable
fun PrompterScreen(
    onClose: () -> Unit,
    viewModel: PrompterViewModel = hiltViewModel(),
) {
    val scriptState by viewModel.script.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()

    KeepScreenOn()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        when (val state = scriptState) {
            PrompterScript.Loading -> CircularProgressIndicator(color = CyanPrimary)
            PrompterScript.Missing -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("This script no longer exists.", color = DarkOnSurface)
                TextButton(onClick = onClose) { Text("Go back", color = CyanPrimary) }
            }
            is PrompterScript.Loaded -> Prompter(
                script = state.script,
                prefs = prefs,
                onClose = onClose,
                onAdjustSpeed = viewModel::adjustSpeed,
                onAdjustFontSize = viewModel::adjustFontSize,
                onToggleMirror = viewModel::toggleMirror,
                onCycleSize = viewModel::cycleSize,
                onToggleMode = viewModel::toggleMode,
            )
        }
    }
}

@Composable
private fun Prompter(
    script: Script,
    prefs: UserPreferences,
    onClose: () -> Unit,
    onAdjustSpeed: (Float) -> Unit,
    onAdjustFontSize: (Float) -> Unit,
    onToggleMirror: () -> Unit,
    onCycleSize: () -> Unit,
    onToggleMode: () -> Unit,
) {
    val context = LocalContext.current
    val camera = rememberPrompterCameraState()
    val playback = rememberPrompterPlayback(prefs.scrollSpeed, prefs.autoReverse)
    val lineMode = prefs.prompterMode == PrompterMode.LINES
    val lines = remember(script.content, prefs.splitSentences) {
        ScriptLines.split(script.content, prefs.splitSentences)
    }
    val cursor = rememberLineCursor()
    val lineIndex = cursor.current(lines.size)

    // --- Permissions -------------------------------------------------------
    var hasCamera by remember { mutableStateOf(context.isGranted(Manifest.permission.CAMERA)) }
    var hasAudio by remember { mutableStateOf(context.isGranted(Manifest.permission.RECORD_AUDIO)) }
    var showCamera by remember { mutableStateOf(true) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        hasCamera = context.isGranted(Manifest.permission.CAMERA)
        hasAudio = context.isGranted(Manifest.permission.RECORD_AUDIO)
    }
    LaunchedEffect(Unit) {
        val missing = recordingPermissions().filterNot(context::isGranted)
        if (missing.isNotEmpty()) permissionLauncher.launch(missing.toTypedArray())
    }
    val cameraActive = hasCamera && showCamera

    fun startFloating() {
        FloatingPrompterService.start(context, script.id)
        // Leave the in-app prompter first so it lets go of the camera for the camera app.
        onClose()
        openCameraApp(context)
    }

    // Asked before floating, never at the same time: Android hides floating windows while any
    // permission dialog is open, so a late dialog would make the prompter vanish over the camera.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { startFloating() } // Works either way; the permission only shows the Stop notification.

    fun toggleRecording() {
        when {
            camera.isRecording -> {
                camera.stopRecording()
                playback.pause()
            }
            // In line-by-line mode the countdown leads straight into recording, with no scrolling.
            cameraActive -> playback.start(withCountdown = true, scroll = !lineMode) {
                camera.startRecording(withAudio = hasAudio) { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
            !hasCamera -> permissionLauncher.launch(recordingPermissions().toTypedArray())
        }
    }

    fun floatOverCameraApp() {
        if (!Settings.canDrawOverlays(context)) {
            showOverlayPermissionDialog = true
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !context.isGranted(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startFloating()
        }
    }

    // --- Layout ------------------------------------------------------------
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (cameraActive) {
            CameraPreview(state = camera, modifier = Modifier.fillMaxSize())
        }

        // The script covers the chosen share of the screen from the top (nearest the lens);
        // below it the camera shows through undimmed.
        val dim = if (cameraActive) prefs.overlayOpacity else 1f
        Box(
            Modifier
                .fillMaxWidth()
                .height(maxHeight * prefs.prompterSize.fraction)
                .background(Color.Black.copy(alpha = dim))
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            if (lineMode) {
                LineByLineView(
                    lines = lines,
                    index = lineIndex,
                    prefs = prefs,
                    onNext = { cursor.next(lines.size) },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                ScriptScroller(
                    content = script.content,
                    prefs = prefs,
                    playback = playback,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        if (!cameraActive && prefs.prompterSize != PrompterSize.FULL) {
            // Nothing to show below the script without a camera; label the empty space.
            Text(
                text = if (hasCamera) "Camera hidden" else "Camera unavailable",
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.align(Alignment.Center).padding(top = maxHeight * prefs.prompterSize.fraction),
            )
        }

        // --- HUD -------------------------------------------------------------
        val scrollProgress by remember(playback) {
            derivedStateOf {
                val max = playback.scrollState.maxValue
                if (max <= 0 || max == Int.MAX_VALUE) 0f else playback.scrollState.value / max.toFloat()
            }
        }
        val progress = if (lineMode) {
            if (lines.isEmpty()) 0f else (lineIndex + 1) / lines.size.toFloat()
        } else {
            scrollProgress
        }
        val secondsLeft = rememberSecondsLeft(playback, prefs.scrollSpeed)

        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = CyanPrimary,
                trackColor = Color.White.copy(alpha = 0.1f),
                drawStopIndicator = {},
                gapSize = 0.dp,
            )

            // In line-by-line mode the line sits still at the top, so the counter and REC timer
            // move into the control panel instead of covering it.
            if (!lineMode) Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (camera.isRecording) RecordingBadge(camera.recordedMillis)
                Spacer(Modifier.weight(1f))
                if (prefs.showWordCounter) {
                    Text(
                        text = "${script.wordCount} words · ${formatDuration(secondsLeft)} left",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(GlassBackground, RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // In line-by-line mode there's no playback to keep clear of, so the controls stay up.
            val controlsVisible = if (lineMode) playback.countdown == 0 else !playback.isActive
            AnimatedVisibility(visible = controlsVisible, enter = fadeIn(), exit = fadeOut()) {
                Controls(
                    title = script.title,
                    prefs = prefs,
                    lineMode = lineMode,
                    lineLabel = "Line ${lineIndex + 1} of ${lines.size}",
                    recordedMillis = camera.recordedMillis,
                    hasPreviousLine = cursor.hasPrevious(lines.size),
                    hasNextLine = cursor.hasNext(lines.size),
                    onPreviousLine = { cursor.previous(lines.size) },
                    onNextLine = { cursor.next(lines.size) },
                    onToggleMode = onToggleMode,
                    isRecording = camera.isRecording,
                    canRecord = cameraActive,
                    cameraEnabled = showCamera,
                    hasCameraPermission = hasCamera,
                    onClose = onClose,
                    onPlay = { playback.start() },
                    onRestart = {
                        playback.pause()
                        playback.rewind()
                        cursor.restart()
                    },
                    onRecord = ::toggleRecording,
                    onToggleCamera = { if (!camera.isRecording) showCamera = !showCamera },
                    onSwitchCamera = camera::switchCamera,
                    onToggleMirror = onToggleMirror,
                    onCycleSize = onCycleSize,
                    onFloat = ::floatOverCameraApp,
                    onAdjustSpeed = onAdjustSpeed,
                    onAdjustFontSize = onAdjustFontSize,
                )
            }
        }

        if (playback.countdown > 0) {
            Text(
                text = "${playback.countdown}",
                color = CyanPrimary,
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = playback::pause,
                    ),
            )
        }
    }

    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            title = { Text("Allow floating prompter") },
            text = {
                Text(
                    "To show your script on top of the camera app, Android needs you to allow " +
                        "\"Display over other apps\" for SuperPrompter. Turn it on, come back, and tap the button again.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showOverlayPermissionDialog = false
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}"),
                        )
                    )
                }) {
                    Text("Open settings", color = CyanPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayPermissionDialog = false }) {
                    Text("Cancel", color = CyanPrimary)
                }
            },
        )
    }
}

@Composable
private fun Controls(
    title: String,
    prefs: UserPreferences,
    lineMode: Boolean,
    lineLabel: String,
    recordedMillis: Long,
    hasPreviousLine: Boolean,
    hasNextLine: Boolean,
    onPreviousLine: () -> Unit,
    onNextLine: () -> Unit,
    onToggleMode: () -> Unit,
    isRecording: Boolean,
    canRecord: Boolean,
    cameraEnabled: Boolean,
    hasCameraPermission: Boolean,
    onClose: () -> Unit,
    onPlay: () -> Unit,
    onRestart: () -> Unit,
    onRecord: () -> Unit,
    onToggleCamera: () -> Unit,
    onSwitchCamera: () -> Unit,
    onToggleMirror: () -> Unit,
    onCycleSize: () -> Unit,
    onFloat: () -> Unit,
    onAdjustSpeed: (Float) -> Unit,
    onAdjustFontSize: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(DarkSurface.copy(alpha = 0.94f), RoundedCornerShape(24.dp))
            // Swallow taps that miss a button. Without this they fall through to the script
            // underneath, which starts playback (and looks like recording started).
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HudButton(Icons.Default.Close, "Close", onClick = onClose)
            Text(
                text = title,
                color = DarkOnSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            )
            if (hasCameraPermission) {
                HudButton(
                    icon = if (cameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    description = if (cameraEnabled) "Hide camera" else "Show camera",
                    enabled = !isRecording,
                    onClick = onToggleCamera,
                )
                if (cameraEnabled) {
                    HudButton(Icons.Default.Cameraswitch, "Switch camera", enabled = !isRecording, onClick = onSwitchCamera)
                }
            }
            HudButton(
                icon = Icons.Default.Flip,
                description = "Mirror text",
                tint = if (prefs.mirrorHorizontal) CyanPrimary else Color.White,
                onClick = onToggleMirror,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (!lineMode) {
                Stepper(
                    label = "Speed ${prefs.scrollSpeed.toInt()}",
                    decreaseIcon = Icons.Default.Remove,
                    increaseIcon = Icons.Default.Add,
                    decreaseDescription = "Slower",
                    increaseDescription = "Faster",
                    onDecrease = { onAdjustSpeed(-SPEED_STEP) },
                    onIncrease = { onAdjustSpeed(SPEED_STEP) },
                )
            }
            Stepper(
                label = "Text ${prefs.fontSize.toInt()}",
                decreaseIcon = Icons.Default.TextDecrease,
                increaseIcon = Icons.Default.TextIncrease,
                decreaseDescription = "Smaller text",
                increaseDescription = "Bigger text",
                onDecrease = { onAdjustFontSize(-FONT_STEP) },
                onIncrease = { onAdjustFontSize(FONT_STEP) },
            )
            if (lineMode) {
                if (isRecording) {
                    RecordingBadge(recordedMillis)
                } else {
                    Text(lineLabel, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val compact = PaddingValues(horizontal = 8.dp)
            OutlinedButton(onClick = onCycleSize, modifier = Modifier.weight(1f), contentPadding = compact) {
                Text("Size ${prefs.prompterSize.label}", color = Color.White, fontSize = 13.sp, maxLines = 1)
            }
            OutlinedButton(onClick = onToggleMode, modifier = Modifier.weight(1f), contentPadding = compact) {
                Text(if (lineMode) "Lines" else "Scroll", color = Color.White, fontSize = 13.sp, maxLines = 1)
            }
            OutlinedButton(onClick = onFloat, modifier = Modifier.weight(1.3f), contentPadding = compact) {
                Icon(
                    Icons.Default.PictureInPictureAlt,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("Camera app", color = Color.White, fontSize = 13.sp, maxLines = 1)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            HudButton(Icons.Default.Replay, if (lineMode) "Back to first line" else "Restart from top", onClick = onRestart)
            if (lineMode) {
                HudButton(Icons.Default.SkipPrevious, "Previous line", enabled = hasPreviousLine, onClick = onPreviousLine)
            }
            FilledIconButton(
                onClick = if (lineMode) onNextLine else onPlay,
                enabled = !lineMode || hasNextLine,
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = CyanPrimary,
                    contentColor = DarkSurface,
                    disabledContainerColor = CyanPrimary.copy(alpha = 0.25f),
                ),
            ) {
                Icon(
                    imageVector = if (lineMode) Icons.Default.SkipNext else Icons.Default.PlayArrow,
                    contentDescription = if (lineMode) "Next line" else "Play",
                    modifier = Modifier.size(36.dp),
                )
            }
            FilledIconButton(
                onClick = onRecord,
                enabled = canRecord || isRecording || !hasCameraPermission,
                modifier = Modifier.size(52.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.12f),
                    contentColor = RecordRed,
                    disabledContainerColor = Color.White.copy(alpha = 0.05f),
                    disabledContentColor = RecordRed.copy(alpha = 0.3f),
                ),
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                    contentDescription = if (isRecording) "Stop recording" else "Record",
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun Stepper(
    label: String,
    decreaseIcon: ImageVector,
    increaseIcon: ImageVector,
    decreaseDescription: String,
    increaseDescription: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        HudButton(decreaseIcon, decreaseDescription, onClick = onDecrease)
        Text(label, color = Color.White, fontSize = 13.sp, modifier = Modifier.width(64.dp), textAlign = TextAlign.Center)
        HudButton(increaseIcon, increaseDescription, onClick = onIncrease)
    }
}

@Composable
private fun HudButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean = true,
    tint: Color = Color.White,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(icon, contentDescription = description, tint = if (enabled) tint else tint.copy(alpha = 0.3f))
    }
}

@Composable
private fun RecordingBadge(recordedMillis: Long) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(GlassBackground, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Box(Modifier.size(8.dp).background(RecordRed, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(
            text = "REC ${formatDuration((recordedMillis / 1000).toInt())}",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

/** Opens the phone's own camera app, in video mode where supported. */
private fun openCameraApp(context: Context) {
    val intents = listOf(
        Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA),
        Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
    )
    for (intent in intents) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        } catch (e: ActivityNotFoundException) {
            // Try the next one.
        }
    }
    Toast.makeText(context, "Open your camera app — the prompter will stay on top.", Toast.LENGTH_LONG).show()
}

private fun recordingPermissions(): List<String> = buildList {
    add(Manifest.permission.CAMERA)
    add(Manifest.permission.RECORD_AUDIO)
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
}

private fun Context.isGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
