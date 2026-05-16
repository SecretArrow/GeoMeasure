package com.geomeasure.pro.core.gps

import android.content.Context
import android.location.GnssStatus
import android.location.LocationManager
import com.geomeasure.pro.domain.model.GpsStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.math.sqrt
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpsStatusProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val listeners = CopyOnWriteArrayList<(GpsStatus) -> Unit>()

    private val gnssCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val usedInFix = (0 until status.satelliteCount).count { status.usedInFix(it) }
            val inView = status.satelliteCount
            val lastLoc = try {
                locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (_: SecurityException) { null }
            val hdop = lastLoc?.extras?.getFloat("hdop") ?:
                if (usedInFix > 0) (1.5f / sqrt(usedInFix.toFloat())) else 0f
            listeners.forEach {
                it(GpsStatus(
                    satellitesUsed = usedInFix,
                    satellitesInView = inView,
                    accuracyM = lastLoc?.accuracy ?: 0f,
                    altitudeM = lastLoc?.altitude ?: 0.0,
                    hdop = hdop
                ))
            }
        }
    }

    fun register(callback: (GpsStatus) -> Unit) {
        listeners.add(callback)
        if (listeners.size == 1) {
            locationManager?.registerGnssStatusCallback(gnssCallback, null)
        }
    }

    fun unregister(callback: (GpsStatus) -> Unit) {
        listeners.remove(callback)
        if (listeners.isEmpty()) {
            locationManager?.unregisterGnssStatusCallback(gnssCallback)
        }
    }
}
