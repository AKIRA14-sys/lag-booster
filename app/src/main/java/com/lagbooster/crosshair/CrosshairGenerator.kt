package com.lagbooster.crosshair

object CrosshairGenerator {

    private val ALL_PRESETS: List<CrosshairPreset> by lazy {
        generateAllPresets()
    }

    fun getAllPresets(): List<CrosshairPreset> = ALL_PRESETS

    fun getPresetsByTier(tier: CrosshairTier): List<CrosshairPreset> {
        return ALL_PRESETS.filter { it.tier == tier }
    }

    fun getPresetById(id: Int): CrosshairPreset {
        return ALL_PRESETS.find { it.id == id } ?: ALL_PRESETS.first()
    }

    private fun generateAllPresets(): List<CrosshairPreset> {
        val list = mutableListOf<CrosshairPreset>()

        val colorsNormal = listOf(
            "#FF1744", "#00E676", "#29B6F6", "#FFEA00", "#AA00FF", "#FF9100", "#FFFFFF", "#3D5AFE"
        )
        val colorsPro = listOf(
            "#00E5FF", "#FF3D00", "#C6FF00", "#E040FB", "#76FF03", "#FFD600", "#18FFFF", "#FF1744"
        )
        val colorsLegendary = listOf(
            "#FF0055", "#00FFCC", "#FFFF00", "#FF00FF", "#00FFFF", "#FF4500", "#7B1FA2", "#00FF66"
        )
        val colorsGod = listOf(
            "#FFD700", "#FF0000", "#00FFFF", "#FF007F", "#39FF14", "#CC00FF", "#00FF99", "#FF3300"
        )

        var currentId = 1

        for (i in 0 until 150) {
            val base = CrosshairBaseStyle.values()[i % 4]
            val primary = colorsNormal[i % colorsNormal.size]
            val gap = 2f + (i % 6) * 1.5f
            val length = 6f + (i % 8) * 2f
            val thickness = 1.5f + (i % 3) * 0.8f
            val dot = (i % 2 == 0)

            list.add(
                CrosshairPreset(
                    id = currentId++,
                    name = "Tactical Standard #${i + 1}",
                    tier = CrosshairTier.NORMAL,
                    baseStyle = base,
                    primaryColorHex = primary,
                    secondaryColorHex = "#111111",
                    gapDp = gap,
                    lengthDp = length,
                    thicknessDp = thickness,
                    dotSizeDp = 2.5f + (i % 3),
                    hasDot = dot,
                    hasOutline = (i % 3 != 0),
                    outlineColorHex = "#000000",
                    rotationDeg = if (i % 10 == 0) 45f else 0f
                )
            )
        }

        for (i in 0 until 150) {
            val base = CrosshairBaseStyle.values()[i % 6]
            val primary = colorsPro[i % colorsPro.size]
            val secondary = colorsPro[(i + 3) % colorsPro.size]
            val gap = 1.5f + (i % 7) * 1.2f
            val length = 8f + (i % 10) * 2.5f
            val thickness = 2f + (i % 4) * 0.5f

            list.add(
                CrosshairPreset(
                    id = currentId++,
                    name = "Pro Apex #${i + 1}",
                    tier = CrosshairTier.PRO,
                    baseStyle = base,
                    primaryColorHex = primary,
                    secondaryColorHex = secondary,
                    gapDp = gap,
                    lengthDp = length,
                    thicknessDp = thickness,
                    dotSizeDp = 3f + (i % 4) * 0.5f,
                    hasDot = true,
                    hasOutline = true,
                    outlineColorHex = "#0A0A0A",
                    rotationDeg = (i % 8) * 15f,
                    ringRadiusDp = 6f + (i % 5) * 2f
                )
            )
        }

        for (i in 0 until 120) {
            val base = CrosshairBaseStyle.values()[i % 10]
            val primary = colorsLegendary[i % colorsLegendary.size]
            val secondary = colorsLegendary[(i + 4) % colorsLegendary.size]

            list.add(
                CrosshairPreset(
                    id = currentId++,
                    name = "Mythic Predator #${i + 1}",
                    tier = CrosshairTier.LEGENDARY,
                    baseStyle = base,
                    primaryColorHex = primary,
                    secondaryColorHex = secondary,
                    gapDp = 1f + (i % 8) * 1.5f,
                    lengthDp = 10f + (i % 12) * 2f,
                    thicknessDp = 2.5f + (i % 3) * 0.8f,
                    dotSizeDp = 3.5f + (i % 3) * 0.8f,
                    hasDot = true,
                    hasOutline = true,
                    outlineColorHex = "#000000",
                    rotationDeg = (i % 12) * 30f,
                    ringRadiusDp = 8f + (i % 6) * 3f
                )
            )
        }

        for (i in 0 until 100) {
            val base = when (i % 9) {
                0 -> CrosshairBaseStyle.SHARINGAN_TRIPLE
                1 -> CrosshairBaseStyle.RINNEGAN_RINGS
                2 -> CrosshairBaseStyle.HEXAGON_SNIPER
                3 -> CrosshairBaseStyle.OCTAGON_TACTICAL
                4 -> CrosshairBaseStyle.CUSTOM_CROSSHAIR
                5 -> CrosshairBaseStyle.PRO_CUSTOM_CROSSHAIR
                6 -> CrosshairBaseStyle.CHEVRON_CROSSHAIR
                7 -> CrosshairBaseStyle.CHARGING_CROSSHAIR
                else -> CrosshairBaseStyle.STARBURST
            }
            val primary = colorsGod[i % colorsGod.size]
            val secondary = colorsGod[(i + 2) % colorsGod.size]

            list.add(
                CrosshairPreset(
                    id = currentId++,
                    name = "God Eye Prime #${i + 1}",
                    tier = CrosshairTier.GOD,
                    baseStyle = base,
                    primaryColorHex = primary,
                    secondaryColorHex = secondary,
                    gapDp = 2f + (i % 5) * 2f,
                    lengthDp = 12f + (i % 8) * 3f,
                    thicknessDp = 3f + (i % 3) * 0.5f,
                    dotSizeDp = 4f + (i % 4) * 0.8f,
                    hasDot = true,
                    hasOutline = true,
                    outlineColorHex = "#000000",
                    rotationDeg = (i % 16) * 22.5f,
                    ringRadiusDp = 10f + (i % 5) * 4f
                )
            )
        }

        return list
    }
}
