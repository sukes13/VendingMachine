package com.sukes13.vendingmachine

sealed interface MachineQuery {
    data object CheckDisplay : MachineQuery
    data object CheckChute : MachineQuery
    data object CheckCoinChute : MachineQuery
    data object CheckActiveAmount : MachineQuery
}
