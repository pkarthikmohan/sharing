package com.aegis.shield.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.aegis.shield.ui.theme.*

private data class ScreenItem(
    val route: String,
    val label: String,
    val icon: String,
    val color: Color,
    val desc: String,
)

private val SCREENS = listOf(
    ScreenItem("home",        "Home",        "🏠", AccentBlue,    "Protection status & feed"),
    ScreenItem("alert/0",     "SMS Alert",   "⚠️", DangerRed,     "Smishing detection demo"),
    ScreenItem("voice_alert", "Voice Alert", "🎙️", WarningOrange, "Deepfake voice detection"),
    ScreenItem("safe_word",   "Safe-Word",   "🔐", SuccessGreen,  "TOTP identity challenge"),
    ScreenItem("sandbox",     "URL Sandbox", "🔍", PurpleAccent,  "Scam page detonator"),
    ScreenItem("dashboard",   "Dashboard",   "📊", AccentBlue,    "Analytics & history"),
    ScreenItem("settings",    "Settings",    "⚙️", TextSecondary, "Configure protection"),
)

@Composable
fun AegisNavMenu(currentRoute: String, navController: NavController) {
    var open by remember { mutableStateOf(false) }

    // Hamburger button
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(BorderBlue)
            .border(1.dp, BorderBlueMid, RoundedCornerShape(10.dp))
            .clickable { open = true },
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(3) { i ->
                Box(
                    Modifier
                        .width(if (i == 1) 13.dp else 18.dp)
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TextSecondary)
                )
            }
        }
    }

    if (open) {
        Dialog(onDismissRequest = { open = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xF0050C1C))
                    .padding(bottom = 16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Aegis", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BackgroundDark)
                            .clickable { open = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("×", color = TextSecondary, fontSize = 20.sp)
                    }
                }

                // 2-column grid
                val chunked = SCREENS.chunked(2)
                chunked.forEach { row ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { screen ->
                            val isCurrent = currentRoute.startsWith(screen.route.substringBefore("/"))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isCurrent) screen.color.copy(alpha = 0.13f) else BackgroundDark)
                                    .border(
                                        1.dp,
                                        if (isCurrent) screen.color else BorderBlue,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        open = false
                                        if (!isCurrent) navController.navigate(screen.route)
                                    }
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(screen.icon, fontSize = 26.sp)
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        screen.label,
                                        color = if (isCurrent) screen.color else Color.White,
                                        fontSize = 13.sp, fontWeight = FontWeight.Bold
                                    )
                                    Text(screen.desc, color = TextMuted, fontSize = 10.sp, lineHeight = 14.sp)
                                }
                            }
                        }
                        // Fill empty slot if odd
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
