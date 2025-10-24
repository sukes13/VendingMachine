package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.CoinRegistry.value
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class CoinTest {

    @ParameterizedTest
    @MethodSource("allValidCoinsTest")
    fun `When inserting a valid coin, value is returned`(coin: Coin, amount: Double) {
        assertThat(coin.value()).isEqualTo(amount)
    }

    @ParameterizedTest
    @MethodSource("inAvailableCoinsInput")
    fun `When amount is provided, get it in as least available coins as possible`(inputValue: Double, availableCoins: List<Coin>, coinResult: List<Coin>) {
        assertThat(CoinRegistry.inAvailableCoins(remainder = inputValue, availableCoins = availableCoins)).containsExactlyInAnyOrder(*coinResult.toTypedArray())
    }

    companion object {
        @JvmStatic
        fun allValidCoinsTest(): Stream<Arguments> =
            Stream.of(
                Arguments.of(COIN_ONE_CENT, 0.01),
                Arguments.of(COIN_TWO_CENT, 0.02),
                Arguments.of(COIN_FIVE_CENT, 0.05),
                Arguments.of(COIN_TEN_CENT, 0.10),
                Arguments.of(COIN_TWENTY_CENT, 0.20),
                Arguments.of(COIN_FIFTY_CENT, 0.50),
                Arguments.of(COIN_ONE_EURO, 1.00),
                Arguments.of(COIN_TWO_EURO, 2.00),
            )

        @JvmStatic
        fun inAvailableCoinsInput(): Stream<Arguments> =
            Stream.of(
                Arguments.of(1.0, listOf(COIN_ONE_EURO), listOf(COIN_ONE_EURO)),
                Arguments.of(2.2, listOf(COIN_ONE_EURO, COIN_ONE_EURO, COIN_TWENTY_CENT), listOf(COIN_ONE_EURO, COIN_ONE_EURO, COIN_TWENTY_CENT)),
                Arguments.of(
                    1.85,
                    listOf(COIN_TWO_EURO, COIN_ONE_EURO, COIN_ONE_EURO, COIN_FIFTY_CENT, COIN_TWENTY_CENT, COIN_TWENTY_CENT, COIN_TEN_CENT, COIN_FIVE_CENT, COIN_FIVE_CENT, COIN_ONE_CENT),
                    listOf(COIN_ONE_EURO, COIN_FIFTY_CENT, COIN_TWENTY_CENT, COIN_TEN_CENT, COIN_FIVE_CENT)
                ),

                Arguments.of(1.0, emptyList<Coin>(), emptyList<Coin>()),
                Arguments.of(1.0, listOf(COIN_TWO_EURO), null),
                Arguments.of(1.0, listOf(COIN_FIVE_CENT), null),
                Arguments.of(1.5, listOf(COIN_ONE_EURO), null),
            )

    }
}