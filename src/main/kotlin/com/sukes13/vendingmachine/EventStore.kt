package com.sukes13.vendingmachine

data class EventStore(val events: List<VendingEvent> = emptyList()) : List<VendingEvent> by events {
    fun append(event: VendingEvent) = copy(events = events + event)

    inline fun <reified T : VendingEvent> eventsOfType() = events.filterIsInstance<T>()

    inline fun <reified T : VendingEvent> eventsSinceLast() =
        copy(events = events.indexOfLast { it is T }.let { lastOccurrence ->
            if (lastOccurrence >= 0) events.drop(lastOccurrence) else events
        })

    fun lastEventOrNull() = events.lastOrNull()

}