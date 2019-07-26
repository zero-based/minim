package com.minim.messenger

import android.content.ContentValues.TAG
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.content.Intent
import android.util.Log
import android.widget.Toast







class ContactsAdaptor(
    private val users: ArrayList<User>
) : RecyclerView.Adapter<ContactsAdaptor.ContactHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ContactHolder(view)
    }

    override fun getItemCount() = users.size


    override fun onBindViewHolder(holder: ContactHolder, position: Int) {

        holder.contactUsername.text=users[position].username

        holder.parentLayout.setOnClickListener {
            /**TODO :Toast.makeText(MainActivity(), users[position].username, Toast.LENGTH_SHORT).show()**/

//            val intent = Intent(mContext, GalleryActivity::class.java)
//            intent.putExtra("image_url", mImages.get(position))
//            intent.putExtra("image_name", mImageNames.get(position))
//            mContext.startActivity(intent)
        }
    }

    inner class ContactHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var contactUsername: TextView = itemView.findViewById(R.id.contactUsername)
        internal var parentLayout: LinearLayout = itemView.findViewById(R.id.contactLinearLayout)

    }

}