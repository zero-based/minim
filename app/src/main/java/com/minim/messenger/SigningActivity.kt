package com.minim.messenger

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
                startPhoneNumberVerification(phoneNumberEditText.text.toString())
            } else {
                phoneNumberEditText.error = "Required Field."
            }
        }
    }


    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this@SigningActivity, // Activity (for callback binding)
            callbacks
        )
    }


    private val callbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(ContentValues.TAG, "onVerificationCompleted:$credential")
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                    if (it.result?.additionalUserInfo!!.isNewUser) {
                        val mainIntent = Intent(this@SigningActivity, SettingsActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(mainIntent)
                        finish()
                    } else {
                        val mainIntent = Intent(this@SigningActivity, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(mainIntent)
                        finish()
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(ContentValues.TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    phoneNumberEditText.error = "Invalid phone number."
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(applicationContext, "Try again Later", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(ContentValues.TAG, "onCodeSent:" + verificationId!!)

                // Save verification ID and resending token so we can use them later
                val i = Intent(this@SigningActivity, VerificationActivity::class.java)
                    .putExtra("verificationId", verificationId)
                startActivity(i)
                finish()
            }
        }
}
