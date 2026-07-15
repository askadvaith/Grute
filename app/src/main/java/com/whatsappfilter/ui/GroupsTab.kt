package com.whatsappfilter.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsappfilter.data.TrackedGroup
import com.whatsappfilter.data.WhatsAppFilterDao
import kotlinx.coroutines.launch

@Composable
fun GroupsTab(
    dao: WhatsAppFilterDao,
    isNotificationServiceEnabled: Boolean,
    onRequestPermission: () -> Unit,
    onNavigateToGroup: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val trackedGroups by dao.getAllTrackedGroups().collectAsState(initial = emptyList())
    var groupToDelete by remember { mutableStateOf<TrackedGroup?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Permission Warning Banner
        if (!isNotificationServiceEnabled) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Permission Required",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "Notification access is needed to intercept messages.",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Enable")
                    }
                }
            }
        }

        Text(
            text = "Groups",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        if (trackedGroups.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No groups detected.",
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(trackedGroups, key = { it.groupName }) { group ->
                    AnimatedVisibility(
                        visible = true,
                        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                    ) {
                        GroupRowItem(
                            group = group,
                            dao = dao,
                            onClick = { onNavigateToGroup(group.groupName) },
                            onDeleteRequest = { groupToDelete = group }
                        )
                    }
                }
            }
        }
    }

    if (groupToDelete != null) {
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Remove Group", color = MaterialTheme.colorScheme.onPrimary) },
            text = { Text("Are you sure you want to stop tracking '${groupToDelete?.groupName}'? This will not delete any messages in WhatsApp.", color = MaterialTheme.colorScheme.onSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    groupToDelete?.let {
                        coroutineScope.launch {
                            dao.deleteTrackedGroup(it.groupName)
                            groupToDelete = null
                        }
                    }
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSecondary)
                }
            }
        )
    }
}

@Composable
fun GroupRowItem(
    group: TrackedGroup,
    dao: WhatsAppFilterDao,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val mutedUsers by dao.getMutedUsersForGroup(group.groupName).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.groupName,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = "${mutedUsers.size} muted",
                        color = if (mutedUsers.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            IconButton(onClick = onDeleteRequest) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Details",
                tint = MaterialTheme.colorScheme.onSecondary
            )
        }
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}
