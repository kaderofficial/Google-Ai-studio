package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "web_projects")
data class WebProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val packageName: String = "com.aistudio.webapp",
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val themeColorHex: String = "#1E1B4B",
    val orientation: String = "PORTRAIT", // PORTRAIT, LANDSCAPE, SENSOR, REVERSE_PORTRAIT
    val isJavaScriptEnabled: Boolean = true,
    val isDomStorageEnabled: Boolean = true,
    val isPullToRefreshEnabled: Boolean = true,
    val isZoomEnabled: Boolean = false,
    val isImmersiveFullscreen: Boolean = false,
    val userAgentType: String = "DEFAULT", // DEFAULT, MOBILE, DESKTOP, CUSTOM
    val customUserAgent: String = "",
    val customCss: String = "",
    val customJs: String = "",
    val cacheMode: String = "DEFAULT", // DEFAULT, OFFLINE_FIRST, CACHE_ELSE_NETWORK, NO_CACHE
    val permInternet: Boolean = true,
    val permCamera: Boolean = false,
    val permLocation: Boolean = false,
    val permStorage: Boolean = false,
    val permAudio: Boolean = false,
    val permNotifications: Boolean = false,
    val deepLinkScheme: String = "",
    val splashScreenTitle: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val isPreset: Boolean = false,
    val category: String = "AI Studio"
)
