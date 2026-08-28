package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.WebProject
import com.example.ui.theme.*
import com.example.ui.viewmodel.DevicePreviewMode

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebEmulatorView(
    project: WebProject,
    currentUrl: String,
    previewMode: DevicePreviewMode,
    isFullscreen: Boolean,
    onUrlChange: (String) -> Unit,
    onProgressChange: (Int, Boolean, String?) -> Unit,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var inputUrlText by remember(currentUrl) { mutableStateOf(currentUrl) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var pageTitle by remember { mutableStateOf("") }
    var showJsConsoleDialog by remember { mutableStateOf(false) }
    var jsCodeToInject by remember { mutableStateOf("document.body.style.backgroundColor = '#1E1B4B'; alert('Injected from WebToAPK!');") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Emulator Controls Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Slate900,
            tonalElevation = 4.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { webViewInstance?.goBack() },
                        enabled = canGoBack,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("emulator_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (canGoBack) Slate100 else Slate600
                        )
                    }

                    IconButton(
                        onClick = { webViewInstance?.goForward() },
                        enabled = canGoForward,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("emulator_forward_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Forward",
                            tint = if (canGoForward) Slate100 else Slate600
                        )
                    }

                    IconButton(
                        onClick = { webViewInstance?.reload() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("emulator_reload_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload",
                            tint = Slate100
                        )
                    }

                    // URL Address Box
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Slate800,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (inputUrlText.startsWith("https://")) Icons.Default.Lock else Icons.Default.Public,
                                contentDescription = "Security Status",
                                tint = if (inputUrlText.startsWith("https://")) EmeraldAccent else AmberAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicUrlInput(
                                value = inputUrlText,
                                onValueChange = { inputUrlText = it },
                                onGo = {
                                    val finalUrl = if (!inputUrlText.startsWith("http://") && !inputUrlText.startsWith("https://")) {
                                        "https://$inputUrlText"
                                    } else inputUrlText
                                    onUrlChange(finalUrl)
                                    webViewInstance?.loadUrl(finalUrl)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Quick Actions
                    IconButton(
                        onClick = { showJsConsoleDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("emulator_js_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Inject JS",
                            tint = CyanLight
                        )
                    }

                    IconButton(
                        onClick = onToggleFullscreen,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("emulator_fullscreen_button")
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Toggle Fullscreen",
                            tint = IndigoLight
                        )
                    }
                }

                // Loading progress indicator
                AnimatedVisibility(visible = isLoading) {
                    LinearProgressIndicator(
                        progress = { loadProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = CyanLight,
                        trackColor = Slate800
                    )
                }
            }
        }

        // Live Device Frame Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(if (isFullscreen) 0.dp else 12.dp),
            contentAlignment = Alignment.Center
        ) {
            val frameShape = if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(28.dp)
            val frameBorder = if (isFullscreen) 0.dp else 6.dp

            Surface(
                modifier = Modifier
                    .then(
                        if (isFullscreen) Modifier.fillMaxSize()
                        else when (previewMode) {
                            DevicePreviewMode.PHONE -> Modifier
                                .widthIn(max = 420.dp)
                                .fillMaxHeight()
                            DevicePreviewMode.TABLET -> Modifier
                                .widthIn(max = 680.dp)
                                .fillMaxHeight()
                            DevicePreviewMode.DESKTOP -> Modifier.fillMaxSize()
                        }
                    )
                    .shadow(if (isFullscreen) 0.dp else 16.dp, frameShape)
                    .clip(frameShape)
                    .border(frameBorder, if (isFullscreen) Color.Transparent else Slate800, frameShape),
                color = Slate900
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Simulated Android Device Notch & Status Bar (if phone frame and not fullscreen)
                    if (!isFullscreen && previewMode == DevicePreviewMode.PHONE) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate950)
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "9:41",
                                color = Slate300,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            // Camera Notch Dot
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Slate800)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Wifi, contentDescription = null, tint = Slate300, modifier = Modifier.size(12.dp))
                                Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = Slate300, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    // Native Android WebView embed
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                
                                settings.apply {
                                    javaScriptEnabled = project.isJavaScriptEnabled
                                    domStorageEnabled = project.isDomStorageEnabled
                                    databaseEnabled = true
                                    allowFileAccess = true
                                    allowContentAccess = true
                                    loadsImagesAutomatically = true
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    
                                    if (project.isZoomEnabled) {
                                        setSupportZoom(true)
                                        builtInZoomControls = true
                                        displayZoomControls = false
                                    } else {
                                        setSupportZoom(false)
                                    }

                                    when (project.userAgentType) {
                                        "MOBILE" -> userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                                        "DESKTOP" -> userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
                                        "CUSTOM" -> if (project.customUserAgent.isNotBlank()) userAgentString = project.customUserAgent
                                    }
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        loadProgress = newProgress
                                        isLoading = newProgress < 100
                                        onProgressChange(newProgress, newProgress < 100, view?.title)
                                    }

                                    override fun onReceivedTitle(view: WebView?, title: String?) {
                                        super.onReceivedTitle(view, title)
                                        pageTitle = title ?: ""
                                        onProgressChange(loadProgress, isLoading, title)
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        isLoading = true
                                        url?.let { inputUrlText = it }
                                        canGoBack = canGoBack()
                                        canGoForward = canGoForward()
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isLoading = false
                                        canGoBack = canGoBack()
                                        canGoForward = canGoForward()

                                        // Inject Custom CSS if specified
                                        if (project.customCss.isNotBlank()) {
                                            val escapedCss = project.customCss.replace("\"", "\\\"").replace("\n", " ")
                                            val cssScript = "javascript:(function() { " +
                                                "var node = document.createElement('style'); " +
                                                "node.type = 'text/css'; " +
                                                "node.innerHTML = '$escapedCss'; " +
                                                "document.head.appendChild(node); " +
                                                "})()"
                                            loadUrl(cssScript)
                                        }

                                        // Inject Custom JS if specified
                                        if (project.customJs.isNotBlank()) {
                                            evaluateJavascript(project.customJs, null)
                                        }
                                    }
                                }

                                loadUrl(currentUrl)
                                webViewInstance = this
                            }
                        },
                        update = { wv ->
                            if (wv.url != currentUrl && currentUrl.isNotBlank()) {
                                wv.loadUrl(currentUrl)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("live_webview_emulator")
                    )
                }
            }
        }

        // Bottom Info Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Slate900,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isLoading) AmberAccent else EmeraldAccent)
                    )
                    Text(
                        text = if (isLoading) "Loading ($loadProgress%)..." else if (pageTitle.isNotBlank()) pageTitle else "Ready",
                        color = Slate300,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 240.dp)
                    )
                }

                Text(
                    text = "${previewMode.label} View (${previewMode.widthDp}x${previewMode.heightDp})",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }

    // JavaScript Console Injection Dialog
    if (showJsConsoleDialog) {
        AlertDialog(
            onDismissRequest = { showJsConsoleDialog = false },
            title = {
                Text(
                    text = "Live JavaScript Console",
                    color = Slate100,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Inject and execute custom JavaScript in the live emulator context:",
                        color = Slate300,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = jsCodeToInject,
                        onValueChange = { jsCodeToInject = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("js_injection_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CodeBg,
                            unfocusedContainerColor = CodeBg,
                            focusedBorderColor = CyanLight,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        webViewInstance?.evaluateJavascript(jsCodeToInject, null)
                        showJsConsoleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier.testTag("execute_js_button")
                ) {
                    Text("Execute JS", color = Slate950, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJsConsoleDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }
}

@Composable
private fun BasicUrlInput(
    value: String,
    onValueChange: (String) -> Unit,
    onGo: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.testTag("emulator_url_input"),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            color = Slate100,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        ),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
            onDone = { onGo() }
        ),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = "Enter web URL...",
                        color = Slate600,
                        fontSize = 13.sp
                    )
                }
                innerTextField()
            }
        }
    )
}
