package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.WebProject
import com.example.util.CodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WebToAPK", appName)
  }

  @Test
  fun `test code generator produces manifest with package name`() {
    val project = WebProject(
      title = "AI Studio Companion",
      url = "https://aistudio.google.com",
      packageName = "com.google.aistudio.test"
    )
    val manifest = CodeGenerator.generateManifest(project)
    assertTrue(manifest.contains("com.google.aistudio.test"))
    assertTrue(manifest.contains("android.permission.INTERNET"))
  }
}
