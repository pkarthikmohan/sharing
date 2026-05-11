package com.aegis.shield

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aegis.shield.presentation.screens.messages.MessagesScreen
import com.aegis.shield.ui.AlertScreen
import com.aegis.shield.ui.CallLogsScreen
import com.aegis.shield.ui.DashboardScreen
import com.aegis.shield.ui.HomeScreen
import com.aegis.shield.ui.SafeWordScreen
import com.aegis.shield.ui.SandboxScreen
import com.aegis.shield.ui.SettingsScreen
import com.aegis.shield.ui.VoiceAlertScreen

@Composable
fun AegisNavGraph(navController: NavHostController, vm: AegisViewModel) {
    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(vm, navController)
        }

        composable(
            route = "alert?sender={sender}&body={body}",
            arguments = listOf(
                navArgument("sender") {
                    type = NavType.StringType
                    nullable = false
                    defaultValue = ""
                },
                navArgument("body") {
                    type = NavType.StringType
                    nullable = false
                    defaultValue = ""
                },
            ),
        ) {
            AlertScreen(navController)
        }

        composable(
            route = "alert/{threatId}",
            arguments = listOf(navArgument("threatId") { type = NavType.LongType; defaultValue = 0L }),
        ) {
            AlertScreen(navController)
        }

        composable(
            route = "voice_alert/{threatId}",
            arguments = listOf(navArgument("threatId") { type = NavType.LongType; defaultValue = 0L }),
        ) { back ->
            val id = back.arguments?.getLong("threatId") ?: 0L
            VoiceAlertScreen(navController, id)
        }

        composable("voice_alert") {
            VoiceAlertScreen(navController, 0L)
        }

        composable("safe_word") {
            SafeWordScreen(vm, navController)
        }

        composable("messages") {
            MessagesScreen(navController)
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
