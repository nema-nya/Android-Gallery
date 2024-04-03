package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImageDao {
    @Insert
    suspend fun insert(image: Image)


    @Query("SELECT * FROM images")
    fun getAllImagesLiveData(): LiveData<List<Image>>

    @Query("DELETE FROM images WHERE id = :id")
    suspend fun deleteImageById(id: Int)

    @Query("SELECT * FROM images ORDER BY ID DESC")
    fun getAllImages(): List<Image>
}

