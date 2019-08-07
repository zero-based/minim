package com.minim.messenger.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity

object Navigation {

    fun start(
        activity: Activity,
        destination: Class<out AppCompatActivity>,
        clearTop: Boolean = false,
        stringExtra: Pair<String, String?>? = null,
        parableExtra: Pair<String, Parcelable?>? = null
    ) {
        val intent = Intent(activity, destination)
        if (clearTop) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (stringExtra != null) {
            intent.putExtra(stringExtra.first, stringExtra.second)
        }
        if (parableExtra != null) {
            intent.putExtra(parableExtra.first, parableExtra.second)
        }
        activity.startActivity(intent)
        activity.finish()
    }

    fun sendEmail(activity: Activity, to: String, subject: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", to, null))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        activity.startActivity(Intent.createChooser(intent, "Send email..."))
    }

    fun openUrl(activity: Activity, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        activity.startActivity(intent)
    }

}