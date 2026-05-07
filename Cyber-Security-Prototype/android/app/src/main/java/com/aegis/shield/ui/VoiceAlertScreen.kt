package com.aegis.shield.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.aegis.shield.AegisViewModel
import com.aegis.shield.ui.components.AegisNavMenu
import com.aegis.shield.ui.theme.*

@Composable
fun VoiceAlertScreen(vm: AegisViewModel, navController: NavController, threatId: Long) {
    val threat = remember(threatId) { vm.getThreat(threatId) }
    val prob   = threat?.score ?: 82
    var challengeSent by remember { mutableStateOf(false) }
    var elapsed by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            elapsed++
        }
    }
    val mins = String.format("%02d", elapsed / 60)
    val secs = String.format("%02d", elapsed % 60)

    // Ripple animation
    val ripple = rememberInfiniteTransition(label = "ripple")
    val rippleScale by ripple.animateFloat(1f, 2f, infiniteRepeatable(tween(2000), RepeatMode.Restart), label = "s")
    val rippleAlpha by ripple.animateFloat(0.8f, 0f, infiniteRepeatable(tween(2000), RepeatMode.Restart), label = "a")

    // Waveform bars animation
    val wave = rememberInfiniteTransition(label = "wave")
    val wavePhase by wave.animateFloat(0f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Restart), label = "w")

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))))
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Status bar
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("9:41", color = TextSecondary, fontSize = 12.sp)
                Text("⚠ CALL ANALYSIS ACTIVE", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                AegisNavMenu("voice_alert", navController)
            }

            // Call info
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Incoming call", color = TextSecondary, fontSize = 14.sp)
                Text(threat?.sender ?: "+91 98765 43210", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Unknown · $mins:$secs", color = TextSecondary, fontSize = 13.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Waveform
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    val heights = listOf(12, 20, 32, 24, 40, 28, 36, 18, 44, 30, 38, 22, 34, 16, 26)
                    heights.forEachIndexed { i, h ->
                        val animH = (h * (0.4f + 0.8f * kotlin.math.sin(wavePhase * 2 * Math.PI + i * 0.4).toFloat().coerceIn(-1f, 1f).let { (it + 1f) / 2f })).toInt()
                        Box(Modifier.width(4.dp).height(animH.dp).clip(RoundedCornerShape(2.dp)).background(DangerRed))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Alert card
            Column(
                Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF3D0000), Color(0xFF1A0000))))
                    .border(2.dp, DangerRed, RoundedCornerShape(20.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pulsing shield
                Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(120.dp)) {
                        drawCircle(DangerRed.copy(rippleAlpha * 0.6f), radius = size.minDimension / 2 * rippleScale, style = Stroke(2.dp.toPx()))
                    }
                    Box(
                        Modifier.size(64.dp).clip(CircleShape).background(DangerRed),
                        contentAlignment = Alignment.Center
                    ) { Text("🛡️", fontSize = 30.sp) }
                }

                Spacer(Modifier.height(16.dp))
                Text("AI VOICE DETECTED", color = Color(0xFFFFAAAA), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
                Text("$prob%", color = DangerRed, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold)
                Text("probability this voice is AI-generated", color = Color(0xFFFFAAAA), fontSize = 13.sp)

                Spacer(Modifier.height(16.dp))

                // Artifacts
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0A0000)).padding(12.dp)
                ) {
                    Text("DETECTED ARTIFACTS", color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.height(8.dp))
                    listOf("Spectral Flatness" to 88, "MFCC Anomaly" to 76, "Pitch Regularity" to 84).forEach { (label, v) ->
                        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, color = Color(0xFFCCCCCC), fontSize = 11.sp)
                            Text("$v%", color = DangerRed, fontSize = 11.sp)
                        }
                        Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF222222))) {
                            Box(Modifier.fillMaxHeight().fillMaxWidth(v / 100f).clip(RoundedCornerShape(2.dp)).background(DangerRed))
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "Voice analysis indicates this may be an AI-cloned voice. The caller may not be who they claim.",
                    color = Color(0xFFFFAAAA), fontSize = 12.sp, lineHeight = 18.sp
                )

                Spacer(Modifier.height(16.dp))

                // Buttons
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(if (challengeSent) SuccessGreen else BorderBlue)
                        .border(1.dp, if (challengeSent) SuccessGreen else BorderBlueMid, RoundedCornerShape(14.dp))
                        .clickable { challengeSent = true; navController.navigate("safe_word") }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (challengeSent) "✓ CHALLENGE SENT" else "🔐 SEND SAFE-WORD CHALLENGE",
                        color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(DangerRed).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Text("📵 END CALL", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1A1A1A)).border(1.dp, Color(0xFF333333), RoundedCornerShape(14.dp))
                            .clickable { navController.popBackStack() }.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("DISMISS", color = Color(0xFF888888), fontSize = 13.sp) }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Aegis is analyzing audio in real-time…", color = TextMuted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(32.dp))
        }
    }
}
