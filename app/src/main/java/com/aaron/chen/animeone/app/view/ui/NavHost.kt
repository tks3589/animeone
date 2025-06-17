package com.aaron.chen.animeone.app.view.ui

import CalendarScreen
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aaron.chen.animeone.app.view.ui.screen.AnimeScreen
import com.aaron.chen.animeone.app.view.ui.screen.RecordScreen
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.app.view.viewmodel.impl.AnimeoneViewModel
import org.koin.androidx.compose.koinViewModel

// 定義bottom nav的頁面與route
@Composable
fun AnimeNavHost(innerPadding: PaddingValues, navController: NavHostController) {
    val viewModel: IAnimeoneViewModel = koinViewModel<AnimeoneViewModel>()
    NavHost(
        navController = navController,
        startDestination = Screen.Anime.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Anime.route) { AnimeScreen(viewModel) }
        composable(Screen.Calendar.route) { CalendarScreen(viewModel) }
        composable(Screen.Record.route) { RecordScreen(viewModel) }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Anime : Screen("anime", "Anime", Icons.Default.Home)
    object Calendar: Screen("calendar", "Calendar", Icons.Default.DateRange)
    object Record : Screen("record", "Record", Icons.Default.Favorite)
}