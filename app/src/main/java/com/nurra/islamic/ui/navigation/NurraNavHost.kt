package com.nurra.islamic.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nurra.islamic.ui.screens.AboutScreen
import com.nurra.islamic.ui.screens.AppUpdateScreen
import com.nurra.islamic.ui.screens.AppearanceSettingsScreen
import com.nurra.islamic.ui.screens.DataSourceInfoScreen
import com.nurra.islamic.ui.screens.DeveloperInfoScreen
import com.nurra.islamic.ui.screens.DoaScreen
import com.nurra.islamic.ui.screens.GitHubInfoScreen
import com.nurra.islamic.ui.screens.HomeScreen
import com.nurra.islamic.ui.screens.LicensesInfoScreen
import com.nurra.islamic.ui.screens.LocationSettingsScreen
import com.nurra.islamic.ui.screens.ManageDownloadsScreen
import com.nurra.islamic.ui.screens.NotificationSettingsScreen
import com.nurra.islamic.ui.screens.PrayerScreen
import com.nurra.islamic.ui.screens.PrivacyPolicyScreen
import com.nurra.islamic.ui.screens.QiblaScreen
import com.nurra.islamic.ui.screens.QuranScreen
import com.nurra.islamic.ui.screens.SettingsScreen
import com.nurra.islamic.ui.screens.StorageSettingsScreen
import com.nurra.islamic.ui.screens.SurahDetailScreen
import com.nurra.islamic.ui.screens.TasbihScreen
import com.nurra.islamic.ui.screens.TechStackInfoScreen
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
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it / 6 },
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(tween(220))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 12 },
                animationSpec = spring(
                    dampingRatio = 1f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(tween(150))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 12 },
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(tween(200))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it / 6 },
                animationSpec = spring(
                    dampingRatio = 1f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(tween(150))
        }
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
                onNavigateToNotification = { navController.navigate(Screen.NotificationSettings.route) },
                onNavigateToStorage = { navController.navigate(Screen.StorageSettings.route) },
                onNavigateToAbout = { navController.navigate(Screen.AboutSettings.route) }
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

        composable(Screen.StorageSettings.route) {
            StorageSettingsScreen(
                viewModel = settingsViewModel,
                onNavigateToManageDownloads = { navController.navigate(Screen.ManageDownloads.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ManageDownloads.route) {
            ManageDownloadsScreen(
                quranViewModel = quranViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AboutSettings.route) {
            AboutScreen(
                onNavigateToUpdate = { navController.navigate(Screen.AppUpdate.route) },
                onNavigateToGitHub = { navController.navigate(Screen.GitHubInfo.route) },
                onNavigateToDeveloper = { navController.navigate(Screen.DeveloperInfo.route) },
                onNavigateToDataSource = { navController.navigate(Screen.DataSourceInfo.route) },
                onNavigateToTechStack = { navController.navigate(Screen.TechStackInfo.route) },
                onNavigateToLicenses = { navController.navigate(Screen.LicensesInfo.route) },
                onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AppUpdate.route) {
            AppUpdateScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.GitHubInfo.route) {
            GitHubInfoScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.DeveloperInfo.route) {
            DeveloperInfoScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.DataSourceInfo.route) {
            DataSourceInfoScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.TechStackInfo.route) {
            TechStackInfoScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.LicensesInfo.route) {
            LicensesInfoScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
