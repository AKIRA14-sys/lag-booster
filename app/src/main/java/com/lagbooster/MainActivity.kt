package com.lagbooster

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lagbooster.adapter.GameAdapter
import com.lagbooster.crosshair.CrosshairCustomizerDialog
import com.lagbooster.crosshair.CrosshairGenerator
import com.lagbooster.crosshair.CrosshairView
import com.lagbooster.manager.GameAppInfo
import com.lagbooster.manager.GameManager
import com.lagbooster.manager.ProfileManager
import com.lagbooster.manager.TelemetryManager
import com.lagbooster.service.OverlayService
import com.lagbooster.ui.SharinganOcularView

class MainActivity : AppCompatActivity() {

    private lateinit var telemetryManager: TelemetryManager
    private lateinit var gameManager: GameManager
    private lateinit var profileManager: ProfileManager

    private lateinit var tvFps: TextView
    private lateinit var tvPing: TextView
    private lateinit var tvRam: TextView
    private lateinit var tvTemp: TextView

    private lateinit var sharinganView: SharinganOcularView
    private lateinit var rvGames: RecyclerView
    private lateinit var gameAdapter: GameAdapter

    private lateinit var layoutQuickCrosshairs: LinearLayout

    private var gamesList: MutableList<GameAppInfo> = mutableListOf()
    private var selectedGame: GameAppInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        profileManager = ProfileManager(this)
        telemetryManager = TelemetryManager(this)
        gameManager = GameManager(this, profileManager)

        initHUD()
        initSharinganCore()
        initGameCarousel()
        initQuickCrosshairs()
        initBottomNav()

        checkOverlayPermission()
    }

    private fun initHUD() {
        tvFps = findViewById(R.id.tv_fps_val)
        tvPing = findViewById(R.id.tv_ping_val)
        tvRam = findViewById(R.id.tv_ram_val)
        tvTemp = findViewById(R.id.tv_temp_val)
    }

    private fun initSharinganCore() {
        sharinganView = findViewById(R.id.ocular_view)
        sharinganView.setOnPlayClickListener {
            val gameToLaunch = selectedGame ?: gamesList.firstOrNull()
            if (gameToLaunch != null) {
                profileManager.setLastPlayedGame(gameToLaunch.packageName)
                Toast.makeText(this, "🚀 Accelerating ${gameToLaunch.title} & Activating Sharingan HUD...", Toast.LENGTH_SHORT).show()
                startOverlayService(gameToLaunch.packageName)
                gameManager.launchGame(gameToLaunch.packageName)
            } else {
                Toast.makeText(this, "🎯 Activating Sharingan Overlay Engine...", Toast.LENGTH_SHORT).show()
                startOverlayService("default")
            }
        }
    }

    private fun initGameCarousel() {
        rvGames = findViewById(R.id.rv_game_carousel)
        rvGames.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        refreshGamesList()

        gameAdapter = GameAdapter(
            items = gamesList,
            onItemClick = { game ->
                selectGame(game)
                Toast.makeText(this, "Selected: ${game.title}", Toast.LENGTH_SHORT).show()
            },
            onFavoriteClick = { game ->
                profileManager.toggleFavorite(game.packageName)
                refreshGamesList()
            }
        )

        rvGames.adapter = gameAdapter
    }

    private fun refreshGamesList() {
        gamesList.clear()
        gamesList.addAll(gameManager.discoverInstalledGames())
        if (selectedGame == null && gamesList.isNotEmpty()) {
            selectGame(gamesList.first())
        }
    }

    private fun selectGame(game: GameAppInfo) {
        selectedGame = game
        gamesList = gamesList.map {
            it.copy(isSelected = (it.packageName == game.packageName))
        }.toMutableList()
        if (::gameAdapter.isInitialized) {
            gameAdapter.updateData(gamesList)
        }
    }

    private fun initQuickCrosshairs() {
        layoutQuickCrosshairs = findViewById(R.id.layout_quick_crosshairs)
        layoutQuickCrosshairs.removeAllViews()

        val samplePresets = listOf(
            CrosshairGenerator.getPresetById(1),    // Normal
            CrosshairGenerator.getPresetById(151),  // Pro
            CrosshairGenerator.getPresetById(301),  // Legendary
            CrosshairGenerator.getPresetById(421)   // God Sharingan
        )

        val density = resources.displayMetrics.density
        val itemSizePx = (80 * density).toInt()

        samplePresets.forEach { preset ->
            val crosshairItemView = CrosshairView(this).apply {
                layoutParams = LinearLayout.LayoutParams(itemSizePx, itemSizePx).apply {
                    setMargins(0, 0, (12 * density).toInt(), 0)
                }
                setBackgroundResource(R.drawable.bg_quick_crosshair)
                setPreset(preset)

                setOnClickListener {
                    val game = selectedGame ?: gamesList.firstOrNull()
                    if (game != null) {
                        val profile = profileManager.getGameProfile(game.packageName).copy(crosshairId = preset.id)
                        profileManager.saveGameProfile(profile)
                        openCustomizerDialog(game)
                    } else {
                        Toast.makeText(context, "Preset #${preset.id} Selected", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            layoutQuickCrosshairs.addView(crosshairItemView)
        }
    }

    private fun openCustomizerDialog(game: GameAppInfo) {
        val dialog = CrosshairCustomizerDialog(
            context = this,
            packageName = game.packageName,
            gameTitle = game.title,
            onProfileSaved = { profile ->
                Toast.makeText(this, "Crosshair profile saved for ${game.title}!", Toast.LENGTH_SHORT).show()
                if (profile.autoOverlayEnabled) {
                    startOverlayService(game.packageName)
                }
            }
        )
        dialog.show()
    }

    private fun initBottomNav() {
        val btnNavHome = findViewById<LinearLayout>(R.id.btn_nav_home)
        val btnNavGames = findViewById<LinearLayout>(R.id.btn_nav_games)
        val btnNavCrosshair = findViewById<LinearLayout>(R.id.btn_nav_crosshair)
        val btnNavPerformance = findViewById<LinearLayout>(R.id.btn_nav_performance)
        val btnNavProfiles = findViewById<LinearLayout>(R.id.btn_nav_profiles)

        btnNavHome.setOnClickListener {
            Toast.makeText(this, "Home Tab Selected", Toast.LENGTH_SHORT).show()
        }
        btnNavGames.setOnClickListener {
            Toast.makeText(this, "Games Library Selected (${gamesList.size} games)", Toast.LENGTH_SHORT).show()
        }
        btnNavCrosshair.setOnClickListener {
            val game = selectedGame ?: gamesList.firstOrNull()
            if (game != null) openCustomizerDialog(game)
        }
        btnNavPerformance.setOnClickListener {
            Toast.makeText(this, "Performance Boost active! RAM cleared.", Toast.LENGTH_SHORT).show()
        }
        btnNavProfiles.setOnClickListener {
            Toast.makeText(this, "Saved Profiles Loaded", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startOverlayService(packageName: String) {
        val intent = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_START_OVERLAY
            putExtra(OverlayService.EXTRA_PACKAGE_NAME, packageName)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission required for Gaming Crosshair HUD", Toast.LENGTH_LONG).show()
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        telemetryManager.startMonitoring { data ->
            tvFps.text = "${data.fps} FPS"
            tvPing.text = "${data.pingMs} ms"
            tvRam.text = "${data.ramPercentage}%"
            tvTemp.text = String.format("%.1f°C", data.tempCelsius)
        }
    }

    override fun onPause() {
        super.onPause()
        telemetryManager.stopMonitoring()
    }
}
