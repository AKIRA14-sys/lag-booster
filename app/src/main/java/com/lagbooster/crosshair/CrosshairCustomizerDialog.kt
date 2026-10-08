package com.lagbooster.crosshair

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.lagbooster.R
import com.lagbooster.manager.GameCrosshairProfile
import com.lagbooster.manager.ProfileManager

class CrosshairCustomizerDialog(
    context: Context,
    private val packageName: String,
    private val gameTitle: String,
    private val onProfileSaved: (GameCrosshairProfile) -> Unit
) : Dialog(context) {

    private val profileManager = ProfileManager(context)
    private lateinit var currentProfile: GameCrosshairProfile
    private var currentPreset: CrosshairPreset = CrosshairGenerator.getPresetById(1)

    private val paletteColors = listOf(
        "#FF1744", "#00E5FF", "#00E676", "#FFEA00", "#AA00FF", "#FF9100", "#FFFFFF", "#FF007F"
    )

    private var selectedColorHex: String = "#FF1744"
    private var selectedSizeDp: Float = 32f
    private var selectedOpacity: Float = 1.0f
    private var selectedRotationDeg: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_crosshair_customizer)

        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        currentProfile = profileManager.getGameProfile(packageName)
        currentPreset = CrosshairGenerator.getPresetById(currentProfile.crosshairId)

        selectedColorHex = currentProfile.colorHex
        selectedSizeDp = currentProfile.sizeDp
        selectedOpacity = currentProfile.opacity
        selectedRotationDeg = currentProfile.rotationDeg

        initViews()
    }

    private fun initViews() {
        val tvPresetInfo = findViewById<TextView>(R.id.tv_preset_info)
        val previewView = findViewById<CrosshairView>(R.id.preview_crosshair_view)
        val btnClose = findViewById<ImageButton>(R.id.btn_close_dialog)

        val btnTierNormal = findViewById<Button>(R.id.btn_tier_normal)
        val btnTierPro = findViewById<Button>(R.id.btn_tier_pro)
        val btnTierLegendary = findViewById<Button>(R.id.btn_tier_legendary)
        val btnTierGod = findViewById<Button>(R.id.btn_tier_god)

        val layoutColorPalette = findViewById<LinearLayout>(R.id.layout_color_palette)

        val seekbarSize = findViewById<SeekBar>(R.id.seekbar_size)
        val seekbarOpacity = findViewById<SeekBar>(R.id.seekbar_opacity)
        val seekbarRotation = findViewById<SeekBar>(R.id.seekbar_rotation)

        val tvSizeVal = findViewById<TextView>(R.id.tv_size_val)
        val tvOpacityVal = findViewById<TextView>(R.id.tv_opacity_val)
        val tvRotationVal = findViewById<TextView>(R.id.tv_rotation_val)

        val cbRedDot = findViewById<CheckBox>(R.id.cb_red_dot)
        val cbAutoOverlay = findViewById<CheckBox>(R.id.cb_auto_overlay)

        val btnReset = findViewById<Button>(R.id.btn_reset_customizer)
        val btnSave = findViewById<Button>(R.id.btn_save_apply)

        tvPresetInfo.text = "Target App: $gameTitle | Preset #${currentPreset.id} [${currentPreset.tier.name}]"

        updatePreview(previewView)

        seekbarSize.progress = selectedSizeDp.toInt()
        tvSizeVal.text = "${selectedSizeDp.toInt()} dp"

        seekbarOpacity.progress = (selectedOpacity * 100).toInt()
        tvOpacityVal.text = "${(selectedOpacity * 100).toInt()}%"

        seekbarRotation.progress = selectedRotationDeg.toInt()
        tvRotationVal.text = "${selectedRotationDeg.toInt()}°"

        cbRedDot.isChecked = currentProfile.redDotEnabled
        cbAutoOverlay.isChecked = currentProfile.autoOverlayEnabled

        layoutColorPalette.removeAllViews()
        paletteColors.forEach { hex ->
            val swatch = View(context).apply {
                val params = LinearLayout.LayoutParams(
                    (32 * context.resources.displayMetrics.density).toInt(),
                    (32 * context.resources.displayMetrics.density).toInt()
                )
                params.setMargins(0, 0, (12 * context.resources.displayMetrics.density).toInt(), 0)
                layoutParams = params

                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(hex))
                    setStroke(2, Color.WHITE)
                }
                background = drawable

                setOnClickListener {
                    selectedColorHex = hex
                    updatePreview(previewView)
                }
            }
            layoutColorPalette.addView(swatch)
        }

        btnTierNormal.setOnClickListener {
            val preset = CrosshairGenerator.getPresetsByTier(CrosshairTier.NORMAL).first()
            selectPreset(preset, previewView, tvPresetInfo)
        }
        btnTierPro.setOnClickListener {
            val preset = CrosshairGenerator.getPresetsByTier(CrosshairTier.PRO).first()
            selectPreset(preset, previewView, tvPresetInfo)
        }
        btnTierLegendary.setOnClickListener {
            val preset = CrosshairGenerator.getPresetsByTier(CrosshairTier.LEGENDARY).first()
            selectPreset(preset, previewView, tvPresetInfo)
        }
        btnTierGod.setOnClickListener {
            val preset = CrosshairGenerator.getPresetsByTier(CrosshairTier.GOD).first()
            selectPreset(preset, previewView, tvPresetInfo)
        }

        seekbarSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedSizeDp = progress.toFloat()
                tvSizeVal.text = "$progress dp"
                updatePreview(previewView)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        seekbarOpacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedOpacity = progress / 100f
                tvOpacityVal.text = "$progress%"
                updatePreview(previewView)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        seekbarRotation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedRotationDeg = progress.toFloat()
                tvRotationVal.text = "$progress°"
                updatePreview(previewView)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        cbRedDot.setOnCheckedChangeListener { _, isChecked ->
            currentProfile = currentProfile.copy(redDotEnabled = isChecked)
            updatePreview(previewView)
        }

        btnClose.setOnClickListener { dismiss() }

        btnReset.setOnClickListener {
            selectedColorHex = "#FF1744"
            selectedSizeDp = 32f
            selectedOpacity = 1.0f
            selectedRotationDeg = 0f
            currentPreset = CrosshairGenerator.getPresetById(1)

            seekbarSize.progress = 32
            seekbarOpacity.progress = 100
            seekbarRotation.progress = 0
            cbRedDot.isChecked = true
            cbAutoOverlay.isChecked = true

            tvPresetInfo.text = "Target App: $gameTitle | Preset #${currentPreset.id} [${currentPreset.tier.name}]"
            updatePreview(previewView)
        }

        btnSave.setOnClickListener {
            val updatedProfile = GameCrosshairProfile(
                packageName = packageName,
                crosshairId = currentPreset.id,
                colorHex = selectedColorHex,
                sizeDp = selectedSizeDp,
                opacity = selectedOpacity,
                rotationDeg = selectedRotationDeg,
                redDotEnabled = cbRedDot.isChecked,
                redDotColorHex = "#FF0000",
                redDotSizeDp = 4f,
                autoOverlayEnabled = cbAutoOverlay.isChecked
            )
            profileManager.saveGameProfile(updatedProfile)
            onProfileSaved(updatedProfile)
            dismiss()
        }
    }

    private fun selectPreset(preset: CrosshairPreset, previewView: CrosshairView, tvPresetInfo: TextView) {
        currentPreset = preset
        tvPresetInfo.text = "Target App: $gameTitle | Preset #${preset.id} [${preset.tier.name}]"
        updatePreview(previewView)
    }

    private fun updatePreview(previewView: CrosshairView) {
        val presetWithDot = currentPreset.copy(hasDot = currentProfile.redDotEnabled)
        previewView.setPreset(presetWithDot)
        previewView.applyCustomizations(
            colorHex = selectedColorHex,
            sizeDp = selectedSizeDp,
            opacity = selectedOpacity,
            rotationDeg = selectedRotationDeg
        )
    }
}
