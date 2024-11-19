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

data class Machine(
    val eventStore: EventStore = EventStore()
) {
    private val vendingMachine get() = VendingMachine.createFrom(eventStore)
    private fun copyAndAdd(newEvents: List<VendingEvent>) = copy(eventStore = eventStore.publish(newEvents))

    //Write
    fun insert(coin: Coin) = copyAndAdd(vendingMachine.insert(coin))
    fun pressButton(productName: String) = copyAndAdd(vendingMachine.pressButton(productName))
    fun takeProducts()= copyAndAdd(vendingMachine.takeProducts())
    fun pressReturnCoinsButton() = copyAndAdd(vendingMachine.pressReturnCoinsButton())
    fun takeCoins() = copyAndAdd(vendingMachine.takeCoins())

    //Read
    fun display() = vendingMachine.display()
    val chute get() = vendingMachine.chute
    val coinChute get() = vendingMachine.coinChute
    val activeAmount get() = vendingMachine.activeAmount
}


data class VendingMachine(private val eventStore: EventStore = EventStore()) {
    val activeAmount get()  = eventStore.eventsOfType<ActiveAmountIncreasedEvent>().sumOf { it.value } -
            eventStore.eventsOfType<ActiveAmountDecreasedEvent>().sumOf { it.value }
    val chute get() = eventStore.eventsSinceLast<ProductsTakenEvent>().eventsOfType<ProductBoughtEvent>().map { it.product }
    val coinChute get() = eventStore.eventsSinceLast<CoinsTakenEvent>().eventsOfType<CoinReturnedEvent>().map { it.coin }
    private val currentTimedEvents get() = eventStore.eventsOfType<TimedVendingEvent>().filter { it.occurredOn.withinTimeFrame() }

    fun display() =
        if (currentTimedEvents.isEmpty()) defaultMessage()
        else temporaryMessage(currentTimedEvents.maxBy { it.occurredOn })

    fun insert(coin: Coin) =
        coin.value()?.let {
            listOf(
                CoinAddedEvent(coin),
                ActiveAmountIncreasedEvent(it)
            )
        } ?: listOf(CoinReturnedEvent(coin))

    fun pressButton(productCode: String) =
        Product.toProduct(productCode).let { product ->
            when {
                activeAmount >= product.price() -> buyProductAndCharge(product)
                else -> listOf(ButtonPressed(product))
            }
        }

    fun pressReturnCoinsButton() =
        CoinRegistry.inCoins(activeAmount)
            .map { CoinReturnedEvent(it) }
            .plus(ActiveAmountDecreasedEvent(activeAmount))

    fun takeProducts() = listOf(ProductsTakenEvent())
    fun takeCoins() = listOf(CoinsTakenEvent())

    private fun buyProductAndCharge(product: Product) =
        listOf(
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

    companion object {
        fun createFrom(eventStore: EventStore): VendingMachine {
            return VendingMachine(eventStore)
        }
    }


}

private fun Double.asString() = String.format("%.2f", this)
internal fun Double.minusPrecise(second: Double) = (toBigDecimal() - second.toBigDecimal()).toDouble()
private fun LocalDateTime.withinTimeFrame() = isAfter(now().minusSeconds(3))
