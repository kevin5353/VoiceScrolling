package com.aaron.voicescrolling.ui.main


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aaron.voicescrolling.ui.theme.VoiceScrollTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            VoiceScrollTheme {
                com.aaron.voicescrolling.ui.main.MainScreen { openAccessibilitySettings() }
            }
        }
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }
}

@Composable
fun MainScreen(onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("scroll_prefs", Context.MODE_PRIVATE) }

    // Load saved scroll rate (default 1)
    var scrollRate by remember { mutableFloatStateOf(sharedPref.getFloat("rate", 1f)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF000000))))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Icon(
            Icons.Default.Mic,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(64.dp)
        )

        Text(
            "VoiceScroll",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text("Background Voice Control", color = Color.LightGray)

        Spacer(modifier = Modifier.height(48.dp))

        // Scroll Rate Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Scrolling Intensity", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = scrollRate,
                    onValueChange = {
                        scrollRate = it
                        sharedPref.edit().putFloat("rate", it).apply()
                    },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF50E3C2),
                        activeTrackColor = Color(0xFF50E3C2)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Gentle", color = Color.Gray, fontSize = 12.sp)
                    Text("Aggressive", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enable Accessibility Service", fontWeight = FontWeight.Bold)
        }
    }
}