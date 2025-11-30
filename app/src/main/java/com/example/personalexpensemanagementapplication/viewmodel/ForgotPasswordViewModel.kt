package com.example.personalexpensemanagementapplication.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit

data class ForgotPasswordState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCodeSent: Boolean = false,
    val verificationId: String? = null
)

class ForgotPasswordViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state = _state.asStateFlow()

    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        _state.update { it.copy(isLoading = true, error = null) }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback is triggered in case of instant verification or auto-retrieval.
                // For this flow, we will handle the logic on the next screen.
                _state.update { it.copy(isLoading = false) }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent. Save the verification ID
                // and navigate to the screen to enter the code.
                _state.update {
                    it.copy(
                        isLoading = false,
                        isCodeSent = true,
                        verificationId = verificationId
                    )
                }
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)             // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun resetState() {
        _state.update { ForgotPasswordState() }
    }
}