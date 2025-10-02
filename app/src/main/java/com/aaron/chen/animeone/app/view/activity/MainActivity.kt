package com.aaron.chen.animeone.app.view.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aaron.chen.animeone.app.view.ui.AnimeNavHost
import com.aaron.chen.animeone.app.view.ui.Screen
import com.aaron.chen.animeone.app.view.ui.theme.AnimeoneTheme
import com.aaron.chen.animeone.app.view.ui.theme.CommonMargin

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                if (!hasNotificationPermission()) {
                    Toast.makeText(this, "需要通知權限！", Toast.LENGTH_SHORT).show()
                } else if (!hasReadMediaPermission()) {
                    Toast.makeText(this, "需要讀取影片權限！", Toast.LENGTH_SHORT).show()
                }
            } else {
                requestPermissions()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissions()
        setContent {
            AnimeoneTheme {
                BottomNavApp()
            }
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasReadMediaPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        // 請求權限
        if (!hasNotificationPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (!hasReadMediaPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
}

@Composable
fun BottomNavApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = currentRoute(navController)
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                // 避免重複堆疊
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title, modifier = Modifier.size(CommonMargin.m5)) },
                        label = { Text(screen.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimeNavHost(innerPadding, navController)
    }
}

val bottomNavItems = listOf(
    Screen.Anime,
    Screen.Calendar,
    Screen.Record,
    Screen.Favorite,
    Screen.Download
)

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}