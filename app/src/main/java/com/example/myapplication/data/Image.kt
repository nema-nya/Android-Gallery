package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class Image(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val imagePath: String,
    val longitude: Double,
    val latitude: Double,
    val description: String
)
