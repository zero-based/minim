package com.minim.messenger.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.minim.messenger.R
import com.minim.messenger.models.User
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
                    addUser(userNameEditText.text.toString())
                    launchApp()
                }
            }

        }
    }

    private fun addUser(username: String) {
        val authUser = FirebaseAuth.getInstance().currentUser ?: return
        val user = User(authUser.uid, authUser.phoneNumber!!, username)
        FirebaseFirestore.getInstance().collection("users")
            .document(user.phoneNumber).set(user)
    }

    private fun launchApp() {
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}