package com.aegis.shield.data.local

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import com.aegis.shield.domain.model.SmsMessage
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SmsDataSource @Inject constructor(
    private val contentResolver: ContentResolver,
) {
    fun fetchSmsHistory(): List<SmsMessage> {
        val uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.THREAD_ID,
        )
        val out = mutableListOf<SmsMessage>()
        contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC",
        )?.use { cursor ->
            val addressCol = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyCol = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateCol = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val threadIdCol = cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
            while (cursor.moveToNext()) {
                out += SmsMessage(
                    sender = cursor.getString(addressCol).orEmpty(),
                    body = cursor.getString(bodyCol).orEmpty(),
                    timestamp = cursor.getLong(dateCol),
                    threadId = cursor.getLong(threadIdCol),
                )
            }
        }
        return out
    }

    fun getSmsFlow(): Flow<List<SmsMessage>> = callbackFlow {
        val smsUri = Telephony.Sms.Inbox.CONTENT_URI
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(fetchSmsHistory())
            }
        }

        contentResolver.registerContentObserver(smsUri, true, observer)
        trySend(fetchSmsHistory())

        awaitClose {
            contentResolver.unregisterContentObserver(observer)
        }
    }
}
