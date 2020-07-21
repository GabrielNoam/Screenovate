package com.screenovate.superdo

/**
 * Feed
 * @author Gabriel Noam
 */
interface Feed<T> {
    fun connect()
    fun disconnect()
    fun filter(filter: (t: T) -> Boolean)
}