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
    val chute = eventStore.eventsSinceOccurrenceOf<ProductsTakenEvent>()
        .filterIsInstance<ProductBoughtEvent>()
        .map { it.product }
    val coinChute = eventStore.eventsOfType<CoinReturnedEvent>()
        .map { it.coin }

    fun insert(coin: Coin) = coin.value()
        ?.let { copyAndAdd(CoinAcceptedEvent(it)) }
        ?: copyAndAdd(CoinReturnedEvent(coin))

    fun pressButton(productCode: String) =
        Product.toProduct(productCode).let { product ->
            when {
                currentAmount >= product.price() -> buyProductAndReturnChange(product)
                else -> copyAndAdd(ButtonPressed(product))
            }
        }

    private fun buyProductAndReturnChange(product: Product): VendingMachine {
        copyAndAdd(ProductBoughtEvent(product)).let {
            val remainder = currentAmount.minusPrecise(product.price())

            return CoinRegistry.inCoins(remainder).fold(it) { acc, coin ->
                acc.copyAndAdd(CoinReturnedEvent(coin))
            }
        }
    }

    fun display() =
        eventStore.lastEventOrNull().let { event ->
            if (event is TimedVendingEvent && event.occurredOn.withinTimeFrame())
                temporaryMessage(event)
            else defaultMessage()
        }

    fun takeProducts() = copyAndAdd(ProductsTakenEvent)

    private fun temporaryMessage(event: TimedVendingEvent) =
        when (event) {
            is ButtonPressed -> "PRICE ${event.product.price().asString()}"
            is ProductBoughtEvent -> "THANK YOU"
        }

    private fun defaultMessage() =
        when (currentAmount) {
            0.0 -> "INSERT COIN"
            else -> currentAmount.asString()
        }
    private fun copyAndAdd(event: VendingEvent) = copy(eventStore = eventStore.append(event))

}

private fun Double.asString() = String.format("%.2f", this)
internal fun Double.minusPrecise(second: Double) = (toBigDecimal() - second.toBigDecimal()).toDouble()
private fun LocalDateTime.withinTimeFrame() = isAfter(now().minusSeconds(3))
