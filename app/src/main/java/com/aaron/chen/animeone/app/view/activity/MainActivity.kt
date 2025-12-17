package com.aaron.chen.animeone.app.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
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
import androidx.lifecycle.lifecycleScope
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
import com.aaron.chen.animeone.extension.launchInAppReview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val showPermissionDialogState: MutableState<DialogType?> = mutableStateOf(null)
    private val showReviewInviteDialogState: MutableState<Boolean> = mutableStateOf(false)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                if (!hasNotificationPermission()) {
                    showPermissionDialogState.value = DialogType.PERMISSION_NOTIFICATION
                } else if (!hasReadMediaPermission()) {
                    showPermissionDialogState.value = DialogType.PERMISSION_VIDEO_READ
                } else if (!hasWriteMediaPermission()) {
                    showPermissionDialogState.value = DialogType.PERMISSION_VIDEO_WRITE
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
                BottomNavApp(showPermissionDialogState, showReviewInviteDialogState)
            }
        }
        lifecycleScope.launch {
            // 讀review state from datastore
            delay(3000)
            showReviewInviteDialogState.value = true
        }
    }

    override fun onResume() {
        super.onResume()

        showPermissionDialogState.value?.let { dialogType ->
            val permissionGranted = when (dialogType) {
                DialogType.PERMISSION_NOTIFICATION -> hasNotificationPermission()
                DialogType.PERMISSION_VIDEO_READ -> hasReadMediaPermission()
                DialogType.PERMISSION_VIDEO_WRITE -> hasWriteMediaPermission()
                else -> false
            }
            if (permissionGranted) {
                showPermissionDialogState.value = null
                requestPermissions()
            }
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun hasReadMediaPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasWriteMediaPermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestPermissions() {
        // 請求權限
        if (!hasNotificationPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else if (!hasReadMediaPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else if (!hasWriteMediaPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}

@Composable
fun BottomNavApp(showPermissionDialogState: MutableState<DialogType?>, showReviewInviteDialogState: MutableState<Boolean>) {
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
        ReviewInviteDialogDisplay(showReviewInviteDialogState)
    }
}

@Composable
private fun ReviewInviteDialogDisplay(showReviewInviteDialogState: MutableState<Boolean>) {
    val activity = LocalActivity.current
    if (showReviewInviteDialogState.value) {
        CustomAlertDialog(
            type = DialogType.REVIEW_INVITE,
            onConfirm = {
                activity?.launchInAppReview()
                showReviewInviteDialogState.value = false
            },
            onDismiss = { showReviewInviteDialogState.value = false }
        )
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