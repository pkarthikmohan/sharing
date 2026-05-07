package com.aegis.shield.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.aegis.shield.AegisViewModel
import com.aegis.shield.ui.components.AegisNavMenu
import com.aegis.shield.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SandboxScreen(vm: AegisViewModel, navController: NavController) {
    var progress by remember { mutableStateOf(0) }
    var loading  by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (progress < 100) {
            delay(250)
            progress = (progress + 8).coerceAtMost(100)
        }
        loading = false
    }

    Column(Modifier.fillMaxSize().background(BackgroundDeepNavy)) {
        // Header
        Row(
            Modifier.fillMaxWidth().background(BackgroundHeader)
                .border(BorderStroke(1.dp, BorderBlue), RoundedCornerShape(0.dp))
                .padding(start = 20.dp, end = 16.dp, top = 46.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔍", fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("URL Sandbox", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Detonating in isolated container", color = TextSecondary, fontSize = 11.sp)
            }
            AegisNavMenu("sandbox", navController)
        }

        // URL display
        Column(
            Modifier.padding(16.dp).fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)).background(BackgroundDark).padding(14.dp)
        ) {
            Text("SUSPICIOUS URL", color = TextSecondary, fontSize = 10.sp)
            Spacer(Modifier.height(4.dp))
            Text("http://echallan-paytm.xyz/pay?ref=MH02XX1234",
                color = Color(0xFFFF6B6B), fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        }

        if (loading) {
            // Loading state
            Column(
                Modifier.fillMaxSize().padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🕵️", fontSize = 32.sp)
                Spacer(Modifier.height(20.dp))
                Text("Analyzing scam page…", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Running in isolated container. No data is submitted.", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(20.dp))
                Box(Modifier.width(200.dp).height(6.dp).clip(RoundedCornerShape(3.dp)).background(BackgroundDark)) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(progress / 100f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(listOf(BorderBlue, AccentBlue))))
                }
                Spacer(Modifier.height(8.dp))
                Text("$progress% · ${when {
                    progress < 30 -> "Navigating to URL…"
                    progress < 60 -> "Capturing screenshot…"
                    progress < 85 -> "Detecting payment elements…"
                    else -> "Cross-referencing PhishTank…"
                }}", color = TextSecondary, fontSize = 11.sp)
            }
        } else {
            // Results
            Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("CONFIRMED SCAM", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp,
                        modifier = Modifier.clip(RoundedCornerShape(30.dp)).background(DangerRed).padding(horizontal = 18.dp, vertical = 8.dp))
                    Text("e-challan phishing", color = TextSecondary, fontSize = 12.sp)
                }

                // Fake scam page preview
                Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)).border(4.dp, DangerRed, RoundedCornerShape(12.dp))) {
                    FakeScamPagePreview()
                }

                Spacer(Modifier.height(16.dp))

                // Threat intelligence
                Column(Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)).background(BackgroundDark).padding(14.dp)) {
                    Text("THREAT INTELLIGENCE", color = TextSecondary, fontSize = 11.sp, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("PhishTank", "VirusTotal").forEach { src ->
                            Column(Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2E0D0D)).border(1.dp, DangerRed.copy(0.27f), RoundedCornerShape(10.dp))
                                .padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⚠️ FLAGGED", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(src, color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Detected elements
                Text("DETECTED ELEMENTS", color = TextSecondary, fontSize = 11.sp, letterSpacing = 0.5.sp, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(10.dp))
                listOf("📱" to "UPI QR Code", "🏦" to "Fake Bank Form", "🏛️" to "Govt Logo Misuse", "💳" to "Payment Gateway").forEach { (icon, label) ->
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 3.dp).fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)).background(Color(0xFF2E0D0D))
                            .border(1.dp, DangerRed.copy(0.27f), RoundedCornerShape(10.dp)).padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(icon, fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(label, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text("Detected", color = DangerRed, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Analysis
                Column(Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)).background(BackgroundDark).padding(14.dp)) {
                    Text("ANALYSIS", color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("This page impersonates the Ministry of Road Transport using stolen government logos. It contains a fake UPI QR code to steal money. The .xyz domain has no government affiliation.",
                        color = Color(0xFFCCCCCC), fontSize = 12.sp, lineHeight = 18.sp)
                }

                Spacer(Modifier.height(16.dp))
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DangerRed).padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                        Text("📨 Report to CERT-In", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(BorderBlue)
                        .border(1.dp, BorderBlueMid, RoundedCornerShape(14.dp)).padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                        Text("⚠️ Share Warning with Contacts", color = AccentBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FakeScamPagePreview() {
    Column(Modifier.background(Color.White).padding(12.dp)) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            .background(Color(0xFF003366)).padding(8.dp)) {
            Column {
                Text("echallan-paytm.xyz", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("🔒 NOT VERIFIED", color = Color(0xFFAACCFF), fontSize = 9.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("Ministry of Road Transport", color = Color(0xFF003366), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Government of India (FAKE)", color = Color(0xFF666666), fontSize = 10.sp)
            Text("🏛️", fontSize = 20.sp)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Color(0xFFFFF3CD))
            .border(1.dp, Color(0xFFFFC107), RoundedCornerShape(4.dp)).padding(8.dp)) {
            Column {
                Text("⚠️ OUTSTANDING CHALLAN NOTICE", color = Color(0xFF856404), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Vehicle: MH02XX1234 | Fine: ₹1,500", color = Color(0xFF333333), fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Pay immediately to avoid legal action and suspension of driving licence.", fontSize = 10.sp, lineHeight = 14.sp, color = Color(0xFF333333))
        Spacer(Modifier.height(8.dp))
        // Watermark
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.clip(RoundedCornerShape(4.dp)).border(3.dp, DangerRed, RoundedCornerShape(4.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("KNOWN SCAM\nDO NOT PAY\n— Aegis —", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            }
        }
    }
}
