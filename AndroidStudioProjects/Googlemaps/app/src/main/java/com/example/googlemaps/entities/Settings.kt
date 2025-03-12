package com.example.googlemaps.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [
    ForeignKey(
        entity = Users::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE

    )])
class Settings {

    @PrimaryKey(autoGenerate = true)
    var id : Long = 0

    @ColumnInfo(name = "distant_unit")
    var distanceUnit : String? = null

    @ColumnInfo(index = true)
    var userId : Long ?= null

    constructor(unit : String, userId : Long){
        this.distanceUnit = unit
        this.userId = userId
    }
    constructor()

    override fun toString(): String {
        return distanceUnit!!
    }
}