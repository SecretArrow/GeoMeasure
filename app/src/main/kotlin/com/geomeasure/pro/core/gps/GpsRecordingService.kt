package com.geomeasure.pro.core.gps

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.geomeasure.pro.R
import com.geomeasure.pro.data.local.prefs.AppPreferences
import com.geomeasure.pro.domain.repository.MeasurementRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GpsRecordingService : Service() {

    @Inject lateinit var locationManager: LocationManager
    @Inject lateinit var repository: MeasurementRepository
    @Inject lateinit var prefs: AppPreferences

    private val binder = LocalBinder()
    private var projectId = ""
    private var pointCount = 0
    private var lastLat = 0.0
    private var lastLon = 0.0

    inner class LocalBinder : Binder() {
        fun getService() = this@GpsRecordingService
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val settings = prefs.getSettingsBlocking()
            if (location.accuracy > settings.gpsAccuracyThreshold) return
            val dist = if (pointCount > 0) FloatArray(1).also {
                Location.distanceBetween(lastLat, lastLon, location.latitude, location.longitude, it)
            }[0] else Float.MAX_VALUE
            if (dist < settings.gpsMinDistance) return
            lastLat = location.latitude
            lastLon = location.longitude
            pointCount++
            updateNotification()
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        projectId = intent?.getStringExtra("project_id") ?: return START_NOT_STICKY
        startForeground(NOTIF_ID, buildNotification("Recording GPS"))
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            prefs.getSettingsBlocking().gpsIntervalMs,
            prefs.getSettingsBlocking().gpsMinDistance,
            locationListener
        )
        return START_STICKY
    }

    override fun onDestroy() {
        locationManager.removeUpdates(locationListener)
        super.onDestroy()
    }

    private fun updateNotification() {
        val notification = buildNotification("$pointCount points recorded")
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIF_ID, notification)
    }

    private fun buildNotification(text: String): android.app.Notification {
        val stopIntent = Intent(this, GpsRecordingService::class.java).apply {
            action = "STOP"
        }
        val stopPendingIntent = android.app.PendingIntent.getService(
            this, 0, stopIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GeoMeasure Pro")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "gps_recording"
        const val NOTIF_ID = 1001
    }
}
