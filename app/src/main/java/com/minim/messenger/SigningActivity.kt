package com.minim.messenger

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.signing_activity.*
import java.util.concurrent.TimeUnit

class SigningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.signing_activity)

        sendVerificationCodeButton.setOnClickListener {
            if (!phoneNumberEditText.text.isNullOrEmpty()) {
                val number = phoneNumberEditText.text.toString()
                PhoneAuthProvider.getInstance().verifyPhoneNumber(number, 60, TimeUnit.SECONDS, this, callbacks)
            } else {
                phoneNumberEditText.error = "Required Field."
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
            val intent = Intent(this@SigningActivity, VerificationActivity::class.java)
                .putExtra("verificationId", verificationId)
            startActivity(intent)
            finish()
        }

        override fun onVerificationFailed(e: FirebaseException) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                phoneNumberEditText.error = "Invalid phone number."
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Toast.makeText(this@SigningActivity, "Try again Later", Toast.LENGTH_LONG).show()
            }
        }

    }

    companion object {
        fun signIn(activity: Activity, credential: PhoneAuthCredential) {
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                val destination = if (it.result?.additionalUserInfo!!.isNewUser) {
                    SettingsActivity::class.java
                } else {
                    MainActivity::class.java
                }
                val intent = Intent(activity, destination)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
                activity.finish()
            }
        }
    }
}
