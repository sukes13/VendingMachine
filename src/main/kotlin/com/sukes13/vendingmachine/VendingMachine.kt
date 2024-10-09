package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.CoinRegistry.minusCoins
import com.sukes13.vendingmachine.CoinRegistry.value
import com.sukes13.vendingmachine.ProductRegistry.price
import com.sukes13.vendingmachine.VendingEvent.ActiveAmountEvent.ActiveAmountDecreasedEvent
import com.sukes13.vendingmachine.VendingEvent.ActiveAmountEvent.ActiveAmountIncreasedEvent
import com.sukes13.vendingmachine.VendingEvent.CoinEvent.CoinAddedEvent
import com.sukes13.vendingmachine.VendingEvent.CoinEvent.CoinReturnedEvent
import com.sukes13.vendingmachine.VendingEvent.ProductsTakenEvent
import com.sukes13.vendingmachine.VendingEvent.TimedVendingEvent
import com.sukes13.vendingmachine.VendingEvent.TimedVendingEvent.ButtonPressed
import com.sukes13.vendingmachine.VendingEvent.TimedVendingEvent.ProductBoughtEvent
import java.time.LocalDateTime
import java.time.LocalDateTime.now


data class VendingMachine(
    val eventStore: EventStore = EventStore()
) {
    val activeAmount: Double
        get() = eventStore.eventsOfType<ActiveAmountIncreasedEvent>().sumOf { it.value } -
                eventStore.eventsOfType<ActiveAmountDecreasedEvent>().sumOf { it.value }

    val availableCoins: List<Coin>
        get() = eventStore.eventsOfType<CoinAddedEvent>().map { it.coin }
            .minusCoins(eventStore.eventsOfType<CoinReturnedEvent>().map { it.coin })

    val chute = eventStore.eventsSinceLast<ProductsTakenEvent>().eventsOfType<ProductBoughtEvent>().map { it.product }

    val coinChute = eventStore.eventsOfType<CoinReturnedEvent>().map { it.coin }

    fun display() =
        eventStore.eventsOfType<TimedVendingEvent>().filter { it.occurredOn.withinTimeFrame() }
            .let { toDisplayMessage(it) }

    private fun toDisplayMessage(activeTimedEvents: List<TimedVendingEvent>) =
        if (activeTimedEvents.isEmpty()) defaultMessage()
        else temporaryMessage(activeTimedEvents.maxBy { it.occurredOn })

    fun insert(coin: Coin) = coin.value()
        ?.let {
            validCoinAdded(coin, it)
        } ?: copyAndAdd(CoinReturnedEvent(coin))

    private fun validCoinAdded(coin: Coin, value: Double): VendingMachine = copyAndAdd(
        CoinAddedEvent(coin),
        ActiveAmountIncreasedEvent(value)
    )

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
            .let { copyAndAdd(*it.toTypedArray()) }

    fun takeProducts() = copyAndAdd(ProductsTakenEvent(chute))

    private fun buyProductAndCharge(product: Product) =
        listOf(ActiveAmountDecreasedEvent(value = product.price()), ProductBoughtEvent(product))
            .let { copyAndAdd(*it.toTypedArray()) }

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

    private fun copyAndAdd(vararg events: VendingEvent) = copy(eventStore = eventStore.append(events.toList()))


}

sealed interface VendingEvent {
    sealed interface CoinEvent : VendingEvent {
        val coin: Coin

        data class CoinAddedEvent(override val coin: Coin) : CoinEvent
        data class CoinReturnedEvent(override val coin: Coin) : CoinEvent
    }

    sealed interface ActiveAmountEvent : VendingEvent {
        data class ActiveAmountIncreasedEvent(val value: Double) : ActiveAmountEvent
        data class ActiveAmountDecreasedEvent(val value: Double) : ActiveAmountEvent
    }

    data class ProductsTakenEvent(val products: List<Product>) : VendingEvent

    sealed class TimedVendingEvent : VendingEvent {
        val occurredOn: LocalDateTime = now()

        class ProductBoughtEvent(val product: Product) : TimedVendingEvent()
        class ButtonPressed(val product: Product) : TimedVendingEvent()
    }
}


private fun Double.asString() = String.format("%.2f", this)
internal fun Double.minusPrecise(second: Double) = (toBigDecimal() - second.toBigDecimal()).toDouble()
private fun LocalDateTime.withinTimeFrame() = isAfter(now().minusSeconds(3))
