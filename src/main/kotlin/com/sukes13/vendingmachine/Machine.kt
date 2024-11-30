package com.sukes13.vendingmachine

data class Machine(
    val eventStore: EventStore = EventStore()
) {
    private val vendingMachine get() = VendingMachine.createFrom(eventStore)
    private fun pushToStore(newEvents: List<VendingEvent>) = copy(eventStore = eventStore.publish(newEvents))

    fun execute(machineCommand: MachineCommand): Machine =
        when (machineCommand) {
            is InsertCoinCommand -> insert(machineCommand.coin)
            else -> TODO("to be implemented")
        }


    //Write
    fun insert(coin: Coin) = pushToStore(vendingMachine.insert(coin))
    fun pressButton(productName: String) = pushToStore(vendingMachine.pressButton(productName))
    fun takeProducts() = pushToStore(vendingMachine.takeProducts())
    fun pressReturnCoinsButton() = pushToStore(vendingMachine.pressReturnCoinsButton())
    fun takeCoins() = pushToStore(vendingMachine.takeCoins())

    //Read
    fun display() = vendingMachine.display()

    val chute get() = vendingMachine.chute
    val coinChute get() = vendingMachine.coinChute
    val activeAmount get() = vendingMachine.activeAmount
}

