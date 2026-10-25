package com.superprompter.ui.prompter

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Owns the CameraX controller plus the state of any in-progress recording. */
@Stable
class PrompterCameraState(private val context: Context) {

    val controller = LifecycleCameraController(context).apply {
        setEnabledUseCases(CameraController.VIDEO_CAPTURE)
        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    }

    var isFrontCamera by mutableStateOf(true)
        private set

    private var recording: Recording? by mutableStateOf(null)

    val isRecording: Boolean get() = recording != null

    var recordedMillis by mutableLongStateOf(0L)
        private set

    fun switchCamera() {
        if (isRecording) return
        val next = if (isFrontCamera) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA
        if (runCatching { controller.hasCamera(next) }.getOrDefault(false)) {
            controller.cameraSelector = next
            isFrontCamera = !isFrontCamera
        }
    }

    // Callers only pass withAudio = true once RECORD_AUDIO has been granted.
    @SuppressLint("MissingPermission")
    fun startRecording(withAudio: Boolean, onFinished: (message: String) -> Unit) {
        if (isRecording) return
        val name = "SuperPrompter_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/SuperPrompter")
            }
        }
        val output = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        ).setContentValues(values).build()

        recordedMillis = 0L
        recording = runCatching {
            controller.startRecording(
                output,
                if (withAudio) AudioConfig.create(true) else AudioConfig.AUDIO_DISABLED,
                ContextCompat.getMainExecutor(context),
            ) { event ->
                when (event) {
                    is VideoRecordEvent.Status ->
                        recordedMillis = event.recordingStats.recordedDurationNanos / 1_000_000

                    is VideoRecordEvent.Finalize -> {
                        recording = null
                        onFinished(
                            if (event.hasError()) {
                                Log.e(TAG, "Recording failed: ${event.error}", event.cause)
                                "Recording failed"
                            } else {
                                "Video saved to Movies/SuperPrompter"
                            }
                        )
                    }
                }
            }
        }.onFailure {
            Log.e(TAG, "Couldn't start recording", it)
            onFinished("Couldn't start recording")
        }.getOrNull()
    }

    fun stopRecording() {
        recording?.stop()
    }

    private companion object {
        const val TAG = "PrompterCamera"
    }
}

@Composable
fun rememberPrompterCameraState(): PrompterCameraState {
    val context = LocalContext.current
    return remember { PrompterCameraState(context) }
}

/** Binds the camera only while this is on screen, and releases it (stopping any recording) on exit. */
@Composable
fun CameraPreview(
    state: PrompterCameraState,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(state, lifecycleOwner) {
        state.controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            state.stopRecording()
            state.controller.unbind()
        }
    }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                controller = state.controller
            }
        },
    )
}
