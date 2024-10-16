package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.CoinRegistry.value
import com.sukes13.vendingmachine.ProductRegistry.price
import com.sukes13.vendingmachine.VendingEvent.*
import com.sukes13.vendingmachine.VendingEvent.ActiveAmountEvent.ActiveAmountDecreasedEvent
import com.sukes13.vendingmachine.VendingEvent.ActiveAmountEvent.ActiveAmountIncreasedEvent
import com.sukes13.vendingmachine.VendingEvent.CoinEvent.CoinAddedEvent
import com.sukes13.vendingmachine.VendingEvent.CoinEvent.CoinReturnedEvent
import com.sukes13.vendingmachine.VendingEvent.TimedVendingEvent.ButtonPressed
import com.sukes13.vendingmachine.VendingEvent.TimedVendingEvent.ProductBoughtEvent
import java.time.LocalDateTime
import java.time.LocalDateTime.now


class VendingMachine(
    val eventStore: EventStore = EventStore()
) {
    val activeAmount: Double
        get() = eventStore.eventsOfType<ActiveAmountIncreasedEvent>().sumOf { it.value } -
                eventStore.eventsOfType<ActiveAmountDecreasedEvent>().sumOf { it.value }

    val chute = eventStore.eventsSinceLast<ProductsTakenEvent>().eventsOfType<ProductBoughtEvent>().map { it.product }

    val coinChute = eventStore.eventsSinceLast< CoinsTakenEvent>().eventsOfType<CoinReturnedEvent>().map { it.coin }

    fun display() =
        eventStore.eventsOfType<TimedVendingEvent>().filter { it.occurredOn.withinTimeFrame() }
            .let { determineDisplayMessage(it) }

    private fun determineDisplayMessage(activeTimedEvents: List<TimedVendingEvent>) =
        if (activeTimedEvents.isEmpty()) defaultMessage()
        else temporaryMessage(activeTimedEvents.maxBy { it.occurredOn })

    fun insert(coin: Coin) =
        coin.value()?.let {
            copyAndAdd(
                CoinAddedEvent(coin),
                ActiveAmountIncreasedEvent(it)
            )
        } ?: copyAndAdd(CoinReturnedEvent(coin))

    fun pressButton(productCode: String) =
        Product.toProduct(productCode).let { product ->
            when {
                activeAmount >= product.price() -> buyProductAndCharge(product)
                else -> copyAndAdd(ButtonPressed(product))
            }
        }

    fun pressReturnCoinsButton() =
        CoinRegistry.inCoins(activeAmount)
            .map { CoinReturnedEvent(it) }
            .plus(ActiveAmountDecreasedEvent(activeAmount))
            .let { eventsToAdd -> copyAndAdd(eventsToAdd) }

    fun takeProducts() = copyAndAdd(ProductsTakenEvent())
    fun takeCoins() = copyAndAdd( CoinsTakenEvent())    
    
    private fun buyProductAndCharge(product: Product) =
        copyAndAdd(
            ActiveAmountDecreasedEvent(value = product.price()),
            ProductBoughtEvent(product)
        )

    private fun temporaryMessage(event: TimedVendingEvent) =
        when (event) {
            is ButtonPressed -> "PRICE ${event.product.price().asString()}"
            is ProductBoughtEvent -> "THANK YOU"
        }

    private fun defaultMessage() =
        when (activeAmount) {
            0.0 -> "INSERT COIN"
            else -> activeAmount.asString()
        }

    private fun copyAndAdd(vararg events: VendingEvent) =
        VendingMachine(eventStore = eventStore.append(events.toList()))

    private fun copyAndAdd(events: List<VendingEvent>): VendingMachine = copyAndAdd(*events.toTypedArray())

}

private fun Double.asString() = String.format("%.2f", this)
internal fun Double.minusPrecise(second: Double) = (toBigDecimal() - second.toBigDecimal()).toDouble()
private fun LocalDateTime.withinTimeFrame() = isAfter(now().minusSeconds(3))
