package com.minim.messenger.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.minim.messenger.R
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        saveSettingsButton.setOnClickListener {

            when {
                TextUtils.isEmpty(userNameEditText.text) -> userNameEditText.error = "Required field."
                userNameEditText.text.contains(" ") -> userNameEditText.error = "username can't contain spaces."
                else -> {
                    // TODO: Add User to Database.
                    val intent = Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
            }

        }
    }

}