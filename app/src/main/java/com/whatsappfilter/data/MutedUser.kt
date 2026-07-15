package com.whatsappfilter.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "muted_users",
    indices = [Index(value = ["userName", "groupName"], unique = true)]
)
data class MutedUser(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userName: String,
    val groupName: String? // If null, the user is globally muted
)
