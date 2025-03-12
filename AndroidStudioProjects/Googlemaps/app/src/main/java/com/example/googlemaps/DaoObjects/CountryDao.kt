package com.example.googlemaps.DaoObjects

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.googlemaps.entities.Country
import kotlinx.coroutines.flow.Flow


@Dao
interface CountryDao {

    @Query("SELECT * FROM countries")
    fun getAllCountries() : LiveData<List<Country>>

    @Insert
    fun addCountry(country: Country)

    @Update
    fun updateCountry(country: Country)

    @Delete
    fun removeCountry(country: Country)

    @Query("SELECT * FROM countries")
    fun getAllCountriesList(): Flow<List<Country>>

    @Query("SELECT * FROM countries WHERE country_name = :countryName LIMIT 1")
    fun getCountryByName(countryName : String) : Flow<Country>

    @Query("DELETE FROM countries")
    fun clearCountriesTable() : Int
}