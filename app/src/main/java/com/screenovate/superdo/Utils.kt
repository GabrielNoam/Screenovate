package com.screenovate.superdo

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