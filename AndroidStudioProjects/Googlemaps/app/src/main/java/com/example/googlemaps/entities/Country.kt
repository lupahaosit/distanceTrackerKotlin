package com.example.googlemaps.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countries")
class Country(@ColumnInfo(name = "country_name") var name: String) {


    @PrimaryKey(autoGenerate = true)
    var id : Long = 0

    override fun toString(): String {
        return name;
    }

}