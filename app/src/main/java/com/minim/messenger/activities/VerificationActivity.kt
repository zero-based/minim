package com.minim.messenger.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.PhoneAuthProvider
import com.minim.messenger.R
import kotlinx.android.synthetic.main.activity_verification.*

class VerificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        verifyButton.setOnClickListener {
            val verificationId = intent.getStringExtra("verificationId")
            val code = verificationCodeEditText.text.toString()
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            SigningActivity.signIn(this@VerificationActivity, credential)
        }

    }


}