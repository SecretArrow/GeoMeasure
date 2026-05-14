package com.geomeasure.pro.core.gps

import android.content.Context
import android.location.GnssStatus
import android.location.LocationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class GpsStatus(
    val accuracyM: Float = 0f,
    val satellitesUsed: Int = 0,
    val satellitesInView: Int = 0,
    val hdop: Float = 0f,
    val altitudeM: Double = 0.0
)

@Singleton
class GpsStatusProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var listeners = mutableListOf<(GpsStatus) -> Unit>()

    private val gnssCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val usedInFix = (0 until status.satelliteCount).count { status.usedInFix(it) }
            val inView = status.satelliteCount
            listeners.forEach { it(GpsStatus(satellitesUsed = usedInFix, satellitesInView = inView)) }
        }
    }

    fun register(callback: (GpsStatus) -> Unit) {
        listeners.add(callback)
        if (listeners.size == 1) {
            locationManager.registerGnssStatusCallback(gnssCallback, null)
        }
    }

    fun unregister(callback: (GpsStatus) -> Unit) {
        listeners.remove(callback)
        if (listeners.isEmpty()) {
            locationManager.unregisterGnssStatusCallback(gnssCallback)
        }
    }
}
