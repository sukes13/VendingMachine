package com.sukes13.vendingmachine

import com.sukes13.vendingmachine.MachineCommand.*
import com.sukes13.vendingmachine.MachineQuery.*

data class Machine(
    val eventStore: EventStore = EventStore()
) {
    private val vendingMachine get() = VendingMachine.createFrom(eventStore)
    private fun publishToEventStore(newEvents: List<VendingEvent>) = copy(eventStore = eventStore.publish(newEvents))

    fun execute(command: MachineCommand): Machine =
        when (command) {
            is InsertCoin -> publishToEventStore(vendingMachine.insert(command.coin))
            is ChooseProduct -> publishToEventStore(vendingMachine.pressProductButton(command.productCode))
            is TakeProducts -> publishToEventStore(vendingMachine.takeProducts())
            is ReturnCoins -> publishToEventStore(vendingMachine.pressReturnCoinsButton())
            is TakeCoins -> publishToEventStore(vendingMachine.takeCoins())
        }

    fun handle(query: MachineQuery) =
        when (query) {
            is CheckDisplay -> vendingMachine.showDisplay()
            is CheckChute -> vendingMachine.chute
            is CheckCoinChute -> vendingMachine.coinChute
            is CheckActiveAmount -> vendingMachine.activeAmount
        }
}
