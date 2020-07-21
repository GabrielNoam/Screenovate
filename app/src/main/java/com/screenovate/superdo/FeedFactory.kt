package com.screenovate.superdo

/**
 * FeedFactory
 * @author Gabriel Noam
 */
interface FeedFactory {
    fun <T> getFeed(): Feed<T>
}