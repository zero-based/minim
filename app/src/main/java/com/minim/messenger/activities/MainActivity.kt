package com.minim.messenger.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.minim.messenger.R
import com.minim.messenger.adaptors.ContactsAdaptor
import com.minim.messenger.models.User
import kotlinx.android.synthetic.main.main_activity.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        fetchCurrentUser(false)
        addButton.setOnClickListener {
            if (SettingsActivity.validateUsername(searchBoxEditText)) {
                addContact(searchBoxEditText.text.toString())
            }
        }
    }

    private fun initRecyclerView() {
        val adapter = ContactsAdaptor(this, authUser.contacts!!)
        contactsRecyclerView.adapter = adapter
        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun addContact(username: String) {
        if (!contactExists(username)) {
            FirebaseFirestore.getInstance().collection("users").document(username).get()
                .addOnCompleteListener {
                    if (it.result!!.exists()) {
                        val contact = User(
                            it.result!!.data!!["uid"].toString(),
                            it.result!!.data!!["phoneNumber"].toString(),
                            it.result!!.data!!["username"].toString()
                        )
                        searchBoxEditText.text.clear()
                        (contactsRecyclerView.adapter as ContactsAdaptor).users.add(contact)
                        (contactsRecyclerView.adapter as ContactsAdaptor).notifyDataSetChanged()
                        FirebaseFirestore.getInstance().collection("users")
                            .document(authUser.username!!).update("contacts", FieldValue.arrayUnion(username))
                    } else {
                        searchBoxEditText.error = "User not found"
                    }
                }
        } else {
            searchBoxEditText.error = "Contact already exists"
        }
    }

    private fun contactExists(username: String): Boolean {
        authUser.contacts!!.forEach {
            if (it.username.equals(username, false)) {
                return true
            }
        }
        return false
    }

    private var authUser: User by Delegates.observable(User()) { _, _, _ ->
        //TODO: Remove Toast.
        Toast.makeText(this,"Contacts Loaded",Toast.LENGTH_LONG).show()
        currentUser = authUser
    }

    private fun fetchCurrentUser(isNewUser: Boolean) {
        if (isNewUser) {
            authUser = User(
                FirebaseAuth.getInstance().currentUser!!.uid,
                FirebaseAuth.getInstance().currentUser!!.phoneNumber
            )
        } else {
            FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo(
                    "uid",
                    FirebaseAuth.getInstance().currentUser!!.uid
                )
                .get()
                .addOnSuccessListener {
                    authUser = User(
                        FirebaseAuth.getInstance().currentUser!!.uid,
                        FirebaseAuth.getInstance().currentUser!!.phoneNumber,
                        it.documents[0]["username"].toString()
                    )

                    val contacts: ArrayList<String> = ArrayList()
                    FirebaseFirestore.getInstance().collection("users")
                        .document(authUser.username!!)
                        .get()
                        .addOnSuccessListener { contactsDoc ->
                            contacts.addAll(contactsDoc.data!!["contacts"] as ArrayList<String>)
                            for (contact in contacts) {
                                FirebaseFirestore.getInstance().collection("users")
                                    .document(contact)
                                    .get()
                                    .addOnSuccessListener { doc ->
                                        if (doc.exists()) {
                                            authUser.contacts!!.add(
                                                User(
                                                    doc["uid"].toString(),
                                                    doc["phoneNumber"].toString(),
                                                    doc["username"].toString()
                                                )
                                            )
                                        }
                                    }
                            }
                            initRecyclerView()
                        }
                }
        }
    }

    companion object {
        lateinit var currentUser: User
    }
}
