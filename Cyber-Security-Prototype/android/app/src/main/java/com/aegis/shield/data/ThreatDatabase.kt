package com.aegis.shield.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ThreatEntity::class], version = 1, exportSchema = false)
abstract class ThreatDatabase : RoomDatabase() {
    abstract fun threatDao(): ThreatDao

    companion object {
        @Volatile private var INSTANCE: ThreatDatabase? = null

        fun getInstance(context: Context): ThreatDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ThreatDatabase::class.java,
                    "aegis_threats.db"
                ).build().also { INSTANCE = it }
            }
    }
}
