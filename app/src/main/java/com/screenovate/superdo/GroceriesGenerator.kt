package com.screenovate.superdo

import android.os.Handler

/**
 * GroceriesGenerator
 * @author Gabriel Noam
 */
class GroceriesGenerator(private val listener: Listener) {

    var count: Int = 0

    interface Listener {
        fun onEvent(grocery: Grocery)
    }

    private val event = Runnable {
        count++
        val index = (vegetables.indices).random()
        val weight = (1 .. 52).random()
        listener.onEvent(Grocery(count, vegetables[index], weight.toString(), ""))
        start()
    }
    private var handler: Handler? = Handler()

    fun start() {
        val rand = (0..10).random()
        handler!!.postDelayed(event, (rand*100).toLong()+200)
    }

    fun stop() {
        handler!!.removeCallbacks(event)
        handler = null
    }

    private val vegetables = listOf(
        "Artichoke",
        "Aubergine",
        "Asparagus",
        "Legumes",
        "Alfalfa sprouts",
        "Azuki beans",
        "Bean sprouts",
        "Black beans",
        "Black-eyed peas",
        "Borlotti bean",
        "Broad beans",
        "Chickpeas",
        "Green beans",
        "Kidney beans",
        "Lentils",
        "Lima",
        "Mung beans",
        "Navy beans",
        "Peanuts",
        "Pinto beans",
        "Runner beans",
        "Split peas",
        "Soy beans",
        "Peas",
        "Mangetout",
        "Broccoflower",
        "Broccoli",
        "Brussels",
        "Cabbage",
        "Kohlrabi",
        "Savoy cabbage",
        "Red cabbage",
        "Cauliflower",
        "Celery",
        "Endive",
        "Fiddleheads",
        "Frisee",
        "Fennel",
        "Greens",
        "Bok choy",
        "Chard",
        "Collard",
        "Kale",
        "Mustard greens",
        "Spinach")
}