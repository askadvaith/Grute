package com.whatsappfilter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WhatsAppFilterDao {

    // Tracked Groups
    @Query("SELECT * FROM tracked_groups ORDER BY groupName ASC")
    fun getAllTrackedGroups(): Flow<List<TrackedGroup>>

    @Query("SELECT * FROM tracked_groups WHERE groupName = :groupName")
    suspend fun getTrackedGroup(groupName: String): TrackedGroup?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackedGroupIgnore(group: TrackedGroup)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackedGroup(group: TrackedGroup)

    @Query("DELETE FROM tracked_groups WHERE groupName = :groupName")
    suspend fun deleteTrackedGroup(groupName: String)


    // Muted Users
    @Query("SELECT * FROM muted_users WHERE groupName = :groupName")
    fun getMutedUsersForGroup(groupName: String): Flow<List<MutedUser>>

    @Query("SELECT * FROM muted_users WHERE groupName = :groupName")
    suspend fun getMutedUsersForGroupSync(groupName: String): List<MutedUser>

    @Query("SELECT * FROM muted_users WHERE groupName IS NULL")
    fun getGloballyMutedUsers(): Flow<List<MutedUser>>

    @Query("SELECT EXISTS(SELECT 1 FROM muted_users WHERE userName = :userName COLLATE NOCASE AND (groupName = :groupName COLLATE NOCASE OR groupName IS NULL))")
    suspend fun isUserMuted(userName: String, groupName: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM muted_users WHERE userName = :userName COLLATE NOCASE AND groupName IS NULL)")
    suspend fun isUserGloballyMuted(userName: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMutedUser(user: MutedUser)

    @Query("DELETE FROM muted_users WHERE userName = :userName COLLATE NOCASE AND groupName = :groupName COLLATE NOCASE")
    suspend fun deleteMutedUserFromGroup(userName: String, groupName: String)

    @Query("DELETE FROM muted_users WHERE userName = :userName COLLATE NOCASE AND groupName IS NULL")
    suspend fun deleteMutedUserGlobally(userName: String)


    // Discovered Users
    @Query("SELECT * FROM discovered_users WHERE groupName = :groupName ORDER BY lastSeen DESC")
    fun getDiscoveredUsersForGroup(groupName: String): Flow<List<DiscoveredUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscoveredUser(user: DiscoveredUser)
}
