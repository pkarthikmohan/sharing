package com.aegis.shield.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.aegis.shield.AegisViewModel
import com.aegis.shield.ui.components.AegisNavMenu
import com.aegis.shield.ui.theme.*
import kotlinx.coroutines.delay

// Trusted contacts come from AegisViewModel (user-managed)

private enum class ChallengeState { IDLE, SENDING, WAITING, VERIFIED, FAILED }

private const val DEMO_PIN = "482917"

// ── Screen ────────────────────────────────────────────────────
@Composable
fun SafeWordScreen(vm: AegisViewModel, navController: NavController) {
    val contacts by vm.trustedContacts.collectAsState()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var state        by remember { mutableStateOf(ChallengeState.IDLE) }
    var countdown    by remember { mutableStateOf(30) }
    var pin          by remember { mutableStateOf("") }
    var showPin      by remember { mutableStateOf(false) }

    var adding by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newRelation by remember { mutableStateOf("") }
    var newNumber by remember { mutableStateOf("") }

    // Auto-advance SENDING → WAITING after 1.5 s
    LaunchedEffect(state) {
        if (state == ChallengeState.SENDING) {
            delay(1500)
            state = ChallengeState.WAITING
        }
    }

    // Countdown while WAITING
    LaunchedEffect(state) {
        if (state == ChallengeState.WAITING) {
            countdown = 30
            repeat(30) {
                delay(1000)
                countdown--
                if (countdown <= 0) state = ChallengeState.IDLE
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(BackgroundDeepNavy)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Header ────────────────────────────────────────────
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 46.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔐", fontSize = 28.sp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Safe-Word", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Zero-knowledge identity challenge", color = TextSecondary, fontSize = 12.sp)
            }
            AegisNavMenu("safe_word", navController)
        }

        // ── Info banner ───────────────────────────────────────
        Box(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AccentBlue.copy(alpha = 0.13f))
                .border(1.dp, BorderBlue, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(
                "Select a trusted contact to send a silent TOTP challenge. " +
                "Your secret never leaves this device.",
                color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Contact list ──────────────────────────────────────
        Text(
            "TRUSTED CONTACTS",
            color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(10.dp))

        if (contacts.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No trusted contacts yet. Add one below.", color = TextMuted, fontSize = 13.sp)
            }
        }

        contacts.forEachIndexed { i, contact ->
            val isSelected = selectedIndex == i
            Row(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) BorderBlue else BackgroundDark)
                    .border(
                        1.dp,
                        if (isSelected) AccentBlue else BorderBlue.copy(alpha = 0.33f),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable {
                        selectedIndex = i
                        state = ChallengeState.IDLE
                        pin = ""
                        showPin = false
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.hsl((i * 80f + 200f) % 360f, 0.6f, 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        contact.name.first().toString(),
                        color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(contact.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("${contact.relation} · ${contact.number}", color = TextSecondary, fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Remove",
                        color = DangerRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(DangerRed.copy(alpha = 0.13f))
                            .border(1.dp, DangerRed.copy(alpha = 0.27f), RoundedCornerShape(10.dp))
                            .clickable {
                                vm.removeTrustedContact(contact.number)
                                if (selectedIndex == i) selectedIndex = null
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                    if (isSelected) Text("✓", color = AccentBlue, fontSize = 18.sp)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Add contact ────────────────────────────────────────
        Text(
            "ADD TRUSTED CONTACT",
            color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(10.dp))

        Column(
            Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)).background(BackgroundDark)
                .border(1.dp, BorderBlue.copy(alpha = 0.33f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            if (!adding) {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(BorderBlue).border(1.dp, BorderBlueMid, RoundedCornerShape(12.dp))
                        .clickable { adding = true }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) { Text("+ Add Contact", color = AccentBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            } else {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = newRelation,
                    onValueChange = { newRelation = it },
                    label = { Text("Relation (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = newNumber,
                    onValueChange = { newNumber = it },
                    label = { Text("Phone number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            vm.addTrustedContact(newName, newRelation, newNumber)
                            newName = ""; newRelation = ""; newNumber = ""
                            adding = false
                        },
                        modifier = Modifier.weight(1f),
                        enabled = newName.isNotBlank() && newNumber.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BorderBlue)
                    ) { Text("Save", fontWeight = FontWeight.Bold) }

                    OutlinedButton(
                        onClick = { adding = false; newName = ""; newRelation = ""; newNumber = "" },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    ) { Text("Cancel") }
                }
            }
        }

        // ── Challenge card ────────────────────────────────────
        val cardBg = when (state) {
            ChallengeState.IDLE     -> BackgroundDark
            ChallengeState.SENDING  -> BorderBlue
            ChallengeState.WAITING  -> Color(0xFF1A3060)
            ChallengeState.VERIFIED -> Color(0xFF0D2E1A)
            ChallengeState.FAILED   -> Color(0xFF2E0D0D)
        }
        val cardBorderColor = when (state) {
            ChallengeState.VERIFIED -> SuccessGreen
            ChallengeState.FAILED   -> DangerRed
            else                    -> BorderBlue
        }

        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {

                // ── IDLE: Send button ──────────────────────────
                ChallengeState.IDLE -> {
                    val enabled = selectedIndex != null
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (enabled) BorderBlue else Color(0xFF0A1020))
                            .border(
                                1.dp,
                                if (enabled) AccentBlue else Color(0xFF222222),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable(enabled = enabled) { state = ChallengeState.SENDING }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Send Silent Challenge",
                            color = if (enabled) AccentBlue else Color(0xFF333333),
                            fontSize = 15.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ── SENDING: Progress bar ──────────────────────
                ChallengeState.SENDING -> {
                    Text("📡", fontSize = 24.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Sending secure challenge to ${selectedIndex?.let { contacts.getOrNull(it)?.name }}…",
                        color = AccentBlue, fontSize = 14.sp, textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = AccentBlue,
                        trackColor = BorderBlue
                    )
                }

                // ── WAITING: Countdown + PIN entry ─────────────
                ChallengeState.WAITING -> {
                    // Countdown arc
                    Box(contentAlignment = Alignment.Center) {
                        val cdAnim by animateIntAsState(
                            targetValue = countdown,
                            animationSpec = tween(900),
                            label = "countdown"
                        )
                        Canvas(Modifier.size(80.dp)) {
                            drawArc(BorderBlue, -90f, 360f, false, style = Stroke(6.dp.toPx()))
                            drawArc(
                                AccentBlue, -90f,
                                (cdAnim / 30f) * 360f,
                                false,
                                style = Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(countdown.toString(), color = AccentBlue, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("seconds remaining", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(16.dp))

                    // Family device simulation
                    Text("Simulating family device — they see:", color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BackgroundHeader)
                            .border(1.dp, BorderBlueMid, RoundedCornerShape(10.dp))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (showPin) DEMO_PIN else "••••••",
                            color = AccentBlue, fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 8.sp
                        )
                    }
                    TextButton(onClick = { showPin = !showPin }) {
                        Text(
                            if (showPin) "Hide PIN (demo)" else "Show PIN (demo)",
                            color = TextMuted, fontSize = 11.sp
                        )
                    }

                    // PIN entry
                    Text("Ask caller to state their PIN:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { v ->
                            if (v.length <= 6 && v.all { it.isDigit() }) pin = v
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 8.sp,
                            color = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        placeholder = {
                            Text(
                                "Enter 6-digit PIN",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = TextMuted
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = AccentBlue,
                            unfocusedBorderColor = BorderBlueMid,
                            cursorColor          = AccentBlue
                        ),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            state = if (pin == DEMO_PIN) ChallengeState.VERIFIED else ChallengeState.FAILED
                        },
                        enabled = pin.length == 6,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BorderBlue)
                    ) {
                        Text("Verify Identity", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // ── VERIFIED ───────────────────────────────────
                ChallengeState.VERIFIED -> {
                    Text("✅", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Identity Verified", color = SuccessGreen, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This is a real call from ${selectedIndex?.let { contacts.getOrNull(it)?.name }}. TOTP matched.",
                        color = Color(0xFF88CCAA), fontSize = 13.sp, lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { state = ChallengeState.IDLE; pin = ""; selectedIndex = null; showPin = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Close") }
                }

                // ── FAILED ─────────────────────────────────────
                ChallengeState.FAILED -> {
                    Text("🚫", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Verification Failed", color = DangerRed, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This is NOT ${selectedIndex?.let { contacts.getOrNull(it)?.name }}. End this call immediately.",
                        color = Color(0xFFFFAAAA), fontSize = 13.sp, lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { state = ChallengeState.IDLE; pin = ""; showPin = false },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Try Again") }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}
