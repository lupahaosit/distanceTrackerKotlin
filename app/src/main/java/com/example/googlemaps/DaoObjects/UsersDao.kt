package com.example.googlemaps.DaoObjects

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.googlemaps.entities.Users
import kotlinx.coroutines.flow.Flow


@Dao
interface UsersDao {

    @Query("SELECT * FROM users")
    fun getAllUsers() : LiveData<List<Users>>

    @Insert()
    suspend fun addUser(user: Users);

    @Update
    suspend fun updateUser(user: Users)

    @Delete
    suspend fun removeUser(user: Users)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserData(email : String) : Users

    @Query("SELECT * FROM users")
    fun getAllUsersList() : Flow<List<Users>>

}