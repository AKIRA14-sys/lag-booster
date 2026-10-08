package com.lagbooster.manager

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class GameAppInfo(
    val packageName: String,
    val title: String,
    val icon: Drawable?,
    val isGame: Boolean,
    val isFavorite: Boolean = false,
    val isSelected: Boolean = false
)

class GameManager(private val context: Context, private val profileManager: ProfileManager) {

    fun discoverInstalledGames(): List<GameAppInfo> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val launchableApps = try {
            pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
        } catch (e: Exception) {
            emptyList()
        }

        val favorites = profileManager.getFavorites()
        val gamesList = mutableListOf<GameAppInfo>()

        for (resolveInfo in launchableApps) {
            val appInfo = resolveInfo.activityInfo.applicationInfo
            val packageName = appInfo.packageName

            if (packageName == context.packageName) continue

            val isCategoryGame = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME
            } else {
                @Suppress("DEPRECATION")
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }

            val appTitle = resolveInfo.loadLabel(pm).toString()
            val appIcon = try {
                resolveInfo.loadIcon(pm)
            } catch (e: Exception) {
                null
            }

            gamesList.add(
                GameAppInfo(
                    packageName = packageName,
                    title = appTitle,
                    icon = appIcon,
                    isGame = isCategoryGame,
                    isFavorite = favorites.contains(packageName)
                )
            )
        }

        if (gamesList.isEmpty()) {
            gamesList.addAll(getBuiltInSampleGames(favorites))
        }

        return gamesList.sortedWith(compareByDescending<GameAppInfo> { it.isFavorite }.thenBy { it.title })
    }

    private fun getBuiltInSampleGames(favorites: Set<String>): List<GameAppInfo> {
        return listOf(
            GameAppInfo("com.demo.apexpredator", "APEX PREDATOR", null, isGame = true, isFavorite = favorites.contains("com.demo.apexpredator")),
            GameAppInfo("com.demo.cybercommand", "CYBER COMMAND", null, isGame = true, isFavorite = favorites.contains("com.demo.cybercommand")),
            GameAppInfo("com.demo.neonwarfare", "NEON WARFARE", null, isGame = true, isFavorite = favorites.contains("com.demo.neonwarfare")),
            GameAppInfo("com.demo.boostracers", "BOOST RACERS", null, isGame = true, isFavorite = favorites.contains("com.demo.boostracers")),
            GameAppInfo("com.demo.shadowstrike", "SHADOW STRIKE", null, isGame = true, isFavorite = favorites.contains("com.demo.shadowstrike"))
        )
    }

    fun launchGame(packageName: String): Boolean {
        profileManager.setLastPlayedGame(packageName)
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        } else {
            false
        }
    }
}
