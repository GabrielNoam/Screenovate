package com.screenovate.superdo

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 *
 * @author Gabriel Noam
 */
//pattern for singleton https://medium.com/@BladeCoder/kotlin-singletons-with-argument-194ef06edd9e
fun <T> T?.singleton(lock: Any, creator: () -> T): T {
    val instance = this
    if (instance != null) return instance

    return synchronized(lock) {
        var instance2 = this
        if (instance2 != null) {
            instance2
        } else {
            instance2 = creator()
            instance2
        }
    }
}

fun String.toFloatOrZero() = when {
        isNullOrEmpty() -> 0F
        else -> try { trim().toFloat() }
            catch (e: NumberFormatException) { 0F }
}

fun hasNetworkAvailable(context: Context): Boolean {
    val service = Context.CONNECTIVITY_SERVICE
    val manager = context.getSystemService(service) as ConnectivityManager?
    val network = manager?.activeNetworkInfo
    Log.d("aa", "hasNetworkAvailable: ${(network != null)}")
    return (network != null)
}

const val REACHABILITY_SERVER = "https://www.google.com"
const val LANDING_SERVER = "https://www.myserver.com"

fun hasInternetConnected(context: Context): Boolean {
    if (hasNetworkAvailable(context)) {
        try {
            val connection = URL(REACHABILITY_SERVER).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "ConnectionTest")
            connection.setRequestProperty("Connection", "close")
            connection.connectTimeout = 1000 // configurable
            connection.connect()
            Log.d("classTag", "hasInternetConnected: ${(connection.responseCode == 200)}")
            return (connection.responseCode == 200)
        } catch (e: IOException) {
            Log.e("classTag", "Error checking internet connection", e)
        }
    } else {
        Log.w("classTag", "No network available!")
    }
    Log.d("classTag", "hasInternetConnected: false")
    return false
}

fun hasServerConnected(context: Context): Boolean {
    if (hasNetworkAvailable(context)) {
        try {
            val connection = URL(LANDING_SERVER).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Test")
            connection.setRequestProperty("Connection", "close")
            connection.connectTimeout = 1500 // configurable
            connection.connect()
            Log.d("classTag", "hasServerConnected: ${(connection.responseCode == 200)}")
            return (connection.responseCode == 200)
        } catch (e: IOException) {
            Log.e("classTag", "Error checking internet connection", e)
        }
    } else {
        Log.w("classTag", "Server is unavailable!")
    }
    Log.d("classTag", "hasServerConnected: false")
    return false
}
