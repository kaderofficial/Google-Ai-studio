package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WebProject
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.WebToApkViewModel

@Composable
fun ConfiguratorScreen(
    viewModel: WebToApkViewModel,
    project: WebProject,
    onNavigateToTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember(project) { mutableStateOf(project.title) }
    var url by remember(project) { mutableStateOf(project.url) }
    var packageName by remember(project) { mutableStateOf(project.packageName) }
    var versionName by remember(project) { mutableStateOf(project.versionName) }
    var versionCode by remember(project) { mutableIntStateOf(project.versionCode) }
    var themeColorHex by remember(project) { mutableStateOf(project.themeColorHex) }
    var orientation by remember(project) { mutableStateOf(project.orientation) }
    var isJsEnabled by remember(project) { mutableStateOf(project.isJavaScriptEnabled) }
    var isDomStorageEnabled by remember(project) { mutableStateOf(project.isDomStorageEnabled) }
    var isPullToRefreshEnabled by remember(project) { mutableStateOf(project.isPullToRefreshEnabled) }
    var isZoomEnabled by remember(project) { mutableStateOf(project.isZoomEnabled) }
    var isFullscreen by remember(project) { mutableStateOf(project.isImmersiveFullscreen) }
    var userAgentType by remember(project) { mutableStateOf(project.userAgentType) }
    var customUserAgent by remember(project) { mutableStateOf(project.customUserAgent) }
    var customCss by remember(project) { mutableStateOf(project.customCss) }
    var customJs by remember(project) { mutableStateOf(project.customJs) }
    var cacheMode by remember(project) { mutableStateOf(project.cacheMode) }
    var permInternet by remember(project) { mutableStateOf(project.permInternet) }
    var permCamera by remember(project) { mutableStateOf(project.permCamera) }
    var permLocation by remember(project) { mutableStateOf(project.permLocation) }
    var permStorage by remember(project) { mutableStateOf(project.permStorage) }
    var permAudio by remember(project) { mutableStateOf(project.permAudio) }
    var permNotifications by remember(project) { mutableStateOf(project.permNotifications) }
    var deepLinkScheme by remember(project) { mutableStateOf(project.deepLinkScheme) }
    var splashTitle by remember(project) { mutableStateOf(project.splashScreenTitle) }
    var category by remember(project) { mutableStateOf(project.category) }

    fun syncToViewModel() {
        viewModel.updateEditingProject {
            it.copy(
                title = title,
                url = url,
                packageName = packageName,
                versionName = versionName,
                versionCode = versionCode,
                themeColorHex = themeColorHex,
                orientation = orientation,
                isJavaScriptEnabled = isJsEnabled,
                isDomStorageEnabled = isDomStorageEnabled,
                isPullToRefreshEnabled = isPullToRefreshEnabled,
                isZoomEnabled = isZoomEnabled,
                isImmersiveFullscreen = isFullscreen,
                userAgentType = userAgentType,
                customUserAgent = customUserAgent,
                customCss = customCss,
                customJs = customJs,
                cacheMode = cacheMode,
                permInternet = permInternet,
                permCamera = permCamera,
                permLocation = permLocation,
                permStorage = permStorage,
                permAudio = permAudio,
                permNotifications = permNotifications,
                deepLinkScheme = deepLinkScheme,
                splashScreenTitle = splashTitle,
                category = category
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "APK Package Manifest Studio",
                            color = Slate100,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configure native Android wrapper parameters",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 1. General App Metadata
            item {
                ConfigCard(
                    title = "App Identity & Target URL",
                    subtitle = "Application label, website endpoint, and package identifier",
                    icon = Icons.Default.AppRegistration,
                    iconColor = CyanLight
                ) {
                    ConfigTextField(
                        label = "App Display Name",
                        value = title,
                        onValueChange = {
                            title = it
                            syncToViewModel()
                        },
                        placeholder = "e.g. Google AI Studio Companion",
                        leadingIcon = Icons.Default.Title,
                        modifier = Modifier.testTag("config_title_input")
                    )

                    ConfigTextField(
                        label = "Target Website URL",
                        value = url,
                        onValueChange = {
                            url = it
                            syncToViewModel()
                            viewModel.analyzeUrl(it)
                        },
                        placeholder = "https://aistudio.google.com",
                        leadingIcon = Icons.Default.Language,
                        isMonospace = true,
                        modifier = Modifier.testTag("config_url_input")
                    )

                    ConfigTextField(
                        label = "Application Package ID",
                        value = packageName,
                        onValueChange = {
                            packageName = it
                            syncToViewModel()
                        },
                        placeholder = "com.company.appname",
                        leadingIcon = Icons.Default.Fingerprint,
                        isMonospace = true,
                        modifier = Modifier.testTag("config_package_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ConfigTextField(
                            label = "Version Name",
                            value = versionName,
                            onValueChange = {
                                versionName = it
                                syncToViewModel()
                            },
                            placeholder = "1.0.0",
                            modifier = Modifier.weight(1f),
                            isMonospace = true
                        )
                        ConfigTextField(
                            label = "Version Code",
                            value = versionCode.toString(),
                            onValueChange = {
                                versionCode = it.toIntOrNull() ?: versionCode
                                syncToViewModel()
                            },
                            placeholder = "1",
                            modifier = Modifier.weight(1f),
                            isMonospace = true
                        )
                    }
                }
            }

            // 2. Display & Windowing
            item {
                ConfigCard(
                    title = "Display & Screen Layout",
                    subtitle = "Orientation, full-screen immersive mode, and splash screen",
                    icon = Icons.Default.Smartphone,
                    iconColor = IndigoLight
                ) {
                    Text("Screen Orientation", color = Slate300, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("PORTRAIT" to "Portrait", "LANDSCAPE" to "Landscape", "SENSOR" to "Sensor Auto").forEach { (key, label) ->
                            FilterChip(
                                selected = orientation == key,
                                onClick = {
                                    orientation = key
                                    syncToViewModel()
                                },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPrimary,
                                    selectedLabelColor = Slate100,
                                    containerColor = Slate850,
                                    labelColor = Slate400
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = orientation == key,
                                    borderColor = if (orientation == key) CyanLight else Slate700
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    ConfigToggleRow(
                        title = "Immersive Fullscreen",
                        description = "Hide system status bar and navigation bar for app-like immersion",
                        checked = isFullscreen,
                        onCheckedChange = {
                            isFullscreen = it
                            syncToViewModel()
                        },
                        icon = Icons.Default.Fullscreen
                    )

                    ConfigTextField(
                        label = "Splash Screen Title",
                        value = splashTitle,
                        onValueChange = {
                            splashTitle = it
                            syncToViewModel()
                        },
                        placeholder = "e.g. AI Studio",
                        leadingIcon = Icons.Default.FlashOn
                    )
                }
            }

            // 3. WebView Engine Settings
            item {
                ConfigCard(
                    title = "WebView Engine & Gestures",
                    subtitle = "V8 JavaScript, LocalStorage, zoom, and cache strategy",
                    icon = Icons.Default.Memory,
                    iconColor = EmeraldAccent
                ) {
                    ConfigToggleRow(
                        title = "JavaScript Execution (V8)",
                        description = "Required for React, Vue, Svelte, and dynamic web apps",
                        checked = isJsEnabled,
                        onCheckedChange = {
                            isJsEnabled = it
                            syncToViewModel()
                        },
                        icon = Icons.Default.Code
                    )

                    ConfigToggleRow(
                        title = "DOM / LocalStorage Persistence",
                        description = "Persist user login sessions, offline tokens, and indexedDB",
                        checked = isDomStorageEnabled,
                        onCheckedChange = {
                            isDomStorageEnabled = it
                            syncToViewModel()
                        },
                        icon = Icons.Default.Storage
                    )

                    ConfigToggleRow(
                        title = "Pull-to-Refresh Gesture",
                        description = "Allow users to swipe down from the top to reload the page",
                        checked = isPullToRefreshEnabled,
                        onCheckedChange = {
                            isPullToRefreshEnabled = it
                            syncToViewModel()
                        },
                        icon = Icons.Default.Refresh
                    )

                    ConfigToggleRow(
                        title = "Pinch-to-Zoom Controls",
                        description = "Support multi-touch viewport scaling on web pages",
                        checked = isZoomEnabled,
                        onCheckedChange = {
                            isZoomEnabled = it
                            syncToViewModel()
                        },
                        icon = Icons.Default.ZoomIn
                    )

                    Text("Cache Strategy", color = Slate300, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("DEFAULT" to "Standard", "OFFLINE_FIRST" to "Offline First", "NO_CACHE" to "No Cache").forEach { (key, label) ->
                            FilterChip(
                                selected = cacheMode == key,
                                onClick = {
                                    cacheMode = key
                                    syncToViewModel()
                                },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPrimary,
                                    selectedLabelColor = Slate100,
                                    containerColor = Slate850,
                                    labelColor = Slate400
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Text("User-Agent Mode", color = Slate300, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("DEFAULT" to "Default", "MOBILE" to "Mobile", "DESKTOP" to "Desktop").forEach { (key, label) ->
                            FilterChip(
                                selected = userAgentType == key,
                                onClick = {
                                    userAgentType = key
                                    syncToViewModel()
                                },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPrimary,
                                    selectedLabelColor = Slate100,
                                    containerColor = Slate850,
                                    labelColor = Slate400
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. Permissions Manager
            item {
                ConfigCard(
                    title = "Android Manifest Permissions",
                    subtitle = "Enable hardware & OS capabilities in AndroidManifest.xml",
                    icon = Icons.Default.Security,
                    iconColor = AmberAccent
                ) {
                    ConfigToggleRow(
                        title = "INTERNET Access",
                        description = "Mandatory for fetching web pages and API requests",
                        checked = permInternet,
                        onCheckedChange = {
                            permInternet = it
                            syncToViewModel()
                        },
                        icon = Icons.Default.Wifi
                    )

                    ConfigToggleRow(
                        title = "Camera Permission",
                        description = "For web camera video calls, QR scanning, and photo upload",
                        checked = permCamera,
                        onCheckedChange = {
                            permCamera = it
                            syncToViewModel()
                        },
                        icon = Icons.Default.PhotoCamera
                    )

                    ConfigToggleRow(
                        title = "Geolocation (GPS)",
                        description = "HTML5 navigator.geolocation support",
                        checked = permLocation,
                        onCheckedChange = {
                            permLocation = it
                            syncToViewModel()
                        },
                        icon = Icons.Default.LocationOn
                    )

                    ConfigToggleRow(
                        title = "Audio Recording",
                        description = "For voice prompts, Gemini audio input, and speech recognition",
                        checked = permAudio,
                        onCheckedChange = {
                            permAudio = it
                            syncToViewModel()
                        },
                        icon = Icons.Default.Mic
                    )

                    ConfigToggleRow(
                        title = "Push Notifications",
                        description = "Web push & system notification permissions",
                        checked = permNotifications,
                        onCheckedChange = {
                            permNotifications = it
                            syncToViewModel()
                        },
                        icon = Icons.Default.Notifications
                    )
                }
            }

            // 5. Custom Code Injection
            item {
                ConfigCard(
                    title = "Custom Script & Style Injection",
                    subtitle = "Inject CSS or JavaScript into the web page on page load",
                    icon = Icons.Default.IntegrationInstructions,
                    iconColor = CyanLight
                ) {
                    Text("Injected CSS (Dark Theme / UI override):", color = Slate300, fontSize = 13.sp)
                    OutlinedTextField(
                        value = customCss,
                        onValueChange = {
                            customCss = it
                            syncToViewModel()
                        },
                        placeholder = { Text("/* e.g. body { filter: invert(0.9); } */", color = Slate600, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CodeBg,
                            unfocusedContainerColor = CodeBg,
                            focusedBorderColor = CyanLight,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    )

                    Text("Injected JavaScript (Bridge / Actions):", color = Slate300, fontSize = 13.sp)
                    OutlinedTextField(
                        value = customJs,
                        onValueChange = {
                            customJs = it
                            syncToViewModel()
                        },
                        placeholder = { Text("// e.g. console.log('WebToAPK initialized');", color = Slate600, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CodeBg,
                            unfocusedContainerColor = CodeBg,
                            focusedBorderColor = CyanLight,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    )
                }
            }
        }

        // Bottom Sticky Action Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Slate900.copy(alpha = 0.95f),
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        syncToViewModel()
                        viewModel.saveCurrentProject()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_config_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = IndigoDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save App Config", color = IndigoDark, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = {
                        syncToViewModel()
                        viewModel.setEmulatorUrl(url)
                        onNavigateToTab(AppTab.EMULATOR)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Slate800,
                        contentColor = IndigoLight
                    ),
                    modifier = Modifier.testTag("preview_config_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preview", fontWeight = FontWeight.SemiBold)
                }

                FilledTonalButton(
                    onClick = {
                        syncToViewModel()
                        onNavigateToTab(AppTab.CODE_EXPORT)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Slate800,
                        contentColor = EmeraldAccent
                    ),
                    modifier = Modifier.testTag("export_config_button")
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
