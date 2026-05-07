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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aegis.shield.AegisViewModel
import com.aegis.shield.data.SmsAnalysis
import com.aegis.shield.data.ThreatBand
import com.aegis.shield.ui.components.AegisNavMenu
import com.aegis.shield.ui.theme.*

@Composable
fun MessagesScreen(vm: AegisViewModel, navController: NavController) {
    val sms by vm.sms.collectAsState()

    LaunchedEffect(Unit) {
        vm.refreshSms()
    }

    Column(Modifier.fillMaxSize().background(BackgroundDeepNavy)) {
        // Header
        Row(
            Modifier.fillMaxWidth().background(BackgroundHeader)
                .padding(start = 20.dp, end = 16.dp, top = 46.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Messages", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Inbox/Sent + OTP + spam detection", color = TextSecondary, fontSize = 12.sp)
            }
            AegisNavMenu("messages", navController)
        }

        // Refresh
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${sms.size} messages scanned", color = TextMuted, fontSize = 12.sp)
            Box(
                Modifier.clip(RoundedCornerShape(12.dp)).background(BorderBlue)
                    .border(1.dp, BorderBlueMid, RoundedCornerShape(12.dp))
                    .clickable { vm.refreshSms() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) { Text("↻ Refresh", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
        }

        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            if (sms.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No SMS loaded. Make sure READ_SMS is allowed.", color = TextMuted, fontSize = 13.sp)
                }
            } else {
                sms.forEach { row ->
                    SmsRow(row)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SmsRow(row: SmsAnalysis) {
    val band = row.band
    val color = bandColor(band)
    val sender = row.msg.address ?: "Unknown"
    val otp = row.otpCode

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
                    band.name,
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(color.copy(0.13f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                )
                Text(sender, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (otp != null) {
                    Text(
                        "OTP $otp",
                        color = SuccessGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(SuccessGreen.copy(0.13f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(row.msg.body, color = TextSecondary, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
        }
    }
}

private fun bandColor(band: ThreatBand) = when (band) {
    ThreatBand.CONFIRMED -> DangerRed
    ThreatBand.LIKELY -> WarningOrange
    ThreatBand.SUSPICIOUS -> WarningYellow
    ThreatBand.SAFE -> SuccessGreen
}

