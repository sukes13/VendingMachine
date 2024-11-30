package com.sukes13.vendingmachine

data class Machine(
    val eventStore: EventStore = EventStore()
) {
    private val vendingMachine get() = VendingMachine.createFrom(eventStore)
    private fun pushToStore(newEvents: List<VendingEvent>) = copy(eventStore = eventStore.publish(newEvents))

    fun execute(command: MachineCommand): Machine =
        when (command) {
            is InsertCoinCommand -> pushToStore(vendingMachine.insert(command.coin))
            is ButtonPressedCommand -> pushToStore(vendingMachine.pressButton(command.productCode))
        }

    //Write
    fun takeProducts() = pushToStore(vendingMachine.takeProducts())
    fun pressReturnCoinsButton() = pushToStore(vendingMachine.pressReturnCoinsButton())
    fun takeCoins() = pushToStore(vendingMachine.takeCoins())

    //Read
    fun display() = vendingMachine.display()

    val chute get() = vendingMachine.chute
    val coinChute get() = vendingMachine.coinChute
    val activeAmount get() = vendingMachine.activeAmount

}
