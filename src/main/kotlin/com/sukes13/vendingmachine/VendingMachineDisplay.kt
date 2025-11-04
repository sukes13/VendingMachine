package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.ProductRegistry.price

class VendingMachineDisplay(val currentTimedEvents: List<VendingEvent.TimedVendingEvent>) {
    fun showMessage(activeAmount : Double) =
        if (currentTimedEvents.isEmpty()) defaultMessage(activeAmount)
        else temporaryMessage(currentTimedEvents.maxBy { it.occurredOn })

    private fun temporaryMessage(event: VendingEvent.TimedVendingEvent) =
        when (event) {
            is VendingEvent.TimedVendingEvent.ButtonPressed -> "PRICE ${event.product.price().asString()}"
            is VendingEvent.TimedVendingEvent.ProductBoughtEvent -> "THANK YOU"
            is VendingEvent.TimedVendingEvent.InsufficientFunds -> "INSUFFICIENT COINS"
        }

    private fun defaultMessage(activeAmount: Double) =
        when (activeAmount) {
            0.0 -> "INSERT COIN"
            else -> activeAmount.asString()
        }
}