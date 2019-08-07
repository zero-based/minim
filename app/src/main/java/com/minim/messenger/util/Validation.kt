package com.minim.messenger.util

import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.minim.messenger.R

object Validation {

    fun isUsernameValid(activity: AppCompatActivity, editText: EditText): Boolean {
        val maxLength = activity.resources.getInteger(R.integer.username_max_length)
        val username = editText.text
        when {
            username.isEmpty() -> editText.error = "Required field."
            username matches "[^a-zA-Z0-9]+".toRegex() -> editText.error = "Use only letters and numbers."
            username.length > maxLength -> editText.error = "Use $maxLength characters or less."
            else -> return true
        }
        return false
    }

    fun isEmailValid(editText: EditText): Boolean {
        if (editText.text.isEmpty()) {
            editText.error = "Required field."
            return false
        }
        return true
    }

}