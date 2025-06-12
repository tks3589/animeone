package com.aaron.chen.animeone.app.view.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.aaron.chen.animeone.app.view.ui.screen.AnimeScreen
import com.aaron.chen.animeone.app.view.ui.screen.CalendarScreen
import com.aaron.chen.animeone.app.view.ui.screen.RecordScreen

// 定義bottom nav的頁面與route
@Composable
fun AnimeNavHost(innerPadding: PaddingValues, navController: NavHostController) {
    androidx.navigation.compose.NavHost(
        navController = navController,
        startDestination = Screen.Anime.route,
        modifier = androidx.compose.ui.Modifier.padding(innerPadding)
    ) {
        composable(Screen.Anime.route) { AnimeScreen() }
        composable(Screen.Calendar.route) { CalendarScreen() }
        composable(Screen.Record.route) { RecordScreen() }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Anime : Screen("anime", "Anime", Icons.Default.Home)
    object Calendar: Screen("calendar", "Calendar", Icons.Default.DateRange)
    object Record : Screen("record", "Record", Icons.Default.Favorite)
}