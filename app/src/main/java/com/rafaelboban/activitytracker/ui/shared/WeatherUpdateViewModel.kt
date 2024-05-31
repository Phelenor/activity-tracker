package com.rafaelboban.activitytracker.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.activitytracker.model.location.Location
import com.rafaelboban.activitytracker.network.model.weather.WeatherForecast
import com.rafaelboban.activitytracker.network.repository.WeatherRepository
import com.rafaelboban.core.shared.utils.F
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
open class WeatherUpdateViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private var weatherUpdateJob: Job? = null

    private lateinit var locationGetter: () -> Location?
    private lateinit var onUpdateCallback: (WeatherForecast?) -> Unit
    private lateinit var onLoadingCallback: (Boolean) -> Unit

    fun startWeatherUpdates(
        currentLocationGetter: () -> Location?,
        onUpdate: (WeatherForecast?) -> Unit,
        onLoading: (Boolean) -> Unit
    ) {
        locationGetter = currentLocationGetter
        onUpdateCallback = onUpdate
        onLoadingCallback = onLoading

        onUpdate(null)

        weatherUpdateJob?.cancel()
        weatherUpdateJob = viewModelScope.launch {
            var canRetry = true
            var shouldRetry = false

            while (isActive) {
                var location = currentLocationGetter()
                var lockCount = 0

                while (location == null) {
                    delay(1.seconds * (lockCount + 1))
                    location = currentLocationGetter()
                    lockCount++
                }

                onLoading(true)

                weatherRepository.getWeatherData(location.latitude.F, location.longitude.F).onSuccess {
                    onUpdate(data.copy(hourly = data.hourly.take(5)))
                    onLoading(false)
                    canRetry = true
                }.onFailure {
                    onLoading(false)
                    shouldRetry = canRetry
                    canRetry = false
                }

                if (shouldRetry) {
                    shouldRetry = false
                    continue
                }

                delay(15.minutes)
            }
        }
    }

    fun restartWeatherUpdates() {
        startWeatherUpdates(locationGetter, onUpdateCallback, onLoadingCallback)
    }
}
