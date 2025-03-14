package com.example.googlemaps.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date

@Entity(tableName = "users",
    foreignKeys = [ForeignKey(
        entity = City::class,
        parentColumns = ["id"],
        childColumns = ["cityId"],
        onDelete = ForeignKey.CASCADE
    )])
class Users {
    var name: String? = null
    @PrimaryKey
    var email : String = ""

    var password : String? = null

    var createdAt : Date? = Calendar.getInstance().time

    @ColumnInfo(index = true)
    var cityId : Long = 1

    constructor(email: String, password : String, name : String, cityId : Long){
        this.email = email
        this.name = name
        this.password = password
        this.cityId = cityId
    }

    constructor(email: String, password : String, name : String, cityId : Long, createdAt: Date){
        this.email = email
        this.name = name
        this.password = password
        this.cityId = cityId
        this.createdAt = createdAt

    }
    constructor()



}