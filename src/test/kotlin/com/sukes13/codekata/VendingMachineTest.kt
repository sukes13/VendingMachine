package com.sukes13.codekata

import com.sukes13.codekata.CoinRegistry.valueOf
import com.sukes13.codekata.VendingEvent.AmountInsertedEvent
import com.sukes13.codekata.VendingEvent.CoinRejectedEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class VendingMachineTest {

    @ParameterizedTest
    @MethodSource("allValidCoinsTest")
    fun `When inserting a valid coin, value is amounted in machine`(coin: Coin, amount: Double) {
        val actual = VendingMachine().insert(coin)

        assertThat(actual.amount()).isEqualTo(amount)
    }

    companion object {
        @JvmStatic
        fun allValidCoinsTest(): Stream<Arguments> =
            Stream.of(
                Arguments.of(COIN_ONE_CENT, 0.01),
                Arguments.of(COIN_TWO_CENT, 0.02),
                Arguments.of(COIN_FIVE_CENT, 0.05),
                Arguments.of(COIN_TEN_CENT, 0.1),
                Arguments.of(COIN_TWENTY_CENT, 0.2),
                Arguments.of(COIN_FIFTY_CENT, 0.5),
                Arguments.of(COIN_ONE_EURO, 1.0),
                Arguments.of(COIN_TWO_EURO, 2.0),
            )
    }

    @Test
    fun `When inserting an invalid coin, value is not saved and coin is returned`() {
        val invalidCoin = COIN_ONE_CENT.copy(diameter = 2.00)
        val actual = VendingMachine().insert(invalidCoin)

        assertThat(actual.amount()).isEqualTo(0.0)
        assertThat(actual.coinChute()).containsExactly(invalidCoin)
    }
}

data class VendingMachine(
    val eventStore: EventStore = EventStore()
) {
    fun amount() = eventStore.filterEvents<AmountInsertedEvent>().sumOf { it.amount }
    fun coinChute() = eventStore.filterEvents<CoinRejectedEvent>().map { it.coin }

    fun insert(coin: Coin) =
        coin.valueOf()
            ?.let { VendingMachine(eventStore.append(AmountInsertedEvent(it))) }
            ?: VendingMachine(eventStore.append(CoinRejectedEvent(coin)))
}

val COIN_ONE_CENT = Coin(16.25, 1.67, 2.30)
val COIN_TWO_CENT = Coin(18.75, 1.67, 3.06)
val COIN_FIVE_CENT = Coin(21.25, 1.67, 3.92)
val COIN_TEN_CENT = Coin(19.75, 1.93, 4.10)
val COIN_TWENTY_CENT = Coin(22.25, 2.14, 5.74)
val COIN_FIFTY_CENT = Coin(24.25, 2.38, 7.80)
val COIN_ONE_EURO = Coin(23.25, 2.33, 7.50)
val COIN_TWO_EURO = Coin(25.75, 2.20, 8.50)

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

    fun Coin.valueOf() = registry[this]
}

data class Coin(
    private val diameter: Double,
    private val thickness: Double,
    private val mass: Double
)


interface VendingEvent {
    class AmountInsertedEvent(val amount: Double) : VendingEvent
    class CoinRejectedEvent(val coin: Coin) : VendingEvent
}

data class EventStore(val events: List<VendingEvent> = emptyList()) : List<VendingEvent> by events {
    fun append(event: VendingEvent) = copy(events = events + event)

    inline fun <reified T : VendingEvent> filterEvents() = events.filterIsInstance<T>()
}