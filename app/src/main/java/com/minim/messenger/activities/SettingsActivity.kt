package com.minim.messenger.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.minim.messenger.R
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val notSupportedToast = Toast.makeText(this, "Not Supported Yet!", Toast.LENGTH_SHORT)
        update_button.setOnClickListener { notSupportedToast.show() }
        delete_button.setOnClickListener { notSupportedToast.show() }
        sign_out_button.setOnClickListener { notSupportedToast.show() }

    }
}
