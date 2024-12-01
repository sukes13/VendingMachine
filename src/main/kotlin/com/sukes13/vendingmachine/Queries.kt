package com.sukes13.vendingmachine

sealed interface MachineQuery

data object CheckDisplay : MachineQuery