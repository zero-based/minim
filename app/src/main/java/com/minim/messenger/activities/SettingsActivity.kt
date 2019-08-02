package com.minim.messenger.activities

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.minim.messenger.R
import com.minim.messenger.models.User
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settings_save_button.setOnClickListener {
            if (isUsernameValid(this, username_edit_text) && isEmailValid(email_edit_text)) {
                addUser(username_edit_text.text.toString(), email_edit_text.text.toString())
            }
        }

    }

    private fun addUser(username: String, email: String) {

        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("users").document(username)

        userDocRef.get().addOnCompleteListener {

            if (it.result!!.exists()) {
                username_edit_text.error = "@$username already in use. Try another."
                return@addOnCompleteListener
            }

            val authUser = FirebaseAuth.getInstance().currentUser
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()
            authUser?.updateProfile(profileUpdates)?.addOnCompleteListener {
                authUser.updateEmail(email).addOnCompleteListener {
                    userDocRef.set(User(authUser)).addOnCompleteListener {
                        SigningActivity.startActivity(this, ContactsActivity::class.java)
                    }
                }
            }
        }

    }

    fun isUsernameValid(activity: AppCompatActivity, editText: EditText): Boolean {
        val maxLength = activity.resources.getInteger(R.integer.username_max_length)
        when {
            editText.text.isEmpty() -> editText.error =
                "Required field."
            editText.text matches "[^a-zA-Z0-9]+".toRegex() -> editText.error =
                "Use only letters and numbers."
            editText.text.length > maxLength -> editText.error =
                "Use $maxLength characters or less."
            else ->
                return true
        }
        return false
    }

    private fun isEmailValid(editText: EditText): Boolean {
        if (editText.text.isEmpty()) {
            editText.error = "Required field."
            return false
        }
        return true
    }

}