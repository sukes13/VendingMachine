package com.sukes13.codekata

import com.sukes13.codekata.CoinRegistry.valueOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VendingMachineTest {

    @Test
    fun `When inserting a valid coin, value is saved in machine`() {
        val actual = VendingMachine().insert(COIN_ONE_CENT)

        assertThat(actual.amount).isEqualTo(0.01)
    }
}

data class VendingMachine(val amount: Double = 0.0) {
    fun insert(coin: Coin) = copy(amount = amount + coin.valueOf())
}


val COIN_ONE_CENT = Coin(16.25, 1.67, 2.30)

object CoinRegistry {
    private val registry = mapOf(
        COIN_ONE_CENT to 0.01
    )

    fun Coin.valueOf() = registry[this] ?: throw InvalidCoinException()
}

class InvalidCoinException : Exception()

data class Coin(
    private val diameter: Double,
    private val thickness: Double,
    private val mass: Double
)


