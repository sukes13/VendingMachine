package com.sukes13.vendingmachine

data class EventStore(val events: List<VendingEvent> = emptyList()) : List<VendingEvent> by events {
    fun publish(events: List<VendingEvent>) = copy(events = this.events + events)
    fun publishEvents(vararg events: VendingEvent) = publish(events.toList())
    
    inline fun <reified T : VendingEvent> eventsOfType() = events.filterIsInstance<T>()

    inline fun <reified T : VendingEvent> eventsSinceLast() =
        copy(events = events.indexOfLast { it is T }.let { lastOccurrence ->
            if (lastOccurrence >= 0)
                events.drop(lastOccurrence + 1)
            else events
        })
}

