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
    val availableCoins = run {
        val coins = mutableListOf<Coin>()
        eventStore.forEach {
            when (it) {
                is CoinAcceptedEvent -> coins.add(it.coin)
                is CoinReturnedEvent -> coins.remove(it.coin)
                is ProductBoughtEvent -> coins.removeAll(CoinRegistry.inCoins(it.product.price()))
                else -> coins
            }
        }
        coins
    }

    val currentAmount = availableCoins.sumOf { it.value() ?: 0.0 }

    //TODO: add products to ProductsTakenEvent and introduce inventory so eventsSinceLast can be removed here too
    val chute = eventStore.eventsSinceLast<ProductsTakenEvent>().eventsOfType<ProductBoughtEvent>().map { it.product }

    val coinChute = eventStore.eventsOfType<CoinReturnedEvent>().map { it.coin }

    fun display() =
        eventStore.lastOrNull { it !is CoinReturnedEvent }.let { event ->
            if (event is TimedVendingEvent && event.occurredOn.withinTimeFrame())
                temporaryMessage(event)
            else defaultMessage()
        }

    fun insert(coin: Coin) = coin.value()
        ?.let { copyAndAdd(CoinAcceptedEvent(coin)) }
        ?: copyAndAdd(CoinReturnedEvent(coin))

    fun pressButton(productCode: String) =
        Product.toProduct(productCode).let { product ->
            when {
                currentAmount >= product.price() -> buyProductAndReturnChange(product)
                else -> copyAndAdd(ButtonPressed(product))
            }
        }

    fun pressReturnCoinsButton() = availableCoins.addAsCoinReturnedEventsTo(this)

    fun takeProducts() = copyAndAdd(ProductsTakenEvent(chute))

    private fun buyProductAndReturnChange(product: Product): VendingMachine {
        copyAndAdd(ProductBoughtEvent(product)).let {
            val remainder = currentAmount.minusPrecise(product.price())
            return CoinRegistry.inCoins(remainder).addAsCoinReturnedEventsTo(it)
        }
    }

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

    private fun List<Coin>.addAsCoinReturnedEventsTo(vendingMachine: VendingMachine) =
        fold(listOf<VendingEvent>()) { newEvents, coin -> newEvents + CoinReturnedEvent(coin) }
            .let { vendingMachine.copyAndAdd(*it.toTypedArray()) }

    private fun copyAndAdd(vararg events: VendingEvent) = copy(eventStore = eventStore.append(events.toList()))

}

sealed interface VendingEvent {
    data class CoinAcceptedEvent(val coin: Coin) : VendingEvent
    data class CoinReturnedEvent(val coin: Coin) : VendingEvent
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
