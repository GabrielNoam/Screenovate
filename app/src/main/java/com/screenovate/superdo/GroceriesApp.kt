package com.screenovate.superdo

import android.app.Application

class GroceriesApp: Application() {
    override fun onCreate() {
        super.onCreate()
        GroceriesDatabase.getInstance(this)
    }
}