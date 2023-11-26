package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.CoinRegistry.value
import com.sukes13.vendingmachine.Product.COLA
import com.sukes13.vendingmachine.ProductRegistry.value
import com.sukes13.vendingmachine.VendingEvent.*


data class VendingMachine(
    val eventStore: EventStore = EventStore()
) {
    private val amount = eventStore.filterEvents<AmountInsertedEvent>().sumOf { it.amount }
    val coinChute = eventStore.filterEvents<CoinRejectedEvent>().map { it.coin }
    val chute: List<Product> = eventStore.filterEvents<ProductBoughtEvent>().map { it.product }
    val display =
        if (eventStore.isNotEmpty() && eventStore.last() is ProductBoughtEvent) "THANK YOU"
        else when (amount) {
            0.0 -> "INSERT COIN"
            else -> amount.asString()
        }

    fun insert(coin: Coin) =
        coin.value()
            ?.let { VendingMachine(eventStore.append(AmountInsertedEvent(it))) }
            ?: VendingMachine(eventStore.append(CoinRejectedEvent(coin)))

    fun pressButton(productCode: String): VendingMachine {
        val product = Product.toProduct(productCode)
        return product.value()?.let {
            if (amount >= it) VendingMachine(eventStore.append(ProductBoughtEvent(product)))
            else VendingMachine(eventStore.append(ButtonPressed(product)))
        } ?: error("No product linked to this bu")
    }
}


private fun Double.asString() = String.format("%.2f", this)