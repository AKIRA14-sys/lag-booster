package com.lagbooster.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.lagbooster.MainActivity
import com.lagbooster.R
import com.lagbooster.crosshair.CrosshairGenerator
import com.lagbooster.crosshair.CrosshairView
import com.lagbooster.manager.GameCrosshairProfile
import com.lagbooster.manager.ProfileManager
import kotlin.math.abs

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var profileManager: ProfileManager

    private var crosshairContainer: FrameLayout? = null
    private var crosshairView: CrosshairView? = null
    private var crosshairLayoutParams: WindowManager.LayoutParams? = null

    private var bubbleContainer: FrameLayout? = null
    private var bubbleLayoutParams: WindowManager.LayoutParams? = null

    private var panelContainer: LinearLayout? = null
    private var panelLayoutParams: WindowManager.LayoutParams? = null
    private var isPanelVisible = false

    private var activeProfile: GameCrosshairProfile? = null

    companion object {
        const val ACTION_START_OVERLAY = "action_start_overlay"
        const val ACTION_STOP_OVERLAY = "action_stop_overlay"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        private const val CHANNEL_ID = "lag_booster_overlay_channel"
        private const val NOTIF_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        profileManager = ProfileManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_OVERLAY
        if (action == ACTION_STOP_OVERLAY) {
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            val notification = buildNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
                startForeground(NOTIF_ID, notification, serviceType)
            } else {
                startForeground(NOTIF_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val pkgName = intent?.getStringExtra(EXTRA_PACKAGE_NAME)
            ?: profileManager.getLastPlayedGame()
            ?: "default"

        activeProfile = profileManager.getGameProfile(pkgName)

        if (canDrawOverlays()) {
            setupCrosshairOverlay()
            setupFloatingBubble()
        }

        return START_STICKY
    }

    private fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun setupCrosshairOverlay() {
        if (!canDrawOverlays()) return
        if (crosshairContainer != null) {
            updateCrosshairView()
            return
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val profile = activeProfile ?: GameCrosshairProfile(packageName = "default")
        val sizePx = (profile.sizeDp * resources.displayMetrics.density).toInt().coerceAtLeast(64)

        crosshairLayoutParams = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        crosshairContainer = FrameLayout(this)
        crosshairView = CrosshairView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        crosshairContainer?.addView(crosshairView)

        updateCrosshairView()

        try {
            windowManager.addView(crosshairContainer, crosshairLayoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateCrosshairView() {
        val profile = activeProfile ?: return
        val preset = CrosshairGenerator.getPresetById(profile.crosshairId)
        val presetWithDot = preset.copy(hasDot = profile.redDotEnabled)

        crosshairView?.setPreset(presetWithDot)
        crosshairView?.applyCustomizations(
            colorHex = profile.colorHex,
            sizeDp = profile.sizeDp,
            opacity = profile.opacity,
            rotationDeg = profile.rotationDeg
        )

        val sizePx = (profile.sizeDp * resources.displayMetrics.density).toInt().coerceAtLeast(64)
        crosshairLayoutParams?.width = sizePx
        crosshairLayoutParams?.height = sizePx
        if (crosshairContainer?.isAttachedToWindow == true) {
            try {
                windowManager.updateViewLayout(crosshairContainer, crosshairLayoutParams)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupFloatingBubble() {
        if (!canDrawOverlays()) return
        if (bubbleContainer != null) return

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        bubbleLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        val density = resources.displayMetrics.density
        val bubbleSizePx = (52 * density).toInt()

        val bubbleView = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(bubbleSizePx, bubbleSizePx)
            setImageResource(android.R.drawable.ic_dialog_dialer)
            setBackgroundResource(R.drawable.bg_quick_crosshair)
            setPadding(12, 12, 12, 12)
        }

        bubbleContainer = FrameLayout(this).apply {
            addView(bubbleView)
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        bubbleContainer?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = bubbleLayoutParams?.x ?: 0
                    initialY = bubbleLayoutParams?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    bubbleLayoutParams?.x = initialX + (event.rawX - initialTouchX).toInt()
                    bubbleLayoutParams?.y = initialY + (event.rawY - initialTouchY).toInt()
                    if (bubbleContainer?.isAttachedToWindow == true) {
                        try {
                            windowManager.updateViewLayout(bubbleContainer, bubbleLayoutParams)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = abs(event.rawX - initialTouchX)
                    val diffY = abs(event.rawY - initialTouchY)
                    if (diffX < 10 && diffY < 10) {
                        toggleQuickControlPanel()
                    }
                    true
                }
                else -> false
            }
        }

        try {
            windowManager.addView(bubbleContainer, bubbleLayoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleQuickControlPanel() {
        if (isPanelVisible) {
            hideQuickControlPanel()
        } else {
            showQuickControlPanel()
        }
    }

    private fun showQuickControlPanel() {
        if (!canDrawOverlays()) return
        if (panelContainer != null) return

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        panelLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        val paddingPx = (16 * resources.displayMetrics.density).toInt()

        panelContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_hud_bar)
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)

            val tvTitle = TextView(context).apply {
                text = "⚡ GAMING HUD CONTROLS"
                setTextColor(Color.parseColor("#00E5FF"))
                textSize = 14f
            }
            addView(tvTitle)

            val tvSub = TextView(context).apply {
                text = "Active Profile: ${activeProfile?.packageName ?: "Global"}"
                setTextColor(Color.parseColor("#80FFFFFF"))
                textSize = 11f
            }
            addView(tvSub)

            val btnToggleCrosshair = TextView(context).apply {
                text = "🎯 Toggle Crosshair Overlay"
                setTextColor(Color.WHITE)
                setPadding(0, 16, 0, 16)
                setOnClickListener {
                    crosshairContainer?.visibility =
                        if (crosshairContainer?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                }
            }
            addView(btnToggleCrosshair)

            val btnOpenApp = TextView(context).apply {
                text = "🏠 Open Lag Booster App"
                setTextColor(Color.WHITE)
                setPadding(0, 16, 0, 16)
                setOnClickListener {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    startActivity(intent)
                    hideQuickControlPanel()
                }
            }
            addView(btnOpenApp)

            val btnClosePanel = TextView(context).apply {
                text = "❌ Close Quick Menu"
                setTextColor(Color.parseColor("#FF1744"))
                setPadding(0, 16, 0, 16)
                setOnClickListener {
                    hideQuickControlPanel()
                }
            }
            addView(btnClosePanel)
        }

        try {
            windowManager.addView(panelContainer, panelLayoutParams)
            isPanelVisible = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideQuickControlPanel() {
        panelContainer?.let {
            if (it.isAttachedToWindow) {
                try {
                    windowManager.removeView(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        panelContainer = null
        isPanelVisible = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lag Booster Gaming Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground service notification for active crosshair overlay"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("Lag Booster Crosshair Active")
            .setContentText("Tap to open controller and optimize gameplay")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideQuickControlPanel()
        bubbleContainer?.let { if (it.isAttachedToWindow) try { windowManager.removeView(it) } catch (e: Exception) {} }
        crosshairContainer?.let { if (it.isAttachedToWindow) try { windowManager.removeView(it) } catch (e: Exception) {} }
    }
}
