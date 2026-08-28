package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.WebProject
import com.example.data.repository.WebProjectRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URI

enum class AppTab(val label: String) {
    DASHBOARD("Projects"),
    CONFIGURATOR("Config"),
    EMULATOR("Live Preview"),
    CODE_EXPORT("Code & Build"),
    AI_STUDIO_TOOLS("Tools")
}

enum class DevicePreviewMode(val label: String, val widthDp: Int, val heightDp: Int) {
    PHONE("Mobile", 390, 780),
    TABLET("Tablet", 680, 880),
    DESKTOP("Desktop", 900, 600)
}

data class WebAnalysisResult(
    val url: String = "",
    val isHttps: Boolean = false,
    val hasValidDomain: Boolean = false,
    val isAiStudioUrl: Boolean = false,
    val isCloudRunUrl: Boolean = false,
    val suggestedTitle: String = "",
    val suggestedPackageName: String = "",
    val securityScore: Int = 0,
    val recommendations: List<String> = emptyList()
)

data class EmulatorState(
    val currentUrl: String = "https://aistudio.google.com",
    val title: String = "",
    val progress: Int = 0,
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val lastError: String? = null,
    val previewMode: DevicePreviewMode = DevicePreviewMode.PHONE,
    val isFullscreen: Boolean = false,
    val injectedScriptsCount: Int = 0
)

class WebToApkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WebProjectRepository
    
    val allProjects: StateFlow<List<WebProject>>
    
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _editingProject = MutableStateFlow(createDefaultProject())
    val editingProject: StateFlow<WebProject> = _editingProject.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _emulatorState = MutableStateFlow(EmulatorState())
    val emulatorState: StateFlow<EmulatorState> = _emulatorState.asStateFlow()

    private val _analysisResult = MutableStateFlow<WebAnalysisResult?>(null)
    val analysisResult: StateFlow<WebAnalysisResult?> = _analysisResult.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = WebProjectRepository(db.webProjectDao())
        
        allProjects = repository.allProjects
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        viewModelScope.launch {
            repository.seedInitialPresetsIfEmpty()
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun selectProject(project: WebProject) {
        _editingProject.value = project
        _emulatorState.value = _emulatorState.value.copy(currentUrl = project.url)
        analyzeUrl(project.url)
    }

    fun updateEditingProject(updater: (WebProject) -> WebProject) {
        _editingProject.value = updater(_editingProject.value)
    }

    fun saveCurrentProject() {
        viewModelScope.launch {
            val current = _editingProject.value
            val id = repository.saveProject(current)
            _editingProject.value = current.copy(id = if (current.id == 0L) id else current.id)
            _snackbarMessage.emit("App package configuration saved successfully!")
        }
    }

    fun deleteProject(project: WebProject) {
        viewModelScope.launch {
            repository.deleteProject(project)
            if (_editingProject.value.id == project.id) {
                _editingProject.value = createDefaultProject()
            }
            _snackbarMessage.emit("Project '${project.title}' deleted")
        }
    }

    fun createNewProject(initialUrl: String = "https://aistudio.google.com") {
        val project = createDefaultProject(initialUrl)
        _editingProject.value = project
        _emulatorState.value = _emulatorState.value.copy(currentUrl = initialUrl)
        _currentTab.value = AppTab.CONFIGURATOR
        analyzeUrl(initialUrl)
    }

    fun quickConvertUrl(urlInput: String) {
        val cleanUrl = if (!urlInput.startsWith("http://") && !urlInput.startsWith("https://")) {
            "https://$urlInput"
        } else urlInput

        val (title, pkg) = deriveTitleAndPackage(cleanUrl)
        val project = WebProject(
            title = title,
            url = cleanUrl,
            packageName = pkg,
            category = if (cleanUrl.contains("aistudio") || cleanUrl.contains("run.app")) "AI Studio" else "Custom",
            splashScreenTitle = title
        )
        _editingProject.value = project
        _emulatorState.value = _emulatorState.value.copy(currentUrl = cleanUrl)
        analyzeUrl(cleanUrl)
        _currentTab.value = AppTab.CONFIGURATOR
    }

    fun updateEmulatorProgress(progress: Int, isLoading: Boolean, title: String? = null) {
        _emulatorState.value = _emulatorState.value.copy(
            progress = progress,
            isLoading = isLoading,
            title = title ?: _emulatorState.value.title
        )
    }

    fun setEmulatorPreviewMode(mode: DevicePreviewMode) {
        _emulatorState.value = _emulatorState.value.copy(previewMode = mode)
    }

    fun toggleEmulatorFullscreen() {
        _emulatorState.value = _emulatorState.value.copy(isFullscreen = !_emulatorState.value.isFullscreen)
    }

    fun setEmulatorUrl(newUrl: String) {
        _emulatorState.value = _emulatorState.value.copy(currentUrl = newUrl)
    }

    fun analyzeUrl(url: String) {
        val isHttps = url.startsWith("https://", ignoreCase = true)
        val isAiStudio = url.contains("aistudio.google.com", ignoreCase = true)
        val isCloudRun = url.contains("run.app", ignoreCase = true)
        val hasValidDomain = try {
            val host = URI(url).host
            host != null && host.contains(".")
        } catch (e: Exception) {
            false
        }

        val (sTitle, sPkg) = deriveTitleAndPackage(url)
        var score = 70
        val recs = mutableListOf<String>()

        if (isHttps) {
            score += 15
            recs.add("✅ SSL/TLS Encryption active (HTTPS)")
        } else {
            score -= 20
            recs.add("⚠️ HTTP cleartext traffic requires networkSecurityConfig or usesCleartextTraffic")
        }

        if (isAiStudio || isCloudRun) {
            score += 15
            recs.add("✨ Verified Google AI Studio deployment URL")
            recs.add("⚡ Recommend DomStorage & JavaScript enabled for dynamic Gemini views")
        } else {
            recs.add("💡 Check if responsive meta viewport tag is present for mobile")
        }

        recs.add("📱 Standard APK Manifest and WebView clients generated")

        _analysisResult.value = WebAnalysisResult(
            url = url,
            isHttps = isHttps,
            hasValidDomain = hasValidDomain,
            isAiStudioUrl = isAiStudio,
            isCloudRunUrl = isCloudRun,
            suggestedTitle = sTitle,
            suggestedPackageName = sPkg,
            securityScore = score.coerceIn(0, 100),
            recommendations = recs
        )
    }

    private fun deriveTitleAndPackage(url: String): Pair<String, String> {
        return try {
            val uri = URI(url)
            val host = uri.host ?: "webapp"
            val parts = host.replace("www.", "").split(".")
            val name = parts.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "WebApp"
            val title = if (url.contains("aistudio")) "AI Studio App" else if (url.contains("gemini")) "Gemini Web" else "$name App"
            val pkg = if (parts.size >= 2) "com.${parts[1]}.${parts[0].replace("-", "")}" else "com.aistudio.$name"
            Pair(title, pkg.lowercase())
        } catch (e: Exception) {
            Pair("Web App", "com.aistudio.webapp")
        }
    }

    private fun createDefaultProject(url: String = "https://aistudio.google.com"): WebProject {
        val (title, pkg) = deriveTitleAndPackage(url)
        return WebProject(
            title = title,
            url = url,
            packageName = pkg,
            versionName = "1.0.0",
            versionCode = 1,
            themeColorHex = "#1E1B4B",
            orientation = "PORTRAIT",
            isJavaScriptEnabled = true,
            isDomStorageEnabled = true,
            isPullToRefreshEnabled = true,
            isZoomEnabled = false,
            userAgentType = "DEFAULT",
            permInternet = true,
            permCamera = false,
            permLocation = false,
            permStorage = false,
            splashScreenTitle = title,
            category = "AI Studio"
        )
    }
}
