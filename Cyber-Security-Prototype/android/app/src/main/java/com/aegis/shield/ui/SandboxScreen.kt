package com.aegis.shield.ui

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.viewinterop.AndroidView
import com.aegis.shield.AegisViewModel
import com.aegis.shield.ui.components.AegisNavMenu
import com.aegis.shield.ui.theme.*
import com.aegis.shield.util.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
@Suppress("UNUSED_PARAMETER")
fun SandboxScreen(
    _vm: AegisViewModel,
    navController: NavController,
    sandboxViewModel: SandboxViewModel = hiltViewModel(),
) {
    val uiState by sandboxViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var inputUrl by remember { mutableStateOf("") }

    val safeGreen = Color(0xFF1E6B45)
    val backgroundColor =
        when (uiState) {
            is SandboxUiState.Safe -> safeGreen.copy(alpha = 0.16f)
            else -> BackgroundDeepNavy
        }

    val activeUrl: String? =
        when (val s = uiState) {
            is SandboxUiState.Safe -> s.url
            is SandboxUiState.Scam -> s.url
            else -> inputUrl.takeIf { it.isNotBlank() }
        }

    val createPdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        val scamState = uiState as? SandboxUiState.Scam ?: return@rememberLauncherForActivityResult
        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch {
            withContext(Dispatchers.IO) {
                PdfReportGenerator.generateCertInThreatSubmission(
                    contentResolver = context.contentResolver,
                    uri = uri,
                    url = scamState.url,
                    threatIntelSources = listOf("VirusTotal"),
                    detectedElements = scamState.flaggedEngines,
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        sandboxViewModel.events.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundColor,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(backgroundColor)) {
            // Header
            Row(
                Modifier.fillMaxWidth().background(BackgroundHeader)
                    .border(BorderStroke(1.dp, BorderBlue), RoundedCornerShape(0.dp))
                    .padding(start = 20.dp, end = 16.dp, top = 46.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("🔍", fontSize = 22.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("URL Sandbox", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        when (uiState) {
                            SandboxUiState.Scanning -> "Detonating in isolated container..."
                            is SandboxUiState.Safe -> "VirusTotal scan complete"
                            is SandboxUiState.Scam -> "VirusTotal flagged this URL"
                            SandboxUiState.Idle -> "Validate and scan a URL"
                        },
                        color = TextSecondary,
                        fontSize = 11.sp,
                    )
                }
                AegisNavMenu("sandbox", navController)
            }

            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundDark)
                    .padding(14.dp),
            ) {
                Text("URL TO SCAN", color = TextSecondary, fontSize = 10.sp)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Paste URL including https://", color = TextSecondary) },
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { sandboxViewModel.scanUrl(inputUrl) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState != SandboxUiState.Scanning && inputUrl.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BorderBlue),
                ) {
                    Text("Scan URL", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // URL display (active)
            if (activeUrl != null) {
                Column(
                    Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)).background(BackgroundDark).padding(14.dp),
                ) {
                    Text("ACTIVE URL", color = TextSecondary, fontSize = 10.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        activeUrl,
                        color = when (uiState) {
                            is SandboxUiState.Safe -> safeGreen
                            is SandboxUiState.Scam -> Color(0xFFFF6B6B)
                            else -> Color.White
                        },
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    )
                }
            }

            when (val state = uiState) {
                SandboxUiState.Idle -> {
                    Column(
                        Modifier.fillMaxSize().padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("🧪", fontSize = 32.sp)
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Enter a URL with http/https to scan.",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "The web preview is rendered with JavaScript disabled for safety.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                        )
                    }
                }

                SandboxUiState.Scanning -> {
                    Column(
                        Modifier.fillMaxSize().padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = BorderBlue)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Detonating in isolated container...",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Querying VirusTotal for detections…",
                            color = TextSecondary,
                            fontSize = 12.sp,
                        )
                    }
                }

                is SandboxUiState.Safe -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 32.dp),
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                "SAFE",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(safeGreen)
                                    .padding(horizontal = 18.dp, vertical = 8.dp),
                            )
                            Text(
                                "VT: harmless=${state.harmless}, suspicious=${state.suspicious}, malicious=${state.malicious}",
                                color = TextSecondary,
                                fontSize = 12.sp,
                            )
                        }

                        Box(
                            Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, safeGreen.copy(alpha = 0.65f), RoundedCornerShape(12.dp)),
                        ) {
                            SecureWebPreview(url = state.url, showScamWatermark = false)
                        }

                        Spacer(Modifier.height(16.dp))

                        Column(
                            Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(BackgroundDark)
                                .border(1.dp, safeGreen.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                .padding(16.dp),
                        ) {
                            Text("RESULT", color = TextSecondary, fontSize = 11.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "URL is Safe. No malicious detections reported by VirusTotal.",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                is SandboxUiState.Scam -> {
                    Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                "SCAM",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(DangerRed)
                                    .padding(horizontal = 18.dp, vertical = 8.dp),
                            )
                            Text(
                                "VT: harmless=${state.harmless}, suspicious=${state.suspicious}, malicious=${state.malicious}",
                                color = TextSecondary,
                                fontSize = 12.sp,
                            )
                        }

                        Box(
                            Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(4.dp, DangerRed, RoundedCornerShape(12.dp)),
                        ) {
                            SecureWebPreview(url = state.url, showScamWatermark = true)
                        }

                        Spacer(Modifier.height(16.dp))

                        Column(
                            Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp)).background(BackgroundDark).padding(14.dp),
                        ) {
                            Text("THREAT INTELLIGENCE", color = TextSecondary, fontSize = 11.sp, letterSpacing = 0.5.sp)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("VirusTotal").forEach { src ->
                                    Column(
                                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF2E0D0D))
                                            .border(1.dp, DangerRed.copy(0.27f), RoundedCornerShape(10.dp))
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text("⚠️ FLAGGED", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(src, color = TextSecondary, fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            "DETECTED ENGINES",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Spacer(Modifier.height(10.dp))

                        Column(
                            Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(BackgroundDark)
                                .padding(10.dp),
                        ) {
                            if (state.flaggedEngines.isEmpty()) {
                                Text("No engine names returned, but the URL is flagged.", color = TextSecondary, fontSize = 12.sp)
                            } else {
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 220.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    items(state.flaggedEngines) { line ->
                                        Row(
                                            Modifier.fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF2E0D0D))
                                                .border(1.dp, DangerRed.copy(0.27f), RoundedCornerShape(10.dp))
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text("⚠️", fontSize = 16.sp)
                                            Spacer(Modifier.width(10.dp))
                                            Text(line, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { createPdfLauncher.launch("CERT_IN_Report_${System.currentTimeMillis()}.pdf") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            ) {
                                Text("📨 Report to CERT-In (PDF)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val message =
                                        "⚠️ SECURITY WARNING: Please do not click on this link: ${state.url}. It has been verified as a scam by the Aegis security app."
                                    val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("smsto:")
                                        putExtra("sms_body", message)
                                        putExtra(Intent.EXTRA_TEXT, message)
                                    }
                                    runCatching { context.startActivity(smsIntent) }.recoverCatching {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, message)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share warning"))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BorderBlue),
                            ) {
                                Text("⚠️ Share Warning via SMS", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecureWebPreview(
    url: String,
    showScamWatermark: Boolean,
) {
    Box(Modifier.fillMaxWidth().height(240.dp).background(Color.White)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = false
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.domStorageEnabled = false
                    settings.setSupportMultipleWindows(false)
                    settings.loadsImagesAutomatically = true
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        settings.safeBrowsingEnabled = true
                    }
                    loadUrl(url)
                }
            },
            update = { webView ->
                if (webView.url != url) webView.loadUrl(url)
            },
        )

        if (showScamWatermark) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xAA2E0D0D)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(3.dp, DangerRed, RoundedCornerShape(10.dp))
                        .background(Color(0xCCFFFFFF))
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                ) {
                    Text(
                        "KNOWN SCAM\nDO NOT PAY\n— Aegis —",
                        color = DangerRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                    )
                }
            }
        }
    }
}
