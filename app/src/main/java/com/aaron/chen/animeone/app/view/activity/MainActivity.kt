package com.aaron.chen.animeone.app.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aaron.chen.animeone.app.view.ui.AnimeNavHost
import com.aaron.chen.animeone.app.view.ui.Screen
import com.aaron.chen.animeone.app.view.ui.theme.AnimeoneTheme
import com.aaron.chen.animeone.app.view.ui.theme.CommonMargin
import com.aaron.chen.animeone.app.view.ui.widget.CustomAlertDialog
import com.aaron.chen.animeone.app.view.ui.widget.DialogType
import com.aaron.chen.animeone.constant.ExtraConst

class MainActivity : ComponentActivity() {
    private val showPermissionDialogState: MutableState<DialogType?> = mutableStateOf(null)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                if (!hasNotificationPermission()) {
                    showPermissionDialogState.value = DialogType.PERMISSION_NOTIFICATION
                } else if (!hasReadMediaPermission()) {
                    showPermissionDialogState.value = DialogType.PERMISSION_VIDEO_STORAGE
                }
            } else {
                requestPermissions()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
        requestPermissions()
        setContent {
            AnimeoneTheme {
                BottomNavApp(showPermissionDialogState)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        showPermissionDialogState.value?.let { dialogType ->
            val permissionGranted = when (dialogType) {
                DialogType.PERMISSION_NOTIFICATION -> hasNotificationPermission()
                DialogType.PERMISSION_VIDEO_STORAGE -> hasReadMediaPermission()
            }
            if (permissionGranted) {
                showPermissionDialogState.value = null
                requestPermissions()
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
fun BottomNavApp(showPermissionDialogState: MutableState<DialogType?>) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = currentRoute(navController)
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            when {
                                currentRoute == Screen.Anime.route && screen.route == Screen.Anime.route -> {
                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                        ExtraConst.SCROLL_TO_TOP, true
                                    )
                                }
                                currentRoute != screen.route -> {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
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
        PermissionDialogDisplay(showPermissionDialogState)
    }
}

@Composable
private fun PermissionDialogDisplay(showPermissionDialogState: MutableState<DialogType?>) {
    val context = LocalContext.current
    showPermissionDialogState.value?.let { dialogType ->
        CustomAlertDialog(
            type = dialogType,
            onConfirm = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                )
            },
            onDismiss = {}
        )
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