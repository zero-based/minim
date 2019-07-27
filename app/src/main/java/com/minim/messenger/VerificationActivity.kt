package com.minim.messenger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.verification_activity.*

class VerificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.verification_activity)

        verifyButton.setOnClickListener {
            val verificationId = intent.getStringExtra("verificationId")
            val code = verificationCodeEditText.text.toString()
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            SigningActivity.signIn(this@VerificationActivity, credential)
        }

    }


}