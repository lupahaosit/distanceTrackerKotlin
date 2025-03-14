package com.example.googlemaps.DaoObjects

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.googlemaps.entities.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions")
    fun getAllSessions() : LiveData<List<Session>>

    @Query("SELECT * FROM sessions WHERE userEmail = :email")
    fun getUsersSessions(email: String) : Flow<List<Session>>

    @Insert
    suspend fun addSession(session: Session)

    @Query("SELECT * FROM sessions")
    fun getAllSessionsList() : Flow<List<Session>>

}