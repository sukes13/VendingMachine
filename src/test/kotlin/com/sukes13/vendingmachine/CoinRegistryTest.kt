package com.sukes13.vendingmachine

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class CoinRegistryTest {

    @ParameterizedTest
    @MethodSource("inCoinsInput")
    fun `When amount is provided, get it in as least coins as possible`(inputValue: Double, coinResult: List<Coin>) {
        assertThat(CoinRegistry.inCoins(inputValue)).containsExactlyInAnyOrder(*coinResult.toTypedArray())
    }

    companion object {
        @JvmStatic
        fun inCoinsInput(): Stream<Arguments> =
            Stream.of(
                Arguments.of(1.0, listOf(COIN_ONE_EURO)),
                Arguments.of(1.01, listOf(COIN_ONE_EURO, COIN_ONE_CENT)),
                Arguments.of(1.11, listOf(COIN_ONE_EURO, COIN_TEN_CENT, COIN_ONE_CENT)),
                Arguments.of(3.61, listOf(COIN_TWO_EURO,COIN_ONE_EURO, COIN_FIFTY_CENT,COIN_TEN_CENT, COIN_ONE_CENT)),
            )
    }
}