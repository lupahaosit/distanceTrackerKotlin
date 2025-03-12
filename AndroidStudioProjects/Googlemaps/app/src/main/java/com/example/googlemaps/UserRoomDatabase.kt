package com.example.googlemaps

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.googlemaps.DaoObjects.CityDao
import com.example.googlemaps.DaoObjects.CountryDao
import com.example.googlemaps.DaoObjects.SessionDao
import com.example.googlemaps.DaoObjects.SettingsDao
import com.example.googlemaps.DaoObjects.UsersDao
import com.example.googlemaps.entities.City
import com.example.googlemaps.entities.Country
import com.example.googlemaps.entities.Session
import com.example.googlemaps.entities.Settings
import com.example.googlemaps.entities.Users


@Database(entities = [(Users::class), City::class,
Country::class, Session::class, Settings::class], version = 26, exportSchema = false)
@TypeConverters(Converters::class)
abstract class UserRoomDatabase: RoomDatabase() {


    abstract fun UsersDao(): UsersDao
    abstract fun CityDao(): CityDao
    abstract fun SessionDao() : SessionDao
    abstract fun SettingsDao() : SettingsDao
    abstract fun CountryDao() : CountryDao

    // реализуем синглтон
    companion object {
        private var INSTANCE: UserRoomDatabase? = null
        fun getInstance(context: Context): UserRoomDatabase {

            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        UserRoomDatabase::class.java,
                        "User_database"

                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}