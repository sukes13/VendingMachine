package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.CoinRegistry.value
import com.sukes13.vendingmachine.ProductRegistry.price
import com.sukes13.vendingmachine.VendingEvent.*
import com.sukes13.vendingmachine.VendingEvent.TimedVendingEvent.*
import java.time.LocalDateTime
import java.time.LocalDateTime.now


data class VendingMachine(
    val eventStore: EventStore = EventStore()
) {
    val currentAmount = eventStore.eventsSinceOccurrenceOf<ProductBoughtEvent>()
        .filterIsInstance<AmountInsertedEvent>()
        .sumOf { it.amount }
    val chute = eventStore.eventsOfType<ProductBoughtEvent>()
        .map { it.product }
    val coinChute = eventStore.eventsOfType<CoinRejectedEvent>()
        .map { it.coin }

    fun display() =
        when (val lastEvent = eventStore.events.lastOrNull()) {
            is ProductBoughtEvent -> if (showingTimePassed(lastEvent.boughtOn)) "INSERT COIN" else "THANK YOU" 
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

    fun pressButton(productCode: String): VendingMachine {
        val product = Product.toProduct(productCode)
        return product.price().let {
            if (currentAmount >= it) copyAndAdd(ProductBoughtEvent(product))
            else copyAndAdd(ButtonPressed(product))
        }
    }

    private fun copyAndAdd(event: VendingEvent) = VendingMachine(eventStore.append(event))

    private fun showingTimePassed(time: LocalDateTime) =
        time.isBefore(now().minusSeconds(3))
}


private fun Double.asString() = String.format("%.2f", this)