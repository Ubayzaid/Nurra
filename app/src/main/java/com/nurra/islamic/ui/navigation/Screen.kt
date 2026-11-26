package com.nurra.islamic.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.CompassCalibration
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "Hari Ini", Icons.Filled.Home, Icons.Outlined.Home)
    object Quran : Screen("quran", "Al-Qur'an", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook)
    object Tasbih : Screen("tasbih", "Tasbih", Icons.Filled.CompassCalibration, Icons.Outlined.CompassCalibration)
    object Doa : Screen("doa", "Doa", Icons.Filled.AutoStories, Icons.Outlined.AutoStories)
    object Settings : Screen("settings", "Setelan", Icons.Filled.Settings, Icons.Outlined.Settings)

    object AppearanceSettings : Screen("settings_appearance", "Tampilan", Icons.Filled.Palette, Icons.Outlined.Palette)
    object LocationSettings : Screen("settings_location", "Lokasi & Jadwal Salat", Icons.Filled.LocationOn, Icons.Outlined.LocationOn)
    object NotificationSettings : Screen("settings_notification", "Notifikasi & Pengingat", Icons.Filled.Notifications, Icons.Outlined.Notifications)
    object StorageSettings : Screen("settings_storage", "Data & Penyimpanan", Icons.Filled.Storage, Icons.Outlined.Storage)
    object ManageDownloads : Screen("manage_downloads", "Kelola Unduhan", Icons.Filled.Download, Icons.Outlined.Download)

    object AboutSettings : Screen("settings_about", "Tentang Nurra", Icons.Filled.Info, Icons.Outlined.Info)
    object AppUpdate : Screen("settings_app_update", "Update Aplikasi", Icons.Filled.SystemUpdate, Icons.Outlined.SystemUpdate)
    object GitHubInfo : Screen("settings_about_github", "GitHub Repository", Icons.Filled.Code, Icons.Outlined.Code)
    object DeveloperInfo : Screen("settings_about_developer", "Pengembang", Icons.Filled.Info, Icons.Outlined.Info)
    object DataSourceInfo : Screen("settings_about_datasource", "Sumber Data", Icons.Filled.Storage, Icons.Outlined.Storage)
    object TechStackInfo : Screen("settings_about_tech", "Teknologi", Icons.Filled.Code, Icons.Outlined.Code)
    object LicensesInfo : Screen("settings_about_license", "Lisensi Terbuka", Icons.Filled.Info, Icons.Outlined.Info)
    object PrivacyPolicy : Screen("settings_about_privacy", "Kebijakan Privasi", Icons.Filled.Policy, Icons.Outlined.Policy)

    object Prayer : Screen("prayer", "Jadwal Salat", Icons.Filled.Home, Icons.Outlined.Home)
    object Qibla : Screen("qibla", "Kompas Kiblat", Icons.Filled.Explore, Icons.Outlined.Explore)

    object SurahDetail : Screen("surah_detail/{surahId}", "Detail Surah", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook) {
        fun createRoute(surahId: Int) = "surah_detail/$surahId"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Quran,
    Screen.Tasbih,
    Screen.Doa,
    Screen.Settings
)
