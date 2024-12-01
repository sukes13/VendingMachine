package com.sukes13.vendingmachine

data class Machine(
    val eventStore: EventStore = EventStore()
) {
    private val vendingMachine get() = VendingMachine.createFrom(eventStore)
    private fun pushToStore(newEvents: List<VendingEvent>) = copy(eventStore = eventStore.publish(newEvents))

    fun execute(command: MachineCommand): Machine =
        when (command) {
            is InsertCoin -> pushToStore(vendingMachine.insert(command.coin))
            is ChooseProduct -> pushToStore(vendingMachine.pressButton(command.productCode))
            is TakeProducts -> pushToStore(vendingMachine.takeProducts())
            is ReturnCoins -> pushToStore(vendingMachine.pressReturnCoinsButton())
            is TakeCoins -> pushToStore(vendingMachine.takeCoins())
        }

    fun handle(query: MachineQuery) =
        when (query) {
            is CheckDisplay -> vendingMachine.display()
        }

    val chute get() = vendingMachine.chute
    val coinChute get() = vendingMachine.coinChute
    val activeAmount get() = vendingMachine.activeAmount
}
