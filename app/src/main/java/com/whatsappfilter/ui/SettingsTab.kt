package com.whatsappfilter.ui

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsappfilter.data.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    settingsManager: SettingsManager
) {
    val context = LocalContext.current
    var showInstructions by remember { mutableStateOf(false) }
    
    if (showInstructions) {
        SetupInstructionsDialog(onDismiss = { showInstructions = false })
    }
    
    var showProxyWarning by remember { mutableStateOf(false) }
    
    var isProxyEnabled by remember { mutableStateOf(settingsManager.isProxyNotificationEnabled) }
    var isSoundEnabled by remember { mutableStateOf(settingsManager.isSoundEnabled) }
    var isVibrateEnabled by remember { mutableStateOf(settingsManager.isVibrateEnabled) }
    var ringtoneName by remember { mutableStateOf(settingsManager.customRingtoneName ?: "Default Notification Sound") }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                settingsManager.customRingtoneUri = uri.toString()
                val ringtone = RingtoneManager.getRingtone(context, uri)
                val name = ringtone.getTitle(context)
                settingsManager.customRingtoneName = name
                ringtoneName = name
            } else {
                settingsManager.customRingtoneUri = null
                settingsManager.customRingtoneName = "Default Notification Sound"
                ringtoneName = "Default Notification Sound"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(vertical = 24.dp)
        )
        
        // Instructions Card
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .bounceClick { showInstructions = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info, 
                    contentDescription = "Info", 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Setup Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "How to configure WhatsApp and the Filter app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
        
        Text(
            text = "Proxy Notifications",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Proxy Notifications Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Enable Proxy Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Play a sound when unmuted users send a message. Requires setting WhatsApp group notification tone to Silent.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = isProxyEnabled,
                onCheckedChange = { 
                    if (it) {
                        showProxyWarning = true
                    } else {
                        isProxyEnabled = false
                        settingsManager.isProxyNotificationEnabled = false
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSecondary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
        
        if (isProxyEnabled) {
            Text(
                text = "Alert Preferences",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Sound Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Play Sound",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isSoundEnabled,
                    onCheckedChange = { 
                        isSoundEnabled = it
                        settingsManager.isSoundEnabled = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSecondary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            
            // Ringtone Picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick {
                        if (isSoundEnabled) {
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                
                                settingsManager.customRingtoneUri?.let { uriString ->
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(uriString))
                                }
                            }
                            ringtonePickerLauncher.launch(intent)
                        }
                    }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Ringtone",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSoundEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        ringtoneName,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSoundEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            
            // Vibrate Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Vibrate",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isVibrateEnabled,
                    onCheckedChange = { 
                        isVibrateEnabled = it
                        settingsManager.isVibrateEnabled = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSecondary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
    
    if (showProxyWarning) {
        AlertDialog(
            onDismissRequest = { showProxyWarning = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Enable Proxy Notifications?", color = MaterialTheme.colorScheme.onPrimary) },
            text = { 
                Text("This is Level 2 configuration.\n\nWARNING: Do NOT use this if simple muting (Level 1) works perfectly for you. Enabling this will bypass native WhatsApp sounds for allowed users in tracked groups and requires you to mute the group natively in WhatsApp.\n\nAre you sure you want to enable this?", color = MaterialTheme.colorScheme.onSecondary) 
            },
            confirmButton = {
                TextButton(onClick = {
                    isProxyEnabled = true
                    settingsManager.isProxyNotificationEnabled = true
                    showProxyWarning = false
                }) {
                    Text("Enable", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProxyWarning = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSecondary)
                }
            }
        )
    }
}
