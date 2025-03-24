package com.example.googlemaps.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [
    ForeignKey(
        entity = Users::class,
        parentColumns = ["email"],
        childColumns = ["userEmail"],
        onDelete = ForeignKey.CASCADE

    )])
class Settings {

    @PrimaryKey(autoGenerate = true)
    var id : Long = 0

    @ColumnInfo(name = "distant_unit")
    var distanceUnit : String? = null

    @ColumnInfo(index = true)
    var userEmail : String ?= null

    constructor(unit : String, userEmail : String){
        this.distanceUnit = unit
        this.userEmail = userEmail
    }
    constructor()

    override fun toString(): String {
        return distanceUnit!!
    }
}