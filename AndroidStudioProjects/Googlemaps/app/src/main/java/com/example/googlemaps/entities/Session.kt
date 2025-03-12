package com.example.googlemaps.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.googlemaps.entities.Users
import java.util.Calendar
import java.util.Date


@Entity(tableName = "sessions",
    foreignKeys =  [ForeignKey(
        Users::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )])
class Session {

    @PrimaryKey(autoGenerate = true)
    var id : Long = 0

    var startedAt : Date? = Calendar.getInstance().time

    var endAt : Date? = null

    var distance : Int = 0
    @ColumnInfo(index = true)
    var userId : Long ?= null
    constructor()
    constructor(userId : Long){
        this.userId = userId
    }

}