package com.screenovate.superdo

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * GroceriesService
 * @author Gabriel Noam
 */
class GroceriesService: Service(), Feed<Grocery> {

    private var filter: (grocery: Grocery) -> Boolean = { true }
    private val binder = LocalBinder()

    private lateinit var groceryDatabase: GroceriesDatabase
    private var customWebSocketClient: CustomWebSocketClient? = null

    private fun getWebSocketClient() = CustomWebSocketClient(

        address =  WEB_SOCKET_ADDRESS,

        onMessageReceived = { message ->
            val grocery = gson.fromJson<Grocery>(message, Grocery::class.java)
            GlobalScope.launch(Dispatchers.IO) {
                if(filter(grocery)) {
                    val ids = groceryDatabase.groceryDao().insert(grocery)
                    Log.i(TAG, "Insert: ${grocery.name}: ${ids[0]}")
                }
            }
        },

        onErrorReceived = { exception ->
            Log.e(TAG, exception?.message, exception)
        })

    companion object {
        private val gson = Gson()
        private const val TAG = "GroceriesService"
        private const val EXTRA_PARAM_FILTER = "filter"
        private const val WEB_SOCKET_ADDRESS = "ws://superdo-groceries.herokuapp.com:80/receive"

        private fun getIntent(context: Context, value: Float?  = null): Intent {
            val intent = Intent(context, GroceriesService::class.java)
            value?.let {
                intent.putExtra(EXTRA_PARAM_FILTER, it)
            }

            return intent
        }
    }

    override fun onCreate() {
        super.onCreate()
        groceryDatabase = GroceriesDatabase.getInstance(this.application)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        disconnect()
        return super.onUnbind(intent)
    }
    override fun onBind(intent: Intent): IBinder {

        return binder
    }

    override fun connect() {
        customWebSocketClient = getWebSocketClient()
        customWebSocketClient?.connect()
    }


    override fun disconnect() {
        try {
            customWebSocketClient?.close()
        } catch (ex: IllegalStateException) {
        } finally {
            customWebSocketClient = null
        }
    }

    override fun filter(filter: (grocery: Grocery) -> Boolean) {
        this.filter = filter
    }

    private inner class LocalBinder : Binder(), FeedFactory {
        override fun <Grocery> getFeed() = this@GroceriesService as Feed<Grocery>
    }
}