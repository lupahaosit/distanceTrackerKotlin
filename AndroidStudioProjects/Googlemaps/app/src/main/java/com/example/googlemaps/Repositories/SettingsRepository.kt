package com.example.googlemaps.Repositories

import com.example.googlemaps.DaoObjects.SettingsDao
import com.example.googlemaps.entities.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingsRepository(private val settingsDao : SettingsDao)  {

    val coroutineScope = CoroutineScope(Dispatchers.IO)
    val settings = settingsDao.getAllSettings()

    fun getUsersSettings(userId : Long) : Settings {
        var settings = coroutineScope.async {
            settingsDao.getUsersSettings(userId).first()
        }

        return runBlocking {
            settings.await()
        }
    }


    fun setUsersSettings(settings: Settings){
        coroutineScope.launch {
            settingsDao.addSettings(settings)
        }
    }

    fun updateUserSettings(settings: Settings){
        coroutineScope.launch {
            settingsDao.updateSettings(settings)
        }
    }


}