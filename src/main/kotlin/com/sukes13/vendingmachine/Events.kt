package com.sukes13.vendingmachine

import java.time.LocalDateTime
import java.time.LocalDateTime.now

sealed interface VendingEvent {
    sealed interface CoinEvent : VendingEvent {
        val coin: Coin

        data class CoinAddedEvent(override val coin: Coin) : CoinEvent
        data class CoinReturnedEvent(override val coin: Coin) : CoinEvent
    }

    sealed interface ActiveAmountEvent : VendingEvent {
        data class ActiveAmountIncreasedEvent(val value: Double) : ActiveAmountEvent
        data class ActiveAmountDecreasedEvent(val value: Double) : ActiveAmountEvent
    }

    class ProductsTakenEvent() : VendingEvent
    class CoinsTakenEvent() : VendingEvent
    
    sealed class TimedVendingEvent : VendingEvent {
        val occurredOn: LocalDateTime = now()

        class ProductBoughtEvent(val product: Product) : TimedVendingEvent()
        class ButtonPressed(val product: Product) : TimedVendingEvent()
        class InsufficientFunds() : TimedVendingEvent()
    }
}