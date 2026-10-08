package com.lagbooster.crosshair

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CrosshairGeneratorTest {

    @Test
    fun testPresetCount() {
        val presets = CrosshairGenerator.getAllPresets()
        assertEquals(520, presets.size)
    }

    @Test
    fun testTiers() {
        val normal = CrosshairGenerator.getPresetsByTier(CrosshairTier.NORMAL)
        val pro = CrosshairGenerator.getPresetsByTier(CrosshairTier.PRO)
        val legendary = CrosshairGenerator.getPresetsByTier(CrosshairTier.LEGENDARY)
        val god = CrosshairGenerator.getPresetsByTier(CrosshairTier.GOD)

        assertEquals(150, normal.size)
        assertEquals(150, pro.size)
        assertEquals(120, legendary.size)
        assertEquals(100, god.size)
    }

    @Test
    fun testGetById() {
        val p1 = CrosshairGenerator.getPresetById(1)
        assertNotNull(p1)
        assertEquals(1, p1.id)
    }
}
