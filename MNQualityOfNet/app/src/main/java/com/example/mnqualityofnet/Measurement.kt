package com.example.mnqualityofnet

import androidx.annotation.Nullable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val technology: String,
    val plmnId: String,
    val lac: Int?,
    val rac: Int?,
    val tac: Int?,
    val cellId: Long,
    val rsrp: Int?,
    val rsrq: Int?,
    val rscp: Int?,
    val eCNo: Int?
)
