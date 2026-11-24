package com.nurra.islamic.viewmodel

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nurra.islamic.model.LocationInfo
import com.nurra.islamic.model.UserLocation
import com.nurra.islamic.repository.LocalLocationRepository
import com.nurra.islamic.repository.LocationRepository
import com.nurra.islamic.util.QiblaCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

data class QiblaUiState(
    val location: LocationInfo = LocationInfo("Jakarta", "Indonesia", -6.2088, 106.8456),
    val qiblaBearing: Float = 295.2f,
    val deviceAzimuth: Float = 0f,
    val relativeQiblaAngle: Float = 295.2f,
    val isAligned: Boolean = false,
    val instructionText: String = "Arahkan HP sesuai penanda Kiblat",
    val isSensorAvailable: Boolean = true
)

class QiblaViewModel(
    application: Application
) : AndroidViewModel(application), SensorEventListener {

    private val locationRepository: LocationRepository = LocalLocationRepository(application.applicationContext)

    private val sensorManager = application.getSystemService(Application.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)

    private var smoothedAzimuth = 0f
    private val alpha = 0.15f // Low-pass filter smoothing coefficient

    init {
        // Observe active location changes from LocationRepository & recalculate Qibla bearing
        viewModelScope.launch {
            locationRepository.getActiveLocation().collect { userLoc ->
                val bearing = QiblaCalculator.calculateQiblaBearing(userLoc.latitude, userLoc.longitude)
                val locInfo = LocationInfo(
                    cityName = userLoc.cityName,
                    countryName = userLoc.countryName,
                    latitude = userLoc.latitude,
                    longitude = userLoc.longitude
                )
                _uiState.value = _uiState.value.copy(
                    location = locInfo,
                    qiblaBearing = bearing,
                    relativeQiblaAngle = (bearing - _uiState.value.deviceAzimuth + 360f) % 360f
                )
            }
        }
    }

    fun startListening() {
        val hasRotation = rotationSensor != null
        val hasAccelMag = accelerometer != null && magnetometer != null

        if (!hasRotation && !hasAccelMag) {
            _uiState.value = _uiState.value.copy(isSensorAvailable = false)
            return
        }

        _uiState.value = _uiState.value.copy(isSensorAvailable = true)

        if (hasRotation) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
        } else {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        var rawAzimuth = 0f

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            rawAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, 3)
            calculateAccelMagAzimuth()?.let { rawAzimuth = it }
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, 3)
            calculateAccelMagAzimuth()?.let { rawAzimuth = it }
        }

        if (rawAzimuth < 0) {
            rawAzimuth += 360f
        }

        // Apply low-pass exponential smoothing filter
        val diff = ((rawAzimuth - smoothedAzimuth + 540f) % 360f) - 180f
        smoothedAzimuth = (smoothedAzimuth + alpha * diff + 360f) % 360f

        val qiblaBearing = _uiState.value.qiblaBearing
        val relativeAngle = (qiblaBearing - smoothedAzimuth + 360f) % 360f

        // Angle delta (-180 to +180)
        val angleDelta = ((relativeAngle + 180f) % 360f) - 180f
        val isAligned = abs(angleDelta) <= 3.5f

        val instruction = when {
            isAligned -> "Kamu Sudah Menghadap Kiblat! 🕋"
            angleDelta < 0 -> "Putar HP ke kiri ${abs(angleDelta.toInt())}°"
            else -> "Putar HP ke kanan ${abs(angleDelta.toInt())}°"
        }

        _uiState.value = _uiState.value.copy(
            deviceAzimuth = smoothedAzimuth,
            relativeQiblaAngle = relativeAngle,
            isAligned = isAligned,
            instructionText = instruction
        )
    }

    private fun calculateAccelMagAzimuth(): Float? {
        val R = FloatArray(9)
        val I = FloatArray(9)
        val success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)
        if (success) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)
            var az = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (az < 0) az += 360f
            return az
        }
        return null
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Reserved
    }
}
