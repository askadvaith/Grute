package com.whatsappfilter.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.whatsappfilter.data.DiscoveredUser
import com.whatsappfilter.data.MutedUser
import com.whatsappfilter.data.WhatsAppFilterDao
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupName: String,
    dao: WhatsAppFilterDao,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val participants by dao.getDiscoveredUsersForGroup(groupName).collectAsState(initial = emptyList())
    val mutedUsersInGroup by dao.getMutedUsersForGroup(groupName).collectAsState(initial = emptyList())
    val globallyMutedUsers by dao.getGloballyMutedUsers().collectAsState(initial = emptyList())

    val mutedUserNames = remember(mutedUsersInGroup) {
        mutedUsersInGroup.map { it.userName }.toSet()
    }
    val globallyMutedUserNames = remember(globallyMutedUsers) {
        globallyMutedUsers.map { it.userName }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 8.dp, end = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = groupName,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    text = "${participants.size} Participants",
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // List area
        if (participants.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Waiting for messages in this group...",
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(participants, key = { it.userName }) { participant ->
                    val isGloballyMuted = globallyMutedUserNames.contains(participant.userName)
                    val isLocallyMuted = mutedUserNames.contains(participant.userName)

                    ParticipantRow(
                        participant = participant,
                        isMuted = isGloballyMuted || isLocallyMuted,
                        isGloballyMuted = isGloballyMuted,
                        onToggleMute = { shouldMute ->
                            coroutineScope.launch {
                                if (shouldMute) {
                                    dao.insertMutedUser(
                                        MutedUser(
                                            userName = participant.userName,
                                            groupName = groupName
                                        )
                                    )
                                } else {
                                    dao.deleteMutedUserFromGroup(participant.userName, groupName)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ParticipantRow(
    participant: DiscoveredUser,
    isMuted: Boolean,
    isGloballyMuted: Boolean,
    onToggleMute: (Boolean) -> Unit
) {
    // Smooth transitions for colors
    val avatarBgColor by animateColorAsState(
        targetValue = if (isMuted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300)
    )
    
    val avatarTextColor by animateColorAsState(
        targetValue = if (isMuted) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary,
        animationSpec = tween(300)
    )

    val textColor by animateColorAsState(
        targetValue = if (isMuted) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary,
        animationSpec = tween(300)
    )

    val indicatorColor by animateColorAsState(
        targetValue = if (isMuted) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(300)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isGloballyMuted) { onToggleMute(!isMuted) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(avatarBgColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = participant.userName.take(1).uppercase(),
                    color = avatarTextColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = participant.userName,
                fontWeight = FontWeight.Medium,
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            
            // Crimson mute indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(indicatorColor, shape = CircleShape)
            )
            
            Spacer(modifier = Modifier.width(16.dp))

            Switch(
                checked = isMuted,
                onCheckedChange = onToggleMute,
                enabled = !isGloballyMuted,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSecondary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.scale(0.8f) // Make switch slightly smaller
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}
