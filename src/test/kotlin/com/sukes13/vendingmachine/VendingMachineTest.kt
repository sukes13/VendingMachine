package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.Product.*
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

    @Test
    fun `When inserting an invalid coin, value is not saved and coin is returned`() {
        val invalidCoin = COIN_ONE_CENT.copy(diameter = 2.00)
        val actual = VendingMachine().insert(invalidCoin)

        assertThat(actual.coinChute).containsExactly(invalidCoin)
        assertThat(actual.display()).isEqualTo("INSERT COIN")
    }

    @ParameterizedTest
    @MethodSource("allProductsBoughtTest")
    fun `When button pressed with enough money inserted, product is in chute, price deducted and display shows thank you`(
        product: Product,
        coins: List<Coin>
    ) {
        val actual = coins.fold(VendingMachine()) { acc, coin -> acc.insert(coin) }
            .pressButton(product.code)

        assertThat(actual.chute).containsExactly(product)
        assertThat(actual.currentAmount).isEqualTo(0.0)
    }

    @Test
    fun `When button pressed with no money inserted, chute stays empty, display shows price`() {
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
    fun `When button pressed with no money inserted, display shows PRICE but changes to INSERT COIN after 3 seconds`() {
        val actual = VendingMachine().pressButton("Cola")

        assertThat(actual.display()).isEqualTo("PRICE 1,00")
        await().atMost(Duration.ofMillis(3101)).untilAsserted {
            assertThat(actual.display()).isEqualTo("INSERT COIN")
        }
    }

    @Test
    fun `When button pressed with insufficient money inserted, display shows PRICE but changes to amount after 3 seconds`() {
        val actual = VendingMachine().insert(COIN_FIFTY_CENT).pressButton("Cola")

        assertThat(actual.display()).isEqualTo("PRICE 1,00")
        await().atMost(Duration.ofMillis(3101)).untilAsserted {
            assertThat(actual.display()).isEqualTo("0,50")
        }
    }

    @Test
    fun `When button pressed with too much money inserted, product bought and change in coin chute`() {
        val actual = VendingMachine().insert(COIN_TWO_EURO).pressButton(CANDY.code)

        assertThat(actual.chute).containsExactly(CANDY)
        assertThat(actual.coinChute).containsExactlyInAnyOrder(
            COIN_ONE_EURO,
            COIN_TWENTY_CENT,
            COIN_TEN_CENT,
            COIN_FIVE_CENT
        )
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

        @JvmStatic
        fun allProductsBoughtTest(): Stream<Arguments> =
            Stream.of(
                Arguments.of(COLA, listOf(COIN_ONE_EURO)),
                Arguments.of(CHIPS, listOf(COIN_FIFTY_CENT)),
                Arguments.of(CANDY, listOf(COIN_FIFTY_CENT, COIN_TEN_CENT, COIN_FIVE_CENT)),
            )
    }
}
