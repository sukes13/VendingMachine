package com.sukes13.vendingmachine


sealed interface MachineCommand

class InsertCoinCommand(val coin: Coin) : MachineCommand
class ButtonPressedCommand(val productCode: String) : MachineCommand



