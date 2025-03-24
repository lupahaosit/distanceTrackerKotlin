package com.example.googlemaps.Services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.googlemaps.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil

class LocationForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLatLng : LatLng
    private lateinit var polyline : PolylineOptions
    private var totalDistance : Double = 0.0

    private var justStarted = true
    private var map : GoogleMap = globalMap.map!!
    private var currentMarker : Marker? = null
    private var isProjectStarted = false
    private val polylinePoints = mutableListOf<Polyline>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        startLocationUpdates()
        return START_STICKY
    }

    private fun startForegroundService() {

        val channelId = "location_channel"
        val channelName = "Location Tracking"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("Location Tracking")
            .setContentText("Your location is being tracked")
            .setSmallIcon(R.drawable.free_icon_trophy_1152912)
            .build()

        startForeground(1, notification)
    }

    private fun startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations) {
                    updateLocationOnMap(location)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

    }

    private fun updateLocationOnMap(location: Location) {

//        var previousPosition: LatLng? = null
//        currentLatLng = LatLng(location.latitude, location.longitude)
//        if (justStarted) {
//            focusCamera()
//            justStarted = false
//        }
//        if (currentMarker != null) {
//            previousPosition =
//                LatLng(currentMarker?.position!!.latitude, currentMarker?.position!!.longitude)
//        }
//        currentMarker?.remove()
//        currentMarker = map.addMarker(MarkerOptions().position(currentLatLng))
//        if (isProjectStarted) {
//
//            previousPosition?.let {
//                polyline = PolylineOptions().add(previousPosition, currentLatLng)
//                polylinePoints.add(map.addPolyline(polyline))
//                totalDistance += SphericalUtil.computeDistanceBetween(
//                    previousPosition,
//                    currentLatLng
//                )
//            }
//        }
        val intent = Intent("UPDATE_LOCATION")
        intent.putExtra("latitude", location.latitude)
        intent.putExtra("longitude", location.longitude)
        intent.putExtra("totalDistance", totalDistance)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun focusCamera(){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}