package com.nurra.islamic.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nurra.islamic.ui.screens.AppearanceSettingsScreen
import com.nurra.islamic.ui.screens.DoaScreen
import com.nurra.islamic.ui.screens.HomeScreen
import com.nurra.islamic.ui.screens.LocationSettingsScreen
import com.nurra.islamic.ui.screens.NotificationSettingsScreen
import com.nurra.islamic.ui.screens.PrayerScreen
import com.nurra.islamic.ui.screens.QiblaScreen
import com.nurra.islamic.ui.screens.QuranScreen
import com.nurra.islamic.ui.screens.SettingsScreen
import com.nurra.islamic.ui.screens.SurahDetailScreen
import com.nurra.islamic.ui.screens.TasbihScreen
import com.nurra.islamic.viewmodel.DoaViewModel
import com.nurra.islamic.viewmodel.HomeViewModel
import com.nurra.islamic.viewmodel.LocationViewModel
import com.nurra.islamic.viewmodel.PrayerViewModel
import com.nurra.islamic.viewmodel.QiblaViewModel
import com.nurra.islamic.viewmodel.QuranViewModel
import com.nurra.islamic.viewmodel.SettingsViewModel
import com.nurra.islamic.viewmodel.TasbihViewModel

@Composable
fun NurraNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    locationViewModel: LocationViewModel,
    quranViewModel: QuranViewModel,
    prayerViewModel: PrayerViewModel,
    qiblaViewModel: QiblaViewModel,
    doaViewModel: DoaViewModel,
    tasbihViewModel: TasbihViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val settings by settingsViewModel.settings.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                locationViewModel = locationViewModel,
                settingsViewModel = settingsViewModel,
                onNavigateToQuran = { navController.navigate(Screen.Quran.route) },
                onNavigateToSurahDetail = { surahId ->
                    navController.navigate(Screen.SurahDetail.createRoute(surahId))
                },
                onNavigateToPrayer = { navController.navigate(Screen.Prayer.route) },
                onNavigateToGoa = { navController.navigate(Screen.Doa.route) },
                onNavigateToTasbih = { navController.navigate(Screen.Tasbih.route) },
                onNavigateToQibla = { navController.navigate(Screen.Qibla.route) }
            )
        }

        composable(Screen.Quran.route) {
            QuranScreen(
                viewModel = quranViewModel,
                onNavigateToSurahDetail = { surahId ->
                    navController.navigate(Screen.SurahDetail.createRoute(surahId))
                }
            )
        }

        composable(
            route = Screen.SurahDetail.route,
            arguments = listOf(navArgument("surahId") { type = NavType.IntType })
        ) { backStackEntry ->
            val surahId = backStackEntry.arguments?.getInt("surahId") ?: 1
            SurahDetailScreen(
                surahId = surahId,
                viewModel = quranViewModel,
                quranFontSizeSp = settings.quranFontSizeSp,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Prayer.route) {
            PrayerScreen(
                viewModel = prayerViewModel,
                onNavigateToQibla = { navController.navigate(Screen.Qibla.route) }
            )
        }

        composable(Screen.Qibla.route) {
            QiblaScreen(
                viewModel = qiblaViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Tasbih.route) {
            TasbihScreen(
                viewModel = tasbihViewModel
            )
        }

        composable(Screen.Doa.route) {
            DoaScreen(
                viewModel = doaViewModel
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateToAppearance = { navController.navigate(Screen.AppearanceSettings.route) },
                onNavigateToLocationPicker = { navController.navigate(Screen.LocationSettings.route) },
                onNavigateToNotification = { navController.navigate(Screen.NotificationSettings.route) }
            )
        }

        composable(Screen.AppearanceSettings.route) {
            AppearanceSettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.LocationSettings.route) {
            LocationSettingsScreen(
                settingsViewModel = settingsViewModel,
                locationViewModel = locationViewModel,
                onSelectManualLocation = { locationViewModel.setSheetVisible(true) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
