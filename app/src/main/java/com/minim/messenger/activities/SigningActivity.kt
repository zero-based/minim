package com.minim.messenger.activities

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.minim.messenger.R
import com.minim.messenger.util.Navigation
import kotlinx.android.synthetic.main.activity_signing.*
import java.util.concurrent.TimeUnit

class SigningActivity : AppCompatActivity() {

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().currentUser ?: return
        Navigation.start(this, ConversationsActivity::class.java, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signing)

        send_verification_code_button.setOnClickListener {
            if (!phone_number_edit_text.text.isNullOrEmpty()) {
                val number = phone_number_edit_text.text.toString()
                PhoneAuthProvider.getInstance().verifyPhoneNumber(number, 60, TimeUnit.SECONDS, this, callbacks)
            } else {
                phone_number_edit_text.error = "Required Field."
            }
        }

    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Instant verification or Auto-retrieval is done
            signIn(this@SigningActivity, credential)
        }

        override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken) {
            // Save verification ID and resending token so we can use them later
            Navigation.start(
                this@SigningActivity,
                VerificationActivity::class.java,
                stringExtra = "verificationId" to verificationId
            )
        }

        override fun onVerificationFailed(e: FirebaseException) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                phone_number_edit_text.error = "Invalid phone number."
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Toast.makeText(this@SigningActivity, "Try again Later", Toast.LENGTH_LONG).show()
            }
        }

    }

    companion object {

        fun signIn(activity: Activity, credential: PhoneAuthCredential) {
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                val isNewUser = it.result?.additionalUserInfo!!.isNewUser
                val destination = if (isNewUser) {
                    ProfileActivity::class.java
                } else {
                    ConversationsActivity::class.java
                }
                Navigation.start(activity, destination, true)
            }
        }

    }
}
