package com.example.googlemaps.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.googlemaps.entities.Users
import com.google.android.gms.maps.model.LatLng
import java.util.Calendar
import java.util.Date


@Entity(tableName = "sessions",
    foreignKeys =  [ForeignKey(
        Users::class,
        parentColumns = ["email"],
        childColumns = ["userEmail"],
        onDelete = ForeignKey.CASCADE
    )])
class Session {

    @PrimaryKey(autoGenerate = true)
    var id : Long = 0

    var startedAt : Date? = Calendar.getInstance().time

    var endAt : Date? = null

    var distance : Int = 0
    @ColumnInfo(index = true)
    var userEmail : String ?= null

    @Ignore
    var sessionPoints : List<LatLng> ?= null;
    constructor()

    constructor(userEmail : String){
        this.userEmail = userEmail
    }

    constructor(startedAt : Date, distance: Int, userEmail: String, endAt : Date){
        this.startedAt = startedAt
        this.distance = distance
        this.userEmail = userEmail
        this.endAt = endAt
    }
}