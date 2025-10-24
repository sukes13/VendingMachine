package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.CoinRegistry.value
import com.sukes13.vendingmachine.ProductRegistry.price
import com.sukes13.vendingmachine.VendingEvent.*
import com.sukes13.vendingmachine.VendingEvent.ActiveAmountEvent.ActiveAmountDecreasedEvent
import com.sukes13.vendingmachine.VendingEvent.ActiveAmountEvent.ActiveAmountIncreasedEvent
import com.sukes13.vendingmachine.VendingEvent.CoinEvent.CoinAddedEvent
import com.sukes13.vendingmachine.VendingEvent.CoinEvent.CoinReturnedEvent
import com.sukes13.vendingmachine.VendingEvent.TimedVendingEvent.ButtonPressed
import com.sukes13.vendingmachine.VendingEvent.TimedVendingEvent.InsufficientFunds
import com.sukes13.vendingmachine.VendingEvent.TimedVendingEvent.ProductBoughtEvent
import java.time.LocalDateTime
import java.time.LocalDateTime.now

data class VendingMachine private constructor(
    val activeAmount: Double = 0.0,
    val chute: List<Product> = emptyList(),
    val coinChute: List<Coin> = emptyList(),
    private val currentTimedEvents: List<TimedVendingEvent> = emptyList(),
    val availableCoins: List<Coin>,
) {
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
        CoinRegistry.inAvailableCoins(remainder = activeAmount, availableCoins = availableCoins)
            ?.map { CoinReturnedEvent(it) }
            ?.plus(ActiveAmountDecreasedEvent(activeAmount))
            ?: listOf(InsufficientFunds())

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
            is InsufficientFunds -> "INSUFFICIENT COINS"
        }

    private fun defaultMessage() =
        when (activeAmount) {
            0.0 -> "INSERT COIN"
            else -> activeAmount.asString()
        }

    companion object {
        fun createFrom(eventStore: EventStore): VendingMachine {
            val activeAmount  = eventStore.eventsOfType<ActiveAmountIncreasedEvent>().sumOf { it.value } -
                    eventStore.eventsOfType<ActiveAmountDecreasedEvent>().sumOf { it.value }
            val availableCoins = eventStore.eventsOfType<CoinAddedEvent>().map { it.coin } -
                    eventStore.eventsOfType<CoinReturnedEvent>().map { it.coin }.toSet()
            val chute  = eventStore.eventsSinceLast<ProductsTakenEvent>().eventsOfType<ProductBoughtEvent>().map { it.product }
            val coinChute  = eventStore.eventsSinceLast<CoinsTakenEvent>().eventsOfType<CoinReturnedEvent>().map { it.coin }
            val currentTimedEvents  = eventStore.eventsOfType<TimedVendingEvent>().filter { it.occurredOn.withinTimeFrame() }

            return VendingMachine(activeAmount = activeAmount,availableCoins = availableCoins, chute = chute, coinChute = coinChute, currentTimedEvents = currentTimedEvents)
        }
    }


}

private fun Double.asString() = String.format("%.2f", this)
internal fun Double.minusPrecise(second: Double) = (toBigDecimal() - second.toBigDecimal()).toDouble()
private fun LocalDateTime.withinTimeFrame() = isAfter(now().minusSeconds(3))
