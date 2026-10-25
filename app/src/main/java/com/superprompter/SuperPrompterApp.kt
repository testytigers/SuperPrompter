package com.superprompter

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SuperPrompterApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
