package com.whatsappfilter

import android.app.Notification
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.whatsappfilter.data.AppDatabase
import com.whatsappfilter.data.DiscoveredUser
import com.whatsappfilter.data.TrackedGroup
import com.whatsappfilter.data.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WhatsAppListenerService : NotificationListenerService() {

    private val processedMessages = object : java.util.LinkedHashMap<String, Boolean>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>?): Boolean {
            return size > 100
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: AppDatabase
    private lateinit var settingsManager: SettingsManager

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(applicationContext)
        settingsManager = SettingsManager(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.whatsapp") return

        val notification = sbn.notification
        // Ignore summary notifications
        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
            return
        }

        val extras = notification.extras ?: return

        // 1. Get Group Name
        // In MessagingStyle notifications, EXTRA_CONVERSATION_TITLE holds the Group Name.
        val groupNameRaw = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
        val groupName = groupNameRaw?.replace(Regex("""\s\(\d+\s+messages?\)"""), "")?.trim()
        if (groupName.isNullOrBlank()) {
            // Not a group notification, ignore.
            return
        }

        // 2. Get Sender Name and Message Info
        var senderName = extractSenderName(extras)?.trim()

        var text = ""
        var time = 0L
        val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
        if (messages != null && messages.isNotEmpty()) {
            val lastMessageBundle = messages.last() as? Bundle
            if (lastMessageBundle != null) {
                text = lastMessageBundle.getCharSequence("text")?.toString() ?: ""
                time = lastMessageBundle.getLong("time", 0L)
            }
        } else {
            text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            time = notification.`when`
        }

        // Handle "Mentioned all", "Mentioned you", "Replied to you" logic
        if (senderName == "Mentioned all" || senderName == "Mentioned you" || senderName == "Replied to you") {
            // The true sender name is prepended to the text like "Sender Name: actual message"
            if (text.contains(": ")) {
                val parts = text.split(": ", limit = 2)
                senderName = parts[0].trim()
                text = parts.getOrNull(1)?.trim() ?: ""
            }
        }

        if (senderName.isNullOrBlank()) {
            return
        }

        val messageSignature = "$groupName:$senderName:$time:$text"

        // Run db operations on background thread
        serviceScope.launch {
            // Auto-discover the group (initially disabled) and the sender
            database.dao.insertTrackedGroupIgnore(TrackedGroup(groupName, isTrackingEnabled = false))
            database.dao.insertDiscoveredUser(DiscoveredUser(senderName, groupName))

            // Check if there are any muted users for this group to determine if it is "tracked"
            val mutedUsers = database.dao.getMutedUsersForGroupSync(groupName)
            val isGroupTracked = mutedUsers.isNotEmpty()
            
            if (isGroupTracked) {
                val isMuted = database.dao.isUserMuted(senderName, groupName)
                if (isMuted) {
                    // Cancel the silent notification
                    cancelNotification(sbn.key)
                } else {
                    // Play sound and vibrate because the group is tracked but this sender is NOT muted
                    var shouldAlert = false
                    synchronized(processedMessages) {
                        if (!processedMessages.containsKey(messageSignature)) {
                            processedMessages[messageSignature] = true
                            shouldAlert = true
                        }
                    }
                    if (shouldAlert && settingsManager.isProxyNotificationEnabled) {
                        triggerAlert()
                    }
                }
            }
        }
    }

    private fun extractSenderName(extras: Bundle): String? {
        var senderName: String? = null

        // Try extracting from MessagingStyle messages (Android 9+)
        val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
        if (messages != null && messages.isNotEmpty()) {
            val lastMessageBundle = messages.last() as? Bundle
            if (lastMessageBundle != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val person = lastMessageBundle.getParcelable<android.app.Person>("sender_person")
                    senderName = person?.name?.toString()
                }
                if (senderName == null) {
                    senderName = lastMessageBundle.getCharSequence("sender")?.toString()
                }
            }
        }

        // Fallback: Parse the title (often formatted as "Group Name: Sender Name" or similar)
        if (senderName == null) {
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            if (title.contains(": ")) {
                val parts = title.split(": ", limit = 2)
                senderName = parts.getOrNull(1)?.trim()
            }
        }

        return senderName
    }

    private fun triggerAlert() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        // Play notification sound
        if (settingsManager.isSoundEnabled && audioManager?.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            try {
                val soundUri = settingsManager.customRingtoneUri?.let { android.net.Uri.parse(it) } 
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    
                val ringtone = RingtoneManager.getRingtone(applicationContext, soundUri)
                ringtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                ringtone?.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Vibrate for 300ms if not in silent mode
        if (settingsManager.isVibrateEnabled && audioManager?.ringerMode != AudioManager.RINGER_MODE_SILENT) {
            try {
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // Modern vibration API (Android 12+)
                        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                        vibratorManager?.defaultVibrator?.vibrate(
                            VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            vibrator.vibrate(300)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
