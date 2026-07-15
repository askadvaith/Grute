package com.whatsappfilter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_groups")
data class TrackedGroup(
    @PrimaryKey val groupName: String,
    val isTrackingEnabled: Boolean = true
)
