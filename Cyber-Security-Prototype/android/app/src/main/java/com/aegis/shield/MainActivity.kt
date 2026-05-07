package com.aegis.shield

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aegis.shield.ui.*
import com.aegis.shield.ui.SafeWordScreen
import com.aegis.shield.ui.theme.AegisTheme
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log

class MainActivity : ComponentActivity() {

    private val vm: AegisViewModel by viewModels()
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permissions handled — services check individually */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        
        // Fetch FCM Registration Token for push messaging
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("MainActivity", "FCM Token: $token")
        }

        requestMissingPermissions()

        // Determine start route from notification deep-link
        val startRoute = intent?.getStringExtra("route") ?: "home"

        setContent {
            AegisTheme {
                val navController = rememberNavController()

                // Handle deep-links from notifications
                LaunchedEffect(startRoute) {
                    if (startRoute != "home") navController.navigate(startRoute)
                }

                NavHost(navController = navController, startDestination = "home") {

                    composable("home") {
                        HomeScreen(vm, navController)
                    }

                    composable(
                        "alert/{threatId}",
                        arguments = listOf(navArgument("threatId") { type = NavType.LongType; defaultValue = 0L })
                    ) { back ->
                        val id = back.arguments?.getLong("threatId") ?: 0L
                        AlertScreen(vm, navController, id)
                    }

                    composable(
                        "voice_alert/{threatId}",
                        arguments = listOf(navArgument("threatId") { type = NavType.LongType; defaultValue = 0L })
                    ) { back ->
                        val id = back.arguments?.getLong("threatId") ?: 0L
                        VoiceAlertScreen(vm, navController, id)
                    }
                    // Shortcut route without ID
                    composable("voice_alert") {
                        VoiceAlertScreen(vm, navController, 0L)
                    }

                    composable("safe_word") {
                        SafeWordScreen(vm, navController)
                    }

                    composable("messages") {
                        MessagesScreen(vm, navController)
                    }

                    composable("call_logs") {
                        CallLogsScreen(vm, navController)
                    }

                    composable("sandbox") {
                        SandboxScreen(vm, navController)
                    }

                    composable("dashboard") {
                        DashboardScreen(vm, navController)
                    }

                    composable("settings") {
                        SettingsScreen(vm, navController)
                    }
                }
            }
        }
    }

    private fun requestMissingPermissions() {
        val required = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            required += Manifest.permission.POST_NOTIFICATIONS
        }
        val missing = required.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) permissionLauncher.launch(missing.toTypedArray())
    }
}
