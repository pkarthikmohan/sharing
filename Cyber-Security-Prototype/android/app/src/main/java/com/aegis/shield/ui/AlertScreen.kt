package com.aegis.shield.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.aegis.shield.AegisViewModel
import com.aegis.shield.data.ThreatBand
import com.aegis.shield.ui.components.AegisNavMenu
import com.aegis.shield.ui.theme.*
import kotlin.math.*

private val THREAT_VECTORS = listOf(
    Triple("Urgency",          "⚡", 98),
    Triple("Financial Demand", "💰", 92),
    Triple("Authority Spoof",  "🏛️", 95),
    Triple("Link Lure",        "🔗", 88),
    Triple("Impersonation",    "👤", 91),
)

@Composable
fun AlertScreen(vm: AegisViewModel, navController: NavController, threatId: Long) {
    val threat = remember(threatId) { vm.getThreat(threatId) }
    val score  = threat?.score ?: 94
    val sender = threat?.sender ?: "VM-TRAI"
    val body   = threat?.body ?: "Dear customer, your vehicle MH02XX1234 has an unpaid e-challan of Rs.1,500. Pay immediately at http://echallan-paytm.xyz/pay to avoid legal action. - Govt of India"
    val band   = runCatching { ThreatBand.valueOf(threat?.band ?: "CONFIRMED") }.getOrDefault(ThreatBand.CONFIRMED)

    var blocked  by remember { mutableStateOf(threat?.blocked ?: false) }
    var reported by remember { mutableStateOf(threat?.reported ?: false) }

    // Animated gauge
    val animScore by animateIntAsState(targetValue = score, animationSpec = tween(1200), label = "score")

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .verticalScroll(rememberScrollState())
    ) {
        // Red alert header
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFFC0392B), Color(0xFF8B0000))))
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚠️", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("SCAM DETECTED", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Text("Intercepted before delivery", color = Color(0xFFFFAAAA), fontSize = 11.sp)
                }
                AegisNavMenu("alert", navController)
            }
        }

        // Radial gauge
        Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
            RadialGauge(animScore)
        }

        // Band badge
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                "CONFIRMED SCAM",
                color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp,
                modifier = Modifier.clip(RoundedCornerShape(30.dp)).background(DangerRed).padding(horizontal = 24.dp, vertical = 6.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // SMS preview
        Column(
            Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)).background(Color(0xFF1A1A1A))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp)).padding(14.dp)
        ) {
            Text("INTERCEPTED MESSAGE · $sender", color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(body, color = Color(0xFFCCCCCC), fontSize = 12.sp, lineHeight = 18.sp)
        }

        Spacer(Modifier.height(16.dp))

        // Threat vectors
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("THREAT VECTORS", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(10.dp))
            THREAT_VECTORS.forEach { (name, icon, score) ->
                Column(Modifier.padding(bottom = 8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$icon $name", color = Color(0xFFCCCCCC), fontSize = 12.sp)
                        Text("$score%", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF222222))) {
                        Box(
                            Modifier.fillMaxHeight().fillMaxWidth(score / 100f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Brush.horizontalGradient(listOf(DangerRed, Color(0xFFE74C3C))))
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Explanation
        Column(
            Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)).background(Color(0xFF1A1A1A)).padding(14.dp)
        ) {
            Text("WHY THIS IS SUSPICIOUS", color = TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "This message impersonates a government authority, creates false urgency around a fake fine, and links to a suspicious .xyz domain unrelated to any official system.",
                color = Color(0xFFCCCCCC), fontSize = 12.sp, lineHeight = 18.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // Action buttons
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // BLOCK
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(if (blocked) SuccessGreen else DangerRed)
                        .clickable { blocked = true; vm.markBlocked(threatId) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) { Text(if (blocked) "✓ BLOCKED" else "🚫 BLOCK", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }

                // REPORT
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(if (reported) BorderBlue else WarningOrange)
                        .clickable { reported = true; vm.markReported(threatId) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) { Text(if (reported) "✓ REPORTED" else "📢 REPORT", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }

            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(BorderBlue).border(1.dp, BorderBlueMid, RoundedCornerShape(14.dp))
                    .clickable { navController.navigate("sandbox") }.padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) { Text("🔍 Investigate Scam URL", color = AccentBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }

            Box(
                Modifier.fillMaxWidth().clickable { navController.popBackStack() }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) { Text("DISMISS", color = Color(0xFF666666), fontSize = 13.sp) }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun RadialGauge(score: Int, sizeDp: Int = 200) {
    val color = when {
        score >= 90 -> DangerRed
        score >= 75 -> WarningOrange
        score >= 40 -> WarningYellow
        else        -> SuccessGreen
    }
    Box(contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(sizeDp.dp)) {
            val r           = size.minDimension / 2 * 0.78f
            val strokeW     = size.minDimension / 16f
            val sweepAngle  = 270f
            val startAngle  = 135f

            // Track
            drawArc(Color(0xFF1A3C6B), startAngle, sweepAngle, false, style = Stroke(strokeW, cap = StrokeCap.Round))
            // Fill
            val fill = (score / 100f) * sweepAngle
            drawArc(color, startAngle, fill, false, style = Stroke(strokeW, cap = StrokeCap.Round))

            // Needle
            val needleAngle = Math.toRadians((startAngle + fill).toDouble())
            val nx = center.x + r * cos(needleAngle).toFloat()
            val ny = center.y + r * sin(needleAngle).toFloat()
            drawLine(color, center, Offset(nx, ny), 3.dp.toPx(), StrokeCap.Round)
            drawCircle(color, 6.dp.toPx(), center)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(20.dp))
            Text(score.toString(), color = color, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Text("RISK SCORE", color = TextSecondary, fontSize = 12.sp)
        }
    }
}
