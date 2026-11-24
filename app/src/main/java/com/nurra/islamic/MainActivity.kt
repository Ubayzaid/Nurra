package com.nurra.islamic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.nurra.islamic.ui.navigation.AdaptiveNavigationContainer
import com.nurra.islamic.ui.theme.NurraTheme
import com.nurra.islamic.viewmodel.DoaViewModel
import com.nurra.islamic.viewmodel.HomeViewModel
import com.nurra.islamic.viewmodel.LocationViewModel
import com.nurra.islamic.viewmodel.PrayerViewModel
import com.nurra.islamic.viewmodel.QiblaViewModel
import com.nurra.islamic.viewmodel.QuranViewModel
import com.nurra.islamic.viewmodel.SettingsViewModel
import com.nurra.islamic.viewmodel.TasbihViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val homeViewModel: HomeViewModel = viewModel()
            val locationViewModel: LocationViewModel = viewModel()
            val quranViewModel: QuranViewModel = viewModel()
            val prayerViewModel: PrayerViewModel = viewModel()
            val qiblaViewModel: QiblaViewModel = viewModel()
            val doaViewModel: DoaViewModel = viewModel()
            val tasbihViewModel: TasbihViewModel = viewModel()

            val settings by settingsViewModel.settings.collectAsState()

            NurraTheme(settings = settings) {
                val navController = rememberNavController()

                AdaptiveNavigationContainer(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    locationViewModel = locationViewModel,
                    quranViewModel = quranViewModel,
                    prayerViewModel = prayerViewModel,
                    qiblaViewModel = qiblaViewModel,
                    doaViewModel = doaViewModel,
                    tasbihViewModel = tasbihViewModel,
                    settingsViewModel = settingsViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
