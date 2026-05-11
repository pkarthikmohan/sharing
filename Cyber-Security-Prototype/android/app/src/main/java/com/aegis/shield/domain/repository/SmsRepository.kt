package com.aegis.shield.domain.repository

import com.aegis.shield.domain.model.SmsMessage
import kotlinx.coroutines.flow.Flow

interface SmsRepository {
    fun getSmsFlow(): Flow<List<SmsMessage>>
}
