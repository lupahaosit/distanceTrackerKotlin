package com.example.googlemaps.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countries")
class Country {

    @ColumnInfo(name = "country_name")
    var name: String? = null
    @PrimaryKey
    var id : Long = 0

    constructor()

    constructor(id : Long, name : String){
        this.id = id
        this.name = name
    }


    override fun toString(): String {
        return name!!;
    }

}