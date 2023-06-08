package com.biryulindevelop.smartalarm.domain

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

class LastLocationProvider(private val context: Context) {

     fun getLocation(callback: (Result<Location>) -> Unit) {
        if (!hasLocationPermission()) {
            callback(Result.failure(Exception("Location permission not granted.")))
            return
        }
        val lastLocationTask = getLocationTask()
        lastLocationTask.addOnCompleteListener { task ->
            val location = task.getLastLocation()
            if (location != null) {
                callback(Result.success(location))
            } else {
                callback(Result.failure(Exception("Location not found.")))
            }
        }
    }

    @SuppressLint("MissingPermission") //permission is checked
    private fun getLocationTask(): Task<Location> {
        return LocationServices
            .getFusedLocationProviderClient(context)
            .lastLocation
    }


     private fun Task<Location>.getLastLocation(): Location? {
        return if (isSuccessful && result != null) {
            result!!
        } else {
            null
        }
    }

    private fun hasLocationPermission(): Boolean {
        return checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PermissionChecker.PERMISSION_GRANTED
    }
}