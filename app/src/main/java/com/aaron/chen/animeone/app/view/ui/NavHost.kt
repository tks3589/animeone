package com.aaron.chen.animeone.app.view.ui

import CalendarScreen
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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

// 定義bottom nav的頁面與route
@Composable
fun AnimeNavHost(innerPadding: PaddingValues, navController: NavHostController) {
    NavHost(
        navController = navController,
        exitTransition = { ExitTransition.None }, // 關閉預設換頁動畫
        enterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        startDestination = Screen.Anime.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Anime.route) { AnimeScreen(navController) }
        composable(Screen.Calendar.route) { CalendarScreen() }
        composable(Screen.Record.route) { RecordScreen() }
        composable(Screen.Favorite.route) { FavoriteScreen() }
        composable(Screen.Download.route) { DownloadScreen() }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Anime : Screen("anime", "動畫列表", Icons.Default.Home)
    object Calendar: Screen("calendar", "時間表", Icons.Default.DateRange)
    object Record : Screen("record", "觀看紀錄", Icons.Default.Refresh)
    object Favorite : Screen("favorite", "我的收藏", Icons.Default.Favorite)
    object Download : Screen("download", "下載影片", Icons.Default.List)
}