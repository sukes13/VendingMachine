package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.CoinRegistry.value
import com.sukes13.vendingmachine.ProductRegistry.price
import com.sukes13.vendingmachine.VendingEvent.*
import com.sukes13.vendingmachine.VendingEvent.ActiveAmountEvent.ActiveAmountIncreasedEvent
import com.sukes13.vendingmachine.VendingEvent.CoinEvent.*
import com.sukes13.vendingmachine.VendingEvent.TimedVendingEvent.*
import java.time.LocalDateTime
import java.time.LocalDateTime.now


data class VendingMachine(
    val eventStore: EventStore = EventStore()
) {
    val activeAmount = eventStore.eventsOfType<ActiveAmountIncreasedEvent>().sumOf { it.value } -
            eventStore.eventsOfType<ActiveAmountEvent.ActiveAmountDecreasedEvent>().sumOf { it.value }

    val availableCoins = CoinRegistry.inCoins(activeAmount)

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
        ?.let { copyAndAdd(CoinAddedEvent(coin)) }
        ?: copyAndAdd(CoinReturnedEvent(coin))

    fun pressButton(productCode: String) =
        Product.toProduct(productCode).let { product ->
            when {
                activeAmount >= product.price() -> buyProductAndReturnChange(product)
                else -> copyAndAdd(ButtonPressed(product))
            }
        }

    fun pressReturnCoinsButton() = availableCoins.addAsCoinReturnedEventsTo(this)

    fun takeProducts() = copyAndAdd(ProductsTakenEvent(chute))

    private fun buyProductAndReturnChange(product: Product) =
        processCoinsForSell(product).copyAndAdd(ProductBoughtEvent(product))

    private fun processCoinsForSell(product: Product): VendingMachine {
        val vendingMachineTemp = CoinRegistry.inCoins(product.price(), availableCoins).addAsCoinUserEventsTo(this)
        val remainder = vendingMachineTemp.activeAmount.minusPrecise(product.price())
        return CoinRegistry.inCoins(remainder).addAsCoinReturnedEventsTo(vendingMachineTemp)
    }

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

    private fun List<Coin>.addAsCoinReturnedEventsTo(vendingMachine: VendingMachine) =
        fold(listOf<VendingEvent>()) { newEvents, coin -> newEvents + CoinReturnedEvent(coin) }
            .let { vendingMachine.copyAndAdd(*it.toTypedArray()) }

    private fun List<Coin>.addAsCoinUserEventsTo(vendingMachine: VendingMachine) =
        fold(listOf<VendingEvent>()) { newEvents, coin -> newEvents + CoinUsedEvent(coin) }
            .let { vendingMachine.copyAndAdd(*it.toTypedArray()) }

    private fun copyAndAdd(vararg events: VendingEvent) = copy(eventStore = eventStore.append(events.toList()))

    fun allCoins() = eventStore.eventsOfType<CoinAddedEvent>().map { it.coin }


}

sealed interface VendingEvent {
    sealed interface CoinEvent : VendingEvent {
        val coin: Coin

        data class CoinAddedEvent(override val coin: Coin) : CoinEvent
        data class CoinReturnedEvent(override val coin: Coin) : CoinEvent
        data class CoinUsedEvent(override val coin: Coin) : CoinEvent
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
