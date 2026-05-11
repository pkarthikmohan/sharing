package com.aegis.shield.di

import com.aegis.shield.data.repository.SmsRepositoryImpl
import com.aegis.shield.data.repository.ThreatRepositoryImpl
import com.aegis.shield.data.TwilioRepository
import com.aegis.shield.data.TwilioRepositoryImpl
import com.aegis.shield.domain.repository.SmsRepository
import com.aegis.shield.domain.repository.ThreatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTwilioRepository(impl: TwilioRepositoryImpl): TwilioRepository

    @Binds
    @Singleton
    abstract fun bindSmsRepository(impl: SmsRepositoryImpl): SmsRepository

    @Binds
    @Singleton
    abstract fun bindThreatRepository(impl: ThreatRepositoryImpl): ThreatRepository
}
