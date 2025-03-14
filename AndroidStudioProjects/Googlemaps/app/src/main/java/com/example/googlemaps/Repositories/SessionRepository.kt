package com.example.googlemaps.Repositories

import com.example.googlemaps.DaoObjects.SessionDao
import com.example.googlemaps.entities.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SessionRepository (private val sessionDao: SessionDao) {

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    val sessions = sessionDao.getAllSessions()

    fun getUsersSessions(email : String) : List<Session>{
        var result = coroutineScope.async {
            sessionDao.getUsersSessions(email).first()
        }
        return runBlocking {
            result.await()
        }
    }

    fun addSession(session : Session){
        coroutineScope.launch {
            sessionDao.addSession(session)
        }
    }

    fun getAllSessions() : List<Session>{
        var result = coroutineScope.async {
            sessionDao.getAllSessionsList().first()
        }

        return runBlocking {
            result.await()
        }
    }


}