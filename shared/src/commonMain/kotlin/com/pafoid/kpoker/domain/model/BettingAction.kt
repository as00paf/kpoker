package com.pafoid.kpoker.domain.model

sealed class BettingAction {
    object Check : BettingAction()
    object Call : BettingAction()
    data class Raise(val amount: Long) : BettingAction()
    object Fold : BettingAction()
    object AllIn : BettingAction()
}
