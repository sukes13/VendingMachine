package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.MachineCommand.*
import com.sukes13.vendingmachine.MachineQuery.*
import com.sukes13.vendingmachine.Product.*
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Disabled
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
        val actual = Machine().execute(InsertCoin(COIN_FIFTY_CENT))

        assertThat(actual.handle(CheckDisplay)).isEqualTo("0,50")
        assertThat(actual.execute(InsertCoin(COIN_TWO_EURO)).handle(CheckDisplay)).isEqualTo("2,50")
    }

    @Test
    fun `When inserting an invalid coin, active value is 0 and coin is returned`() {
        val actual = Machine().execute(InsertCoin(invalidCoin))

        assertThat(actual.handle(CheckCoinChute) as List<*>).containsExactly(invalidCoin)
        assertThat(actual.handle(CheckActiveAmount)).isEqualTo(0.0)
        assertThat(actual.handle(CheckDisplay)).isEqualTo("INSERT COIN")
    }

    @ParameterizedTest
    @MethodSource("allProductsBoughtTest")
    fun `When button pressed with enough money inserted, product is in chute, price deducted and display shows thank you`(
        product: Product,
        coins: List<Coin>
    ) {
        val actual = coins
            .fold(Machine()) { acc, coin -> acc.execute(InsertCoin(coin)) }
            .execute(ChooseProduct(product.code))

        assertThat(actual.handle(CheckChute) as List<*>).containsExactly(product)
        assertThat(actual.handle(CheckActiveAmount)).isEqualTo(0.0)
    }

    @Test
    fun `When button pressed with no money inserted, chute stays empty, display shows price`() {
        val actual = Machine().execute(ChooseProduct("Cola"))

        assertThat(actual.handle(CheckChute) as List<*>).isEmpty()
        assertThat(actual.handle(CheckActiveAmount)).isEqualTo(0.0)
        assertThat(actual.handle(CheckDisplay)).isEqualTo("PRICE 1,00")
    }

    @Test
    fun `When product bought, display shows THANK YOU but changes to INSERT COIN after 3 seconds`() {
        val actual = Machine().execute(InsertCoin(COIN_ONE_EURO))
            .execute(ChooseProduct("Cola"))

        assertThat(actual.handle(CheckDisplay)).isEqualTo("THANK YOU")
        await().atMost(Duration.ofMillis(3501)).untilAsserted {
            assertThat(actual.handle(CheckDisplay)).isEqualTo("INSERT COIN")
        }
    }

    @Test
    fun `When button pressed with no money inserted, display shows PRICE but changes to INSERT COIN after 3 seconds`() {
        val actual = Machine().execute(ChooseProduct("Cola"))

        assertThat(actual.handle(CheckDisplay)).isEqualTo("PRICE 1,00")
        await().atMost(Duration.ofMillis(3501)).untilAsserted {
            assertThat(actual.handle(CheckDisplay)).isEqualTo("INSERT COIN")
        }
    }

    @Test
    fun `When button pressed with insufficient money inserted, display shows PRICE but changes to amount after 3 seconds`() {
        val actual = Machine().execute(InsertCoin(COIN_FIFTY_CENT))
            .execute(ChooseProduct("Cola"))

        assertThat(actual.handle(CheckDisplay)).isEqualTo("PRICE 1,00")
        await().atMost(Duration.ofMillis(3501)).untilAsserted {
            assertThat(actual.handle(CheckDisplay)).isEqualTo("0,50")
        }
    }

    @Test
    fun `When button pressed with too much money inserted, product bought and active amount is inserted minus price`() {
        val actual = Machine().execute(InsertCoin(COIN_TWO_EURO))
            .execute(ChooseProduct(CANDY.code))

        assertThat(actual.handle(CheckChute) as List<*>).containsExactly(CANDY)
        assertThat(actual.handle(CheckActiveAmount)).isEqualTo(1.35)
    }

    @Test
    fun `When products taken from chute, chute is empty`() {
        val actual = Machine().execute(InsertCoin(COIN_TWO_EURO))
            .execute(ChooseProduct(CANDY.code))
            .execute(TakeProducts)

        assertThat(actual.handle(CheckChute) as List<*>).isEmpty()
    }

    @Test
    fun `When no coins inserted and 'return coins' is pressed, coin chute is empty`() {
        val actual = Machine().execute(ReturnCoins)

        assertThat(actual.handle(CheckCoinChute) as List<*>).isEmpty()
    }

    @Test
    fun `When two coins inserted and return coins is pressed, coins in coin chute`() {
        val actual =            Machine().execute(InsertCoin(COIN_TWO_EURO))
            .execute(InsertCoin(COIN_TWO_EURO))
            .execute(ReturnCoins)

        assertThat(actual.handle(CheckCoinChute) as List<*>).containsExactlyInAnyOrder(COIN_TWO_EURO, COIN_TWO_EURO)
    }


    @Test
    fun `When coins taken from coin chute, coin chute is empty`() {
        val actual = Machine()
            .execute(InsertCoin(COIN_FIFTY_CENT))
            .execute(ReturnCoins)

        assertThat(actual.handle(CheckCoinChute) as List<*>).containsExactlyInAnyOrder(COIN_FIFTY_CENT)
        assertThat(actual.execute(TakeCoins).handle(CheckCoinChute) as List<*>).isEmpty()
    }

    @Test
    fun `When COLA-button pressed while showing 'thank you' after a purchase, price of COLA is shown iso 'thank you'`() {
        val machineAfterPurchase = Machine().execute(InsertCoin(COIN_ONE_EURO))
            .execute(ChooseProduct(COLA.code))
        assertThat(machineAfterPurchase.handle(CheckDisplay)).isEqualTo("THANK YOU")

        await().timeout(Duration.ofSeconds(1)).untilAsserted {
            assertThat(
                machineAfterPurchase.execute(ChooseProduct(COLA.code)).handle(CheckDisplay)
            ).isEqualTo("PRICE 1,00")
        }
    }

    @Test
    fun `When CANDY-button pressed while showing 'thank you' after a purchase, price of CANDY is shown iso 'thank you'`() {
        val machineAfterPurchase = Machine().execute(InsertCoin(COIN_ONE_EURO))
            .execute(ChooseProduct(CANDY.code))
        assertThat(machineAfterPurchase.handle(CheckDisplay)).isEqualTo("THANK YOU")

        await().timeout(Duration.ofSeconds(1)).untilAsserted {
            assertThat(
                machineAfterPurchase.execute(ChooseProduct(CANDY.code)).handle(CheckDisplay)
            ).isEqualTo("PRICE 0,65")
        }
    }

    @Test
    fun `When some coins inserted and rejected, correct number of availableCoins in Machine`() {
        val actual = Machine()
            .execute(InsertCoin(COIN_TWO_EURO))
            .execute(ReturnCoins)
            .execute(InsertCoin(invalidCoin))
            .execute(InsertCoin(COIN_TWO_EURO))

        assertThat(actual.handle(CheckActiveAmount)).isEqualTo(2.0)
        assertThat(actual.handle(CheckCoinChute) as List<*>).containsExactlyInAnyOrder(COIN_TWO_EURO, invalidCoin)
    }

    @Test
    fun `When scenario with all actions happens, machine still works`() {
        val actual = Machine().execute(InsertCoin(COIN_TWO_EURO))
            .execute(ReturnCoins)
            .execute(InsertCoin(COIN_TWO_EURO))
            .execute(InsertCoin(invalidCoin))
            .execute(ChooseProduct(CANDY.code))

        assertThat(actual.handle(CheckChute) as List<*>).containsExactly(CANDY)
        assertThat(actual.handle(CheckDisplay)).isEqualTo("THANK YOU")
        assertThat(actual.execute(TakeProducts).handle(CheckChute) as List<*>).isEmpty()
        assertThat(actual.handle(CheckCoinChute) as List<*>).containsExactlyInAnyOrder(
            COIN_TWO_EURO,
            invalidCoin,
        )
    }

    @Test
    @Disabled
    fun `When return coins button pressed, available coins in coin chute`() {
        val actual = Machine()
            .execute(InsertCoin(COIN_FIFTY_CENT))
            .execute(InsertCoin(COIN_FIFTY_CENT))
            .execute(InsertCoin(COIN_ONE_CENT))
            .execute(ReturnCoins)

        assertThat(actual.handle(CheckCoinChute) as List<*>).containsExactlyInAnyOrder(COIN_FIFTY_CENT, COIN_FIFTY_CENT, COIN_ONE_CENT)
        assertThat(actual.handle(CheckCoinChute) as List<*>).isEmpty()
    }

    companion object {
        @JvmStatic
        fun allProductsBoughtTest(): Stream<Arguments> =
            Stream.of(
                Arguments.of(COLA, listOf(COIN_ONE_EURO)),
                Arguments.of(CHIPS, listOf(COIN_FIFTY_CENT)),
                Arguments.of(CANDY, listOf(COIN_FIFTY_CENT, COIN_TEN_CENT, COIN_FIVE_CENT)),
            )
    }
}
