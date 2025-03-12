package com.example.googlemaps.Repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.googlemaps.DaoObjects.CountryDao
import com.example.googlemaps.entities.Country
import com.example.googlemaps.entities.Users
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CountryRepository(private val countryDao : CountryDao) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)


    val countryList: LiveData<List<Country>> = countryDao.getAllCountries()


    fun addCountry(country: Country){
        coroutineScope.launch(Dispatchers.IO) {
            countryDao.addCountry(country)
        }
    }
    fun getAllCountries(): Flow<List<Country>> {
        return countryDao.getAllCountriesList()
    }

    fun cleanData() : Int {
        return countryDao.clearCountriesTable()
    }

    fun changeData(country: Country){
        var item = countryDao.updateCountry(country)
    }

    fun getCountryByName(countryName: String) : Flow<Country>{
        val item = countryDao.getCountryByName(countryName)
        return item
    }

}