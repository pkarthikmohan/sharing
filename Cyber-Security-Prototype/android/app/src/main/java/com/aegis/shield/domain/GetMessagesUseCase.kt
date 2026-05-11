package com.aegis.shield.domain

import com.aegis.shield.domain.model.SmsMessage
import com.aegis.shield.domain.repository.SmsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetMessagesUseCase @Inject constructor(
    private val smsRepository: SmsRepository,
) {
    operator fun invoke(): Flow<List<SmsMessage>> = smsRepository.getSmsFlow()
}
