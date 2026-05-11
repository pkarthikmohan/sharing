package com.aegis.shield.data.repository

import com.aegis.shield.data.local.SmsDataSource
import com.aegis.shield.domain.model.SmsMessage
import com.aegis.shield.domain.repository.SmsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class SmsRepositoryImpl @Inject constructor(
    private val smsDataSource: SmsDataSource,
) : SmsRepository {
    override fun getSmsFlow(): Flow<List<SmsMessage>> = smsDataSource.getSmsFlow()
}
