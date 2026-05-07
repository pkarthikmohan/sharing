package com.aegis.shield

import android.app.Application
import com.google.firebase.FirebaseApp
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AegisApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
            Log.d("AegisApp", "Firebase initialized successfully.")
        } catch (e: Exception) {
            Log.e("AegisApp", "Failed to initialize Firebase. Missing google-services.json?", e)
        }
        
        // Room DB is initialized lazily via ThreatDatabase.getInstance()
    }
}
