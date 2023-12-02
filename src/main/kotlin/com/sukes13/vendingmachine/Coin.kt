package com.sukes13.vendingmachine

val COIN_ONE_CENT = Coin("1 cent", 16.25, 1.67, 2.30)
val COIN_TWO_CENT = Coin("2 cent", 18.75, 1.67, 3.06)
val COIN_FIVE_CENT = Coin("5 cent", 21.25, 1.67, 3.92)
val COIN_TEN_CENT = Coin("10 cent", 19.75, 1.93, 4.10)
val COIN_TWENTY_CENT = Coin("20 cent", 22.25, 2.14, 5.74)
val COIN_FIFTY_CENT = Coin("50 cent", 24.25, 2.38, 7.80)
val COIN_ONE_EURO = Coin("1 euro", 23.25, 2.33, 7.50)
val COIN_TWO_EURO = Coin("2 euro", 25.75, 2.20, 8.50)

data class Coin(
    private val name: String,
    private val diameter: Double,
    private val thickness: Double,
    private val mass: Double,
) {
    override fun toString() = "Coin($name)"
}

object CoinRegistry {
    private val registry = mapOf(
        COIN_ONE_CENT to 0.01,
        COIN_TWO_CENT to 0.02,
        COIN_FIVE_CENT to 0.05,
        COIN_TEN_CENT to 0.10,
        COIN_TWENTY_CENT to 0.2,
        COIN_FIFTY_CENT to 0.5,
        COIN_ONE_EURO to 1.0,
        COIN_TWO_EURO to 2.0,
    )

    fun Coin.value() = registry[this]

    tailrec fun inCoins(remainder: Double, coins: List<Coin> = emptyList()): List<Coin> {
        if (remainder <= 0.0) {
            return coins
        }
        val highestValueInRemainder = registry.maxBy { (_, value) ->
            if ((remainder.minusPrecise(value)) >= 0.0) value else 0.0
        }
        return inCoins(
            remainder.minusPrecise(highestValueInRemainder.value),
            coins + highestValueInRemainder.key
        )
    }
}