package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.Product.COLA


enum class Product(private val productCode: String) {
    COLA("Cola");

    companion object {
        fun toProduct(code: String) = values().single { it.productCode == code }
    }
}

object ProductRegistry {
    private val registry = mapOf(
        COLA to 1.0,
    )

    fun Product.value() = registry[this]
}