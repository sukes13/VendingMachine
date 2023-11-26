package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.CoinRegistry.value
import com.sukes13.vendingmachine.Product.COLA
import com.sukes13.vendingmachine.ProductRegistry.value
import com.sukes13.vendingmachine.VendingEvent.*


data class VendingMachine(
    val eventStore: EventStore = EventStore()
) {
    private val amount = eventStore.filterEvents<AmountInsertedEvent>().sumOf { it.amount }
    val coinChute = eventStore.filterEvents<CoinRejectedEvent>().map { it.coin }
    val chute: List<Product> = eventStore.filterEvents<ProductBoughtEvent>().map { it.product }
    val display =
        if (eventStore.isNotEmpty() && eventStore.last() is ProductBoughtEvent) "THANK YOU"
        else when (amount) {
            0.0 -> "INSERT COIN"
            else -> amount.asString()
        }

    fun insert(coin: Coin) =
        coin.value()
            ?.let { VendingMachine(eventStore.append(AmountInsertedEvent(it))) }
            ?: VendingMachine(eventStore.append(CoinRejectedEvent(coin)))

    fun pressButton(productCode: String): VendingMachine {
        val product = Product.toProduct(productCode)
        return product.value()?.let {
            if (amount >= it) VendingMachine(eventStore.append(ProductBoughtEvent(product)))
            else VendingMachine(eventStore.append(ButtonPressed(product)))
        } ?: error("No product linked to this bu")
    }

}

val COIN_ONE_CENT = Coin(16.25, 1.67, 2.30)
val COIN_TWO_CENT = Coin(18.75, 1.67, 3.06)
val COIN_FIVE_CENT = Coin(21.25, 1.67, 3.92)
val COIN_TEN_CENT = Coin(19.75, 1.93, 4.10)
val COIN_TWENTY_CENT = Coin(22.25, 2.14, 5.74)
val COIN_FIFTY_CENT = Coin(24.25, 2.38, 7.80)
val COIN_ONE_EURO = Coin(23.25, 2.33, 7.50)
val COIN_TWO_EURO = Coin(25.75, 2.20, 8.50)

data class Coin(
    private val diameter: Double,
    private val thickness: Double,
    private val mass: Double
)

object CoinRegistry {
    private val registry = mapOf(
        COIN_ONE_CENT to 0.01,
        COIN_TWO_CENT to 0.02,
        COIN_FIVE_CENT to 0.05,
        COIN_TEN_CENT to 0.1,
        COIN_TWENTY_CENT to 0.2,
        COIN_FIFTY_CENT to 0.5,
        COIN_ONE_EURO to 1.0,
        COIN_TWO_EURO to 2.0,
    )

    fun Coin.value() = registry[this]
}

enum class Product(private val productCode: String) {
    COLA("Cola");

    companion object {
        fun toProduct(code: String) = values().single { it.productCode == code }
    }
}

object ProductRegistry {
    private val registry = mapOf(
        COLA to 1.0,
    )

    fun Product.value() = registry[this]
}

interface VendingEvent {
    class AmountInsertedEvent(val amount: Double) : VendingEvent
    class CoinRejectedEvent(val coin: Coin) : VendingEvent
    class ProductBoughtEvent(val product: Product) : VendingEvent
    class ButtonPressed(val product: Product) : VendingEvent
}

data class EventStore(val events: List<VendingEvent> = emptyList()) : List<VendingEvent> by events {
    fun append(event: VendingEvent) = copy(events = events + event)

    inline fun <reified T : VendingEvent> filterEvents() = events.filterIsInstance<T>()
}

private fun Double.asString() = String.format("%.2f", this)