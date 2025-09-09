package com.example.indra



import android.app.Application
import com.mappls.sdk.maps.Mappls

class IndraApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Mappls SDK
        Mappls.getInstance(this)
    }
}