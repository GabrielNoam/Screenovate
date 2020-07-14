package com.screenovate.superdo

import android.app.Application
import androidx.lifecycle.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

/**
 * GroceriesViewModel
 * @author Gabriel Noam
 */
class GroceriesViewModel(application: Application)
    : AndroidViewModel(application), LifecycleObserver {

    val filter = MutableLiveData<String>()
    val feedController =  GroceriesFeedController(application)

    private val groceryDatabase  = GroceriesDatabase.getInstance(this.getApplication())
    var groceries = groceryDatabase.groceryDao().getAll()

    // TODO Support Generic Filter with hi-order function
    fun filterFeed(value: Float) {
        feedController.filterFeed(value.toFloat())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun startFeed() {
        feedController.startFeed()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stopFeed() {
        feedController.stopFeed()
    }
}