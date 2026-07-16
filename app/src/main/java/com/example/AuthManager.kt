package com.example

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class AuthManager(private val auth: FirebaseAuth) {
    val currentUser get() = auth.currentUser

    fun signOut() {
        auth.signOut()
    }

    // Email/Password
    fun signInWithEmail(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess() else onError(task.exception?.message ?: "Unknown error")
            }
    }

    fun signUpWithEmail(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess() else onError(task.exception?.message ?: "Unknown error")
            }
    }

    fun signInWithCredential(credential: com.google.firebase.auth.AuthCredential, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess() else onError(task.exception?.message ?: "Unknown error")
            }
    }

    fun sendVerificationCode(phoneNumber: String, activity: Activity, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Helper to log errors
    fun logError(tag: String, message: String, exception: Exception?) {
        Log.e(tag, message, exception)
    }
}
