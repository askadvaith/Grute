package com.whatsappfilter.data

import androidx.room.Entity

@Entity(
    tableName = "discovered_users",
    primaryKeys = ["userName", "groupName"]
)
data class DiscoveredUser(
    val userName: String,
    val groupName: String,
    val lastSeen: Long = System.currentTimeMillis()
)
