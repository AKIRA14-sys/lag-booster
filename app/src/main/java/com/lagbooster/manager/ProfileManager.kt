package com.lagbooster.manager

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

data class GameCrosshairProfile(
    val packageName: String,
    val crosshairId: Int = 1,
    val colorHex: String = "#FF1744",
    val sizeDp: Float = 32f,
    val opacity: Float = 1.0f,
    val rotationDeg: Float = 0f,
    val redDotEnabled: Boolean = true,
    val redDotColorHex: String = "#FF0000",
    val redDotSizeDp: Float = 4f,
    val autoOverlayEnabled: Boolean = true
)

class ProfileManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("lag_booster_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FAVORITES = "key_favorites"
        private const val KEY_LAST_PLAYED = "key_last_played"
        private const val KEY_PROFILE_PREFIX = "profile_"
    }

    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun isFavorite(packageName: String): Boolean {
        return getFavorites().contains(packageName)
    }

    fun toggleFavorite(packageName: String): Boolean {
        val current = getFavorites().toMutableSet()
        val isFavNow = if (current.contains(packageName)) {
            current.remove(packageName)
            false
        } else {
            current.add(packageName)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        return isFavNow
    }

    fun getLastPlayedGame(): String? {
        return prefs.getString(KEY_LAST_PLAYED, null)
    }

    fun setLastPlayedGame(packageName: String) {
        prefs.edit().putString(KEY_LAST_PLAYED, packageName).apply()
    }

    fun getGameProfile(packageName: String): GameCrosshairProfile {
        val jsonString = prefs.getString(KEY_PROFILE_PREFIX + packageName, null)
            ?: return GameCrosshairProfile(packageName = packageName)

        return try {
            val json = JSONObject(jsonString)
            GameCrosshairProfile(
                packageName = json.optString("packageName", packageName),
                crosshairId = json.optInt("crosshairId", 1),
                colorHex = json.optString("colorHex", "#FF1744"),
                sizeDp = json.optDouble("sizeDp", 32.0).toFloat(),
                opacity = json.optDouble("opacity", 1.0).toFloat(),
                rotationDeg = json.optDouble("rotationDeg", 0.0).toFloat(),
                redDotEnabled = json.optBoolean("redDotEnabled", true),
                redDotColorHex = json.optString("redDotColorHex", "#FF0000"),
                redDotSizeDp = json.optDouble("redDotSizeDp", 4.0).toFloat(),
                autoOverlayEnabled = json.optBoolean("autoOverlayEnabled", true)
            )
        } catch (e: Exception) {
            GameCrosshairProfile(packageName = packageName)
        }
    }

    fun saveGameProfile(profile: GameCrosshairProfile) {
        val json = JSONObject().apply {
            put("packageName", profile.packageName)
            put("crosshairId", profile.crosshairId)
            put("colorHex", profile.colorHex)
            put("sizeDp", profile.sizeDp.toDouble())
            put("opacity", profile.opacity.toDouble())
            put("rotationDeg", profile.rotationDeg.toDouble())
            put("redDotEnabled", profile.redDotEnabled)
            put("redDotColorHex", profile.redDotColorHex)
            put("redDotSizeDp", profile.redDotSizeDp.toDouble())
            put("autoOverlayEnabled", profile.autoOverlayEnabled)
        }
        prefs.edit().putString(KEY_PROFILE_PREFIX + profile.packageName, json.toString()).apply()
    }
}
