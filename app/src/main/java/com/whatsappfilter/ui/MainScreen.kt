package com.whatsappfilter.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.whatsappfilter.data.SettingsManager
import com.whatsappfilter.data.WhatsAppFilterDao

@Composable
fun MainScreen(
    dao: WhatsAppFilterDao,
    settingsManager: SettingsManager,
    isNotificationServiceEnabled: Boolean,
    onRequestPermission: () -> Unit,
    onNavigateToGroup: (String) -> Unit,
    onExitRequest: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    BackHandler {
        if (selectedTab != 0) {
            selectedTab = 0
        } else {
            onExitRequest()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            CustomBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Crossfade(
            targetState = selectedTab,
            animationSpec = tween(durationMillis = 350),
            modifier = Modifier.padding(paddingValues)
        ) { tab ->
            when (tab) {
                0 -> GroupsTab(dao, isNotificationServiceEnabled, onRequestPermission, onNavigateToGroup)
                1 -> GlobalMuteTab(dao)
                2 -> SettingsTab(settingsManager)
            }
        }
    }
}

@Composable
fun CustomBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = Icons.Default.List,
            label = "Groups",
            isSelected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        BottomNavItem(
            icon = Icons.Default.Notifications,
            label = "Global",
            isSelected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
        BottomNavItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            isSelected = selectedTab == 2,
            onClick = { onTabSelected(2) }
        )
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSecondary,
        animationSpec = tween(300)
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSecondary,
        animationSpec = tween(300)
    )
    val yOffset by animateDpAsState(
        targetValue = if (isSelected) (-4).dp else 0.dp,
        animationSpec = tween(300)
    )
    val indicatorWidth by animateDpAsState(
        targetValue = if (isSelected) 16.dp else 0.dp,
        animationSpec = tween(300)
    )
    
    // Remove ripple effect for an understated feel
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
            .offset(y = yOffset),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(indicatorWidth)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
        )
    }
}
