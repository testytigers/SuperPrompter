package com.superprompter.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.superprompter.MainActivity
import com.superprompter.R
import com.superprompter.data.prefs.UserPreferencesRepository
import com.superprompter.data.repo.ScriptRepository
import com.superprompter.domain.PrompterSize
import com.superprompter.domain.UserPreferences
import com.superprompter.ui.theme.SuperPrompterTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shows the prompter in a small always-on-top window, so it can be used over the phone's own
 * camera app. Runs as a foreground service so Android doesn't kill it while the camera app is in front.
 */
@AndroidEntryPoint
class FloatingPrompterService : LifecycleService(), SavedStateRegistryOwner {

    @Inject lateinit var scriptRepository: ScriptRepository
    @Inject lateinit var preferencesRepository: UserPreferencesRepository

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private val scriptId = MutableStateFlow(0L)
    private var overlayView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var size = PrompterSize.THIRD

    override fun onCreate() {
        // Must be restored before the lifecycle moves past INITIALIZED (super.onCreate dispatches ON_CREATE).
        savedStateRegistryController.performRestore(null)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        val id = intent?.getLongExtra(EXTRA_SCRIPT_ID, 0L) ?: 0L
        if (id == 0L || !Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        scriptId.value = id
        // Window first: when Android restarts us in the background, Android 15+ only allows the
        // foreground service if an overlay window is already showing.
        if (overlayView == null) showOverlay()
        try {
            startInForeground()
        } catch (e: IllegalStateException) {
            // ForegroundServiceStartNotAllowedException; the overlay still works, just without the notification.
            Log.w(TAG, "Couldn't enter the foreground", e)
        }
        // If Android has to kill us (e.g. the camera app needs the memory), bring the prompter back.
        return START_REDELIVER_INTENT
    }

    /** Rotation, folding or a resolution change: refit the window to the new screen. */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyLayout()
    }

    override fun onDestroy() {
        overlayView?.let(windowManager::removeView)
        overlayView = null
        super.onDestroy()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun showOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            (screenHeight() * size.fraction).toInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            // Not focusable: the camera app behind keeps the keyboard/back button and gets every touch
            // outside this window. Keep screen on: if the phone locked, the prompter would be hidden.
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            // y = 0 is just below the camera cutout: Android keeps overlay windows out of it,
            // so the reading line ends up right under the front camera.
            y = 0
        }

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingPrompterService)
            setViewTreeSavedStateRegistryOwner(this@FloatingPrompterService)
            setContent {
                SuperPrompterTheme {
                    FloatingPrompter(
                        scriptFlow = scriptId.flatMapLatest { scriptRepository.observeById(it) },
                        preferencesFlow = preferencesRepository.preferences,
                        onAdjustSpeed = { delta -> updatePrefs { setScrollSpeed(it.scrollSpeed + delta) } },
                        onAdjustFontSize = { delta -> updatePrefs { setFontSize(it.fontSize + delta) } },
                        onCycleSize = { updatePrefs { setPrompterSize(floatingSizeAfter(it.prompterSize)) } },
                        onToggleMode = { updatePrefs { setPrompterMode(it.prompterMode.toggled()) } },
                        onDrag = ::moveBy,
                        onClose = ::stopSelf,
                    )
                }
            }
        }
        windowManager.addView(view, params)
        overlayView = view
        layoutParams = params

        // Follow the chosen prompter size. Full screen would hide the camera's own controls,
        // so the floating window tops out at half.
        lifecycleScope.launch {
            preferencesRepository.preferences
                .map { floatingSize(it.prompterSize) }
                .distinctUntilChanged()
                .collect { newSize ->
                    size = newSize
                    applyLayout()
                }
        }
    }

    private fun updatePrefs(block: suspend UserPreferencesRepository.(current: UserPreferences) -> Unit) {
        lifecycleScope.launch {
            preferencesRepository.block(preferencesRepository.preferences.first())
        }
    }

    /** Sizes the window for the current screen and keeps it fully on screen. */
    private fun applyLayout() {
        val view = overlayView ?: return
        val params = layoutParams ?: return
        val screenHeight = screenHeight()
        params.height = (screenHeight * size.fraction).toInt()
        params.y = params.y.coerceIn(0, (screenHeight - params.height).coerceAtLeast(0))
        windowManager.updateViewLayout(view, params)
    }

    private fun moveBy(dy: Float) {
        val view = overlayView ?: return
        val params = layoutParams ?: return
        val maxY = (screenHeight() - params.height).coerceAtLeast(0)
        params.y = (params.y + dy.toInt()).coerceIn(0, maxY)
        windowManager.updateViewLayout(view, params)
    }

    /** Current screen height, which changes on rotation. */
    private fun screenHeight(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.height()
        } else {
            resources.displayMetrics.heightPixels
        }


    private fun startInForeground() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Floating prompter", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Shown while the prompter floats over other apps"
            }
        )
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, FloatingPrompterService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_prompter)
            .setContentTitle("Prompter is floating")
            .setContentText("Open your camera app and press play on the prompter.")
            .setContentIntent(openIntent)
            .addAction(0, "Stop", stopIntent)
            .setOngoing(true)
            .build()
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            },
        )
    }

    companion object {
        private const val EXTRA_SCRIPT_ID = "script_id"
        private const val ACTION_STOP = "com.superprompter.overlay.STOP"
        private const val CHANNEL_ID = "floating_prompter"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "FloatingPrompter"

        /** The floating window never goes full screen; it would cover the camera app's shutter button. */
        fun floatingSize(size: PrompterSize): PrompterSize =
            if (size == PrompterSize.FULL) PrompterSize.HALF else size

        /** Cycles 1/2 → 1/3 → 1/4 → 1/5 → 1/2, skipping full screen. */
        fun floatingSizeAfter(size: PrompterSize): PrompterSize {
            val next = floatingSize(size).next()
            return if (next == PrompterSize.FULL) next.next() else next
        }

        fun start(context: Context, scriptId: Long) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, FloatingPrompterService::class.java).putExtra(EXTRA_SCRIPT_ID, scriptId),
            )
        }
    }
}
