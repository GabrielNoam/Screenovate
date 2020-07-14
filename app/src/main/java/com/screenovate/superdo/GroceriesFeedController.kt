package com.screenovate.superdo

import android.content.Context

/**
 * GroceriesFeedController
 * @author Gabriel Noam
 */
class GroceriesFeedController(private val context: Context) {

    fun stopFeed() {
        GroceriesService.stop(context)
    }

    fun startFeed() {
        GroceriesService.start(context)
    }

    // TODO Support Generic Filter with hi-order function
    fun filterFeed(value: Float) {
        GroceriesService.filter(context, value)
    }
}