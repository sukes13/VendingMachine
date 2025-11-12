package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.VendingEvent.CoinEvent.CoinAddedEvent
import com.sukes13.vendingmachine.VendingEvent.CoinEvent.CoinReturnedEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EventStoreTest {

    @Test
    fun `eventsSinceLast test`() {
        val event1 = CoinAddedEvent(COIN_FIFTY_CENT)
        val event2 = CoinAddedEvent(COIN_ONE_CENT)
        val eventStore = EventStore(
            listOf(
                CoinAddedEvent(COIN_ONE_EURO),
                CoinReturnedEvent(COIN_ONE_EURO),
                event1,
                event2
            )
        )

        assertThat(eventStore.eventsSinceLast<CoinReturnedEvent>()).containsExactlyInAnyOrder(event1, event2)
    }
}