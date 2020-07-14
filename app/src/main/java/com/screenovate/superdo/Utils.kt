package com.screenovate.superdo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

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

fun <T> LiveData<T>.getDistinct(): LiveData<T> {
    val distinctLiveData = MediatorLiveData<T>()
    distinctLiveData.addSource(this, object : Observer<T> {
        private var initialized = false
        private var lastObj: T? = null
        override fun onChanged(obj: T?) {
            if (!initialized) {
                initialized = true
                lastObj = obj
                distinctLiveData.postValue(lastObj)
            } else if ((obj == null && lastObj != null)
                || obj != lastObj) {
                lastObj = obj
                distinctLiveData.postValue(lastObj)
            }
        }
    })
    return distinctLiveData
}