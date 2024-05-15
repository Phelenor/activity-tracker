package com.rafaelboban.activitytracker.model.ui

import androidx.compose.ui.graphics.Color
import com.rafaelboban.activitytracker.model.location.Location

data class PolylineUi(
    val location1: Location,
    val location2: Location,
    val color: Color
)
