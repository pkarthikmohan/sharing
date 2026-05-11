package com.aegis.shield.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aegis.shield.AegisViewModel
import com.aegis.shield.data.ThreatBand
import com.aegis.shield.data.ThreatType
import com.aegis.shield.ui.components.AegisNavMenu
import com.aegis.shield.ui.theme.*
import kotlin.math.*

@Composable
fun DashboardScreen(vm: AegisViewModel, navController: NavController) {
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        uri?.let { dashboardViewModel.exportReportToPdf(context, it) }
    }
    LaunchedEffect(Unit) {
        dashboardViewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    val allThreats by dashboardViewModel.allThreats.collectAsState()
    var period by remember { mutableStateOf("7d") }

    val msFilter = when (period) {
        "24h" -> System.currentTimeMillis() - 86_400_000L
        "30d" -> System.currentTimeMillis() - 30 * 86_400_000L
        else  -> System.currentTimeMillis() - 7 * 86_400_000L
    }
    val filtered = allThreats.filter { it.timestamp >= msFilter }

    val smishingCount = filtered.count { it.type == ThreatType.SMISHING.name }
    val vishingCount  = filtered.count { it.type == ThreatType.VISHING.name }
    val urlCount      = filtered.count { it.type == ThreatType.URL_SCAM.name }
    val total         = filtered.size

    Box(Modifier.fillMaxSize().background(BackgroundDeepNavy)) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
        )
        Column(Modifier.fillMaxSize().background(BackgroundDeepNavy)) {
        // Header
        Row(
            Modifier.fillMaxWidth().background(BackgroundHeader)
                .padding(start = 20.dp, end = 16.dp, top = 46.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Dashboard", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Threat analytics & history", color = TextSecondary, fontSize = 12.sp)
            }
            AegisNavMenu("dashboard", navController)
        }

        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
            // Period selector
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("24h", "7d", "30d").forEach { p ->
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp))
                            .background(if (period == p) BorderBlue else BackgroundDark)
                            .border(1.dp, if (period == p) AccentBlue else BorderBlue, RoundedCornerShape(20.dp))
                            .clickable { period = p }.padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) { Text(p, color = if (period == p) AccentBlue else TextMuted, fontSize = 12.sp) }
                }
            }

            // Line chart card
            Column(Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)).background(BackgroundDark).padding(16.dp)) {
                Text("Risk Score History", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(14.dp))
                MiniLineChart(filtered.map { it.score })
            }

            Spacer(Modifier.height(16.dp))

            // Donut chart card
            Column(Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)).background(BackgroundDark).padding(16.dp)) {
                Text("Threat Breakdown", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(14.dp))
                DonutChart(smishingCount, vishingCount, urlCount, total)
            }

            Spacer(Modifier.height(16.dp))

            // Top senders (live from Room)
            val topSenders by dashboardViewModel.topBlockedSenders.collectAsState()

            Text("Top Blocked Senders", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(10.dp))

            if (topSenders.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No data for this period", color = TextMuted, fontSize = 13.sp)
                }
            } else {
                topSenders.forEachIndexed { i, (sender, count) ->
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)).background(BackgroundDark).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(BorderBlue), contentAlignment = Alignment.Center) {
                            Text("#${i + 1}", color = AccentBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(sender, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Blocked threats", color = TextSecondary, fontSize = 11.sp)
                        }
                        Box(Modifier.clip(RoundedCornerShape(20.dp)).background(DangerRed.copy(0.13f))
                            .border(1.dp, DangerRed.copy(0.27f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("${count}x", color = DangerRed, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stats grid
            val stats = listOf(
                Triple("Accuracy Rate",   "98.2%", SuccessGreen),
                Triple("Avg Response",    "1.4s",  AccentBlue),
                Triple("False Positives", "0.3%",  WarningOrange),
                Triple("Crowd Reports",   "$total", PurpleAccent),
            )
            Column(Modifier.padding(horizontal = 16.dp)) {
                stats.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { (label, value, color) ->
                            Column(
                                Modifier.weight(1f).padding(vertical = 5.dp)
                                    .clip(RoundedCornerShape(14.dp)).background(BackgroundDark).padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                Text(label, color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)).background(BackgroundDark)
                    .border(1.dp, BorderBlueMid, RoundedCornerShape(14.dp))
                    .clickable {
                        createPdfLauncher.launch("Aegis_Threat_Report_${System.currentTimeMillis()}.pdf")
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) { Text("📄 Export Threat Report (PDF)", color = AccentBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
        }
    }
    }
}

@Composable
private fun MiniLineChart(scores: List<Int>) {
    if (scores.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Text("No data", color = TextMuted, fontSize = 12.sp)
        }
        return
    }
    Canvas(Modifier.fillMaxWidth().height(100.dp)) {
        val w = size.width; val h = size.height
        val max = scores.max().toFloat().coerceAtLeast(1f)
        val pts = scores.mapIndexed { i, s ->
            Offset(20f + (i.toFloat() / (scores.size - 1).coerceAtLeast(1)) * (w - 40f),
                h - 20f - (s / max) * (h - 40f))
        }
        if (pts.size < 2) return@Canvas
        val path = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, AccentBlue, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        pts.forEach { drawCircle(BackgroundDeepNavy, 5.dp.toPx(), it); drawCircle(AccentBlue, 4.dp.toPx(), it, style = Stroke(2.dp.toPx())) }
    }
}

@Composable
private fun DonutChart(smishing: Int, vishing: Int, url: Int, total: Int) {
    val data = listOf(smishing to DangerRed, vishing to WarningOrange, url to PurpleAccent, (total - smishing - vishing - url).coerceAtLeast(0) to AccentBlue)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Canvas(Modifier.size(140.dp)) {
            val r = size.minDimension / 2 * 0.85f
            if (total == 0) {
                drawCircle(BorderBlue, r, style = Stroke(20.dp.toPx()))
                return@Canvas
            }
            var start = -90f
            data.forEach { (count, color) ->
                val sweep = (count.toFloat() / total) * 360f
                drawArc(color, start, sweep, useCenter = true)
                start += sweep
            }
            drawCircle(BackgroundDeepNavy, r * 0.55f)
            // Total text via separate Text composable below
        }
        Column {
            listOf("Smishing" to DangerRed, "Vishing" to WarningOrange, "URL Scam" to PurpleAccent).forEachIndexed { i, (label, color) ->
                val count = listOf(smishing, vishing, url)[i]
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                    Spacer(Modifier.width(8.dp))
                    Text(label, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text(count.toString(), color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
