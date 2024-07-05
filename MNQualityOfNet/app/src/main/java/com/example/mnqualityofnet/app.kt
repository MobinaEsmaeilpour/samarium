package com.example.mnqualityofnet

import android.app.Application
import androidx.room.Room

class App : Application() {
    private lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "measurements_database"
        ).build()
    }
}
