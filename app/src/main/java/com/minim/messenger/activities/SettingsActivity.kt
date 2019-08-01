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
            if (isUsernameValid(this, username_edit_text)) {
                addUser(username_edit_text.text.toString())
            }
        }

    }

    private fun addUser(username: String) {

        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("users").document(username)

        userDocRef.get().addOnCompleteListener {

            if (it.result!!.exists()) {
                username_edit_text.error = "@$username already in use. Try another."
                return@addOnCompleteListener
            }

            // Set user's display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()
            val authUser = FirebaseAuth.getInstance().currentUser
            authUser?.updateProfile(profileUpdates)

            // Add user document
            val user = User(authUser?.uid, authUser?.phoneNumber!!, username)
            userDocRef.set(user).addOnCompleteListener {
                // Add contacts document
                val contactsDocRef = firestore.collection("contacts").document(username)
                val usernames = hashMapOf("usernames" to arrayListOf<String>())
                contactsDocRef.set(usernames).addOnCompleteListener {
                    SigningActivity.startActivity(this, ContactsActivity::class.java)
                }
            }

        }

    }


    fun isUsernameValid(activity: AppCompatActivity, editText: EditText): Boolean {
        val maxLength: Int = activity.resources.getInteger(R.integer.username_max_length)
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

}