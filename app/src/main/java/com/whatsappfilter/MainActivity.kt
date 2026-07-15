package com.whatsappfilter

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.whatsappfilter.data.AppDatabase
import com.whatsappfilter.ui.GroupDetailScreen
import com.whatsappfilter.ui.MainScreen
import com.whatsappfilter.data.SettingsManager
import com.whatsappfilter.ui.theme.WhatsAppFilterTheme
import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

sealed class Screen {
    object Main : Screen()
    data class GroupDetail(val groupName: String) : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var settingsManager: SettingsManager
    private var isNotificationServiceEnabledState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = AppDatabase.getInstance(applicationContext)
        settingsManager = SettingsManager(applicationContext)

        setContent {
            WhatsAppFilterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
                    val isServiceEnabled by isNotificationServiceEnabledState

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState is Screen.GroupDetail && initialState is Screen.Main) {
                                slideInHorizontally(
                                    animationSpec = tween(400),
                                    initialOffsetX = { fullWidth -> fullWidth }
                                ) togetherWith slideOutHorizontally(
                                    animationSpec = tween(400),
                                    targetOffsetX = { fullWidth -> -fullWidth }
                                )
                            } else if (targetState is Screen.Main && initialState is Screen.GroupDetail) {
                                slideInHorizontally(
                                    animationSpec = tween(400),
                                    initialOffsetX = { fullWidth -> -fullWidth }
                                ) togetherWith slideOutHorizontally(
                                    animationSpec = tween(400),
                                    targetOffsetX = { fullWidth -> fullWidth }
                                )
                            } else {
                                slideInHorizontally(
                                    animationSpec = tween(400),
                                    initialOffsetX = { fullWidth -> fullWidth }
                                ) togetherWith slideOutHorizontally(
                                    animationSpec = tween(400),
                                    targetOffsetX = { fullWidth -> -fullWidth }
                                )
                            }
                        },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            is Screen.Main -> {
                                var showExitPrompt by remember { mutableStateOf(false) }
                                val context = LocalContext.current
                                val scope = rememberCoroutineScope()
                                
                                MainScreen(
                                    dao = database.dao,
                                    settingsManager = settingsManager,
                                    isNotificationServiceEnabled = isServiceEnabled,
                                    onRequestPermission = { openNotificationAccessSettings() },
                                    onNavigateToGroup = { groupName ->
                                        currentScreen = Screen.GroupDetail(groupName)
                                    },
                                    onExitRequest = {
                                        if (showExitPrompt) {
                                            finish()
                                        } else {
                                            showExitPrompt = true
                                            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                                            scope.launch {
                                                kotlinx.coroutines.delay(2000)
                                                showExitPrompt = false
                                            }
                                        }
                                    }
                                )
                            }
                            is Screen.GroupDetail -> {
                                BackHandler {
                                    currentScreen = Screen.Main
                                }
                                GroupDetailScreen(
                                    groupName = screen.groupName,
                                    dao = database.dao,
                                    onBack = { currentScreen = Screen.Main }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isNotificationServiceEnabledState.value = checkNotificationServiceEnabled()
    }

    private fun checkNotificationServiceEnabled(): Boolean {
        val cn = ComponentName(this, WhatsAppListenerService::class.java)
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
    }

    private fun openNotificationAccessSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback for older devices/custom ROMs
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }
    }
}
