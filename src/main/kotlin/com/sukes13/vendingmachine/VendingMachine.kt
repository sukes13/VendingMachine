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
        .filterIsInstance<CoinAcceptedEvent>()
        .sumOf { it.amount }
    val chute = eventStore.eventsOfType<ProductBoughtEvent>()
        .map { it.product }
    val coinChute = eventStore.eventsOfType<CoinReturnedEvent>()
        .map { it.coin }

    fun insert(coin: Coin) = coin.value()
        ?.let { copyAndAdd(CoinAcceptedEvent(it)) }
        ?: copyAndAdd(CoinReturnedEvent(coin))

    fun pressButton(productCode: String) =
        Product.toProduct(productCode).let { product ->
            when {
                currentAmount >= product.price() -> copyAndAdd(ProductBoughtEvent(product))
                else -> copyAndAdd(ButtonPressed(product))
            }
        }

    fun display() =
        eventStore.events.lastOrNull().let { lastEvent ->
            if (lastEvent is TimedVendingEvent && lastEvent.occurredOn.withinSpecialTimeFrame())
                specialMessage(lastEvent)
            else defaultMessage()
        }

    private fun specialMessage(event: TimedVendingEvent) =
        when (event) {
            is ButtonPressed -> "PRICE ${event.product.price().asString()}"
            is ProductBoughtEvent -> "THANK YOU"
        }

    private fun defaultMessage() =
        when (currentAmount) {
            0.0 -> "INSERT COIN"
            else -> currentAmount.asString()
        }

    private fun copyAndAdd(event: VendingEvent) = VendingMachine(eventStore.append(event))

    private fun LocalDateTime.withinSpecialTimeFrame() = isAfter(now().minusSeconds(3))
}


private fun Double.asString() = String.format("%.2f", this)