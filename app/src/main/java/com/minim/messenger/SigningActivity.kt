package com.minim.messenger

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.signing_activity.*

class SigningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signing_activity)

        sendVerificationCodeButton.setOnClickListener {
            if (!phoneNumberEditText.text.toString().isNullOrEmpty()) {
                val i = Intent(this, VerificationActivity::class.java)
                startActivity(i)
            } else {
                Toast.makeText(this, "Enter Phone Number", Toast.LENGTH_LONG).show()
            }
        }
    }
}
