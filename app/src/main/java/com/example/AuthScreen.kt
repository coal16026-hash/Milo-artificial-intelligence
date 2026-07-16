package com.example

import android.widget.Toast
import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.FirebaseException
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val authManager = remember { AuthManager(FirebaseAuth.getInstance()) }
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Authentication", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = {
                coroutineScope.launch {
                    try {
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId("YOUR_WEB_CLIENT_ID") // Needs to be configured correctly!
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        val result = credentialManager.getCredential(context as Activity, request)
                        val credential = result.credential
                        
                        if (credential is GoogleIdTokenCredential) {
                            val authCredential = com.google.firebase.auth.GoogleAuthProvider.getCredential(credential.idToken, null)
                            authManager.signInWithCredential(authCredential, { onAuthSuccess() }, { error -> Toast.makeText(context, "Google Sign-In Failed: $error", Toast.LENGTH_SHORT).show() })
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Google Sign-In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Sign in with Google")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                isLoading = true
                authManager.signInWithEmail(email, password, {
                    isLoading = false
                    onAuthSuccess()
                }, { error ->
                    isLoading = false
                    Toast.makeText(context, "Sign In Failed: $error", Toast.LENGTH_SHORT).show()
                })
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Sign In with Email")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(onClick = {
                isLoading = true
                val activity = context as? Activity
                if (activity != null) {
                    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                            authManager.signInWithCredential(credential, {
                                isLoading = false
                                onAuthSuccess()
                            }, { error ->
                                isLoading = false
                                Toast.makeText(context, "Phone Sign-In Failed: $error", Toast.LENGTH_SHORT).show()
                            })
                        }
                        override fun onVerificationFailed(e: FirebaseException) {
                            isLoading = false
                            Toast.makeText(context, "Phone Auth Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                            isLoading = false
                            Toast.makeText(context, "Code Sent", Toast.LENGTH_SHORT).show()
                        }
                    }
                    authManager.sendVerificationCode(phoneNumber, activity, callbacks)
                } else {
                    isLoading = false
                    Toast.makeText(context, "Phone Auth requires an Activity", Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Sign In with Phone")
            }
        }
    }
}
