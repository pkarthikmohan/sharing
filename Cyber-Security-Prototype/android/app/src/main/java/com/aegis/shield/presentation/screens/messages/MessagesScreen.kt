package com.aegis.shield.presentation.screens.messages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aegis.shield.domain.model.SmsMessage
import com.aegis.shield.ui.components.AegisNavMenu
import com.aegis.shield.ui.theme.AccentBlue
import com.aegis.shield.ui.theme.BackgroundDark
import com.aegis.shield.ui.theme.BackgroundDeepNavy
import com.aegis.shield.ui.theme.BackgroundHeader
import com.aegis.shield.ui.theme.BorderBlue
import com.aegis.shield.ui.theme.BorderBlueMid
import com.aegis.shield.ui.theme.DangerRed
import com.aegis.shield.ui.theme.SuccessGreen
import com.aegis.shield.ui.theme.TextMuted
import com.aegis.shield.ui.theme.TextSecondary
import com.aegis.shield.ui.theme.WarningOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessagesScreen(
    navController: NavController,
    viewModel: MessagesViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val smsGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_SMS,
    ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS,
        ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(smsGranted) {
        viewModel.updatePermissionState(smsGranted)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(BackgroundDeepNavy),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(BackgroundHeader)
                .padding(start = 20.dp, end = 16.dp, top = 46.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Messages", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Real-time SMS inbox monitoring", color = TextSecondary, fontSize = 12.sp)
            }
            AegisNavMenu("messages", navController)
        }

        if (!uiState.hasSmsPermission) {
            PermissionRequiredCard(onOpenSettings = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null),
                )
                context.startActivity(intent)
            })
            return
        }

        if (uiState.isLoading) {
            SyncingShimmer()
        } else if (uiState.messages.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No inbox messages found.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(uiState.messages, key = { "${it.threadId}-${it.timestamp}" }) { msg ->
                    SmsCard(msg)
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun PermissionRequiredCard(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BackgroundDark)
            .border(1.dp, BorderBlue, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Permission Required", color = Color.White, fontWeight = FontWeight.Bold)
        Text("Enable SMS permission to load real-time inbox messages.", color = TextSecondary, fontSize = 12.sp)
        Button(
            onClick = onOpenSettings,
            colors = ButtonDefaults.buttonColors(containerColor = BorderBlue),
        ) {
            Text("Open Settings")
        }
    }
}

@Composable
private fun SyncingShimmer() {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Syncing...", color = AccentBlue, fontWeight = FontWeight.SemiBold)
        repeat(5) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundDark)
                    .border(1.dp, BorderBlueMid, RoundedCornerShape(12.dp)),
            )
        }
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AccentBlue, trackColor = BorderBlueMid)
    }
}

@Composable
private fun SmsCard(msg: SmsMessage) {
    val initials = msg.sender.take(2).uppercase(Locale.getDefault())
    val (riskLabel, riskColor) = riskBadge(msg.body)
    val time = remember(msg.timestamp) {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(msg.timestamp))
    }

    Row(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundDark)
            .border(1.dp, BorderBlueMid, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(AccentBlue.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(initials.ifBlank { "?" }, color = AccentBlue, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.size(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(msg.sender, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.size(8.dp))
                Text(
                    riskLabel,
                    color = riskColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(riskColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
            Spacer(Modifier.size(4.dp))
            Text(msg.body, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
            Spacer(Modifier.size(4.dp))
            Text(time, color = TextMuted, fontSize = 10.sp)
        }
    }
}

private fun riskBadge(body: String): Pair<String, Color> {
    val text = body.lowercase(Locale.getDefault())
    return when {
        listOf("urgent", "verify", "click", "bank", "upi", "otp").count { text.contains(it) } >= 3 ->
            "HIGH RISK" to DangerRed
        listOf("payment", "account", "link", "expired").any { text.contains(it) } ->
            "MEDIUM" to WarningOrange
        else -> "LOW" to SuccessGreen
    }
}
