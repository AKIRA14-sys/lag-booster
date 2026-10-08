package com.lagbooster.crosshair

enum class CrosshairTier { NORMAL, PRO, LEGENDARY, GOD }

enum class CrosshairBaseStyle {
    CROSS,
    T_SHAPE,
    CIRCLE_DOT,
    DIAMOND,
    CHEVRON,
    DUAL_RING,
    SHARINGAN_TRIPLE,
    RINNEGAN_RINGS,
    STARBURST,
    TRI_FORCE,
    HEXAGON_SNIPER,
    OCTAGON_TACTICAL,
    ULTRA_COMPACT
}

data class CrosshairPreset(
    val id: Int,
    val name: String,
    val tier: CrosshairTier,
    val baseStyle: CrosshairBaseStyle,
    val primaryColorHex: String,
    val secondaryColorHex: String = "#00FFFF",
    val gapDp: Float = 4f,
    val lengthDp: Float = 10f,
    val thicknessDp: Float = 2f,
    val dotSizeDp: Float = 3f,
    val hasDot: Boolean = true,
    val hasOutline: Boolean = true,
    val outlineColorHex: String = "#000000",
    val rotationDeg: Float = 0f,
    val ringRadiusDp: Float = 8f
)
