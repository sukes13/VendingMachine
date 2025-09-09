package com.sukes13.vendingmachine


sealed interface MachineCommand {
    class InsertCoin(val coin: Coin) : MachineCommand
    class ChooseProduct(val productCode: String) : MachineCommand
    data object TakeProducts : MachineCommand
    data object ReturnCoins : MachineCommand
    data object TakeCoins : MachineCommand
}




