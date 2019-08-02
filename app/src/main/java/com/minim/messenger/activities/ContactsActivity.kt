package com.minim.messenger.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.minim.messenger.R
import com.minim.messenger.adapters.ContactsAdapter
import com.minim.messenger.models.Conversation
import com.minim.messenger.models.User
import kotlinx.android.synthetic.main.activity_contacts.*

class ContactsActivity : AppCompatActivity() {

    private lateinit var adapter: ContactsAdapter
    private var contacts = arrayListOf<User>()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        val authUser = auth.currentUser!!
        currentUser = User(authUser.uid, authUser.phoneNumber, authUser.displayName)

        initRecyclerView()
        fetchContacts()
        initConversationListeners()

        add_contact_button.setOnClickListener {
            if (SettingsActivity().isUsernameValid(this, search_edit_text)) {
                addContact(search_edit_text.text.toString())
            }
        }

        search_edit_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) = Unit
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s.toString())
            }
        })

    }

    private fun initRecyclerView() {
        adapter = ContactsAdapter(this, contacts)
        contacts_recycler_view.adapter = adapter
        contacts_recycler_view.layoutManager = LinearLayoutManager(this)
    }

    private fun contactExists(username: String): Boolean {
        contacts.find { it.username.equals(username) } ?: return false
        return true
    }

    private fun addContact(username: String) {

        if (contactExists(username) || username == currentUser.username) {
            search_edit_text.error = "Contact already exists"
            return
        }

        val docRef = firestore.collection("users").document(username)
        docRef.get().addOnCompleteListener {

            val doc = it.result!!
            if (!doc.exists()) {
                search_edit_text.error = "User not found"
                return@addOnCompleteListener
            }

            search_edit_text.text.clear()
            search_edit_text.clearFocus()

            val contact = doc.toObject(User::class.java)!!
            val conversation = Conversation(arrayListOf(currentUser, contact))
            firestore.collection("conversations").document(conversation.id).set(conversation.document)

            adapter.contacts.add(contact)
            adapter.notifyDataSetChanged()

        }

    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchContacts() {
        firestore.collection("conversations")
            .whereArrayContains("participants", currentUser.username!!)
            .get()
            .addOnSuccessListener { documents ->

                if (documents.size() == 0) {
                    contacts_progress_bar.visibility = View.GONE
                }

                for ((i, document) in documents.withIndex()) {

                    val participants = document.data["participants"] as ArrayList<String>
                    val contactUsername = participants.find { it != currentUser.username }!!

                    firestore.collection("users")
                        .document(contactUsername)
                        .get()
                        .addOnSuccessListener {
                            val contact = it.toObject(User::class.java)!!
                            adapter.contacts.add(contact)
                        }.addOnCompleteListener {
                            if (i == documents.size() - 1) {
                                contacts_progress_bar.visibility = View.GONE
                                adapter.notifyDataSetChanged()
                            }
                        }

                }
            }
    }

    private fun initConversationListeners() {
        firestore.collection("conversations")
            .whereArrayContains("participants", currentUser.username!!)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                for (doc in value!!) {
                    // TODO: Notify User
                }
            }
    }

    companion object {
        lateinit var currentUser: User
    }

}
