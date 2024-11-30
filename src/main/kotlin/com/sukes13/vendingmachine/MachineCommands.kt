package com.sukes13.vendingmachine


sealed interface MachineCommand

class InsertCoinCommand(val coin: Coin) : MachineCommand
class PressButtonCommand(val productCode: String) : MachineCommand
data object TakeProductsCommand : MachineCommand



