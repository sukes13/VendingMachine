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

    fun insert(coin: Coin) = coin.value()
        ?.let { copyAndAdd(AmountInsertedEvent(it)) }
        ?: copyAndAdd(CoinRejectedEvent(coin))

    fun pressButton(productCode: String) =
        Product.toProduct(productCode).let { product ->
            when {
                currentAmount >= product.price() -> copyAndAdd(ProductBoughtEvent(product))
                else -> copyAndAdd(ButtonPressed(product))
            }
        }

    fun display(): String {
        val lastEvent = eventStore.events.lastOrNull()
        return if (lastEvent is TimedVendingEvent && showTemporary(lastEvent.occurredOn)) {
            temporaryDisplayed(lastEvent)
        } else defaultDisplayed()
    }

    private fun temporaryDisplayed(lastEvent: TimedVendingEvent) =
        when (lastEvent) {
            is ButtonPressed -> "PRICE ${lastEvent.product.price().asString()}"
            is ProductBoughtEvent -> "THANK YOU"
        }

    private fun defaultDisplayed() =
        when (currentAmount) {
            0.0 -> "INSERT COIN"
            else -> currentAmount.asString()
        }

    private fun copyAndAdd(event: VendingEvent) = VendingMachine(eventStore.append(event))

    private fun showTemporary(time: LocalDateTime) = time.isAfter(now().minusSeconds(3))
}


private fun Double.asString() = String.format("%.2f", this)