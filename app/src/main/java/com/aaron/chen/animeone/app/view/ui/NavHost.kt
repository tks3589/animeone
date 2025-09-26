package com.aaron.chen.animeone.app.view.ui

import CalendarScreen
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aaron.chen.animeone.app.view.ui.screen.AnimeScreen
import com.aaron.chen.animeone.app.view.ui.screen.DownloadScreen
import com.aaron.chen.animeone.app.view.ui.screen.FavoriteScreen
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
        composable(Screen.Favorite.route) { FavoriteScreen(viewModel) }
        composable(Screen.Download.route) { DownloadScreen(viewModel) }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Anime : Screen("anime", "動畫列表", Icons.Default.Home)
    object Calendar: Screen("calendar", "時間表", Icons.Default.DateRange)
    object Record : Screen("record", "觀看紀錄", Icons.Default.Refresh)
    object Favorite : Screen("favorite", "我的收藏", Icons.Default.Favorite)
    object Download : Screen("download", "下載管理", Icons.Default.List)
}