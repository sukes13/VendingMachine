package com.sukes13.vendingmachine

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CoinRegistryTest {
    @Test
    fun `When amount is provided, get it in as least coins as possible`() {
        assertThat(CoinRegistry.inCoins(1.0)).containsExactly(COIN_ONE_EURO)
    }
}