package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.Product.*


enum class Product(val code: String) {
    COLA("Cola"),
    CHIPS("Chips"),
    CANDY("Candy");

    companion object {
        fun toProduct(code: String) = values().single { it.code == code }
    }
}

object ProductRegistry {
    private val registry = mapOf(
        COLA to 1.0,
        CHIPS to 0.5,
        CANDY to 0.65,
    )

    fun Product.price() = registry[this] ?: error("No price added for $code")
}