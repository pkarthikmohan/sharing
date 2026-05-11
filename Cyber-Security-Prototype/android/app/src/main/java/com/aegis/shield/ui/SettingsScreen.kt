package com.aegis.shield.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.aegis.shield.AegisViewModel
import com.aegis.shield.AegisSettings
import com.aegis.shield.ui.components.AegisNavMenu
import com.aegis.shield.ui.theme.*

@Composable
fun SettingsScreen(vm: AegisViewModel, navController: NavController) {
    val settings by vm.settings.collectAsState()

    Column(Modifier.fillMaxSize().background(BackgroundDeepNavy)) {
        // Header
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
            .border(BorderStroke(1.dp, BorderBlue), RoundedCornerShape(0.dp)),
            verticalAlignment = Alignment.CenterVertically) {
            Text("⚙️", fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Configure Aegis protection", color = TextSecondary, fontSize = 11.sp)
            }
            AegisNavMenu("settings", navController)
        }

        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 40.dp)) {

            SectionHeader("PROTECTION")
            Column(Modifier.padding(horizontal = 12.dp).clip(RoundedCornerShape(16.dp)).background(BackgroundDark)) {
                SettingRow("💬", "SMS Monitoring", "Intercept and analyse incoming messages",
                    settings.smsEnabled) { vm.updateSettings(settings.copy(smsEnabled = !settings.smsEnabled)) }
                SettingRow("📞", "Call Audio Analysis", "Real-time deepfake voice detection",
                    settings.callEnabled) { vm.updateSettings(settings.copy(callEnabled = !settings.callEnabled)) }
                SettingRow("📡", "Auto-Report Scams", "Anonymously share confirmed threats",
                    settings.autoReport) { vm.updateSettings(settings.copy(autoReport = !settings.autoReport)) }
                SettingRow("🪟", "Overlay Alerts", "Show full-screen alerts for CONFIRMED threats",
                    settings.overlayAlerts) { vm.updateSettings(settings.copy(overlayAlerts = !settings.overlayAlerts)) }
            }

            SectionHeader("THREAT SENSITIVITY")
            val sensLabels = listOf("Conservative", "Balanced", "Aggressive")
            val sensColors = listOf(SuccessGreen, AccentBlue, DangerRed)
            Column(Modifier.padding(horizontal = 12.dp).clip(RoundedCornerShape(16.dp)).background(BackgroundDark).padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    sensLabels.forEachIndexed { i, label ->
                        val sel = settings.sensitivity == i
                        Box(
                            Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                .background(if (sel) sensColors[i].copy(0.13f) else BackgroundDeepNavy)
                                .border(1.dp, if (sel) sensColors[i] else BorderBlue, RoundedCornerShape(10.dp))
                                .clickable { vm.updateSettings(settings.copy(sensitivity = i)) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) { Text(label, color = if (sel) sensColors[i] else TextMuted, fontSize = 11.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(when (settings.sensitivity) {
                    0 -> "Only flags high-confidence threats (score ≥ 80). Minimises false positives."
                    2 -> "Flags all suspicious activity (score ≥ 40). May generate more false positives."
                    else -> "Balanced detection threshold (score ≥ 60). Recommended for most users."
                }, color = TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
            }

            SectionHeader("MODEL")
            Column(Modifier.padding(horizontal = 12.dp).clip(RoundedCornerShape(16.dp)).background(BackgroundDark)) {
                listOf(
                    Triple("🤖", "Smishing Model", "v2.4.1"),
                    Triple("🎙️", "Deepfake Model",  "v1.8.0"),
                    Triple("📅", "Last Updated",    "07 May 2026"),
                ).forEach { (icon, label, value) ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp)
                        .border(BorderStroke(0.dp, Color.Transparent), RoundedCornerShape(0.dp)),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(icon, fontSize = 18.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(label, color = Color(0xFFCCCCCC), fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(value, color = AccentBlue, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                }
                Box(Modifier.fillMaxWidth().clickable { }.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text("🔄 Check for Model Updates", color = AccentBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            SectionHeader("DATA")
            var cleared by remember { mutableStateOf(false) }
            Box(Modifier.padding(horizontal = 12.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BackgroundDark)
                .clickable { cleared = true; vm.clearHistory() }.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(
                    if (cleared) "✓ Threat history cleared" else "🗑️  Clear Local Threat History",
                    color = if (cleared) SuccessGreen else DangerRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(24.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Aegis v1.0.0 · com.aegis.shield", color = TextDimmed, fontSize = 11.sp)
                Text("Privacy-first · On-device ML · Zero raw data transmitted", color = BorderBlue, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 8.dp))
}

@Composable
private fun SettingRow(icon: String, label: String, sub: String, on: Boolean, onChange: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
            .border(BorderStroke(0.dp, Color.Transparent), RoundedCornerShape(0.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(BackgroundDeepNavy), contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 18.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(sub, color = TextMuted, fontSize = 11.sp)
        }
        AegisToggle(on, onChange)
    }
}

@Composable
private fun AegisToggle(on: Boolean, onChange: () -> Unit) {
    Box(
        Modifier.width(48.dp).height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (on) AccentBlue else Color(0xFF1A2D4A))
            .border(1.dp, if (on) AccentBlue else Color(0xFF2A4C6B), RoundedCornerShape(13.dp))
            .clickable(onClick = onChange)
    ) {
        Box(
            Modifier.padding(3.dp).size(20.dp)
                .align(if (on) Alignment.CenterEnd else Alignment.CenterStart)
                .clip(CircleShape)
                .background(if (on) Color.White else Color(0xFF4A6A8A))
        )
    }
}
