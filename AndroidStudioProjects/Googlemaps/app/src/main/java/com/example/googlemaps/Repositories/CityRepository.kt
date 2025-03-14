package com.example.googlemaps.Repositories

import android.util.Log
import androidx.room.Insert
import androidx.room.Query
import com.example.googlemaps.DaoObjects.CityDao
import com.example.googlemaps.entities.City
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CityRepository(private val cityDao : CityDao) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    var cityList = cityDao.getAllCities()

    fun addCity(city: City){
        Log.d("CityRepository", "Добавление города: ${city.name} (${city.countryId})")
        coroutineScope.launch(Dispatchers.IO) {
            cityDao.addCity(city)
            Log.d("CityRepository", "Город ${city.name} добавлен в БД")
        }
    }

    fun getAllCities() : Flow<List<City>> {
        return cityDao.getAllCitiesList()
    }

    fun getAllCitiesByCountry(countryId : Long) : List<City>{
        var result = coroutineScope.async {
            cityDao.getCitiesByCountry(countryId).first()
        }
        return runBlocking {
            result.await()
        }
    }

    fun getAllCitiesList() : List<City>{
        var result = coroutineScope.async {
            cityDao.getAllCitiesList().first()
        }
        return runBlocking {
            result.await()
        }
    }



}