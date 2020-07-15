package com.screenovate.superdo

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.*

/**
 * GroceriesViewModel
 * @author Gabriel Noam
 */
class GroceriesViewModel(application: Application)
    : AndroidViewModel(application), LifecycleObserver {

    val filter = MutableLiveData<String>()

    private var isBound: Boolean = false
    private lateinit var feed: Feed<Grocery>

    private val groceryDatabase  = GroceriesDatabase.getInstance(this.getApplication())
    var groceries = groceryDatabase.groceryDao().getAll()

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as GroceriesService.LocalBinder
            feed = binder.getFeed()
            isBound = true
        }

        override fun onServiceDisconnected(arg: ComponentName) {
            isBound = false
        }
    }

    fun start() {
        if(isBound) feed.connect()
    }

    fun stop() {
        if(isBound) feed.disconnect()
    }

    fun filter(filter: (grocery: Grocery) -> Boolean) {
        if(isBound) feed.filter(filter)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    internal fun bind() {
        Intent(getApplication(), GroceriesService::class.java).also {
            getApplication<GroceriesApp>().bindService(it, connection, Context.BIND_AUTO_CREATE)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    internal fun unbind() {
        getApplication<GroceriesApp>().unbindService(connection)
    }
}