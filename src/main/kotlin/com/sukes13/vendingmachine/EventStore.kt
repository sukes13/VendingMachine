package com.sukes13.vendingmachine

import java.time.LocalDateTime
import java.time.LocalDateTime.now

sealed interface VendingEvent {
    class CoinAcceptedEvent(val amount: Double) : VendingEvent
    class CoinReturnedEvent(val coin: Coin) : VendingEvent
    object ProductsTakenEvent :VendingEvent

    sealed class TimedVendingEvent : VendingEvent {
        val occurredOn: LocalDateTime = now()

        class ProductBoughtEvent(val product: Product) : TimedVendingEvent()
        class ButtonPressed(val product: Product) : TimedVendingEvent()
    }
}

data class EventStore(val events: List<VendingEvent> = emptyList()) : List<VendingEvent> by events {
    fun append(event: VendingEvent) = copy(events = events + event)

    inline fun <reified T : VendingEvent> eventsOfType() = events.filterIsInstance<T>()

    inline fun <reified T : VendingEvent> eventsSinceOccurrenceOf() =
        events.indexOfLast { it is T }.let { lastOccurrence ->
            if (lastOccurrence >= 0) events.drop(lastOccurrence).take(lastOccurrence)  else events
        }

    fun lastEventOrNull() = events.lastOrNull()

}