package com.nurra.islamic.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nurra.islamic.viewmodel.DoaViewModel
import com.nurra.islamic.viewmodel.HomeViewModel
import com.nurra.islamic.viewmodel.LocationViewModel
import com.nurra.islamic.viewmodel.PrayerViewModel
import com.nurra.islamic.viewmodel.QiblaViewModel
import com.nurra.islamic.viewmodel.QuranViewModel
import com.nurra.islamic.viewmodel.SettingsViewModel
import com.nurra.islamic.viewmodel.TasbihViewModel

@Composable
fun AdaptiveNavigationContainer(
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    val isTopLevelDestination = bottomNavItems.any { it.route == currentRoute }

    if (isWideScreen && isTopLevelDestination) {
        // Wide Screen / Tablet Navigation Rail Architecture
        Row(modifier = modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier
                    .fillMaxHeight()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Spacer(modifier = Modifier.weight(1f))

                bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route

                    NavigationRailItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            Box(modifier = Modifier.weight(1f)) {
                NurraNavHost(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    locationViewModel = locationViewModel,
                    quranViewModel = quranViewModel,
                    prayerViewModel = prayerViewModel,
                    qiblaViewModel = qiblaViewModel,
                    doaViewModel = doaViewModel,
                    tasbihViewModel = tasbihViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    } else {
        // Phone Compact Layout: Zero-Inset Parent Scaffold for True Edge-To-Edge
        Scaffold(
            modifier = modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0), // ZERO INSETS on parent Scaffold so child screen extends to top coordinate Y=0
            bottomBar = {
                AnimatedVisibility(
                    visible = isTopLevelDestination,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            ),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        windowInsets = NavigationBarDefaults.windowInsets
                    ) {
                        bottomNavItems.forEach { screen ->
                            val isSelected = currentRoute == screen.route

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NurraNavHost(
                navController = navController,
                homeViewModel = homeViewModel,
                locationViewModel = locationViewModel,
                quranViewModel = quranViewModel,
                prayerViewModel = prayerViewModel,
                qiblaViewModel = qiblaViewModel,
                doaViewModel = doaViewModel,
                tasbihViewModel = tasbihViewModel,
                settingsViewModel = settingsViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
