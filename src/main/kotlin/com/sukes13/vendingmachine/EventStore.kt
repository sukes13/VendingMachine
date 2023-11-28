package com.sukes13.vendingmachine

interface VendingEvent {
    class AmountInsertedEvent(val amount: Double) : VendingEvent
    class CoinRejectedEvent(val coin: Coin) : VendingEvent
    class ProductBoughtEvent(val product: Product) : VendingEvent
    class ButtonPressed(val product: Product) : VendingEvent
}

data class EventStore(val events: List<VendingEvent> = emptyList()) : List<VendingEvent> by events {
    fun append(event: VendingEvent) = copy(events = events + event)

    inline fun <reified T : VendingEvent> eventsOfType() = events.filterIsInstance<T>()

    inline fun <reified T : VendingEvent> eventsSinceOccurrenceOf() =
        events.indexOfLast { it is T }.let { lastOccurrence ->
            if (lastOccurrence >= 0) events.takeLast(lastOccurrence) else events
        }

}