package com.pafoid.kpoker.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val passwordHash: String, // In a real app, this would be a hash
    val bankroll: Long = 10000L
)
