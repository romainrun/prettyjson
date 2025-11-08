package com.prettyjson.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import com.prettyjson.android.di.appModule

class PrettyJSONApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin for dependency injection
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@PrettyJSONApplication)
            modules(appModule)
        }
    }
}




