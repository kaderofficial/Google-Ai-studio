package com.example.util

import com.example.data.model.WebProject

object CodeGenerator {

    fun generateManifest(project: WebProject): String {
        val permissions = buildList {
            if (project.permInternet) add("    <uses-permission android:name=\"android.permission.INTERNET\" />")
            add("    <uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />")
            if (project.permCamera) add("    <uses-permission android:name=\"android.permission.CAMERA\" />")
            if (project.permLocation) {
                add("    <uses-permission android:name=\"android.permission.ACCESS_FINE_LOCATION\" />")
                add("    <uses-permission android:name=\"android.permission.ACCESS_COARSE_LOCATION\" />")
            }
            if (project.permStorage) {
                add("    <uses-permission android:name=\"android.permission.READ_EXTERNAL_STORAGE\" android:maxSdkVersion=\"32\" />")
                add("    <uses-permission android:name=\"android.permission.READ_MEDIA_IMAGES\" />")
            }
            if (project.permAudio) add("    <uses-permission android:name=\"android.permission.RECORD_AUDIO\" />")
            if (project.permNotifications) add("    <uses-permission android:name=\"android.permission.POST_NOTIFICATIONS\" />")
        }.joinToString("\n")

        val orientationAttr = when (project.orientation) {
            "LANDSCAPE" -> "android:screenOrientation=\"landscape\""
            "SENSOR" -> "android:screenOrientation=\"fullSensor\""
            "REVERSE_PORTRAIT" -> "android:screenOrientation=\"reversePortrait\""
            else -> "android:screenOrientation=\"portrait\""
        }

        val deepLinkBlock = if (project.deepLinkScheme.isNotBlank()) {
            """
            <!-- Deep Linking Intent Filter -->
            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="${project.deepLinkScheme.substringBefore("://").ifBlank { "https" }}" />
            </intent-filter>
            """.trimIndent().prependIndent("            ")
        } else ""

        return """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="${project.packageName}">

$permissions

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="${project.title}"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:usesCleartextTraffic="true"
        android:hardwareAccelerated="true"
        android:theme="@style/Theme.WebToApk">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            $orientationAttr
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:windowSoftInputMode="adjustResize"
            android:label="${project.title}">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
$deepLinkBlock
        </activity>

    </application>
</manifest>
        """.trimIndent()
    }

    fun generateMainActivity(project: WebProject): String {
        val userAgentConfig = when (project.userAgentType) {
            "MOBILE" -> "settings.userAgentString = \"Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36\""
            "DESKTOP" -> "settings.userAgentString = \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36\""
            "CUSTOM" -> if (project.customUserAgent.isNotBlank()) "settings.userAgentString = \"${project.customUserAgent}\"" else "// Default user agent"
            else -> "// Default User-Agent will be used"
        }

        val cacheModeConfig = when (project.cacheMode) {
            "OFFLINE_FIRST" -> "settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK"
            "CACHE_ELSE_NETWORK" -> "settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK"
            "NO_CACHE" -> "settings.cacheMode = WebSettings.LOAD_NO_CACHE"
            else -> "settings.cacheMode = WebSettings.LOAD_DEFAULT"
        }

        val zoomConfig = if (project.isZoomEnabled) {
            """
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            """.trimIndent()
        } else {
            "settings.setSupportZoom(false)"
        }

        val customCssInjection = if (project.customCss.isNotBlank()) {
            val escapedCss = project.customCss.replace("\"", "\\\"").replace("\n", " ")
            """
            // Injected Custom CSS
            val cssScript = "javascript:(function() { " +
                "var node = document.createElement('style'); " +
                "node.type = 'text/css'; " +
                "node.innerHTML = '$escapedCss'; " +
                "document.head.appendChild(node); " +
                "})()"
            webView.loadUrl(cssScript)
            """.trimIndent()
        } else ""

        val customJsInjection = if (project.customJs.isNotBlank()) {
            val escapedJs = project.customJs.replace("\"", "\\\"").replace("\n", " ")
            """
            // Injected Custom JavaScript
            webView.evaluateJavascript("$escapedJs", null)
            """.trimIndent()
        } else ""

        return """
package ${project.packageName}

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private val targetUrl = "${project.url}"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ${if (project.isImmersiveFullscreen) {
            "window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)"
        } else {
            "// Normal status bar layout"
        }}

        // Initialize Native WebView Components
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)

        setupWebViewSettings()
        setupClients()
        setupBackNavigation()
        setupPullToRefresh()

        if (savedInstanceState == null) {
            loadInitialUrl()
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebViewSettings() {
        val settings = webView.settings
        settings.javaScriptEnabled = ${project.isJavaScriptEnabled}
        settings.domStorageEnabled = ${project.isDomStorageEnabled}
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.loadsImagesAutomatically = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        
        $cacheModeConfig
        $zoomConfig
        $userAgentConfig
    }

    private fun setupClients() {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                if (newProgress >= 100) {
                    progressBar.visibility = View.GONE
                    swipeRefresh.isRefreshing = false
                } else {
                    progressBar.visibility = View.VISIBLE
                }
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                callback?.invoke(origin, true, false)
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false
                
                $customCssInjection
                $customJsInjection
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true && !isNetworkAvailable()) {
                    webView.loadUrl("file:///android_asset/offline_fallback.html")
                }
            }
        }
    }

    private fun setupPullToRefresh() {
        swipeRefresh.isEnabled = ${project.isPullToRefreshEnabled}
        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun loadInitialUrl() {
        if (isNetworkAvailable()) {
            webView.loadUrl(targetUrl)
        } else {
            webView.loadUrl("file:///android_asset/offline_fallback.html")
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }
}
        """.trimIndent()
    }

    fun generateBuildGradle(project: WebProject): String {
        return """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "${project.packageName}"
    compileSdk = 35

    defaultConfig {
        applicationId = "${project.packageName}"
        minSdk = 24
        targetSdk = 35
        versionCode = ${project.versionCode}
        versionName = "${project.versionName}"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug") // or release config
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.webkit:webkit:1.12.0")
}
        """.trimIndent()
    }

    fun generateOfflineHtml(project: WebProject): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${project.title} - Offline</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background: linear-gradient(135deg, #0F172A 0%, #1E1B4B 100%);
            color: #F8FAFC;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            padding: 24px;
            text-align: center;
        }
        .card {
            background: rgba(30, 41, 59, 0.85);
            backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 24px;
            padding: 40px 24px;
            max-width: 420px;
            width: 100%;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
        }
        .icon {
            width: 64px;
            height: 64px;
            margin: 0 auto 20px;
            background: linear-gradient(135deg, #6366F1, #38BDF8);
            border-radius: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 30px;
        }
        h1 { font-size: 22px; font-weight: 700; margin-bottom: 8px; }
        p { color: #94A3B8; font-size: 14px; line-height: 1.5; margin-bottom: 24px; }
        .btn {
            display: inline-block;
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, #4F46E5, #06B6D4);
            color: #FFFFFF;
            font-weight: 600;
            font-size: 15px;
            border-radius: 12px;
            border: none;
            cursor: pointer;
            text-decoration: none;
            transition: opacity 0.2s;
        }
        .btn:active { opacity: 0.8; }
    </style>
</head>
<body>
    <div class="card">
        <div class="icon">⚡</div>
        <h1>${project.title}</h1>
        <p>No Internet connection detected. Please connect to Wi-Fi or mobile data to access this application.</p>
        <button class="btn" onclick="window.location.reload()">Retry Connection</button>
    </div>
</body>
</html>
        """.trimIndent()
    }

    fun generateBuildGuide(project: WebProject): String {
        return """
# 🚀 Build & Packaging Guide for ${project.title}

### 1. In Google AI Studio Build:
1. Open the project settings panel.
2. Click **Download APK** or **Export Project (ZIP)** to download the pre-compiled Android package.
3. For signing, navigate to Secrets panel and ensure your environment variables are configured.

### 2. Fast Command Line Build (Gradle):
```bash
# Clone or extract project repository
cd ${project.title.lowercase().replace(" ", "-")}

# Assemble Debug APK (ready to install on Android device)
gradle assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk

# Install directly on connected Android device:
gradle installDebug

# Build Signed Release APK:
gradle assembleRelease
```

### 3. Generated Package Details:
- **Application Name**: ${project.title}
- **Package Identifier**: `${project.packageName}`
- **Target URL**: `${project.url}`
- **Version**: ${project.versionName} (Code: ${project.versionCode})
- **Orientation**: ${project.orientation}
- **Pull To Refresh**: ${if (project.isPullToRefreshEnabled) "Enabled" else "Disabled"}
- **JavaScript Engine**: ${if (project.isJavaScriptEnabled) "Enabled (V8 DOM)" else "Disabled"}
        """.trimIndent()
    }

    fun generateBuildScript(project: WebProject): String {
        val slug = project.title.lowercase().replace(Regex("[^a-z0-9]"), "-").trim('-').ifBlank { "webapp" }
        return """
#!/usr/bin/env bash
# ==============================================================================
# Automated Build Script for ${project.title} (v${project.versionName})
# Generated by WebToAPK Builder
# ==============================================================================

set -euo pipefail

echo "========================================================"
echo "⚡ Building Android Package for ${project.title}"
echo "📦 Package Name: ${project.packageName}"
echo "🌐 Target URL:   ${project.url}"
echo "========================================================"

BUILD_TYPE="${'$'}{1:-debug}"

# Check for Gradle installation
if command -v gradle &> /dev/null; then
    GRADLE_CMD="gradle"
elif [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    GRADLE_CMD="./gradlew"
else
    echo "❌ Error: Gradle not found in PATH and ./gradlew wrapper is missing."
    exit 1
fi

echo "🚀 Starting compilation (Mode: ${'$'}BUILD_TYPE)..."

if [ "${'$'}BUILD_TYPE" = "release" ]; then
    ${'$'}GRADLE_CMD :app:assembleRelease
    OUTPUT_PATH="app/build/outputs/apk/release/app-release-unsigned.apk"
    echo ""
    echo "✅ Release APK generated successfully!"
    echo "📁 Path: ${'$'}OUTPUT_PATH"
else
    ${'$'}GRADLE_CMD :app:assembleDebug
    OUTPUT_PATH="app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "✅ Debug APK generated successfully!"
    echo "📁 Path: ${'$'}OUTPUT_PATH"
    echo ""
    echo "💡 To install immediately onto an attached device or emulator, run:"
    echo "   ${'$'}GRADLE_CMD :app:installDebug"
fi

echo "========================================================"
echo "✨ Build completed!"
echo "========================================================"
        """.trimIndent()
    }

    fun generateSettingsGradle(project: WebProject): String {
        return """
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "${project.title.replace("\"", "\\\"")}"
include(":app")
        """.trimIndent()
    }

    fun generateRootBuildGradle(): String {
        return """
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
}
        """.trimIndent()
    }

    fun generateGradleProperties(): String {
        return """
# Gradle Build Properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
        """.trimIndent()
    }

    fun generateProguardRules(): String {
        return """
# ProGuard & R8 Configuration for WebToAPK WebView App
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep native WebView classes
-keep class android.webkit.** { *; }
-keepclassmembers class * extends android.webkit.WebChromeClient { *; }
-keepclassmembers class * extends android.webkit.WebViewClient { *; }

# Keep data models
-keepclassmembers class * {
    *** get*();
    *** set*(***);
    *** is*();
}
        """.trimIndent()
    }

    fun generateActivityLayout(project: WebProject): String {
        return """
<?xml version="1.0" encoding="utf-8"?>
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/swipeRefresh"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <WebView
            android:id="@+id/webView"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />

        <ProgressBar
            android:id="@+id/progressBar"
            style="?android:attr/progressBarStyleHorizontal"
            android:layout_width="match_parent"
            android:layout_height="4dp"
            android:layout_gravity="top"
            android:max="100"
            android:progress="0"
            android:visibility="gone" />

    </FrameLayout>

</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
        """.trimIndent()
    }

    fun generateReadme(project: WebProject): String {
        return """
# ${project.title} - Android Application

Packaged from web application: [${project.url}](${project.url})

## 📱 Project Specifications
- **Package ID**: `${project.packageName}`
- **Version**: `${project.versionName}` (Build `${project.versionCode}`)
- **Orientation**: `${project.orientation}`
- **JavaScript Execution**: ${if (project.isJavaScriptEnabled) "Enabled" else "Disabled"}
- **Local DOM Storage**: ${if (project.isDomStorageEnabled) "Enabled" else "Disabled"}
- **Pull-to-Refresh**: ${if (project.isPullToRefreshEnabled) "Enabled" else "Disabled"}

## 🚀 Quick Start
Run the included build script:
```bash
chmod +x build.sh
./build.sh debug
```

Or assemble with Gradle:
```bash
gradle assembleDebug
```
        """.trimIndent()
    }

    fun generateProjectJson(project: WebProject): String {
        return """
{
  "title": "${project.title}",
  "url": "${project.url}",
  "packageName": "${project.packageName}",
  "versionName": "${project.versionName}",
  "versionCode": ${project.versionCode},
  "themeColor": "${project.themeColorHex}",
  "orientation": "${project.orientation}",
  "settings": {
    "javaScript": ${project.isJavaScriptEnabled},
    "domStorage": ${project.isDomStorageEnabled},
    "pullToRefresh": ${project.isPullToRefreshEnabled},
    "zoom": ${project.isZoomEnabled},
    "fullscreen": ${project.isImmersiveFullscreen},
    "userAgent": "${project.userAgentType}",
    "cacheMode": "${project.cacheMode}"
  },
  "permissions": {
    "internet": ${project.permInternet},
    "camera": ${project.permCamera},
    "location": ${project.permLocation},
    "storage": ${project.permStorage},
    "audio": ${project.permAudio},
    "notifications": ${project.permNotifications}
  }
}
        """.trimIndent()
    }
}
