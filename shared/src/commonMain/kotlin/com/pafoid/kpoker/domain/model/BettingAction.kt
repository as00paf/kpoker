package com.pafoid.kpoker.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
sealed class BettingAction {
    @Serializable
    @SerialName("check")
    object Check : BettingAction()
    
    @Serializable
    @SerialName("call")
    object Call : BettingAction()
    
    @Serializable
    @SerialName("raise")
    data class Raise(val amount: Long) : BettingAction()
    
    @Serializable
    @SerialName("fold")
    object Fold : BettingAction()
    
    @Serializable
    @SerialName("all_in")
    object AllIn : BettingAction()
}
