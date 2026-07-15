package com.whatsappfilter.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("whatsapp_filter_prefs", Context.MODE_PRIVATE)

    var isProxyNotificationEnabled: Boolean
        get() = prefs.getBoolean(KEY_PROXY_ENABLED, false) // Default to false
        set(value) = prefs.edit().putBoolean(KEY_PROXY_ENABLED, value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var isVibrateEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATE_ENABLED, value).apply()

    var customRingtoneUri: String?
        get() = prefs.getString(KEY_CUSTOM_RINGTONE_URI, null)
        set(value) = prefs.edit().putString(KEY_CUSTOM_RINGTONE_URI, value).apply()
        
    var customRingtoneName: String?
        get() = prefs.getString(KEY_CUSTOM_RINGTONE_NAME, null)
        set(value) = prefs.edit().putString(KEY_CUSTOM_RINGTONE_NAME, value).apply()

    companion object {
        private const val KEY_PROXY_ENABLED = "proxy_enabled"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATE_ENABLED = "vibrate_enabled"
        private const val KEY_CUSTOM_RINGTONE_URI = "custom_ringtone_uri"
        private const val KEY_CUSTOM_RINGTONE_NAME = "custom_ringtone_name"
    }
}
