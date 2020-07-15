package com.screenovate.superdo

interface Feed<T> {
    fun connect()
    fun disconnect()
    fun filter(filter: (t: T) -> Boolean)
}