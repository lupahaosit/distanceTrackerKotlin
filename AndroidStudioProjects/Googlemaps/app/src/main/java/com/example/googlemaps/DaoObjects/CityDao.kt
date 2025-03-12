package com.example.googlemaps.DaoObjects

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.googlemaps.entities.City
import com.example.googlemaps.entities.Country
import kotlinx.coroutines.flow.Flow


@Dao
interface CityDao {

    @Query("SELECT * FROM cities")
    fun getAllCities() : LiveData<List<City>>

    @Update
    suspend fun updateCity(city : City)

    @Insert
    suspend fun addCity(city : City)

    @Delete
    suspend fun removeCity(city: City)

    @Query("SELECT * FROM cities WHERE country_id = :countryId")
    fun getCitiesByCountry(countryId : Long) : Flow<List<City>>

    @Query("SELECT * FROM cities")
    fun getAllCitiesList() : Flow<List<City>>

    @Query ("SELECT * FROM cities WHERE city_name = :name")
    fun getCity(){

    }


}