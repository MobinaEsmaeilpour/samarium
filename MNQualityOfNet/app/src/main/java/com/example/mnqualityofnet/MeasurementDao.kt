package com.example.mnqualityofnet

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MeasurementDao {
    @Insert
    suspend fun insert(measurement: Measurement)

    @Query("SELECT * FROM measurements")
    fun getAllMeasurements(): LiveData<List<Measurement>>
}