package com.example.googlemaps.Repositories

import com.example.googlemaps.DaoObjects.UsersDao
import com.example.googlemaps.entities.Users
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class UsersRepository(private val usersDao: UsersDao) {

    val coroutineScope = CoroutineScope(Dispatchers.IO)
    val users = usersDao.getAllUsers()

    fun addUser(user: Users){
        coroutineScope.launch {
            usersDao.addUser(user)
        }
    }

    fun getUserByEmail(email : String) : Users {
        var result = coroutineScope.async {
            usersDao.getUserData(email)
        }
        return runBlocking {
            result.await()
        }
    }

    fun getAllUsers() : List<Users>{
        var result = coroutineScope.async {
            usersDao.getAllUsersList().first()
        }
        return runBlocking {
            result.await()
        }
    }
    fun changeUserInfo(user: Users){

    }





}