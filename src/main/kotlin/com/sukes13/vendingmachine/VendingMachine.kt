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
    fun insert(coin: Coin) = VendingMachine(eventStore).insert(coin, this)
    fun display() = VendingMachine(eventStore).display()
    
    fun publishEvents(vararg events: VendingEvent) =
        copy(eventStore = eventStore.publish(events.toList()))

    fun publishEvents(events: List<VendingEvent>) = 
        publishEvents(*events.toTypedArray())

    fun pressButton(productName: String) = VendingMachine(eventStore).pressButton(productName,this)
    fun takeProducts()= VendingMachine(eventStore).takeProducts(this)
    fun pressReturnCoinsButton() = VendingMachine(eventStore).pressReturnCoinsButton(this)
    fun takeCoins() = VendingMachine(eventStore).takeCoins(this)

    //Read
    val chute = VendingMachine(eventStore).chute
    val coinChute = VendingMachine(eventStore).coinChute
    val activeAmount = VendingMachine(eventStore).activeAmount
}


//TODO: split read and write sides
class VendingMachine {
    val activeAmount: Double
    val chute: List<Product>
    val coinChute: List<Coin>
    val currentTimedEvents: List<TimedVendingEvent>

    constructor(eventStore: EventStore = EventStore()) {
        this.activeAmount = eventStore.eventsOfType<ActiveAmountIncreasedEvent>().sumOf { it.value } -
                eventStore.eventsOfType<ActiveAmountDecreasedEvent>().sumOf { it.value }
        this.chute =
            eventStore.eventsSinceLast<ProductsTakenEvent>().eventsOfType<ProductBoughtEvent>().map { it.product }
        this.coinChute = eventStore.eventsSinceLast<CoinsTakenEvent>().eventsOfType<CoinReturnedEvent>().map { it.coin }
        this.currentTimedEvents =
            eventStore.eventsOfType<TimedVendingEvent>().filter { it.occurredOn.withinTimeFrame() }
    }

    fun display() =
        if (currentTimedEvents.isEmpty()) defaultMessage()
        else temporaryMessage(currentTimedEvents.maxBy { it.occurredOn })

    fun insert(coin: Coin, machine: Machine) =
        coin.value()?.let {
            machine.publishEvents(
                CoinAddedEvent(coin),
                ActiveAmountIncreasedEvent(it)
            )
        } ?: machine.publishEvents(CoinReturnedEvent(coin))

    fun pressButton(productCode: String, machine: Machine) =
        Product.toProduct(productCode).let { product ->
            when {
                activeAmount >= product.price() -> buyProductAndCharge(product, machine)
                else -> machine.publishEvents(ButtonPressed(product))
            }
        }

    fun pressReturnCoinsButton(machine: Machine) =
        CoinRegistry.inCoins(activeAmount)
            .map { CoinReturnedEvent(it) }
            .plus(ActiveAmountDecreasedEvent(activeAmount))
            .let { eventsToAdd -> machine.publishEvents(eventsToAdd) }

    fun takeProducts(machine: Machine) = machine.publishEvents(ProductsTakenEvent())
    fun takeCoins(machine: Machine) = machine.publishEvents(CoinsTakenEvent())

    private fun buyProductAndCharge(product: Product, machine: Machine) =
        machine.publishEvents(
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


}

private fun Double.asString() = String.format("%.2f", this)
internal fun Double.minusPrecise(second: Double) = (toBigDecimal() - second.toBigDecimal()).toDouble()
private fun LocalDateTime.withinTimeFrame() = isAfter(now().minusSeconds(3))
