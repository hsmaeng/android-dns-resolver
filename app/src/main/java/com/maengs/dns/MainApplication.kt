package com.maengs.dns

import android.app.Application
import com.maengs.dns.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androix.startup.KoinStartup.onKoinStartup

class MainApplication : Application() {
    init {
        onKoinStartup {
            androidLogger()
            androidContext(this@MainApplication)
            modules(appModule)
        }
    }
}
