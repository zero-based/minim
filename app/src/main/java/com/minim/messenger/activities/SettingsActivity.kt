package com.minim.messenger.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.minim.messenger.R
import kotlinx.android.synthetic.main.activity_settings.*
import android.content.Intent
import android.net.Uri

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val notSupportedToast = Toast.makeText(this, "Not Supported Yet!", Toast.LENGTH_SHORT)
        update_button.setOnClickListener { notSupportedToast.show() }
        help_button.setOnClickListener {
            val url = "https://michaelsafwathanna.github.io/minim/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        delete_button.setOnClickListener { notSupportedToast.show() }
        sign_out_button.setOnClickListener { notSupportedToast.show() }

    }
}
