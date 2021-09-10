package ca.bc.gov.vaxcheck.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import ca.bc.gov.vaxcheck.R

/**
 * Helper function to read file from asset
 * and return String JSON.
 */
fun Context.readJsonFromAsset(fileName: String) =
    this.assets.open(fileName).bufferedReader().use { it.readText() }

/**
 * Helper function to check if device is online or not.
 */
fun Context.isOnline(): Boolean {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

    if (capabilities != null) {

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    return false
}

/**
 * Helper function to convert String to clickable link.
 */
fun TextView.setSpannableLink(text: String? = null, onClick: () -> Unit) {
    val spannableString = SpannableString(
        if (text.isNullOrBlank()) {
            this.text
        } else {
            text
        }
    )
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(p0: View) {
            onClick()

        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = resources.getColor(R.color.white, null)
        }
    }

    spannableString.setSpan(
        clickableSpan,
        0,
        spannableString.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
    this.movementMethod = LinkMovementMethod.getInstance()
}