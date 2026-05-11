package com.aegis.shield.di

import android.content.ContentResolver
import android.content.Context
import com.aegis.shield.data.local.SmsDataSource
import com.aegis.shield.data.ThreatDao
import com.aegis.shield.data.ThreatDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    companion object {
        @Provides
        @Singleton
        @JvmStatic
        fun provideContentResolver(@ApplicationContext context: Context): ContentResolver =
            context.contentResolver

        @Provides
        @Singleton
        @JvmStatic
        fun provideSmsDataSource(contentResolver: ContentResolver): SmsDataSource =
            SmsDataSource(contentResolver)

        @Provides
        @Singleton
        @JvmStatic
        fun provideThreatDatabase(@ApplicationContext context: Context): ThreatDatabase =
            ThreatDatabase.getInstance(context)

        @Provides
        @Singleton
        @JvmStatic
        fun provideThreatDao(database: ThreatDatabase): ThreatDao = database.threatDao()
    }
}
