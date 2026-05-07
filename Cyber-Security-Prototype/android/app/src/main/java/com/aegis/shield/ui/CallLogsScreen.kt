package com.aegis.shield.ui

import android.provider.CallLog
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aegis.shield.AegisViewModel
import com.aegis.shield.data.CallAnalysis
import com.aegis.shield.data.ThreatBand
import com.aegis.shield.ui.components.AegisNavMenu
import com.aegis.shield.ui.theme.*

@Composable
fun CallLogsScreen(vm: AegisViewModel, navController: NavController) {
    val calls by vm.calls.collectAsState()

    LaunchedEffect(Unit) {
        vm.refreshCallLogs()
    }

    Column(Modifier.fillMaxSize().background(BackgroundDeepNavy)) {
        // Header
        Row(
            Modifier.fillMaxWidth().background(BackgroundHeader)
                .padding(start = 20.dp, end = 16.dp, top = 46.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Call Logs", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("History + risk labels", color = TextSecondary, fontSize = 12.sp)
            }
            AegisNavMenu("call_logs", navController)
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${calls.size} calls", color = TextMuted, fontSize = 12.sp)
            Box(
                Modifier.clip(RoundedCornerShape(12.dp)).background(BorderBlue)
                    .border(1.dp, BorderBlueMid, RoundedCornerShape(12.dp))
                    .clickable { vm.refreshCallLogs() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) { Text("↻ Refresh", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
        }

        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            if (calls.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No call logs loaded. Make sure READ_CALL_LOG is allowed.", color = TextMuted, fontSize = 13.sp)
                }
            } else {
                calls.forEach { row ->
                    CallRow(row)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CallRow(row: CallAnalysis) {
    val color = bandColor(row.band)
    val nameOrNum = row.entry.cachedName ?: row.entry.number ?: "Unknown"
    val typeLabel = when (row.entry.type) {
        CallLog.Calls.INCOMING_TYPE -> "Incoming"
        CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
        CallLog.Calls.MISSED_TYPE -> "Missed"
        CallLog.Calls.REJECTED_TYPE -> "Rejected"
        else -> "Call"
    }

    Row(
        Modifier.padding(horizontal = 16.dp).fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundDark)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape)
                .background(color.copy(alpha = 0.13f))
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(row.score.toString(), color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    row.band.name,
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(color.copy(0.13f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                )
                Text(typeLabel, color = TextSecondary, fontSize = 11.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(nameOrNum, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(row.reason, color = TextMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun bandColor(band: ThreatBand) = when (band) {
    ThreatBand.CONFIRMED -> DangerRed
    ThreatBand.LIKELY -> WarningOrange
    ThreatBand.SUSPICIOUS -> WarningYellow
    ThreatBand.SAFE -> SuccessGreen
}

