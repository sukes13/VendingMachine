package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.Product.COLA
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.util.stream.Stream

class VendingMachineTest {

    @ParameterizedTest
    @MethodSource("allValidCoinsTest")
    fun `When inserting a valid coin, value is displayed by machine`(coin: Coin, amount: String) {
        val actual = VendingMachine().insert(coin)

        assertThat(actual.display()).isEqualTo(amount)
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
        assertThat(actual.display()).isEqualTo("INSERT COIN")
    }

    @Test
    fun `When one euro inserted and cola button pressed, cola is in chute, price deducted and display shows thank you`() {
        val actual = VendingMachine().insert(COIN_ONE_EURO).pressButton("Cola")

        assertThat(actual.chute).containsExactly(COLA)
        assertThat(actual.currentAmount).isEqualTo(0.0)
        assertThat(actual.display()).isEqualTo("THANK YOU")
    }

    @Test
    fun `When cola button pressed, chute stays empty, display shows price`() {
        val actual = VendingMachine().pressButton("Cola")

        assertThat(actual.chute).isEmpty()
        assertThat(actual.currentAmount).isEqualTo(0.0)
        assertThat(actual.display()).isEqualTo("PRICE 1,00")
    }

    @Test
    fun `When product bought, display shows THANK YOU but changes to INSERT COIN after 3 seconds`() {
        val actual = VendingMachine().insert(COIN_ONE_EURO).pressButton("Cola")

        assertThat(actual.display()).isEqualTo("THANK YOU")
        await().atMost(Duration.ofMillis(3101)).untilAsserted {
            assertThat(actual.display()).isEqualTo("INSERT COIN")
        }
    }

    @Test
    fun `When product bought, display shows PRICE but changes to INSERT COIN after 3 seconds`() {
        val actual = VendingMachine().pressButton("Cola")

        assertThat(actual.display()).isEqualTo("PRICE 1,00")
        await().atMost(Duration.ofMillis(3101)).untilAsserted {
            assertThat(actual.display()).isEqualTo("INSERT COIN")
        }
    }

}
