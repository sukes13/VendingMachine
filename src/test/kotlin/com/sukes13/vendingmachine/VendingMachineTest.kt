package com.sukes13.vendingmachine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class VendingMachineTest {

    @ParameterizedTest
    @MethodSource("allValidCoinsTest")
    fun `When inserting a valid coin, value is displayed by machine`(coin: Coin, amount: String) {
        val actual = VendingMachine().insert(coin)

        assertThat(actual.display).isEqualTo(amount)
    }

    companion object {
        @JvmStatic
        fun allValidCoinsTest(): Stream<Arguments> =
            Stream.of(
                Arguments.of(COIN_ONE_CENT, "0,01"),
                Arguments.of(COIN_TWO_CENT, "0,02"),
                Arguments.of(COIN_FIVE_CENT, "0,05"),
                Arguments.of(COIN_TEN_CENT, "0,10"),
                Arguments.of(COIN_TWENTY_CENT, "0,20"),
                Arguments.of(COIN_FIFTY_CENT, "0,50"),
                Arguments.of(COIN_ONE_EURO, "1,00"),
                Arguments.of(COIN_TWO_EURO, "2,00"),
            )
    }

    @Test
    fun `When inserting an invalid coin, value is not saved and coin is returned`() {
        val invalidCoin = COIN_ONE_CENT.copy(diameter = 2.00)
        val actual = VendingMachine().insert(invalidCoin)

        assertThat(actual.coinChute).containsExactly(invalidCoin)
        assertThat(actual.display).isEqualTo("INSERT COIN")
    }
}
