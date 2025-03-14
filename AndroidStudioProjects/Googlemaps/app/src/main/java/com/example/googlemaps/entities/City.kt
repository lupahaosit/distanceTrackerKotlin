package com.example.googlemaps.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "cities",
    foreignKeys = [ForeignKey(
        Country::class,
        parentColumns = ["id"],
        childColumns = ["country_id"],
        onDelete = ForeignKey.CASCADE
    )])
class City() {

    @PrimaryKey
    var id : Long = 0

    @ColumnInfo(name = "city_name")
    var name : String? = null

    @ColumnInfo(name = "country_id", index = true)
    var countryId : Long? = null

    constructor(id : Long, name: String, countryId: Long) : this() { // Вызываем пустой конструктор
        this.id = id
        this.name = name
        this.countryId = countryId
    }

    override fun toString(): String {
        return name!!;
    }
}