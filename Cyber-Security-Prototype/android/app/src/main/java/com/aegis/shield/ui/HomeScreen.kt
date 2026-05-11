package com.aegis.shield.ui

import android.net.Uri
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.aegis.shield.AegisViewModel
import com.aegis.shield.data.ThreatBand
import com.aegis.shield.data.ThreatEntity
import com.aegis.shield.ui.components.AegisNavMenu
import com.aegis.shield.ui.theme.*

@Composable
fun HomeScreen(vm: AegisViewModel, navController: NavController) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val recent by homeViewModel.recentThreats.collectAsState()
    val stats by homeViewModel.threatStats.collectAsState()

    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.8f, label = "scale",
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Restart)
    )
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.4f, targetValue = 0f, label = "alpha",
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Restart)
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(BackgroundDeepNavy)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shield + pulse
            Box(contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(48.dp)) {
                    drawCircle(AccentBlue.copy(alpha = pulseAlpha), radius = size.minDimension / 2 * pulseScale, style = Stroke(2.dp.toPx()))
                }
                Text("🛡️", fontSize = 28.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Aegis", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("YOUR AI SHIELD AGAINST SCAMS", color = AccentBlue, fontSize = 11.sp, letterSpacing = 1.sp)
            }
            AegisNavMenu("home", navController)
        }

        Spacer(Modifier.height(12.dp))

        // Protection card
        Box(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF1A3C6B), Color(0xFF0D2647))))
                .border(1.dp, Color(0xFF2A5C9B), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(SuccessGreen),
                    contentAlignment = Alignment.Center
                ) { Text("✓", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Aegis is active", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Protecting your calls and SMS", color = TextSecondary, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                            .shadow(8.dp, CircleShape, clip = true)
                    )
                    Text("LIVE", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Stats row
        Row(
            Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                Triple("SMS Scanned",     stats.smsScanned,     AccentBlue),
                Triple("Calls Monitored", stats.callsMonitored, PurpleAccent),
                Triple("Threats Blocked", stats.threatsBlocked, DangerRed),
            ).forEach { (label, value, color) ->
                StatCard(Modifier.weight(1f), label, value, color)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Recent threats
        Text(
            "RECENT THREATS", color = TextSecondary, fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(10.dp))

        if (recent.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No threats detected yet", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = recent, key = { it.id }) { threat ->
                    ThreatRow(threat) {
                        val url =
                            "alert?sender=${Uri.encode(threat.sender)}&body=${Uri.encode(threat.body.orEmpty())}"
                        navController.navigate(url)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, label: String, value: Int, color: Color) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundDark)
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value.toString(), color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 10.sp, lineHeight = 13.sp)
    }
}

@Composable
private fun ThreatRow(threat: ThreatEntity, onClick: () -> Unit) {
    val band  = runCatching { ThreatBand.valueOf(threat.band) }.getOrDefault(ThreatBand.SUSPICIOUS)
    val color = bandColor(band)
    val ago   = formatAgo(threat.timestamp)

    Row(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundDark)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left accent bar
        Box(
            Modifier
                .width(3.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    threat.band,
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(color.copy(0.13f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                )
                Text(threat.type, color = TextSecondary, fontSize = 11.sp)
            }
            Text(threat.sender, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(ago, color = TextMuted, fontSize = 11.sp)
        }
        Box(
            Modifier.size(40.dp).clip(CircleShape)
                .background(color.copy(0.13f))
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(threat.score.toString(), color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
    }
}

private fun bandColor(band: ThreatBand) = when (band) {
    ThreatBand.CONFIRMED  -> DangerRed
    ThreatBand.LIKELY     -> WarningOrange
    ThreatBand.SUSPICIOUS -> WarningYellow
    ThreatBand.SAFE       -> SuccessGreen
}

private fun formatAgo(ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    return when {
        diff < 60_000        -> "just now"
        diff < 3_600_000     -> "${diff / 60_000} min ago"
        diff < 86_400_000    -> "${diff / 3_600_000} hr ago"
        else                 -> "${diff / 86_400_000} days ago"
    }
}
