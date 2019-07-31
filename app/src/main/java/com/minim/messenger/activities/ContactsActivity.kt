package com.minim.messenger.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.minim.messenger.R
import com.minim.messenger.adaptors.ContactsAdaptor
import com.minim.messenger.models.User
import kotlinx.android.synthetic.main.contacts_activity.*

class ContactsActivity : AppCompatActivity() {

    private lateinit var adapter: ContactsAdaptor
    private var contacts = arrayListOf<User>()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.contacts_activity)

        val authUser = auth.currentUser!!
        currentUser = User(authUser.uid, authUser.phoneNumber, authUser.displayName)

        initRecyclerView()
        fetchContacts()

        addButton.setOnClickListener {
            if (SettingsActivity.validateUsername(searchBoxEditText)) {
                addContact(searchBoxEditText.text.toString())
            }
        }

    }

    private fun initRecyclerView() {
        adapter = ContactsAdaptor(this, contacts)
        contactsRecyclerView.adapter = adapter
        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun contactExists(username: String): Boolean {
        contacts.find { it.username.equals(username) } ?: return false
        return true
    }

    private fun addContact(username: String) {

        if (contactExists(username) || username == currentUser.username) {
            searchBoxEditText.error = "Contact already exists"
            return
        }

        val docRef = firestore.collection("users").document(username)
        docRef.get().addOnCompleteListener {

            val result = it.result!!
            if (!result.exists()) {
                searchBoxEditText.error = "User not found"
                return@addOnCompleteListener
            }

            searchBoxEditText.text.clear()
            searchBoxEditText.clearFocus()

            val data = result.data!!
            val contact = User(
                data["uid"].toString(),
                data["phoneNumber"].toString(),
                data["username"].toString()
            )

            val contactsDocRef = firestore.collection("contacts").document(currentUser.username!!)
            contactsDocRef.update("usernames", FieldValue.arrayUnion(contact.username!!))

            adapter.contacts.add(contact)
            adapter.notifyDataSetChanged()

        }

    }

    private fun fetchContacts() {

        val contactsDocRef = firestore.collection("contacts").document(currentUser.username!!)
        contactsDocRef.get().addOnSuccessListener {
            val usernames = it.data!!["usernames"] as ArrayList<String>
            for ((i, username) in usernames.withIndex()) {
                val docRef = firestore.collection("users").document(username)
                docRef.get().addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val contact = doc.toObject(User::class.java)!!
                        adapter.contacts.add(contact)
                        adapter.notifyItemInserted(i)
                    }
                }
            }
        }

    }

    companion object {
        lateinit var currentUser: User
    }

}
