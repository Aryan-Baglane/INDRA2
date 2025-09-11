package com.example.indra



import android.app.Application
import com.mappls.sdk.maps.Mappls
import android.util.Log

class IndraApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Mappls SDK
        try {
            Mappls.getInstance(this)
        } catch (e: IllegalStateException) {
            Log.e("IndraApp", "Mappls init failed: ${e.message}")
        } catch (e: Exception) {
            Log.e("IndraApp", "Mappls init unexpected error", e)
        }
    }
}