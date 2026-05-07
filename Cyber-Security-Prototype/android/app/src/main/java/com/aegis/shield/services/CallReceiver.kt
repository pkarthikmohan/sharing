package com.aegis.shield.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        Log.d("CallReceiver", "Phone state changed: $state, number: $incomingNumber")

        when (state) {
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // Determine contact name if available
                var contactName = "Unknown Caller"
                if (!incomingNumber.isNullOrBlank()) {
                    contactName = getContactName(context, incomingNumber) ?: "Unknown Caller ($incomingNumber)"
                }

                // Start the audio monitoring service
                Log.d("CallReceiver", "Call active. Starting audio monitoring for $contactName.")
                val serviceIntent = Intent(context, CallAudioService::class.java).apply {
                    putExtra("CALLER_NAME", contactName)
                }
                
                context.startForegroundService(serviceIntent)
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                // Call ended, stop the audio monitoring service
                Log.d("CallReceiver", "Call ended. Stopping audio monitoring.")
                val serviceIntent = Intent(context, CallAudioService::class.java)
                context.stopService(serviceIntent)
            }
        }
    }

    private fun getContactName(context: Context, phoneNumber: String): String? {
        var contactName: String? = null
        val uri: Uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                if (index != -1) {
                    contactName = cursor.getString(index)
                }
            }
        }
        return contactName
    }
}
