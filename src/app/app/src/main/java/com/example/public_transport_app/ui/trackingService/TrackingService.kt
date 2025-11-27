package com.example.public_transport_app.ui.trackingService

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.HandlerThread
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.public_transport_app.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.core.net.toUri
import com.example.public_transport_app.data.repository.RouteRepository
import com.example.public_transport_app.utils.SensorDataProvider
import com.example.public_transport_app.utils.accuracyThresholdMeters
import com.example.public_transport_app.utils.emitLowAccuracyAfterMs
import com.example.public_transport_app.utils.trackingTimeInterval
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : Service() {

    enum class RunState {
        RUNNING, PAUSED, STOPPED
    }

    @Inject
    lateinit var routeRepository: RouteRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var locationThread: HandlerThread
    private var locationUpdatesJob: Job? = null

    private var routeId: String? = null
    private var runId: String? = null

    private val _currentState = MutableStateFlow(RunState.STOPPED)

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    companion object {
        const val CHANNEL_ID = "run_tracking_channel"
        const val NOTIFICATION_ID = 1
        const val ROUTE_ID = "route_id"
        const val RUN_ID = "run_id"
    }

    override fun onCreate() {
        super.onCreate()
        locationThread = HandlerThread("LocationThread").apply { start() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        routeId = intent?.getStringExtra(ROUTE_ID)
        runId = intent?.getStringExtra(RUN_ID)
        startForegroundService()
        return START_STICKY
    }

    private fun startForegroundService() {
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        _currentState.value = RunState.RUNNING
        observeTrackingState()
    }

    private fun observeTrackingState() {
        locationUpdatesJob?.cancel()
        locationUpdatesJob = serviceScope.launch {
            _currentState.collectLatest { state ->

                if (state == RunState.RUNNING) {
                    // Comenzar a escuchar datos de ubicación en flujo constante
                    val flow = SensorDataProvider.getSensorDataFlow(
                        applicationContext,
                        intervalMs = trackingTimeInterval,
                        accuracyThresholdMeters = accuracyThresholdMeters,
                        emitLowAccuracyAfterMs = emitLowAccuracyAfterMs,
                        looper = locationThread.looper,
                    )
                    flow.collect { data ->

                        if (runId == null) return@collect
                        routeRepository.addRunPoint(
                            runId = runId!!,
                            latLng = LatLng(data.latitude, data.longitude),
                            speed = data.speedMps,
                            time = data.timestamp,
                            accuracy = data.accuracy
                        )
                    }
                } else {
                    // Pausado o detenido: no recolectar ubicación
                }
            }
        }
    }


    override fun onDestroy() {
        _currentState.value = RunState.STOPPED
        locationUpdatesJob?.cancel()
        serviceScope.cancel()
        locationThread.quitSafely()
        super.onDestroy()
    }

    // Métodos públicos accesibles desde binder

    fun getRouteId(): String? = routeId
    fun getRunId(): String? = runId
    fun getCurrentState(): RunState = _currentState.value

    fun pauseTracking() {
        _currentState.value = RunState.PAUSED
    }

    fun resumeTracking() {
        if (_currentState.value != RunState.RUNNING) {
            _currentState.value = RunState.RUNNING
            // observeTrackingState se encarga de reiniciar la colección cuando cambie el estado
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Seguimiento de viaje",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val deepLinkUri = "publictransport://route/$routeId".toUri()
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            `package` = packageName
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.tracking_service_title))
            .setContentText(getString(R.string.tracking_service_description))
            .setSmallIcon(R.drawable.logo_bus)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
