package com.screenovate.superdo

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.*
import kotlinx.coroutines.launch


/**
 * GroceriesViewModel
 * @author Gabriel Noam
 */

class GroceriesViewModel(application: Application) : AndroidViewModel(application),
    LifecycleObserver, Feed<Grocery> {

    private var isBound: Boolean = false
    private lateinit var feed: Feed<Grocery>

    var message: MutableLiveData<Msg> = MutableLiveData()
        private set

    var feedState: MutableLiveData<FeedStatus> =  MutableLiveData(FeedStatus.ERROR)
        private set

    private val groceryDatabase = GroceriesDatabase.getInstance(this.getApplication())
    var groceries = groceryDatabase.groceryDao().getAll()

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            val factory = binder as FeedFactory
            feed = factory.getFeed()
            isBound = true
            connect()
        }

        override fun onServiceDisconnected(arg: ComponentName) {
            feedState.value = FeedStatus.OFF
            isBound = false
        }
    }

    private val networkCallback: NetworkCallback = object : NetworkCallback() {
        override fun onAvailable(network: Network) {
            if(isBound && feedState.value != FeedStatus.ON) {
                viewModelScope.launch {
                    message.value = Msg(Msg.Type.Snack, application.getString(R.string.network_reconnected), Msg.Status.Info)
                    connect()
                }
            }
        }

        override fun onLost(network: Network) {
            if(isBound) {
                viewModelScope.launch {
                    message.value = Msg(Msg.Type.Dialog, application.getString(R.string.network_disconnected), Msg.Status.Error)
                    feedState.value = FeedStatus.ERROR
                }
            }
        }
    }

    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private fun registerNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }
    }

    private fun unregisterNetworkCallback() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (i: IllegalArgumentException){}

    }


    override fun connect() {
        isBound.takeIf { it && feedState.value != FeedStatus.ON }.also {
            feed.connect()
            feedState.value = FeedStatus.ON
        }
    }
    override fun disconnect() {
        isBound.takeIf { it && feedState.value != FeedStatus.OFF }.also {
            feed.disconnect()
            feedState.value  = FeedStatus.OFF
        }
    }

    override fun filter(filter: (grocery: Grocery) -> Boolean) {
        isBound.takeIf { it }.also { feed.filter(filter) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    internal fun bind() {
        Intent(getApplication(), GroceriesService::class.java).also {
            getApplication<GroceriesApp>().bindService(it, connection, Context.BIND_AUTO_CREATE)
        }

        message.value = Msg(Msg.Type.Snack, "Checking Internet Connection ...")

//        if (!hasNetworkAvailable(getApplication())) {
//            feedState.value = FeedStatus.ERROR
//
//            message.value = Msg(
//                Msg.Type.Dialog,
//                "No Internet Connection, " +
//                        "Feed is back when connection available", Msg.Status.Error)
//        }

        registerNetworkCallback()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    internal fun unbind() {
        unregisterNetworkCallback()
        getApplication<GroceriesApp>().unbindService(connection)
    }
}