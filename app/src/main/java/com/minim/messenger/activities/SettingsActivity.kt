package com.minim.messenger.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.minim.messenger.R
import com.minim.messenger.models.User
import com.minim.messenger.util.Navigation
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = User(auth.currentUser!!)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        username_edit_text.setText(currentUser.username)
        email_edit_text.setText(currentUser.email)

        update_button.setOnClickListener {
            Toast.makeText(this, "Not Supported Yet!", Toast.LENGTH_SHORT).show()
        }

        help_button.setOnClickListener {
            val url = "https://michaelsafwathanna.github.io/minim/"
            Navigation.openUrl(this, url)
        }

        delete_button.setOnClickListener {

            auth.currentUser?.delete()?.addOnSuccessListener {
                val uid = currentUser.uid
                firestore.collection("users").document(uid!!).delete()

                val sentMessages = firestore.collection("messages")
                    .whereEqualTo("sender", uid).get()
                val receivedMessages = firestore.collection("messages")
                    .whereEqualTo("receiver", uid).get()
                val conversations = firestore.collection("conversations")
                    .whereArrayContains("participants", uid).get()

                Tasks.whenAllSuccess<QuerySnapshot>(sentMessages, receivedMessages, conversations)
                    .addOnSuccessListener {
                        it.forEach { q ->
                            q.documents.forEach { d -> d.reference.delete() }
                        }
                    }

                Navigation.start(this@SettingsActivity, SigningActivity::class.java, true)
            }

        }

        sign_out_button.setOnClickListener {
            auth.signOut()
            Navigation.start(this, SigningActivity::class.java, true)
        }

    }
}
