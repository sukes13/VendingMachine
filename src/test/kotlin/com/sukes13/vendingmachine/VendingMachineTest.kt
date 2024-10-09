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
    private val invalidCoin = Coin(name = "invalid", diameter = 2.00, mass = 99.0, thickness = 66.0)

    @Test
    fun `When inserting a valid coin, active value is displayed by machine`() {
        val actual = VendingMachine().insert(COIN_FIFTY_CENT)

        assertThat(actual.display()).isEqualTo("0,50")
        assertThat(actual.insert(COIN_TWO_EURO).display()).isEqualTo("2,50")
    }

    @Test
    fun `When inserting a valid coin, coin is added to coin-registry`() {
        val actual = VendingMachine()
            .insert(COIN_FIFTY_CENT)
            .insert(COIN_FIFTY_CENT)
            .insert(COIN_ONE_CENT)

        assertThat(actual.availableCoins).containsExactlyInAnyOrder(COIN_FIFTY_CENT, COIN_FIFTY_CENT, COIN_ONE_CENT)
    }

    @Test
    fun `When inserting an invalid coin, active value is 0 and coin is returned`() {
        val actual = VendingMachine().insert(invalidCoin)

        assertThat(actual.coinChute).containsExactly(invalidCoin)
        assertThat(actual.availableCoins).isEmpty()
        assertThat(actual.display()).isEqualTo("INSERT COIN")
    }

    @ParameterizedTest
    @MethodSource("allProductsBoughtTest")
    fun `When button pressed with enough money inserted, product is in chute, price deducted and display shows thank you`(
        product: Product,
        coins: List<Coin>
    ) {
        val actual = coins
            .fold(VendingMachine()) { acc, coin -> acc.insert(coin) }
            .pressButton(product.code)

        assertThat(actual.chute).containsExactly(product)
        assertThat(actual.activeAmount).isEqualTo(0.0)
    }

    @Test
    fun `When button pressed with no money inserted, chute stays empty, display shows price`() {
        val actual = VendingMachine().pressButton("Cola")

        assertThat(actual.chute).isEmpty()
        assertThat(actual.activeAmount).isEqualTo(0.0)
        assertThat(actual.display()).isEqualTo("PRICE 1,00")
    }

    @Test
    fun `When product bought, display shows THANK YOU but changes to INSERT COIN after 3 seconds`() {
        val actual = VendingMachine().insert(COIN_ONE_EURO).pressButton("Cola")

        assertThat(actual.display()).isEqualTo("THANK YOU")
        await().atMost(Duration.ofMillis(3501)).untilAsserted {
            assertThat(actual.display()).isEqualTo("INSERT COIN")
        }
    }

    @Test
    fun `When button pressed with no money inserted, display shows PRICE but changes to INSERT COIN after 3 seconds`() {
        val actual = VendingMachine().pressButton("Cola")

        assertThat(actual.display()).isEqualTo("PRICE 1,00")
        await().atMost(Duration.ofMillis(3501)).untilAsserted {
            assertThat(actual.display()).isEqualTo("INSERT COIN")
        }
    }

    @Test
    fun `When button pressed with insufficient money inserted, display shows PRICE but changes to amount after 3 seconds`() {
        val actual = VendingMachine().insert(COIN_FIFTY_CENT).pressButton("Cola")

        assertThat(actual.display()).isEqualTo("PRICE 1,00")
        await().atMost(Duration.ofMillis(3501)).untilAsserted {
            assertThat(actual.display()).isEqualTo("0,50")
        }
    }

    @Test
    fun `When button pressed with too much money inserted, product bought and active amount is inserted minus price`() {
        val actual = VendingMachine().insert(COIN_TWO_EURO).pressButton(CANDY.code)

        assertThat(actual.chute).containsExactly(CANDY)
        assertThat(actual.activeAmount).isEqualTo(1.35)
    }

    @Test
    fun `When products taken from chute, chute is empty`() {
        val actual = VendingMachine().insert(COIN_TWO_EURO).pressButton(CANDY.code).takeProducts()

        assertThat(actual.chute).isEmpty()
    }

    @Test
    fun `When no coins inserted and 'return coins' is pressed, coin chute is empty`() {
        val actual = VendingMachine().pressReturnCoinsButton()

        assertThat(actual.coinChute).isEmpty()
    }

    @Test
    fun `When two coins inserted and return coins is pressed, coins in coin chute`() {
        val actual = VendingMachine().insert(COIN_TWO_EURO).insert(COIN_TWO_EURO).pressReturnCoinsButton()

        assertThat(actual.coinChute).containsExactlyInAnyOrder(COIN_TWO_EURO, COIN_TWO_EURO)
    }

    @Test
    fun `When COLA-button pressed while showing 'thank you' after a purchase, price of COLA is shown iso 'thank you'`() {
        val machineAfterPurchase = VendingMachine().insert(COIN_ONE_EURO).pressButton(COLA.code)
        assertThat(machineAfterPurchase.display()).isEqualTo("THANK YOU")

        await().timeout(Duration.ofSeconds(1)).untilAsserted {
            assertThat(machineAfterPurchase.pressButton(COLA.code).display()).isEqualTo("PRICE 1,00")
        }
    }

    @Test
    fun `When CANDY-button pressed while showing 'thank you' after a purchase, price of CANDY is shown iso 'thank you'`() {
        val machineAfterPurchase = VendingMachine().insert(COIN_ONE_EURO).pressButton(CANDY.code)
        assertThat(machineAfterPurchase.display()).isEqualTo("THANK YOU")

        await().timeout(Duration.ofSeconds(1)).untilAsserted {
            assertThat(machineAfterPurchase.pressButton(CANDY.code).display()).isEqualTo("PRICE 0,65")
        }
    }

    @Test
    fun `When some coins inserted and rejected, correct number of availableCoins in Machine`() {
        val actual = VendingMachine()
            .insert(COIN_TWO_EURO)
            .pressReturnCoinsButton()
            .insert(invalidCoin)
            .insert(COIN_TWO_EURO)

        assertThat(actual.activeAmount).isEqualTo(2.0)
        assertThat(actual.coinChute).containsExactlyInAnyOrder(COIN_TWO_EURO, invalidCoin)
        assertThat(actual.availableCoins).containsExactlyInAnyOrder(COIN_TWO_EURO)
    }

    @Test
    fun `When scenario with all actions happens, machine still works`() {
        val actual = VendingMachine().insert(COIN_TWO_EURO)
            .pressReturnCoinsButton()
            .insert(COIN_TWO_EURO)
            .insert(invalidCoin)
            .pressButton(CANDY.code)

        assertThat(actual.availableCoins).containsExactly(COIN_TWO_EURO)
        assertThat(actual.chute).containsExactly(CANDY)
        assertThat(actual.display()).isEqualTo("THANK YOU")
        assertThat(actual.takeProducts().chute).isEmpty()
        assertThat(actual.coinChute).containsExactlyInAnyOrder(
            COIN_TWO_EURO,
            invalidCoin,
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
