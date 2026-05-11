package com.aegis.shield.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aegis.shield.AegisViewModel
import com.aegis.shield.ui.components.AegisNavMenu
import com.aegis.shield.ui.theme.*

// ── Screen ────────────────────────────────────────────────────
@Composable
fun SafeWordScreen(
    vm: AegisViewModel,
    navController: NavController,
    safeWordViewModel: SafeWordViewModel = hiltViewModel(),
) {
    val contacts by vm.trustedContacts.collectAsState()
    val uiState by safeWordViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var enteredPin by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }

    var adding by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newRelation by remember { mutableStateOf("") }
    var newNumber by remember { mutableStateOf("") }
    var verificationStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.smsSent) {
        if (uiState.smsSent) {
            snackbarHostState.showSnackbar("SMS Sent Successfully!")
            safeWordViewModel.clearSmsSentFlag()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            safeWordViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = BackgroundDeepNavy,
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
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
                    "Select a trusted contact, generate the PIN locally, then send the same PIN by Twilio SMS.",
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
                            enteredPin = ""
                            verificationStatus = null
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

            Spacer(Modifier.height(20.dp))

            // ── Twilio sender interface ──────────────────────────
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BackgroundDark)
                    .border(1.dp, BorderBlue, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val selectedContact = selectedIndex?.let { contacts.getOrNull(it) }
                Button(
                    onClick = { selectedContact?.let { safeWordViewModel.sendPin(it.number) } },
                    enabled = selectedContact != null && !uiState.isSending,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BorderBlue)
                ) {
                    Text(
                        if (uiState.isSending) "Sending..." else "Send PIN via Twilio",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.isSending) {
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = AccentBlue,
                        trackColor = BorderBlueMid
                    )
                }

                uiState.generatedPin?.let { generatedPin ->
                    Spacer(Modifier.height(18.dp))
                    Text("SENDER INTERFACE", color = TextSecondary, fontSize = 12.sp, letterSpacing = 0.8.sp)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackgroundHeader)
                            .border(1.dp, AccentBlue, RoundedCornerShape(12.dp))
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            val masked = "•".repeat(generatedPin.length)
                            Text(
                                if (isPinVisible) generatedPin else masked,
                                color = AccentBlue,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 8.sp,
                            )
                            Spacer(Modifier.width(10.dp))
                            IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                Icon(
                                    imageVector = if (isPinVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (isPinVisible) "Hide PIN" else "Reveal PIN",
                                    tint = AccentBlue,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Valid for ${uiState.secondsRemaining}s", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    val progress by animateFloatAsState(
                        targetValue = uiState.secondsRemaining / 30f,
                        animationSpec = tween(300),
                        label = "pin-progress"
                    )
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = AccentBlue,
                        trackColor = BorderBlueMid
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("Ask caller to read back this 6-digit PIN", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { value ->
                            if (value.length <= 6 && value.all(Char::isDigit)) enteredPin = value
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        placeholder = { Text("Enter 6-digit PIN") },
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            verificationStatus = if (enteredPin == generatedPin) "Identity Verified" else "Verification Failed"
                        },
                        enabled = enteredPin.length == 6,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) { Text("Verify Identity") }
                }

                verificationStatus?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        it,
                        color = if (it.contains("Verified")) SuccessGreen else DangerRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
