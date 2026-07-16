package com.example.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// NOTE: This application uses a custom AppColors class for styling custom elements.
// However, we wire MaterialTheme's colorScheme here to sync with the same settings
// retrieved from SharedPreferences. This ensures that any standard Material3 components 
// (such as default Dialogs, Menus, or standard Buttons) will seamlessly inherit the 
// correct colors automatically.

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val prefs = remember { context.getSharedPreferences("milo_settings", Context.MODE_PRIVATE) }
  
  var themeSetting by remember { 
      mutableStateOf(prefs.getString("theme", "Follow System") ?: "Follow System") 
  }
  
  DisposableEffect(prefs) {
      val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
          if (key == "theme") {
              themeSetting = prefs.getString("theme", "Follow System") ?: "Follow System"
          }
      }
      prefs.registerOnSharedPreferenceChangeListener(listener)
      onDispose {
          prefs.unregisterOnSharedPreferenceChangeListener(listener)
      }
  }

  val isSystemDark = isSystemInDarkTheme()
  val isDark = when (themeSetting) {
      "Pure black (AMOLED)", "Dark gray", "Charcoal / Medium gray", "Midnight Blue", "Forest Green", "Sunset Orange", "Rose Gold" -> true
      "Soft Light Gray", "Pure white" -> false
      "Follow System" -> isSystemDark
      else -> isSystemDark
  }

  val bg = when (themeSetting) {
      "Pure black (AMOLED)" -> Color(0xFF000000)
      "Dark gray" -> Color(0xFF0A0A0A)
      "Charcoal / Medium gray" -> Color(0xFF1C1C1E)
      "Midnight Blue" -> Color(0xFF0B1220)
      "Forest Green" -> Color(0xFF0D1A13)
      "Sunset Orange" -> Color(0xFF1F1310)
      "Rose Gold" -> Color(0xFF1A0E14)
      "Soft Light Gray" -> Color(0xFFEDEDED)
      "Pure white" -> Color(0xFFFFFFFF)
      "Follow System" -> if (isSystemDark) Color(0xFF0A0A0A) else Color(0xFFEDEDED)
      else -> if (isSystemDark) Color(0xFF0A0A0A) else Color(0xFFEDEDED)
  }

  val primary = when (themeSetting) {
      "Midnight Blue" -> Color(0xFF4A9EFF)
      "Forest Green" -> Color(0xFF3DDC84)
      "Sunset Orange" -> Color(0xFFFF9142)
      "Rose Gold" -> Color(0xFFFF5C8A)
      "Pure black (AMOLED)" -> Color(0xFFFFFFFF)
      else -> if (isDark) Color(0xFFEDEDED) else Color(0xFF111111)
  }

  val colorScheme = if (isDark) {
      darkColorScheme(
          primary = primary,
          background = bg,
          surface = bg,
          onBackground = Color(0xFFEDEDED),
          onSurface = Color(0xFFEDEDED)
      )
  } else {
      lightColorScheme(
          primary = primary,
          background = bg,
          surface = bg,
          onBackground = Color(0xFF111111),
          onSurface = Color(0xFF111111)
      )
  }

  MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
  )
}
