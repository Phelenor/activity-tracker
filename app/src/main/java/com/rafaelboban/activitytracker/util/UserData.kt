package com.rafaelboban.activitytracker.util

import com.rafaelboban.activitytracker.model.User

object UserData {

    var user: User? = null

    fun requireUser() = checkNotNull(user)
}
