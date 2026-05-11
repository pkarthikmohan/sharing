package com.aegis.shield.di

import com.aegis.shield.data.remote.TwilioApiService
import com.aegis.shield.data.remote.api.VirusTotalApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val TWILIO_BASE_URL = "https://api.twilio.com/2010-04-01/Accounts/"
    private const val VIRUSTOTAL_BASE_URL = "https://www.virustotal.com/"

    @Provides
    @Singleton
    fun provideTwilioRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(TWILIO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideTwilioApiService(retrofit: Retrofit): TwilioApiService =
        retrofit.create(TwilioApiService::class.java)

    @Provides
    @Singleton
    fun provideVirusTotalApiService(): VirusTotalApiService =
        Retrofit.Builder()
            .baseUrl(VIRUSTOTAL_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VirusTotalApiService::class.java)
}
