package com.screenovate.superdo

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * GroceriesService
 * @author Gabriel Noam
 */
class GroceriesService: LifecycleService() {

    private var value: Float = 0F

    private lateinit var groceryDatabase: GroceriesDatabase

    private val customWebSocketClient = CustomWebSocketClient(

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

        fun start(context: Context) = context.startService(getIntent(context))
        fun stop(context: Context) = context.stopService(getIntent(context))
        fun filter(context: Context, value: Float) = context.startService(getIntent(context, value))
    }

    override fun onCreate() {
        super.onCreate()
        groceryDatabase = GroceriesDatabase.getInstance(this.application)
    }

    private fun filter(grocery: Grocery): Boolean {
        var weightStr = grocery.weight
        weightStr = weightStr.replace("kg","").trim()
        try {
            val weight = weightStr.toFloat()
            return value.equals(0F) || weight < value
        } catch (e: RuntimeException) {}

        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent?.hasExtra(EXTRA_PARAM_FILTER) == true)
            value = intent.getFloatExtra(EXTRA_PARAM_FILTER, 0F)

        GlobalScope.launch(Dispatchers.IO) {
            if(!customWebSocketClient.isOpen) {
                customWebSocketClient.connectionLostTimeout = 1000
                customWebSocketClient.connect()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        customWebSocketClient.close()
        super.onDestroy()
    }
}