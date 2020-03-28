package com.niks.filelog

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LogDWO::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun logDWODao(): LogDWODao

    companion object {
        const val DATABASE_NAME = "logs_db"
    }
}