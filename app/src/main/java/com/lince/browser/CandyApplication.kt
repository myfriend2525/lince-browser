package com.lince.browser

import android.app.Application
import com.lince.browser.data.AppLogging

class CandyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (getProcessName() == packageName) AppLogging.initialize(this)
    }
}
