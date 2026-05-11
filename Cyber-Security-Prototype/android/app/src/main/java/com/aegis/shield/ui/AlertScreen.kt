package com.aegis.shield.ui

import android.widget.Toast
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aegis.shield.data.ThreatBand
import com.aegis.shield.ui.components.AegisNavMenu
import com.aegis.shield.ui.theme.AccentBlue
import com.aegis.shield.ui.theme.BorderBlue
import com.aegis.shield.ui.theme.BorderBlueMid
import com.aegis.shield.ui.theme.DangerRed
import com.aegis.shield.ui.theme.SuccessGreen
import com.aegis.shield.ui.theme.TextSecondary
import com.aegis.shield.ui.theme.WarningOrange
import com.aegis.shield.ui.theme.WarningYellow
import com.aegis.shield.util.ThreatVectorScore
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val IDLE_VECTORS: List<ThreatVectorScore> = listOf(
    ThreatVectorScore("Urgency", "⚡", 0),
    ThreatVectorScore("Financial Demand", "💰", 0),
    ThreatVectorScore("Authority Spoof", "🏛️", 0),
    ThreatVectorScore("Link Lure", "🔗", 0),
    ThreatVectorScore("Impersonation", "👤", 0),
)

@Composable
fun AlertScreen(
    navController: NavController,
    alertViewModel: AlertViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by alertViewModel.uiState.collectAsState()
    val toastMessage by alertViewModel.toastMessage.collectAsState()

    LaunchedEffect(toastMessage) {
        val msg = toastMessage ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        alertViewModel.consumeToast()
    }

    val displayVectors = remember(uiState) {
        when (uiState) {
            is AlertUiState.Idle -> IDLE_VECTORS
            is AlertUiState.Analyzed -> (uiState as AlertUiState.Analyzed).threatVectors
        }
    }

    val displayScore = remember(uiState) {
        when (uiState) {
            is AlertUiState.Idle -> 0
            is AlertUiState.Analyzed -> (uiState as AlertUiState.Analyzed).riskScore
        }
    }

    val animScore by animateIntAsState(targetValue = displayScore, animationSpec = tween(1200), label = "score")

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .verticalScroll(rememberScrollState()),
    ) {
        when (uiState) {
            is AlertUiState.Idle -> IdleHeader(navController)
            is AlertUiState.Analyzed -> AnalyzedHeader(navController)
        }

        Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
            RadialGauge(animScore)
        }

        when (val state = uiState) {
            is AlertUiState.Analyzed -> {
                val bandLabel = when (state.band) {
                    ThreatBand.CONFIRMED -> "CONFIRMED SCAM"
                    ThreatBand.LIKELY -> "LIKELY SCAM"
                    ThreatBand.SUSPICIOUS -> "SUSPICIOUS"
                    ThreatBand.SAFE -> "LOW RISK"
                }
                val bandColor = when (state.band) {
                    ThreatBand.CONFIRMED -> DangerRed
                    ThreatBand.LIKELY -> Color(0xFFCC5A00)
                    ThreatBand.SUSPICIOUS -> WarningOrange
                    ThreatBand.SAFE -> SuccessGreen
                }

                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        bandLabel,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp,
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(bandColor)
                                .padding(horizontal = 24.dp, vertical = 6.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))

                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A1A1A))
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                ) {
                    Text(
                        "INTERCEPTED MESSAGE · ${state.sender}",
                        color = DangerRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(state.body.ifBlank { "—" }, color = Color(0xFFCCCCCC), fontSize = 12.sp, lineHeight = 18.sp)
                }

                Spacer(Modifier.height(16.dp))

                ThreatVectorsSection(displayVectors)

                Spacer(Modifier.height(16.dp))

                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A1A1A))
                        .padding(14.dp),
                ) {
                    Text("WHY THIS IS SUSPICIOUS", color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(state.explanation, color = Color(0xFFCCCCCC), fontSize = 12.sp, lineHeight = 18.sp)
                }

                Spacer(Modifier.height(16.dp))

                ActionsSection(
                    navController = navController,
                    blocked = state.blocked,
                    reported = state.reported,
                    onBlock = alertViewModel::markBlocked,
                    onReport =
                        {
                            alertViewModel.reportScamToAuthority(
                                sender = state.sender,
                                body = state.body,
                            )
                        },
                )
            }
            is AlertUiState.Idle -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "Standing by · feed a flagged SMS below",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF151515))
                        .border(1.dp, Color(0xFF2A3F5F), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                ) {
                    Text("OPEN FROM HOME FEED OR NOTIFICATION", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Pick a Medium or High risk SMS from Recent Threats so Aegis can run live vector scoring.",
                        color = Color(0xFF999999),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    )
                }

                Spacer(Modifier.height(16.dp))

                ThreatVectorsSection(displayVectors)

                Spacer(Modifier.height(16.dp))

                ActionsSectionIdle(navController)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun IdleHeader(navController: NavController) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A3C6B), Color(0xFF0D2647))))
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🛡️", fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Risk Detector",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                )
                Text("Waiting for a message to analyse", color = Color(0xFF9EC5FF), fontSize = 11.sp)
            }
            AegisNavMenu("alert", navController)
        }
    }
}

@Composable
private fun AnalyzedHeader(navController: NavController) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFFC0392B), Color(0xFF8B0000))))
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⚠️", fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "SCAM DETECTED",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                )
                Text("Live analysis from message content", color = Color(0xFFFFAAAA), fontSize = 11.sp)
            }
            AegisNavMenu("alert", navController)
        }
    }
}

@Composable
private fun ThreatVectorsSection(vectors: List<ThreatVectorScore>) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text(
            "THREAT VECTORS",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
        )
        Spacer(Modifier.height(10.dp))
        vectors.forEach { vec ->
            Column(Modifier.padding(bottom = 8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${vec.emoji} ${vec.label}", color = Color(0xFFCCCCCC), fontSize = 12.sp)
                    Text("${vec.pct}%", color = if (vec.pct > 0) DangerRed else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF222222)),
                ) {
                    if (vec.pct > 0) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(vec.pct / 100f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(DangerRed, Color(0xFFE74C3C))),
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionsSection(
    navController: NavController,
    blocked: Boolean,
    reported: Boolean,
    onBlock: () -> Unit,
    onReport: () -> Unit,
) {
    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                    .background(if (blocked) SuccessGreen else DangerRed)
                    .clickable(enabled = true, onClick = onBlock).padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (blocked) "✓ BLOCKED" else "🚫 BLOCK",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                    .background(if (reported) BorderBlue else WarningOrange)
                    .clickable(onClick = onReport).padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (reported) "✓ REPORTED" else "📢 REPORT",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        SandboxDismissRow(navController)
    }
}

@Composable
private fun ActionsSectionIdle(navController: NavController) {
    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF222222))
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Block / Report unlock when analysing a threat",
                color = Color(0xFF888888),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        SandboxDismissRow(navController)
    }
}

@Composable
private fun SandboxDismissRow(navController: NavController) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(BorderBlue).border(1.dp, BorderBlueMid, RoundedCornerShape(14.dp))
            .clickable { navController.navigate("sandbox") }.padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("🔍 Investigate Scam URL", color = AccentBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }

    Box(
        Modifier.fillMaxWidth().clickable { navController.popBackStack() }.padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("DISMISS", color = Color(0xFF666666), fontSize = 13.sp)
    }
}

@Composable
private fun RadialGauge(score: Int, sizeDp: Int = 200) {
    val color =
        when {
            score <= 0 -> TextSecondary
            score >= 90 -> DangerRed
            score >= 75 -> WarningOrange
            score >= 40 -> WarningYellow
            else -> SuccessGreen
        }
    Box(contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(sizeDp.dp)) {
            val pivot = Offset(size.width / 2f, size.height / 2f)
            val minDim = kotlin.math.min(size.width, size.height)
            val r = minDim / 2 * 0.78f
            val strokeW = minDim / 16f
            val sweepAngle = 270f
            val startAngle = 135f

            drawArc(Color(0xFF1A3C6B), startAngle, sweepAngle, false, style = Stroke(strokeW, cap = StrokeCap.Round))
            val fill = if (score <= 0) 0f else (score / 100f) * sweepAngle
            if (fill > 0f) {
                drawArc(color, startAngle, fill, false, style = Stroke(strokeW, cap = StrokeCap.Round))
            }

            val rad = ((startAngle + fill) * (PI.toFloat() / 180f)).toDouble()
            val nx = pivot.x + r * cos(rad).toFloat()
            val ny = pivot.y + r * sin(rad).toFloat()
            if (score > 0) {
                drawLine(color, pivot, Offset(nx, ny), 3.dp.toPx(), StrokeCap.Round)
                drawCircle(color, 6.dp.toPx(), pivot)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(20.dp))
            Text(score.toString(), color = color, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Text("RISK SCORE", color = TextSecondary, fontSize = 12.sp)
        }
    }
}
