package com.example.data.repository

import com.example.data.dao.WebProjectDao
import com.example.data.model.WebProject
import kotlinx.coroutines.flow.Flow

class WebProjectRepository(private val dao: WebProjectDao) {

    val allProjects: Flow<List<WebProject>> = dao.getAllProjects()

    fun getProject(id: Long): Flow<WebProject?> = dao.getProjectById(id)

    suspend fun saveProject(project: WebProject): Long {
        return dao.insertProject(project.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateProject(project: WebProject) {
        dao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProject(project: WebProject) {
        dao.deleteProject(project)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun seedInitialPresetsIfEmpty() {
        if (dao.getProjectCount() == 0) {
            val presets = listOf(
                WebProject(
                    title = "AI Studio Workspace",
                    url = "https://aistudio.google.com",
                    packageName = "com.google.aistudio.mobile",
                    versionName = "1.0.0",
                    themeColorHex = "#1E293B",
                    orientation = "PORTRAIT",
                    isJavaScriptEnabled = true,
                    isDomStorageEnabled = true,
                    isPullToRefreshEnabled = true,
                    isZoomEnabled = true,
                    userAgentType = "DEFAULT",
                    permInternet = true,
                    permStorage = true,
                    splashScreenTitle = "Google AI Studio",
                    category = "AI Studio",
                    isPreset = true
                ),
                WebProject(
                    title = "Gemini Web Experience",
                    url = "https://gemini.google.com",
                    packageName = "com.google.gemini.webapp",
                    versionName = "1.2.0",
                    themeColorHex = "#0B0F19",
                    orientation = "PORTRAIT",
                    isJavaScriptEnabled = true,
                    isDomStorageEnabled = true,
                    isPullToRefreshEnabled = true,
                    permInternet = true,
                    permCamera = true,
                    permAudio = true,
                    splashScreenTitle = "Gemini AI",
                    category = "AI Chat",
                    isPreset = true
                ),
                WebProject(
                    title = "AI Studio Dev Instance",
                    url = "https://ais-dev-a6u6v4jtp7zke6ibj56hki-616750276336.asia-southeast1.run.app",
                    packageName = "com.aistudio.devrunner",
                    versionName = "0.1.0",
                    themeColorHex = "#312E81",
                    orientation = "SENSOR",
                    isJavaScriptEnabled = true,
                    isDomStorageEnabled = true,
                    isPullToRefreshEnabled = true,
                    isZoomEnabled = true,
                    permInternet = true,
                    splashScreenTitle = "AI Studio Live Preview",
                    category = "Cloud Run Dev",
                    isPreset = true
                ),
                WebProject(
                    title = "Modern Web Application",
                    url = "https://developer.android.com",
                    packageName = "com.android.developer.hub",
                    versionName = "2.0.0",
                    themeColorHex = "#064E3B",
                    orientation = "PORTRAIT",
                    isJavaScriptEnabled = true,
                    isDomStorageEnabled = true,
                    isPullToRefreshEnabled = true,
                    permInternet = true,
                    splashScreenTitle = "Android Dev Docs",
                    category = "Documentation",
                    isPreset = true
                )
            )
            for (p in presets) {
                dao.insertProject(p)
            }
        }
    }
}
