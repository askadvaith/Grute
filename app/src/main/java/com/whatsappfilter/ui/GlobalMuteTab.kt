package com.whatsappfilter.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsappfilter.data.MutedUser
import com.whatsappfilter.data.WhatsAppFilterDao
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalMuteTab(dao: WhatsAppFilterDao) {
    val coroutineScope = rememberCoroutineScope()
    val globallyMutedUsers by dao.getGloballyMutedUsers().collectAsState(initial = emptyList())
    var newGlobalUser by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Global Mute",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Minimal Search/Input Field
        OutlinedTextField(
            value = newGlobalUser,
            onValueChange = { newGlobalUser = it },
            placeholder = { Text("Add Contact Name", color = MaterialTheme.colorScheme.onSecondary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (newGlobalUser.isNotBlank()) {
                        coroutineScope.launch {
                            dao.insertMutedUser(MutedUser(userName = newGlobalUser.trim(), groupName = null))
                            newGlobalUser = ""
                        }
                    }
                }
            ),
            trailingIcon = {
                IconButton(onClick = {
                    if (newGlobalUser.isNotBlank()) {
                        coroutineScope.launch {
                            dao.insertMutedUser(MutedUser(userName = newGlobalUser.trim(), groupName = null))
                            newGlobalUser = ""
                        }
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(8.dp)
        )

        if (globallyMutedUsers.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No globally muted users.",
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(globallyMutedUsers, key = { it.userName }) { user ->
                    AnimatedVisibility(
                        visible = true, // Key handles actual add/remove animation implicitly in newer Compose, but we will leave this structure for future-proofing
                        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
                        exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { it / 2 }
                    ) {
                        GlobalMuteRowItem(user = user, onDelete = {
                            coroutineScope.launch {
                                dao.deleteMutedUserGlobally(user.userName)
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun GlobalMuteRowItem(
    user: MutedUser,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.userName,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Global Rule",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Rule",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}
