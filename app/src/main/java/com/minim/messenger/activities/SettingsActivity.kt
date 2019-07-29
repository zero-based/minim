package com.minim.messenger.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
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
            if (validateUsername(userNameEditText)) {
                addUser(userNameEditText.text.toString())
            }
        }
    }

    private fun addUser(username: String) {
        val authUser = FirebaseAuth.getInstance().currentUser ?: return
        val user = User(authUser.uid, authUser.phoneNumber!!, username)
        FirebaseFirestore.getInstance().collection("users").document(username).get()
            .addOnCompleteListener {
                if (it.result!!.exists()) {
                    userNameEditText.error = "@${userNameEditText.text} already in use. Try another."
                } else {
                    FirebaseFirestore.getInstance().collection("users")
                        .document(user.username!!).set(user)
                    launchApp()
                }
            }
    }

    private fun launchApp() {
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    companion object {
        fun validateUsername(editText: EditText): Boolean {
            when {
                editText.text.isEmpty() -> editText.error =
                    "Required field."
                editText.text matches "[^a-zA-Z0-9]+".toRegex() -> editText.error =
                    "Use only letters and numbers."
                editText.text.length > 18 -> editText.error =
                    "Use less than 18 characters."
                else ->
                    return true
            }
            return false

        }
    }

}