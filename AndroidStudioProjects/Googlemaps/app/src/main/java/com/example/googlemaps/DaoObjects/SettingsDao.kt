package com.example.googlemaps.DaoObjects

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.googlemaps.entities.Settings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings")
    fun getAllSettings() : LiveData<List<Settings>>

    @Query("SELECT * FROM settings WHERE userId = :id LIMIT 1")
    fun getUsersSettings(id : Long) : Flow<Settings>

    @Update
    suspend fun updateSettings(settings: Settings)

    @Insert
    suspend fun addSettings(settings: Settings)
}