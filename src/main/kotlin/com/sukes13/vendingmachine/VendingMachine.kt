package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.CoinRegistry.value
import com.sukes13.vendingmachine.ProductRegistry.price
import com.sukes13.vendingmachine.VendingEvent.*


data class VendingMachine(
    val eventStore: EventStore = EventStore()
) {
    val currentAmount =
        eventStore.eventsOfType<AmountInsertedEvent>().sumOf { it.amount }
            .minus(eventStore.eventsOfType<ProductBoughtEvent>().sumOf { it.product.price() })
    val coinChute = eventStore.eventsOfType<CoinRejectedEvent>().map { it.coin }
    val chute: List<Product> = eventStore.eventsOfType<ProductBoughtEvent>().map { it.product }
    val display =
        when (val lastEvent = eventStore.events.lastOrNull()) {
            is ProductBoughtEvent -> "THANK YOU"
            is ButtonPressed -> lastEvent.product.price().asString()
            else -> when (currentAmount) {
                0.0 -> "INSERT COIN"
                else -> currentAmount.asString()
            }
        }

    fun insert(coin: Coin) =
        coin.value()
            ?.let { copyAndAdd(AmountInsertedEvent(it)) }
            ?: copyAndAdd(CoinRejectedEvent(coin))

    private fun copyAndAdd(event: VendingEvent) = VendingMachine(eventStore.append(event))

    fun pressButton(productCode: String): VendingMachine {
        val product = Product.toProduct(productCode)
        return product.price().let {
            if (currentAmount >= it) copyAndAdd(ProductBoughtEvent(product))
            else copyAndAdd(ButtonPressed(product))
        } ?: error("No product linked to this bu")
    }
}


private fun Double.asString() = String.format("%.2f", this)