package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            // If default initialization fails (likely missing google-services.json),
            // initialize with placeholder values to prevent startup crashes.
            try {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:1234567890:android:1234567890")
                    .setApiKey("placeholder-api-key")
                    .setProjectId("placeholder-project-id")
                    .build()
                FirebaseApp.initializeApp(this, options)
            } catch (e2: Exception) {
                // Should not happen if options are valid
            }
        }
    }
}
