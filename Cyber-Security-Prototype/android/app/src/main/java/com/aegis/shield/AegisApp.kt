package com.aegis.shield

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AegisApp : Application() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()

        try {
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "Firebase initialized successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase. Missing google-services.json?", e)
            return
        }

        firebaseAuth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = firebaseAuth.currentUser?.uid
                    Log.d(TAG, "Anonymous sign-in succeeded. uid=$uid")
                } else {
                    Log.e(TAG, "Anonymous sign-in failed.", task.exception)
                }
            }
    }

    companion object {
        private const val TAG = "AegisApp"
    }
}
